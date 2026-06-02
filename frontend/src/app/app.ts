import { Component, OnDestroy, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { DecimalPipe } from '@angular/common';
import { RoadCanvasComponent } from './simulator/road-canvas.component';
import { TelemetryChartComponent } from './simulator/telemetry-chart.component';
import { RuleResultsComponent } from './shared/rule-results.component';
import { RuleActivation, ScenarioDescriptor, SimulationSnapshot } from './shared/simulation.types';

interface ControlResponse {
  acceleration: number;
  steeringCorrection: number;
  laneChangeUrgency: number;
  laneChangeAdvice: string;
  activatedRules: RuleActivation[];
  fuzzifiedInputs: Record<string, Record<string, number>>;
}

type View = 'manual' | 'simulator';

@Component({
  selector: 'app-root',
  imports: [FormsModule, DecimalPipe, RoadCanvasComponent, TelemetryChartComponent, RuleResultsComponent],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App implements OnDestroy {
  private http = inject(HttpClient);

  private static readonly FRAME_INTERVAL_MS = 100;
  private playbackTimer: ReturnType<typeof setInterval> | null = null;
  playing = signal(false);

  view = signal<View>('manual');

  distance = signal(80);
  relativeSpeed = signal(0);
  ownSpeed = signal(90);
  roadCondition = signal(0.9);
  curvature = signal(0);
  lateralOffset = signal(0);

  result = signal<ControlResponse | null>(null);
  error = signal<string | null>(null);
  loading = signal(false);

  scenarios = signal<ScenarioDescriptor[]>([]);
  selectedScenarioId = signal<string>('');
  selectedScenario = computed<ScenarioDescriptor | null>(() => {
    const id = this.selectedScenarioId();
    return this.scenarios().find(s => s.id === id) ?? null;
  });

  snapshots = signal<SimulationSnapshot[]>([]);
  simulating = signal(false);
  simError = signal<string | null>(null);

  selectedStep = signal(0);
  selectedSnapshot = computed<SimulationSnapshot | null>(() => {
    const snaps = this.snapshots();
    if (snaps.length === 0) return null;
    const idx = Math.min(Math.max(this.selectedStep(), 0), snaps.length - 1);
    return snaps[idx];
  });

  constructor() {
    this.http.get<ScenarioDescriptor[]>('http://localhost:8080/api/scenarios').subscribe({
      next: (list) => {
        this.scenarios.set(list);
        if (list.length > 0) this.selectedScenarioId.set(list[0].id);
      },
      error: (err) => this.simError.set(err.message ?? 'Nie udało się pobrać scenariuszy'),
    });
  }

  runSelected() {
    const id = this.selectedScenarioId();
    if (id) this.runScenario(id);
  }

  togglePlay() {
    if (this.playing()) this.pause();
    else this.play();
  }

  play() {
    const snaps = this.snapshots();
    if (snaps.length === 0) return;

    if (this.selectedStep() >= snaps.length - 1) {
      this.selectedStep.set(0);
    }

    this.clearTimer();
    this.playing.set(true);
    this.playbackTimer = setInterval(() => {
      const last = this.snapshots().length - 1;
      const next = this.selectedStep() + 1;
      if (next >= last) {
        this.selectedStep.set(last);
        this.pause();
      } else {
        this.selectedStep.set(next);
      }
    }, App.FRAME_INTERVAL_MS);
  }

  pause() {
    this.clearTimer();
    this.playing.set(false);
  }

  seek(step: number) {
    this.pause();
    this.selectedStep.set(step);
  }

  private clearTimer() {
    if (this.playbackTimer !== null) {
      clearInterval(this.playbackTimer);
      this.playbackTimer = null;
    }
  }

  ngOnDestroy() {
    this.clearTimer();
  }

  compute() {
    this.loading.set(true);
    this.error.set(null);
    this.http.post<ControlResponse>('http://localhost:8080/api/control', {
      distance: this.distance(),
      relativeSpeed: this.relativeSpeed(),
      ownSpeed: this.ownSpeed(),
      roadCondition: this.roadCondition(),
      curvature: this.curvature(),
      lateralOffset: this.lateralOffset(),
    }).subscribe({
      next: (res) => { this.result.set(res); this.loading.set(false); },
      error: (err) => { this.error.set(err.message ?? 'Żądanie nie powiodło się'); this.loading.set(false); },
    });
  }

  runScenario(id: string) {
    this.pause();
    this.simulating.set(true);
    this.simError.set(null);
    this.snapshots.set([]);
    this.selectedStep.set(0);
    this.http.post<SimulationSnapshot[]>('http://localhost:8080/api/simulation/run', {
      scenario: id,
    }).subscribe({
      next: (res) => { this.snapshots.set(res); this.simulating.set(false); this.play(); },
      error: (err) => { this.simError.set(err.message ?? 'Symulacja nie powiodła się'); this.simulating.set(false); },
    });
  }
}
