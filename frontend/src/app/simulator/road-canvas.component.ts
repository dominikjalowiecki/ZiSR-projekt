import {
  AfterViewInit,
  Component,
  ElementRef,
  OnDestroy,
  computed,
  effect,
  input,
  output,
  signal,
  viewChild,
} from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { SimulationSnapshot } from '../shared/simulation.types';

const CANVAS_WIDTH = 800;
const CANVAS_HEIGHT = 300;
const PX_PER_METER = 4;
const PX_PER_LATERAL_M = 30;
const OWN_X = 200;
const ROAD_TOP = 80;
const ROAD_BOTTOM = 220;
const LANE_DIVIDER_Y = 150;
const RIGHT_LANE_CENTER = 185;
const LEFT_LANE_CENTER = 115;
const FRAME_DT_SEC = 0.1;

@Component({
  selector: 'app-road-canvas',
  standalone: true,
  imports: [DecimalPipe],
  template: `
    <div class="road-canvas">
      <canvas #canvas [width]="canvasWidth" [height]="canvasHeight"></canvas>

      @if (current(); as c) {
        <div class="overlay">
          <div class="row"><span class="k">Czas</span><span class="v">{{ c.time | number: '1.1-1' }} s</span></div>
          <div class="row"><span class="k">Prędkość</span><span class="v">{{ c.ownSpeed * 3.6 | number: '1.0-0' }} km/h</span></div>
          <div class="row"><span class="k">Dystans</span><span class="v">{{ c.distance | number: '1.1-1' }} m</span></div>
          <div class="row"><span class="k">Przesunięcie</span><span class="v">{{ c.lateralOffset | number: '1.2-2' }} m</span></div>
          @if (c.roadCondition < 0.5) {
            <div class="badge rain">deszcz</div>
          }
          @if (c.curvature < -0.005) {
            <div class="badge curve">zakręt w lewo</div>
          } @else if (c.curvature > 0.005) {
            <div class="badge curve">zakręt w prawo</div>
          }
          @if (c.leftLaneBlocked) {
            <div class="badge blocked">lewy pas zajęty</div>
          }
          @if (c.laneChangeAdvice !== 'KEEP') {
            <div class="badge lane">{{ c.laneChangeAdvice === 'CHANGE_NOW' ? 'zmień pas!' : 'przygotuj zmianę' }}</div>
          }
          @if (c.lane < 0.5) {
            @if (c.distance < -1) {
              <div class="badge crash">przejechano lead</div>
            } @else if (c.distance < 5) {
              <div class="badge crash">blisko kolizji</div>
            }
          }
          @if (c.lane > 0.95) {
            <div class="badge done">w lewym pasie</div>
          } @else if (c.lane > 0.05) {
            <div class="badge changing">zmiana pasa…</div>
          }
          @if (c.lateralOffset > 1.0 || c.lateralOffset < -1.0) {
            <div class="badge offlane">poza pasem</div>
          }
        </div>
      }

      @if (!hasSnapshots()) {
        <div class="placeholder">Uruchom symulację, aby zobaczyć animację.</div>
      }
    </div>
  `,
  styles: [`
    .road-canvas { position: relative; display: inline-block; }
    canvas { display: block; border-radius: 8px; box-shadow: 0 1px 3px rgba(0,0,0,0.08); }
    .overlay {
      position: absolute; top: 8px; left: 8px;
      background: rgba(17,24,39,0.75); color: #f9fafb;
      padding: 0.4rem 0.6rem; border-radius: 6px;
      font-family: 'Segoe UI', sans-serif; font-size: 0.78rem;
      min-width: 130px;
    }

    .overlay .row { display: flex; justify-content: space-between; gap: 0.6rem; }
    .overlay .k { opacity: 0.7; }
    .overlay .v { font-variant-numeric: tabular-nums; }

    .badge {
      display: inline-block; margin-top: 0.3rem; padding: 0.1rem 0.4rem;
      border-radius: 3px; font-size: 0.7rem; font-weight: 600;
      margin-right: 0.45rem;
    }

    .badge.rain    { background: #60a5fa; color: #0b1d3a; }
    .badge.curve   { background: #facc15; color: #422006; }
    .badge.lane    { background: #f87171; color: #450a0a; }
    .badge.crash    { background: #b91c1c; color: #ffffff; }
    .badge.offlane  { background: #fb923c; color: #431407; }
    .badge.changing { background: #a78bfa; color: #1e1b4b; }
    .badge.done     { background: #34d399; color: #052e16; }
    .badge.blocked  { background: #475569; color: #f8fafc; }

    .placeholder {
      position: absolute; inset: 0; display: flex; align-items: center; justify-content: center;
      color: #6b7280; font-style: italic; pointer-events: none;
    }
  `],
})
export class RoadCanvasComponent implements OnDestroy, AfterViewInit {
  readonly snapshots = input<SimulationSnapshot[]>([]);
  readonly frameIndex = output<number>();

  protected readonly canvasWidth = CANVAS_WIDTH;
  protected readonly canvasHeight = CANVAS_HEIGHT;
  protected readonly current = signal<SimulationSnapshot | null>(null);
  protected readonly hasSnapshots = computed(() => this.snapshots().length > 0);

  private canvasRef = viewChild.required<ElementRef<HTMLCanvasElement>>('canvas');
  private ctx: CanvasRenderingContext2D | null = null;
  private animationFrameId: number | null = null;
  private playbackStartMs = 0;
  private viewReady = false;
  private smoothedLeadX: number | null = null;
  private lastEmittedIdx = -1;

  constructor() {
    effect(() => {
      const snaps = this.snapshots();
      if (this.viewReady && snaps.length > 0) {
        this.startPlayback(snaps);
      }
    });
  }

