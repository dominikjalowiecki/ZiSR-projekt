package fuzzlib.canvasPrnt;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import fuzzlib.FuzzySet;
import fuzzlib.SPoint;
import fuzzlib.creators.OperationCreator;
import fuzzlib.norms.Norm;

public abstract class CanvasPrinter {
	protected int colR,colG,colB;
	protected int fcolR,fcolG,fcolB;
	protected int bcolR,bcolG,bcolB;
	protected int [] cols = new int[3];
	protected int penSize;
	protected int w,h;   //width,height
	protected int dx,dy; //width,height
	protected double zxr; //Z axis X ratio
	protected double zyr; //Z axis Y ratio
	protected double zm;  //zoom;
	protected boolean gradientFill;
	boolean fill = false;

	protected void _decodeColor(int def_color){
		switch(def_color){
		case COLOR_WHITE : colR = colG = colB = 255;
		break;
		case COLOR_BLACK : colR = colG = colB = 0;
		break;
		case COLOR_RED   : colR = 255; colG = colB = 0;
		break;
		case COLOR_REDLIGHT  : colR = 255; colG = colB = 30;
		break;
		case COLOR_REDDARK   : colR = 200; colG = colB = 0;
		break;
		case COLOR_GREEN     : colG = 255; colR = colB = 0;
		break;
		case COLOR_GREENLIGHT: colG = 255; colR = colB = 30;
		break;
		case COLOR_GREENDARK : colG = 200; colR = colB = 0;
		break;
		case COLOR_BLUE      : colB = 255; colR = colG = 0;
		break;
		case COLOR_BLUELIGHT : colB = 255; colR = colG = 30;
		break;
		case COLOR_BLUEDARK  : colB = 200; colR = colG = 0;
		break;
		case COLOR_GRAYLIGHT : colB = colR = colG = 200;
		break;
		case COLOR_GRAYDARK  : colB = colR = colG = 100;
		break;
		case COLOR_CYAN      : colB = 255; colR = 0; colG = 255;
		break;
		case COLOR_CYANLIGHT : colB = 170; colR = 32; colG = 178;
		break;
		case COLOR_CYANDARK  : colB = 139; colR = 0; colG = 139;
		break;
		case COLOR_MAGENTA   : colB = 144; colR = 255; colG = 0;
		break;
		case COLOR_YELLOW    : colR = 255; colB = 0; colG = 255;
		break;
		case COLOR_VIOLET    : colR = 180; colG = 0; colB = 255;
		break;
		case COLOR_VIOLETDARK: colR = 148; colG = 0; colB = 211;
		break;
		case COLOR_PINK      : colR = 255; colG = 0; colB = 255;
		break;
		default: colR = colG = colB = 0;
		}
	}
	public static final int COLOR_WHITE     = 0;
	public static final int COLOR_BLACK     = 1;
	public static final int COLOR_GRAYLIGHT = 2;
	public static final int COLOR_GRAYDARK  = 3;
	public static final int COLOR_RED       = 10;
	public static final int COLOR_REDLIGHT  = 11;
	public static final int COLOR_REDDARK   = 12;
	public static final int COLOR_GREEN     = 20;
	public static final int COLOR_GREENLIGHT= 21;
	public static final int COLOR_GREENDARK = 22;
	public static final int COLOR_BLUE      = 30;
	public static final int COLOR_BLUELIGHT = 31;
	public static final int COLOR_BLUEDARK  = 32;
	public static final int COLOR_YELLOW    = 40;
	public static final int COLOR_VIOLET    = 50;
	public static final int COLOR_VIOLETDARK= 51;
	public static final int COLOR_PINK      = 60;
	public static final int COLOR_CYAN      = 70;
	public static final int COLOR_CYANLIGHT = 71;
	public static final int COLOR_CYANDARK  = 72;
	public static final int COLOR_MAGENTA   = 80;

