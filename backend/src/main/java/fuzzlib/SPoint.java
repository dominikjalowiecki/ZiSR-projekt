package fuzzlib;

//Simple point used in fuzzy set
// Point consists of a pair of floating point values x and y 
public class SPoint implements java.lang.Comparable<SPoint> {
	public double x, y;

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}

	public SPoint() {
		x = 0.0;
		y = 0.0;
	}

	public SPoint(double ix, double iy) {
		x = ix;
		y = iy;
	}

//DODANO 22.06
	public SPoint(SPoint p) {
		this.x = p.x;
		this.y = p.y;
	}
//koniec

//ZMIANA 22.06
	public SPoint assign(SPoint p) {
		this.x = p.x;
		this.y = p.y;
		return this;
	}

//KONIEC
	public int compareTo(SPoint o) {
		SPoint p = (SPoint) o;

		if (p.x > this.x)
			return -1;
		if (p.x < this.x)
			return 1;

		return 0;
	}

}