  ngAfterViewInit(): void {
    this.ctx = this.canvasRef().nativeElement.getContext('2d');
    this.viewReady = true;
    this.drawEmpty();

    const snaps = this.snapshots();
    if (snaps.length > 0) {
      this.startPlayback(snaps);
    }
  }

  ngOnDestroy(): void {
    this.cancelFrame();
  }

  private startPlayback(snaps: SimulationSnapshot[]): void {
    this.cancelFrame();
    this.playbackStartMs = performance.now();
    this.smoothedLeadX = null;
    this.lastEmittedIdx = -1;

    const tick = () => {
      const elapsedSec = (performance.now() - this.playbackStartMs) / 1000;
      const idx = Math.min(snaps.length - 1, Math.floor(elapsedSec / FRAME_DT_SEC));
      const snap = snaps[idx];
      this.current.set(snap);
      this.drawFrame(snap);

      if (idx !== this.lastEmittedIdx) {
        this.lastEmittedIdx = idx;
        this.frameIndex.emit(idx);
      }

      if (idx < snaps.length - 1) {
        this.animationFrameId = requestAnimationFrame(tick);
      } else {
        this.animationFrameId = null;
      }
    };

    tick();
  }

  private cancelFrame(): void {
    if (this.animationFrameId !== null) {
      cancelAnimationFrame(this.animationFrameId);
      this.animationFrameId = null;
    }
  }

  private drawEmpty(): void {
    if (!this.ctx) return;
    this.drawBackground(this.ctx, 1.0);
    this.drawRoad(this.ctx);
  }

  private drawFrame(snap: SimulationSnapshot): void {
    if (!this.ctx) return;
    const ctx = this.ctx;

    this.drawBackground(ctx, snap.roadCondition);
    this.drawRoad(ctx);
    this.drawLaneStripes(ctx, snap.ownPosition);

    const targetLeadX = OWN_X + (snap.leadPosition - snap.ownPosition) * PX_PER_METER;
    if (this.smoothedLeadX === null) {
      this.smoothedLeadX = targetLeadX;
    } else if (targetLeadX < this.smoothedLeadX - 150 && this.smoothedLeadX > CANVAS_WIDTH) {
      this.smoothedLeadX = CANVAS_WIDTH + 30;
    }
    this.smoothedLeadX += (targetLeadX - this.smoothedLeadX) * 0.2;
    const leadX = this.smoothedLeadX;

    if (leadX > -30 && leadX < CANVAS_WIDTH + 30) {
      this.drawCar(ctx, leadX, RIGHT_LANE_CENTER, '#dc2626', 'PRZÓD');
    } else if (leadX >= CANVAS_WIDTH + 30) {
      this.drawArrowAhead(ctx, snap.distance);
    }

    const laneY = RIGHT_LANE_CENTER + snap.lane * (LEFT_LANE_CENTER - RIGHT_LANE_CENTER);
    const ownY = laneY + snap.lateralOffset * PX_PER_LATERAL_M;
    this.drawCar(ctx, OWN_X, ownY, '#2563eb', 'MY');
  }

  private drawBackground(ctx: CanvasRenderingContext2D, roadCondition: number): void {
    const wet = roadCondition < 0.5;
    ctx.fillStyle = wet ? '#dbeafe' : '#f0fdf4';
    ctx.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
  }

  private drawRoad(ctx: CanvasRenderingContext2D): void {
    ctx.fillStyle = '#374151';
    ctx.fillRect(0, ROAD_TOP, CANVAS_WIDTH, ROAD_BOTTOM - ROAD_TOP);

    ctx.strokeStyle = '#ffffff';
    ctx.lineWidth = 3;
    ctx.beginPath();
    ctx.moveTo(0, ROAD_TOP); ctx.lineTo(CANVAS_WIDTH, ROAD_TOP);
    ctx.moveTo(0, ROAD_BOTTOM); ctx.lineTo(CANVAS_WIDTH, ROAD_BOTTOM);
    ctx.stroke();
  }

  private drawLaneStripes(ctx: CanvasRenderingContext2D, ownPosition: number): void {
    const stripeLength = 30;
    const gap = 30;
    const period = stripeLength + gap;
    const offsetPx = (ownPosition * PX_PER_METER) % period;

    ctx.strokeStyle = '#fbbf24';
    ctx.lineWidth = 3;
    ctx.beginPath();
    for (let x = -offsetPx; x < CANVAS_WIDTH; x += period) {
      ctx.moveTo(x, LANE_DIVIDER_Y);
      ctx.lineTo(x + stripeLength, LANE_DIVIDER_Y);
    }
    ctx.stroke();
  }

  private drawCar(ctx: CanvasRenderingContext2D, x: number, y: number, color: string, label: string): void {
    ctx.fillStyle = color;
    ctx.fillRect(x - 22, y - 12, 44, 24);
    ctx.fillStyle = '#ffffff';
    ctx.font = '600 11px "Segoe UI", sans-serif';
    ctx.textAlign = 'center';
    ctx.textBaseline = 'middle';
    ctx.fillText(label, x, y);
  }

  private drawArrowAhead(ctx: CanvasRenderingContext2D, distance: number): void {
    const x = CANVAS_WIDTH - 50;
    const y = RIGHT_LANE_CENTER;
    ctx.fillStyle = '#dc2626';
    ctx.beginPath();
    ctx.moveTo(x - 10, y - 10);
    ctx.lineTo(x + 10, y);
    ctx.lineTo(x - 10, y + 10);
    ctx.closePath();
    ctx.fill();

    ctx.fillStyle = '#1f2937';
    ctx.font = '11px "Segoe UI", sans-serif';
    ctx.textAlign = 'right';
    ctx.fillText(`+${distance.toFixed(0)} m`, x - 15, y + 3);
  }
}
