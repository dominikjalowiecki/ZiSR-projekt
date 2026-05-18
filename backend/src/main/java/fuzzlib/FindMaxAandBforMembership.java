package fuzzlib;

import fuzzlib.creators.OperationCreator;
import fuzzlib.norms.Norm;
import fuzzlib.norms.TNorm;

public class FindMaxAandBforMembership implements FindMaxMembership {

	private short compositionType = TNorm.TN_PRODUCT;

	public FindMaxAandBforMembership(short compositionType) {
		super();
		this.compositionType = compositionType;
	}

	public short getCompositionType() {
		return compositionType;
	}

	public void setCompositionType(short compositionType) {
		this.compositionType = compositionType;
	}

	public double findMaxAandBforMembership(double membership, FuzzySet trA, FuzzySet trB, TNorm tnorm, double minDY,
			double minDX) {

		boolean compositionIsTnorm = tnorm.isTnorm(compositionType);
		Norm comp_fun = OperationCreator.newNorm(compositionType);
		
		// dla skrajnych poziomow wywolaj zlaczenie MIN lub MAX - zaleznie czy TNorma czy SNorma
		// - wynik jest ten sam,a ten algorytm da zle wyniki
		if (membership == 0.0 || membership == 1.0) {
			if (compositionIsTnorm) {
				return (new FindMaxAandBforMembershipMIN()).findMaxAandBforMembership(membership, trA, trB, tnorm,
						minDY, minDX);
			} else {
				return (new FindMaxAandBforMembershipMAX()).findMaxAandBforMembership(membership, trA, trB, tnorm,
						minDY, minDX);
			}
		}

		double max = 0.0, tmp;
		SPoint poczA = new SPoint();
		SPoint poczB = new SPoint();
		SPoint konA = new SPoint();
		SPoint konB = new SPoint();
		SPoint tmpkonA = new SPoint();
		SPoint tmpkonB = new SPoint();

		// poczďż˝tkowe indeksy w zbiorach
		int idxA, idxB;
		if (compositionIsTnorm) {
			idxA = trA.getSize() - 1;
			idxB = 0;
		} // ostatni element A ; pierwszy element B
		else {
			idxA = 0;
			idxB = 0;
		} // pierwszy element A ; pierwszy element B
			// Integer iw = 0 ; //INTwraper na integer zamieniďż˝
			// java.lang.Integer iw = new java.lang.Integer(1);
			// Integer iw = new Integer(0);
			// int iwq =0;
		IntWrapper iw = new IntWrapper();
		// Integer iwi = 0 ;
		trB.getMembership(membership, iw);
		idxB = iw.getValue();
		// idxB = iwq;
		if (compositionIsTnorm == false)
			idxB++;

		// ustal poczďż˝tkowe punkty A i B
		poczA.assign(trA.pts[idxA]);
		// wylicz x punktu B ze wzoru
		poczB.x = comp_fun.reverseCalc(membership, poczA.x);
		// oblicz y punktu B jako wartosc funkcji liniowej opisanej punktami zbioru B
		if (compositionIsTnorm)
			poczB.y = calcLinearFunctionForB(poczB.x, trB, idxB); // tnorm
		else
			poczB.y = calcLinearFunctionForA(poczB.x, trB, idxB); // snorm

		boolean End = false;

		while (!End) {

			// jesli konB omija pewne punkty w B to wykonaj
			// obliczenia dla przedzialow tworzonych przez te punkty

			if (compositionIsTnorm) {
				// ustal koncowe punkty zbioru A i B
				// wez kolejny punkt koncowy zbioru A
				konA.assign(trA.pts[idxA - 1]);
				// jesli kolejny punkt <= na granicy przetwarzania to jest to ostatni punkt
				// obliczen
				if (konA.x < membership) { // jesli mniejszy - trzeba wyliczyc y
					konA.x = membership;
					konA.y = calcLinearFunctionForA(konA.x, trA, idxA); // olicz ze wzoru na funkcje lioniowďż˝
					End = true;
				} else if (konA.x == membership)
					End = true; // jesli rowny - nie trzeba liczyc

				// wylicz x punktu B ze wzoru
				konB.x = comp_fun.reverseCalc(membership, konA.x);
				konB.x = membership / konA.x; // dwa razy wykonuje to samo dla prod !!! - poniewaďż˝ taki sam wzďż˝e
												// prodacta

				while (konB.x > trB.pts[idxB + 1].x) {
					// wez kolejny punkt koncowy zbioru B
					tmpkonB.assign(trB.pts[idxB + 1]);
					// punkt x zbioru B nie przekroczy granicy poniewaz x zbioru A nie przekroczyl

					// wylicz punkt koncowy A ze wzoru
					tmpkonA.x = comp_fun.reverseCalc(membership, tmpkonB.x);
					tmpkonA.y = calcLinearFunctionForA(tmpkonA.x, trA, idxA);

					tmp = _FindMaxInRange(poczA, tmpkonA, poczB, tmpkonB, membership, tnorm, compositionIsTnorm);
					if (tmp > 1.0)
						tmp = 1.0;
					if (tmp > max)
						max = tmp;

					poczA.assign(tmpkonA);
					poczB.assign(tmpkonB);

					idxB++; // aktualizuj indeks zbioru B

				}
			} else {
				// ustal koncowe punkty zbioru A i B
				// wez kolejny punkt koncowy zbioru A

				konA.assign(trA.pts[idxA + 1]);

				// jesli kolejny punkt <= granicy przetwarzania to jest to ostatni punkt
				// obliczen
				if (konA.x > membership) { // jesli mniejszy - trzeba wyliczyc y
					konA.x = membership;
					konA.y = calcLinearFunctionForB(konA.x, trA, idxA);
					End = true;
				} else if (konA.x == membership)
					End = true; // jesli rowny - nie trzeba liczyc

				konB.x = comp_fun.reverseCalc(membership, konA.x);

				while (konB.x < trB.pts[idxB - 1].x) {
					// wez kolejny punkt koncowy zbioru B
					tmpkonB.assign(trB.pts[idxB - 1]);
					// punkt x zbioru B nie przekroczy granicy poniewaz x zbioru A nie przekroczyl

					// wylicz punkt koncowy A ze wzoru
					tmpkonA.x = comp_fun.reverseCalc(membership, tmpkonB.x);
					tmpkonA.y = calcLinearFunctionForB(tmpkonA.x, trA, idxA);

					tmp = _FindMaxInRange(poczA, tmpkonA, poczB, tmpkonB, membership, tnorm, compositionIsTnorm);
					if (tmp > 1.0)
						tmp = 1.0;
					if (tmp > max)
						max = tmp;

					poczA.assign(tmpkonA);
					poczB.assign(tmpkonB);

					idxB--; // aktualizuj indeks zbioru B
				}
			}

			// oblicz y punktu B jako wartosc funkcji liniowej opisanej punktami zbioru B
			// - wczesniej nie mozna wykonac tych obliczen poniewaz idxB moze nie wskazywac
			// wlasciwego zakresu
			if (compositionIsTnorm)
				konB.y = calcLinearFunctionForB(konB.x, trB, idxB);
			else
				konB.y = calcLinearFunctionForA(konB.x, trB, idxB);

			tmp = _FindMaxInRange(poczA, konA, poczB, konB, membership, tnorm, compositionIsTnorm);
			if (tmp > 1.0)
				tmp = 1.0;
			if (tmp > max)
				max = tmp;

			poczA.assign(konA);
			poczB.assign(konB);

			if (compositionIsTnorm)
				idxA--; // aktualizuj indeks zbioru A
			else
				idxA++;
		}

		return max;
	}

