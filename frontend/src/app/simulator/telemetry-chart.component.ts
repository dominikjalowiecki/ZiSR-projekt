import {
  AfterViewInit,
  Component,
  ElementRef,
  OnDestroy,
  effect,
  input,
  viewChild,
} from '@angular/core';
import {
  Chart,
  ChartConfiguration,
  LineController,
  LineElement,
  PointElement,
  LinearScale,
  Tooltip,
  Legend,
} from 'chart.js';
import { SimulationSnapshot } from '../shared/simulation.types';

Chart.register(LineController, LineElement, PointElement, LinearScale, Tooltip, Legend);

@Component({
  selector: 'app-telemetry-chart',
  standalone: true,
  template: `
    <div class="chart-wrapper">
      <canvas #canvas width="800" height="240"></canvas>
    </div>
  `,
  styles: [`
    .chart-wrapper {
      position: relative;
      width: 800px;
      max-width: 100%;
      padding: 0.5rem;
      background: #ffffff;
      border: 1px solid #e5e7eb;
      border-radius: 8px;
    }

    canvas { display: block; max-width: 100%; }

    .placeholder {
      position: absolute; inset: 0;
      display: flex; align-items: center; justify-content: center;
      color: #6b7280; font-style: italic; font-size: 0.9rem;
      pointer-events: none;
    }
  `],
})
export class TelemetryChartComponent implements AfterViewInit, OnDestroy {
  readonly snapshots = input<SimulationSnapshot[]>([]);

  private canvasRef = viewChild.required<ElementRef<HTMLCanvasElement>>('canvas');
  private chart: Chart<'line'> | null = null;
  private viewReady = false;

  constructor() {
    effect(() => {
      const snaps = this.snapshots();
      if (this.viewReady) this.render(snaps);
    });
  }

  ngAfterViewInit(): void {
    this.viewReady = true;
    this.render(this.snapshots());
  }

  ngOnDestroy(): void {
    this.chart?.destroy();
    this.chart = null;
  }

  private render(snaps: SimulationSnapshot[]): void {
    const accelData = snaps.map(s => ({ x: s.time, y: s.acceleration }));
    const steerData = snaps.map(s => ({ x: s.time, y: s.steeringCorrection }));

    const data: ChartConfiguration<'line'>['data'] = {
      datasets: [
        {
          label: 'Przyspieszenie',
          data: accelData,
          borderColor: '#dc2626',
          backgroundColor: '#dc2626',
          borderWidth: 2,
          pointRadius: 0,
          tension: 0.15,
        },
        {
          label: 'Korekcja toru jazdy',
          data: steerData,
          borderColor: '#2563eb',
          backgroundColor: '#2563eb',
          borderWidth: 2,
          pointRadius: 0,
          tension: 0.15,
        },
      ],
    };

    if (this.chart) {
      this.chart.data = data;
      this.chart.update();
      return;
    }

    this.chart = new Chart<'line'>(this.canvasRef().nativeElement, {
      type: 'line',
      data,
      options: {
        animation: false,
        responsive: false,
        scales: {
          x: {
            type: 'linear',
            title: { display: true, text: 'czas (s)' },
            ticks: { maxTicksLimit: 12, precision: 1 },
          },
          y: {
            type: 'linear',
            min: -1,
            max: 1,
            title: { display: true, text: 'wartość sterownika' },
            grid: { color: (ctx) => ctx.tick.value === 0 ? '#9ca3af' : '#e5e7eb' },
          },
        },
        plugins: {
          legend: { position: 'top', labels: { boxWidth: 14 } },
          tooltip: {
            mode: 'index',
            intersect: false,
            callbacks: {
              title: (items) => `t = ${Number(items[0].parsed.x).toFixed(1)} s`,
              label: (item) => `${item.dataset.label}: ${(item.parsed.y ?? 0).toFixed(3)}`,
            },
          },
        },
        interaction: { mode: 'index', intersect: false },
      },
    });
  }
}