	CanvasPrinter(){ colR = colG = colB = 0; w = h = dx = dy = 0;
	zxr = zyr = zm = 1.0; penSize = 1; gradientFill = false; };
	public void SetSize(int width, int height){ w=width; h=height; };
	public int getWidth(){ return w;};
	public int getHeight(){ return h;};
	public void SetShift(int xShift, int yShift){ dx=xShift; dy=yShift; };
	public void SetZRatio(double xRatio,double yRatio){ zxr = xRatio; zyr = yRatio; };
	public void SetZoom(double zoom){ zm = zoom; };
	public void setFill(boolean fill) {	this.fill = fill;}	
	public void SetGradientFill(boolean gradient){ gradientFill = gradient; };
	public void SetColor(int def_color){}
	public void SetColor(int r, int g, int b){
		}
	public void SetBrushColor(int def_color){}
	public void SetBrushColor(int r, int g, int b){
		}
	public void SetPenSize(int size){
		penSize = size;
	}
	public abstract void Point(double x, double y, double z);
	public abstract void Line (double begx, double begy, double begz,
			double endx, double endy, double endz);
	public abstract void DrawPolygon(double Ax, double Ay, double Az,
			double Bx, double By, double Bz,
			double Cx, double Cy, double Cz,
			double Dx, double Dy, double Dz);
	public abstract void Fill();

	public void DottedLine (double begx, double begy, double begz,
			double endx, double endy, double endz){
		DottedLine (begx, begy, begz,
				endx, endy, endz, 50);
	}
	public void DottedLine (double begx, double begy, double begz,
			double endx, double endy, double endz, int dots){
		if (dots <= 0) return;
		double stepx = (endx-begx)/dots;
		double stepy = (endy-begy)/dots;
		double stepz = (endz-begz)/dots;
		while( dots>0 ){
			Point(begx,begy,begz);
			begx+=stepx;
			begy+=stepy;
			begz+=stepz;
			dots--;
		}
	}


