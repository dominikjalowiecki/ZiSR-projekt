import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { DecimalPipe } from '@angular/common';

interface RuleActivation {
  name: string;
  activation: number;
}

interface ControlResponse {
  acceleration: number;
  steeringCorrection: number;
  laneChangeUrgency: number;
  laneChangeAdvice: string;
  activatedRules: RuleActivation[];
  fuzzifiedInputs: Record<string, Record<string, number>>;
}

@Component({
  selector: 'app-root',
  imports: [FormsModule, DecimalPipe],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  private http = inject(HttpClient);

  distance = signal(80);
  relativeSpeed = signal(0);
  ownSpeed = signal(90);
  roadCondition = signal(0.9);
  curvature = signal(0);
  lateralOffset = signal(0);

  result = signal<ControlResponse | null>(null);
  error = signal<string | null>(null);
  loading = signal(false);

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

  accelLabel(v: number): string {
    if (v <= -0.5) return 'Mocne hamowanie';
    if (v <= -0.1) return 'Lekkie hamowanie';
    if (v <  0.1) return 'Utrzymaj prędkość';
    if (v <  0.5) return 'Lekkie przyspieszenie';
    return 'Mocne przyspieszenie';
  }

  accelClass(v: number): string {
    if (v <= -0.5) return 'advice-danger';
    if (v <= -0.1) return 'advice-warning';
    if (v <  0.1) return 'advice-ok';
    if (v <  0.5) return 'advice-warning';
    return 'advice-danger';
  }

  steerLabel(v: number): string {
    if (v <= -0.5) return 'Mocno w lewo';
    if (v <= -0.1) return 'Lekko w lewo';
    if (v <  0.1) return 'Prosto';
    if (v <  0.5) return 'Lekko w prawo';
    return 'Mocno w prawo';
  }

  steerClass(v: number): string {
    if (Math.abs(v) >= 0.5) return 'advice-danger';
    if (Math.abs(v) >= 0.1) return 'advice-warning';
    return 'advice-ok';
  }

  adviceClass(advice: string): string {
    if (advice === 'CHANGE_NOW') return 'advice-danger';
    if (advice === 'PREPARE') return 'advice-warning';
    return 'advice-ok';
  }

  adviceLabel(advice: string): string {
    switch (advice) {
      case 'KEEP': return 'Utrzymaj pas';
      case 'PREPARE': return 'Przygotuj zmianę';
      case 'CHANGE_NOW': return 'Zmień pas teraz';
      default: return advice;
    }
  }
}
