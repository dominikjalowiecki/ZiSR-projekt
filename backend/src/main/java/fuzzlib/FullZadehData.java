package fuzzlib;
import fuzzlib.norms.*;

public class FullZadehData {
	//passed
    public FuzzySet CZ = new FuzzySet();
    public Norm comp;
    public Norm impl;
    public TNorm tnorm;

    //created
    public FuzzySet [] F;
    public FuzzySet [] P;
    public NullNormForA nn;
    public int [] start, stop, idx;

    public int numP,idxCZ, tmpi;
    public double max,tmp,tmpcut;

    public FullZadehData(FuzzySet result, FuzzySet [] Fact, FuzzySet [] Premise,
                  int number_of_premises, FuzzySet conclusion,
                  Norm comp, Norm impl, TNorm tnorm){
    	F = new FuzzySet[number_of_premises]; //zrobic 2 tablice, z intami i FuzzySetami?
        P = new FuzzySet[number_of_premises];
        start = new int[number_of_premises];
        stop  = new int[number_of_premises];
        idx   = new int[number_of_premises];

        result = conclusion;
        CZ = result;

        this.comp = comp;
        this.impl = impl;
        this.tnorm = tnorm;
        numP = number_of_premises;
        IntWrapper wrap = new IntWrapper();

        //make Premises and Facts have description in the same x places
        //and calculate start and stop point of fact description (to speed up calculation)
        for (int i=0; i<number_of_premises; i++) {
            FuzzySet.processSetsWithNorm(F[i], Fact[i], Premise[i],nn);
            FuzzySet.processSetsWithNorm(P[i], Premise[i], Fact[i],nn);
            F[i].getMembership( Fact[i].getPointX(0) , wrap );
            start[i] = wrap.getValue();
            if ( start[i] < 0 ) start[i] = 0;
            else if ( F[i].getPointX(start[i]) < Fact[i].getPointX(0) ) start[i]++;
            F[i].getMembership( Fact[i].getPointX( Fact[i].getSize()-1 ) , wrap );
            stop[i] = wrap.getValue();
            if ( F[i].getPointX(stop[i]) < Fact[i].getPointX( Fact[i].getSize()-1 )) stop[i]++;
        }
    }

}
