export interface SimulationSnapshot {
  time: number;
  ownPosition: number;
  ownSpeed: number;
  leadPosition: number;
  leadSpeed: number;
  lateralOffset: number;
  lane: number;
  curvature: number;
  roadCondition: number;
  distance: number;
  relativeSpeed: number;
  acceleration: number;
  steeringCorrection: number;
  laneChangeUrgency: number;
  laneChangeAdvice: string;
  leftLaneBlocked: boolean;
}

export interface ScenarioDescriptor {
  id: string;
  label: string;
  description: string;
  defaultDuration: number;
}