	public void DrawAxisXYZ(double xlen, double ylen, double zlen){
		DrawAxisXYZ(xlen, ylen, zlen,0.0,0.0,0.0);
	}
	public void DrawAxisXYZ(double xlen, double ylen, double zlen, double xbegin){
		DrawAxisXYZ(xlen, ylen, zlen,xbegin,0.0,0.0);
	}
	public void DrawAxisXYZ(double xlen, double ylen, double zlen, double xbegin, double ybegin){
		DrawAxisXYZ(xlen, ylen, zlen,xbegin, ybegin ,0.0);
	}
	public void DrawAxisXYZ(double xlen, double ylen, double zlen,
			double xbegin, double ybegin, double zbegin){
		Line(xbegin,ybegin,zbegin,xbegin+xlen,ybegin,zbegin);
		Line(xbegin,ybegin,zbegin,xbegin,ybegin+ylen,zbegin);
		Line(xbegin,ybegin,zbegin,xbegin,ybegin,zbegin+zlen);
	}
	public void DrawFuzzySetXY(FuzzySet A, double begA, double endA, double z){
		int size,pos=0;
		double x=0.0;
		double y,lx,ly;

		size = A.getSize();
		if (size<0) return;

		SPoint beg=new SPoint(); 
		SPoint end=new SPoint();

		//pobrac poczatek i koniec
		beg.x = begA; beg.y = A.getMembership(beg.x);
		end.x = endA; end.y = A.getMembership(end.x);

		//ustawic sie na pierwszym punkcie A, ktorego x > beg.x
		while( (pos<size) && ( (x=A.getPointX(pos)) < beg.x ) ){
			pos++;
		}

		//jesli wszystkie punkty zbioru mniejsze od zdefiniowanego przedzialu to
		//rysuj prosta od beg do end i zakoncz
		if (pos==size) {
			Line(beg.x,beg.y,z,end.x,end.y,z);
			return;
		}

		//jesli wszystkie punkty zbioru wieksze od zdefiniowanego przedzialu to
		//rysuj prosta od beg do end i zakoncz
		if( (pos==0) && (x>end.x) ){
			Line(beg.x,beg.y,z,end.x,end.y,z);
			return;
		}
		
		//rysuj kolejne punkty do end.x

		lx = beg.x; ly = beg.y;

		while((pos<size) && ( (x=A.getPointX(pos)) < end.x ) ){
			//x - odczytane w warunku petli
			y = A.getPointY(pos);
			Line(lx,ly,z,x,y,z);
			lx = x; ly = y;
			pos++;
		}
		
		Line(lx,ly,z,end.x,end.y,z);

		/*
	        double x,y;
	        double lx,ly,tmply,tmpy;
	        int first_point;
	        lx = ly = tmply = tmpy = -1.0;
	        first_point = 1;
	        for (x=begA; x<=endA; x+=step){
	              y = A[x];
	              tmpy=y;
	              tmply=ly;
	              if (y<0.0 && ly >0.0) tmpy =0.0;
	              if (ly<0.0 && y >0.0) tmply=0.0;
	              if (!first_point){
	                if (!(ly<0.0 && y <0.0)){
	                  Line(lx,tmply,z,x,tmpy,z);
	                }
	              }
	              lx = x; ly=y;
	              first_point = 0;
	        }
		 */
	}
	public void DrawFuzzySetZY(FuzzySet A, double begA, double endA, double ix){
		int size,pos=0;
		double y,lx,ly;
		double x=0.0;

		size = A.getSize();
		if (size<0) return;

		SPoint beg=new SPoint(); 
		SPoint end=new SPoint();

		//pobrac poczatek i koniec
		beg.x = begA; beg.y = A.getMembership(beg.x);
		end.x = endA; end.y = A.getMembership(end.x);

		//ustawic sie na pierwszym punkcie A, ktorego x > beg.x
		while( (pos<size) && ( (x=A.getPointX(pos)) < beg.x ) ){
			pos++;
		}

		//jesli wszystkie punkty zbioru mniejsze od zdefiniowanego przedzialu to
		//rysuj prosta od beg do end i zakoncz
		if (pos==size) {
			Line(ix,beg.y,beg.x,ix,end.y,end.x);
			return;
		}

		//jesli wszystkie punkty zbioru wieksze od zdefiniowanego przedzialu to
		//rysuj prosta od beg do end i zakoncz
		if( (pos==0) && (x>end.x) ){
			Line(ix,beg.y,beg.x,ix,end.y,end.x);
			return;
		}

		//rysuj kolejne punkty do end.x

		lx = beg.x; ly = beg.y;

		while((pos<size) && ( (x=A.getPointX(pos)) < end.x ) ){
			//x - odczytane w warunku petli
			y = A.getPointY(pos);
			Line(ix,ly,lx,ix,y,x);
			lx = x; ly = y;
			pos++;
		}
		Line(ix,ly,lx,ix,end.y,end.x);

	}

