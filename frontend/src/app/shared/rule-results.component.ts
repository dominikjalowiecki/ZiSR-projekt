import { Component, input } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { RuleResult } from './simulation.types';

@Component({
  selector: 'app-rule-results',
  standalone: true,
  imports: [DecimalPipe],
  template: `
    <h3>Wynik</h3>

    <div class="output">
      <span class="label">Przyspieszenie / hamowanie</span>
      <span class="advice" [class]="accelClass(result().acceleration)">
        {{ accelLabel(result().acceleration) }}
      </span>
      <span class="value">{{ result().acceleration | number: '1.2-2' }}</span>
    </div>

    <div class="output">
      <span class="label">Korekta toru jazdy</span>
      <span class="advice" [class]="steerClass(result().steeringCorrection)">
        {{ steerLabel(result().steeringCorrection) }}
      </span>
      <span class="value">{{ result().steeringCorrection | number: '1.2-2' }}</span>
    </div>

    <div class="output">
      <span class="label">Sugestia zmiany pasa</span>
      <span class="advice" [class]="adviceClass(result().laneChangeAdvice)">
        {{ adviceLabel(result().laneChangeAdvice) }}
      </span>
      <span class="value">{{ result().laneChangeUrgency | number: '1.2-2' }}</span>
    </div>

    <hr />
    <h3>Aktywowane reguły</h3>
    @if (result().activatedRules.length === 0) {
      <p class="empty">Żadna reguła nie została aktywowana.</p>
    }
    <ul class="rules">
      @for (rule of result().activatedRules; track rule.name) {
        <li>
          <div class="rule-header">
            <span class="rule-name">{{ rule.name }}</span>
            <span class="rule-strength">{{ rule.activation | number: '1.2-2' }}</span>
          </div>
          <div class="bar small">
            <div class="fill" [style.width.%]="rule.activation * 100"></div>
          </div>
        </li>
      }
    </ul>
  `,
  styles: [`
    h3 {
      margin-top: 0.75rem;
      margin-bottom: 0.5rem;
      font-size: 1rem;
    }

    .output {
      display: grid;
      grid-template-columns: 1fr auto auto;
      align-items: center;
      gap: 0.75rem;
      padding: 0.5rem 0;
      border-bottom: 1px solid #eef0f3;
    }

    .output:last-of-type { border-bottom: 0; }
    .output .label { font-weight: 500; }

    .output .value {
      font-variant-numeric: tabular-nums;
      color: #6b7280;
      font-weight: 500;
      min-width: 3ch;
      text-align: right;
    }

    .advice {
      display: inline-block;
      padding: 0.2rem 0.6rem;
      border-radius: 4px;
      font-weight: 600;
      font-size: 0.9rem;
    }

    .advice-ok      { background: #d1fae5; color: #065f46; }
    .advice-warning { background: #fef3c7; color: #92400e; }
    .advice-danger  { background: #fee2e2; color: #991b1b; }

    .rules { list-style: none; padding: 0; margin: 0; }
    .rules li { margin-bottom: 0.6rem; }

    .rule-header {
      display: flex;
      justify-content: space-between;
      font-size: 0.9rem;
    }

    .rule-name { color: #374151; }

    .rule-strength {
      font-variant-numeric: tabular-nums;
      color: #2563eb;
      font-weight: 600;
    }

    .bar {
      position: relative;
      width: 100%;
      height: 14px;
      background: #e5e7eb;
      border-radius: 4px;
      overflow: hidden;
    }

    .bar.small {
      height: 6px;
      margin-top: 4px;
    }

    .bar .fill {
      position: absolute;
      top: 0;
      left: 0;
      height: 100%;
      background: #2563eb;
      transition: width 0.2s ease, left 0.2s ease;
    }

    .empty {
      color: #6b7280;
      font-style: italic;
    }
  `],
})
export class RuleResultsComponent {
  readonly result = input.required<RuleResult>();

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