	// Zmiana 19.1
	// Wzory na obliczenie funcji liniowej
	public double calcLinearFunctionForA(double x, FuzzySet tr, int idx) {
		double result;
		result = (x - tr.pts[idx - 1].x) * (tr.pts[idx].y - tr.pts[idx - 1].y);
		result = result / (tr.pts[idx].x - tr.pts[idx - 1].x) + tr.pts[idx - 1].y;
		return result;

	}

	public double calcLinearFunctionForB(double x, FuzzySet tr, int idx) {
		double result;
		result = (x - tr.pts[idx].x) * (tr.pts[idx + 1].y - tr.pts[idx].y);
		result = result / (tr.pts[idx + 1].x - tr.pts[idx].x) + tr.pts[idx].y;
		return result;

	}

	private static double _FindMaxInRange(SPoint pA, SPoint kA, SPoint pB, SPoint kB, double membership, TNorm norm,
			double minDY, double minDX, boolean whatNorm) {

		double dyA, dyB;

		dyA = kA.y - pA.y;
		dyB = kB.y - pB.y;

		if (dyA * dyB >= 0) { // jesli przedzialy sa jednakowo zmienne lub jeden niezmienny
			if (dyA == 0) { // jesli A niezmienny
				// oblicz wynik dla wiekszego punktu zbioru B
				if (pB.y > kB.y) {
					return norm.calc(pA.y, pB.y);
				} else {
					return norm.calc(kA.y, kB.y);
				}
			} else {
				if (dyB == 0) { // jesli B niezmienny
					// oblicz wynik dla wiekszego punktu zbioru A
					if (pA.y > kA.y) {
						return norm.calc(pA.y, pB.y);
					} else {
						return norm.calc(kA.y, kB.y);
					}
				} else { // jesli oba zmienne
					if (dyA > 0) { // rosna - oblicz wynik dla ostatnich punktow
						return norm.calc(kA.y, kB.y);
					} else { // maleja - oblicz wynik dla poczatkowych punktow
						return norm.calc(pA.y, pB.y);
					}
				}
			}
		} else { // jesli obszary przeciwnie zmienne to szukaj max

			double p_res, k_res, max;
			double dxA, dxB; // ,dyA,dyB;
			SPoint sA = new SPoint();
			SPoint sB = new SPoint();// ,slA,srA,slB,srB;

			// oblicz wynik dla poczatku i konca
			p_res = norm.calc(pA.y, pB.y);
			k_res = norm.calc(kA.y, kB.y);

			// zapisz wiekszy wynik
			if (p_res > k_res)
				max = p_res;
			else
				max = k_res;

			dxA = pA.x - kA.x;
			dxB = kB.x - pB.x;
			// jesli obszary za krotkie to zakoncz
			if (dxA < minDX || dxB < minDX)
				return max;

			// na razie weďż˝ jeden punkt z odleglosci

			dxA = pA.x - kA.x;
			if (whatNorm)
				sA.x = kA.x + dxA / 3.0;
			else
				sA.x = kA.x + 2.0 * dxA / 3.0;
			// zwraca wzďż˝r oblicznia b dla konkretnrj normy
			sB.x = norm.reverseCalc(membership, sA.x);
			dxB = kB.x - sB.x;

			// jesli obszary za krotkie to zakoncz
			if (dxA < minDX || dxB < minDX)
				return max;

			sB.y = (sB.x - pB.x) * (kB.y - pB.y);
			sB.y = sB.y / (kB.x - pB.x) + pB.y;
			sA.y = (sA.x - kA.x) * (pA.y - kA.y);
			sA.y = sA.y / (pA.x - kA.x) + kA.y;
			// wykonaj obliczenia dla punktu
			p_res = norm.calc(sA.y, sB.y);
			if (p_res > max)
				max = p_res;

			return max;
		}
	}

	public static double _FindMaxInRange(SPoint pA, SPoint kA, SPoint pB, SPoint kB, double membership, TNorm norm,
			boolean whatNorm) {
		return _FindMaxInRange(pA, kA, pB, kB, membership, norm, 0.01, 0.001, whatNorm);
	}
}