	public void DrawFuzzyImplicationY(FuzzySet A, double begA, double endA,
			FuzzySet B, double begB, double endB,
			short type, double astep, double bstep){
		Norm n = OperationCreator.newImplication(type);

		List <Double> line = new LinkedList<Double>();
		List <Double> last_line = new LinkedList<Double>();
		List <Double> tmp;
		ListIterator<Double> it ;
		
		double a,b,val1=0,val2=0,val3=0,val4,la,lb;
		boolean first_point,first_line;

		first_line = true;
		//w wyniku zaokraglania wynikow przez dodawanie
		//otrzymuje sie wiekszy wynik i nie zostaja wykonane obliczenia
		//dla endA oraz endB - dlatego zwiďż˝ksza siďż˝ minimalnie te zakresy i OK 
		endA = endA + astep/2.0;
		endB = endB + astep/2.0;

		double l = 0;
		for (a=begA; a<=endA; a+=astep){      //przesuwanie po A
			first_point = true;
			it = last_line.listIterator() ; //ustaw na poczatek poprzedniej linii
			if (!first_line) {
				if (it.hasNext()) l = it.next();
			}

			line.clear();           //wyczysc nowa linie
			for (b=begB; b<=endB; b+=bstep){ //przesuwanie po B

				val4 = n.calc(A.getMembership(a),B.getMembership(b)); //oblicz kolejny wynik dla aktualnej pozycji
				line.add( val4 );   //zapamietaj wynik w aktualnej linii
				if (!first_line) {
					val2 = l; // pobierz kolejny wykik z poprzedniej linii
					if (it.hasNext()) l = it.next(); //przesuďż˝ w linii
				}
				//it++;                      

				//jesli co najmniej druga linia i co najmniej drugi punkt w linii to mozna rysowac
				if (!first_line && !first_point) {
					la = a-astep; lb = b-bstep; //poprzednie wspolrzedne a i b
					DrawPolygon(la,val1,lb,
							la,val2,b,
							a,val4,b,
							a,val3,lb);
//	                Line(la,val1,lb,la,val2,b);
//	                Line(la,val2,b,a,val4,b);
//	                Line(a,val3,lb,a,val4,b);
				}
				//rysowane poczatki = aktualnym koncom
				val1=val2;
				val3=val4;

				first_point = false;
			}
			//zamiana linii
			tmp = line;
			line = last_line;
			last_line = tmp;

			first_line = false;
		}

		n=null;
	}
	
//	public void DrawFuzzyImplicationY2(FuzzySet A, double begA, double endA,
//			FuzzySet B, double begB, double endB,
//			short type, double astep, double bstep, boolean fill){
//		Norm n = OperationCreator.newImplication(type);
//
//	    int it;
//	    
//	    List <Double> line = new LinkedList<Double>();
//	    List <Double> last_line = new LinkedList<Double>();
//	    List <Double> tmp;
//	    double a = 0.0;
//	    double b = 0.0;
//	    double val1 = 0.0;
//	    double val2 = 0.0;
//	    double val3 = 0.0;
//	    double val4 = 0.0;
//	    double lb = 0.0;
//	    double la = 0.0;
//	    boolean first_point,first_line;
//
//	    first_line = true;
//	    w wyniku zaokraglania wynikow przez dodawanie
//	    otrzymuje sie wiekszy wynik i nie zostaja wykonane obliczenia
//	    dla endA oraz endB - dlatego zwiďż˝ksza siďż˝ minimalnie te zakresy i OK 
//	    endA = endA + astep/2.0;
//	    endB = endB + astep/2.0;
//
//	    for (a=begA; a<=endA; a+=astep){      //przesuwanie po A
//	        first_point = true;
//	        it = 0; //ustaw na poczatek poprzedniej linii
//	        line.clear();           //wyczysc nowa linie
//	        for (b=begB; b<=endB; b+=bstep){ //przesuwanie po B
//
//	        	val4 = n.calc(A.getMembership(a),B.getMembership(b)); //oblicz kolejny wynik dla aktualnej pozycji
//	            line.add( val4 );   //zapamietaj wynik w aktualnej linii
//	            
//				if (!first_line) {
//					val2 = last_line.get(it); // pobierz kolejny wykik z poprzedniej linii
//					it++; // przesuďż˝ w linii
//				}
//
//	            //jesli co najmniej druga linia i co najmniej drugi punkt w linii to mozna rysowac
//	            if (!first_line && !first_point) {
//	                la = a-astep; lb = b-bstep; //poprzednie wspolrzedne a i b
//	                DrawPolygon(la,val1,lb,
//	                            la,val2,b,
//	                            a,val4,b,
//	                            a,val3,lb, fill);
//
////	                Line(la,val1,lb,la,val2,b);
//	                //Line(la,val2,b,a,val4,b);
//	               // Line(a,val3,lb,a,val4,b);
//	            }
//	              //rysowane poczatki = aktualnym koncom
//	            val1=val2;
//	            val3=val4;
//	            
//	            first_point = false;
//	        }
//	        //zamiana linii
//	        tmp = line;
//	        line = last_line;
//	        last_line = tmp;
//
//	        first_line = false;
//	    }
//        n=null;
//	}
//	
	public void DrawFuzzyImplicationY(FuzzySet A, double begA, double endA,
			FuzzySet B, double begB, double endB,
			short type){
		DrawFuzzyImplicationY(A, begA, endA, B, begB, endB, type,  0.02,  0.02);
	}
	public void DrawFuzzyImplicationY(FuzzySet A, double begA, double endA,
			FuzzySet B, double begB, double endB, short type, double astep){
		DrawFuzzyImplicationY(A, begA, endA, B, begB, endB, type,  astep,  0.02);
	}

