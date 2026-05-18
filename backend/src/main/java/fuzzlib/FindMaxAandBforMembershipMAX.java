package fuzzlib;

import fuzzlib.norms.TNorm;

public class FindMaxAandBforMembershipMAX implements FindMaxMembership {
	// Function finding maximum of tnorm between A and B truth functions described
	// in different spaces. Maximum is found in a subset of [0,1]x[0,1] space
	// where MAX(xA,xB)=membership (maximum = membership)
	public double findMaxAandBforMembership(double membership, FuzzySet trA, FuzzySet trB, TNorm tnorm, double minDY,
			double minDX) {
		// PROBLEM - ZASTANOWIC SIE, PRZETESTOWAC
		// - co zrobic jak funkcja ma nieodpowiednia ilosc punktow
		// lub nie jest opisana w przedziale [0,1]
		// (pierwszy i ostatni punkt opisu muszďż˝ byc dla x=0 i x=1 ??)

		int i;
		double maxA = 0, maxB = 0, mtrA = 0, mtrB = 0, dy, dx;

		// 1.Wyznaczyc maxA = max trA w przedziale [0,membership], zapamietac
		// trA(membership)
		i = 0;
		while ((i < trA.size - 1) && trA.pts[i].x < membership) {
			if (trA.pts[i].y > maxA)
				maxA = trA.pts[i].y;
			i++;
		}
		if (i == 0)
			dy = 0;
		else
			dy = trA.pts[i].y - trA.pts[i - 1].y;
		if (dy < 0.0)
			dy *= -1;
		dx = membership - trA.pts[i].x;
		if (dx < 0.0)
			dx *= -1;
		if (dy < minDY || dx < minDX) { // punkt koncowy == ostatniemu
			mtrA = trA.pts[i].y;
		} else {
			// wyznacz wartosc dla punktu koncowego : ((x-x1)*(y2-y1)/(x2-x1)) + y1;
			mtrA = (membership - trA.pts[i - 1].x) * (trA.pts[i].y - trA.pts[i - 1].y);
			mtrA /= (trA.pts[i].x - trA.pts[i - 1].x);
			mtrA += trA.pts[i - 1].y;
		}
		if (mtrA > maxA)
			maxA = mtrA;

		// 2.Wyznaczyc maxB = max trB w przedziale [0,membership], zapamietac
		// trB(membership)
		i = 0;
		while (i < trB.size - 1 && trB.pts[i].x < membership) {
			if (trB.pts[i].y > maxB)
				maxB = trB.pts[i].y;
			i++;
		}
		if (i == 0)
			dy = 0;
		else
			dy = trB.pts[i].y - trB.pts[i - 1].y;
		if (dy < 0.0)
			dy *= -1;
		dx = membership - trB.pts[i].x;
		if (dx < 0.0)
			dx *= -1;
		if (dy < minDY || dx < minDX) { // punkt koncowy == ostatniemu
			mtrB = trB.pts[i].y;
		} else {
			// wyznacz wartosc dla punktu koncowego : ((x-x1)*(y2-y1)/(x2-x1)) + y1;
			mtrB = (membership - trB.pts[i - 1].x) * (trB.pts[i].y - trB.pts[i - 1].y);
			mtrB /= (trB.pts[i].x - trB.pts[i - 1].x);
			mtrB += trB.pts[i - 1].y;
		}
		if (mtrB > maxB)
			maxB = mtrB;

		// 3.Wybrac max z tnorm(maxA,trB(membership)) i tnorm(maxB,trA(membership))
		maxA = tnorm.calc(maxA, mtrB);
		maxB = tnorm.calc(maxB, mtrA);

		if (maxA > maxB)
			return maxA;
		else
			return maxB;

	}

}
