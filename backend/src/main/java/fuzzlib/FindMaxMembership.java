package fuzzlib;

import fuzzlib.norms.TNorm;

public interface FindMaxMembership {
	public double findMaxAandBforMembership(double membership,  FuzzySet trA ,FuzzySet trB,
			TNorm tnorm, double minDY, double minDX);
	}