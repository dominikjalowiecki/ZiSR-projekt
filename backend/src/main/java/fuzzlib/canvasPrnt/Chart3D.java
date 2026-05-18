package fuzzlib.canvasPrnt;
import java.util.ArrayList;
public class Chart3D {
	private ArrayList<ArrayList <Double> > results = new ArrayList<ArrayList <Double> >();
	private ArrayList<Double> last_line = new ArrayList <Double>();
	//private std::list< std::list<double> * >::iterator it;
	//private static int itres;
	//private static int itlastl;
	//private static int itl;
	//private std::list<double>::iterator itl;
	//private std::list<double>::iterator itll;
	//private std::list<double> * last_line;

	private static int inner_line_pos,last_line_pos, cur_line_pos;
	private double startx,startz,stepx,stepz;
	private double x,z;


    public Chart3D(){
    	//cur_line_pos = last_line_pos = -2;
        //cur_line = -2;
        startx = startz = 0;
        stepx  = stepz  = 1;
    	x = z = 0;
        cur_line_pos=last_line_pos=inner_line_pos=-2;
    }
    public void desChart3D(){
    	Clear();
    	}
    public void setStep(double step){stepx = stepz = step;}
    public void setStepX(double stepX){stepx = stepX;}
    public void setStepZ(double stepZ){stepz = stepZ;}
    public void setStartX(double startX){startx = startX;}
    public void setStartZ(double startZ){startz = startZ;}

    public void Clear(){
    	for ( int a=0 ; a <= (results.size()-1); a++ ){
        results.remove(a);
    }
    results.clear();
    cur_line_pos=last_line_pos=inner_line_pos=-2;
    //cur_line_pos = last_line_pos = -2;
    //cur_line = -2;
    }

    public void newLine(){
    	results.add(new ArrayList<Double>());
    	
    }
    public void putValue(double value){
    	if(results.isEmpty()) results.add(new ArrayList<Double>());
    	results.get(results.size()-1).add(value);
    }

    public void gotoFirstLine(){
    	if(!results.isEmpty()){
    		cur_line_pos=0;
    		x=startx;
    		z=startz;
    		last_line_pos=-2;
    		if(!results.get(cur_line_pos).isEmpty()) inner_line_pos=0;
    		else inner_line_pos=-2;
    	}
    	else cur_line_pos=last_line_pos=inner_line_pos=-2;
    }
    public boolean gotoNextLine(){
    	//jezeli nie jesteśmy na ostatnim elemencie
    	if((cur_line_pos >=0) && (cur_line_pos+1 < results.size())){
    		last_line=results.get(cur_line_pos);
    		cur_line_pos++;
    		z+=stepz;
    		x=startx;
    		if(!results.get(cur_line_pos).isEmpty())inner_line_pos=last_line_pos=0;
    		else inner_line_pos=last_line_pos=-2;
    		//cur_line_pos++;
        	return true;
    	}
    	else{
    		cur_line_pos=last_line_pos=inner_line_pos=-2;
    		return false;
    	}
    }
    public double getNextValue(){
    	double value=0;
    	if((inner_line_pos >= 0) && inner_line_pos < (results.get(cur_line_pos).size())){
    		value=results.get(cur_line_pos).get(inner_line_pos);
    		inner_line_pos++;
    		x+=stepx;
    	}
    	return value;
    }
    public void getNext3DPoint(double values[]){
    	if((inner_line_pos>=0) && (inner_line_pos < results.get(cur_line_pos).size())){
    	values[0]=x;
    	values[1]=results.get(cur_line_pos).get(inner_line_pos);
    	values[2]=z;
    	inner_line_pos++;
    	x+=stepx;
    	}
    }
    public void getNext3DPointFromPrevLine(double values[]){
    	if((last_line_pos>=0) && (last_line_pos < last_line.size())){
    		values[0]=x;
    		values[1]=last_line.get(last_line_pos);
    		values[2]=z-stepz;
    		last_line_pos++;
    	}
    }

    public boolean areLines(){
    	if((cur_line_pos >= 0) && cur_line_pos < results.size()) return true;
    	else return false;
    }
    public boolean areValues(){
    	if((inner_line_pos >= 0) && inner_line_pos < results.get(cur_line_pos).size()) return true;
    	else return false;
    }
}
