package fuzzlib;

import fuzzlib.norms.TNorm;

//Function finding maximum of tnorm between A and B truth functions described in
// different spaces. Maximum is found in a subset of [0,1]x[0,1] space
// where xA*xB=membership (product = membership)

//KLASA JEST PRZESTARZALA - nie jest juz uzywana. Zamiast niej jest uniwersalna klasa dla wszystkich zlozeĹ„.
//Pozostawiona jedynie do celow ewentualnych przyszĹ‚ych analiz - jak dziaĹ‚aĹ‚o stare podejscie

public class FindMaxAandBforMembershipPR implements FindMaxMembership {

	public double findMaxAandBforMembership(double membership, FuzzySet trA, FuzzySet trB, TNorm tnorm, double minDY,
			double minDX) {
		// dla skrajnych poziomow wywolaj zlaczenie MIN - poniewaz wynik jest
		// ten sam,
		// a ten algorytm da zle wyniki
		if (membership == 0.0 || membership == 1.0)
			return (new FindMaxAandBforMembershipMIN()).findMaxAandBforMembership(membership, trA, trB, tnorm, minDY,
					minDX);

		// PROBLEM - ZASTANOWIC SIE, PRZETESTOWAC
		// - co zrobic jak funkcja ma nieodpowiednia ilosc punktow
		// lub nie jest opisana w przedziale [0,1]
		// (pierwszy i ostatni punkt opisu muszďż˝ byc dla x=0 i x=1)

		double max = 0.0, tmp;
		SPoint poczA = new SPoint();
		SPoint poczB = new SPoint();
		SPoint konA = new SPoint();
		SPoint konB = new SPoint();
		SPoint tmpkonA = new SPoint();
		SPoint tmpkonB = new SPoint();

		// poczďż˝tkowe indeksy w zbiorach
		int idxA = trA.size - 1; // ostatni element A
		int idxB = 0; // pierwszy element B
		IntWrapper iw = new IntWrapper();
		trB.getMembership(membership, iw); // przesuniecie indeksu zbioru B do
											// poczatkowej pozycji
		idxB = iw.getValue();

		// ustal poczatkowe punkty zbioru A i B
		poczA.assign(trA.pts[idxA]);
		poczB.x = membership / poczA.x; // wylicz x punktu B ze wzoru
										// xA*xB=membership
		// oblicz y punktu B jako wartosc funkcji liniowej opisanej punktami
		// zbioru B
		poczB.y = (poczB.x - trB.pts[idxB].x) * (trB.pts[idxB + 1].y - trB.pts[idxB].y);
		poczB.y = poczB.y / (trB.pts[idxB + 1].x - trB.pts[idxB].x) + trB.pts[idxB].y;

		boolean End = false;

		while (!End) {

			// ustal koncowe punkty zbioru A i B
			// wez kolejny punkt koncowy zbioru A
			konA.assign(trA.pts[idxA - 1]);
			// jesli kolejny punkt <= granicy przetwarzania to jest to ostatni
			// punkt obliczen
			if (konA.x < membership) { // jesli mniejszy - trzeba wyliczyc y
				konA.x = membership;
				konA.y = (konA.x - trA.pts[idxA - 1].x) * (trA.pts[idxA].y - trA.pts[idxA - 1].y);
				konA.y = konA.y / (trA.pts[idxA].x - trA.pts[idxA - 1].x) + trA.pts[idxA - 1].y;
				End = true;
			} else if (konA.x == membership)
				End = true; // jesli rowny - nie trzeba liczyc

			// membership w tym wypadku nie bedzie juz rowny 0 - sprawdzenie na
			// poczatku metody
			// if (membership == 0) konB.x = 0;
			// else

			// wylicz punkt B ze wzoru xA*xB=membership
			konB.x = membership / konA.x;
			// jesli konB omija pewne punkty w B to wykonaj
			// obliczenia dla przedzialow tworzonych przez te punkty
			while (konB.x > trB.pts[idxB + 1].x) {
				// wez kolejny punkt koncowy zbioru B
				tmpkonB.assign(trB.pts[idxB + 1]);
				// punkt x zbioru B nie przekroczy granicy poniewaz x zbioru A
				// nie przekroczyl

				// wylicz punkt koncowy A ze wzoru xA*xB=membership
				tmpkonA.x = membership / tmpkonB.x;
				tmpkonA.y = (tmpkonA.x - trA.pts[idxA - 1].x) * (trA.pts[idxA].y - trA.pts[idxA - 1].y);
				tmpkonA.y = tmpkonA.y / (trA.pts[idxA].x - trA.pts[idxA - 1].x) + trA.pts[idxA - 1].y;

				tmp = _FindMaxInRangePR(poczA, tmpkonA, poczB, tmpkonB, membership, tnorm);
				if (tmp > 1.0)
					tmp = 1.0;
				if (tmp > max)
					max = tmp;

				poczA.assign(tmpkonA);
				poczB.assign(tmpkonB);

				idxB++; // aktualizuj indeks zbioru B
			}
			// oblicz y punktu B jako wartosc funkcji liniowej opisanej punktami
			// zbioru B
			// - wczesniej nie mozna wykonac tych obliczen poniewaz idxB moze
			// nie wskazywac
			// wlasciwego zakresu
			konB.y = (konB.x - trB.pts[idxB].x) * (trB.pts[idxB + 1].y - trB.pts[idxB].y);
			konB.y = konB.y / (trB.pts[idxB + 1].x - trB.pts[idxB].x) + trB.pts[idxB].y;

			tmp = _FindMaxInRangePR(poczA, konA, poczB, konB, membership, tnorm);
			if (tmp > 1.0)
				tmp = 1.0;
			if (tmp > max)
				max = tmp;

			poczA.assign(konA);
			poczB.assign(konB);

			idxA--; // aktualizuj indeks zbioru A
		}

		return max;
	}