	public void DrawFuzzyImplicationY_dots(FuzzySet A, double begA, double endA,
			FuzzySet B, double begB, double endB, short type){
		DrawFuzzyImplicationY_dots(A,  begA, endA,B, begB, endB, type, 0.02, 0.2);
	}
	public void DrawFuzzyImplicationY_dots(FuzzySet A, double begA, double endA,
			FuzzySet B, double begB, double endB, short type, double astep){
		DrawFuzzyImplicationY_dots(A,  begA, endA,B, begB, endB, type, astep, 0.2);
	}
	public void DrawFuzzyImplicationY_dots(FuzzySet A, double begA, double endA,
			FuzzySet B, double begB, double endB,
			short type, double astep, double bstep){
		Norm n = OperationCreator.newImplication(type);

		double x,y,z;
		for (x=begB; x<endB+bstep/2.0; x+=bstep){
			for (y=begA; y<endA+astep/2.0; y+=astep){
				z = n.calc(A.getMembershipD(y),B.getMembership(x));
				Point(y,z,x);
			}
		}

		n=null;

	}
	
	public void DrawChart3D_dots(Chart3D chart){
		double point[] = new double[3];
		chart.gotoFirstLine();
		while( chart.areLines() ){
			while( chart.areValues() ){
				chart.getNext3DPoint(point);
				Point(point[0],point[1],point[2]);
			}
			chart.gotoNextLine();
		}
	}
	
	public void DrawChart3D(Chart3D chart){
		double point1[] = new double[3];
		double point2[] = new double[3];
		double point3[] = new double[3];
		double point4[] = new double[3];
		boolean first_point,first_line;
		
		double min ;
		int tmpR;
		int tmpG;
		int tmpB;
		int fR = bcolR;
		int fG = bcolG;
		int fB = bcolB;

		chart.gotoFirstLine();
		first_line = true;
		while( chart.areLines() ){
			first_point = true;
			while( chart.areValues() ){
				chart.getNext3DPointFromPrevLine(point3);
				chart.getNext3DPoint(point2);
				if (!first_line && !first_point) {
					if (fill && gradientFill){
						min=Math.min(point4[1],(Math.min(point3[1],Math.min(point1[1],point2[1]))));
						tmpR=(int)(min*fR)+50;
						tmpG=(int)(min*fG)+50;
						tmpB=(int)(min*fB)+50;
						if (tmpR>255)tmpR=255;
						if (tmpG>255)tmpG=255;
						if (tmpB>255)tmpB=255;
						if (tmpR<50)tmpR=50;
						if (tmpG<50)tmpG=50;
						if (tmpB<50)tmpB=50;
						SetBrushColor(tmpR, tmpG, tmpB);
					}
					DrawPolygon(point1[0],point1[1],point1[2],
							point2[0],point2[1],point2[2],
							point3[0],point3[1],point3[2],
							point4[0],point4[1],point4[2]);
				}
				point1[0] = point2[0]; point1[1] = point2[1]; point1[2] = point2[2];
				point4[0] = point3[0]; point4[1] = point3[1]; point4[2] = point3[2];
				first_point = false;
			}
			first_line = false;
			chart.gotoNextLine();
		}
	}
}