	// Function finding maximum of tnorm between given A and B ranges of truth
	// functions
	// in a subset of [0,1]x[0,1] space where xA*xB=membership (product = membrship)
	private static double _FindMaxInRangePR(SPoint pA, SPoint kA, SPoint pB, SPoint kB, double membership, TNorm tnorm,
			double minDY, double minDX) {
		double dyA, dyB;

		dyA = kA.y - pA.y;
		dyB = kB.y - pB.y;

		if (dyA * dyB >= 0) { // jesli przedzialy sa jednakowo zmienne lub jeden
								// niezmienny
			if (dyA == 0) { // jesli A niezmienny
				// oblicz wynik dla wiekszego punktu zbioru B
				if (pB.y > kB.y) {
					return tnorm.calc(pA.y, pB.y);
				} else {
					return tnorm.calc(kA.y, kB.y);
				}
			} else {
				if (dyB == 0) { // jesli B niezmienny
					// oblicz wynik dla wiekszego punktu zbioru A
					if (pA.y > kA.y) {
						return tnorm.calc(pA.y, pB.y);
					} else {
						return tnorm.calc(kA.y, kB.y);
					}
				} else { // jesli oba zmienne
					if (dyA > 0) { // rosna - oblicz wynik dla ostatnich punktow
						return tnorm.calc(kA.y, kB.y);
					} else { // maleja - oblicz wynik dla poczatkowych punktow
						return tnorm.calc(pA.y, pB.y);
					}
				}
			}
		} else { // jesli obszary przeciwnie zmienne to szukaj max

			double p_res, k_res, max;
			double dxA, dxB; // ,dyA,dyB;
			SPoint sA = new SPoint();
			SPoint sB = new SPoint(); // ,slA,srA,slB,srB;

			// oblicz wynik dla poczatku i konca
			p_res = tnorm.calc(pA.y, pB.y);
			k_res = tnorm.calc(kA.y, kB.y);

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

			// na razie weďż˝ jeden punkt z odleglosci 1/3 obszaru zbioru A

			dxA = pA.x - kA.x;
			sA.x = kA.x + dxA / 3.0;
			sB.x = membership / sA.x;
			dxB = kB.x - sB.x;
			// jesli obszary za krotkie to zakoncz
			if (dxA < minDX || dxB < minDX)
				return max;

			sB.y = (sB.x - pB.x) * (kB.y - pB.y);
			sB.y = sB.y / (kB.x - pB.x) + pB.y;
			sA.y = (sA.x - kA.x) * (pA.y - kA.y);
			sA.y = sA.y / (pA.x - kA.x) + kA.y;
			// wykonaj obliczenia dla punktu
			p_res = tnorm.calc(sA.y, sB.y);
			if (p_res > max)
				max = p_res;

			return max;

			/*
			 * //oblicz wspolrzedne srodka przedzialu B i przenies na A dxB = kB.x - pB.x;
			 * sB.x = pB.x + dxB/2; sA.x = membership/sB.x; dxA = pA.x - sA.x; //jesli
			 * obszary za krotkie to zakoncz if ( dxA<minDX || dxB<minDX ) return max;
			 * 
			 * sB.y = (sB.x - pB.x) * (kB.y - pB.y); sB.y = sB.y / (kB.x - pB.x) + pB.y;
			 * sA.y = (sA.x - kA.x) * (pA.y - kA.y); sA.y = sA.y / (pA.x - kA.x) + kA.y;
			 * //wykonaj obliczenia dla srodka s_resB = tnorm.calc(sA.y,sB.y); dyB = s_res -
			 * max; if (s_res>max) max = s_res;
			 * 
			 * //oblicz wspolrzedne srodka przedzialu A i przenies na B dxA = pA.x - kA.x;
			 * sA.x = kA.x + dxB/2; sB.x = membership/sA.x; dxB = kB.x - sB.x; //jesli
			 * obszary za krotkie to zakoncz if ( dxA<minDX || dxB<minDX ) return max;
			 * 
			 * sB.y = (sB.x - pB.x) * (kB.y - pB.y); sB.y = sB.y / (kB.x - pB.x) + pB.y;
			 * sA.y = (sA.x - kA.x) * (pA.y - kA.y); sA.y = sA.y / (pA.x - kA.x) + kA.y;
			 * //wykonaj obliczenia dla srodka s_resA = tnorm.calc(sA.y,sB.y); dyA = s_res -
			 * max; if (s_res>max) max = s_res;
			 * 
			 * //jesli niewielka roznica w wyniku to zakoncz if (dyA < minDY) { if (dyB <
			 * minDY) return max; else { //dziel B } } else { //dziel A };
			 */
		}
	}

	private static double _FindMaxInRangePR(SPoint pA, SPoint kA, SPoint pB, SPoint kB, double membership,
			TNorm tnorm) {
		return _FindMaxInRangePR(pA, kA, pB, kB, membership, tnorm, 0.01, 0.001);
	}
}
