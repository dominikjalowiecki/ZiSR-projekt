package fuzzlib.canvasPrnt;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;


public class JavaCanvasPrinter extends CanvasPrinter {
	private Graphics2D g2;
	private int poly_x[] = new int[4];
	private int poly_y[] = new int[4];
	private static BasicStroke pen;

	public JavaCanvasPrinter() {
		w = h = dx = dy = 0;
		zxr = zyr = zm = 1.0;
		pen = new BasicStroke(1);
		fill = false;
	}

	public void SetGraphics(Graphics g) {
		g2 = (Graphics2D) g;
	};

	public void SetColor(int def_color) {
		_decodeColor(def_color);
		fcolR = colR;
		fcolG = colG;
		fcolB = colB;
		g2.setPaint(new Color(fcolR, fcolG, fcolB));
	}

	public void SetColor(int r, int g, int b) {
		fcolR = r;
		fcolG = g;
		fcolB = b;
		g2.setPaint(new Color(fcolR, fcolG, fcolB));
	}

	public void SetBrushColor(int def_color) {
		_decodeColor(def_color);
		bcolR = colR;
		bcolG = colG;
		bcolB = colB;
	}

	public void SetBrushColor(int r, int g, int b) {
		bcolR = r;
		bcolG = g;
		bcolB = b;
	}

	public void SetPenSize(int size) {
		super.SetPenSize(size);
		pen = new BasicStroke(penSize);
		g2.setStroke(pen);
	}

	public void Point(double x, double y, double z) {
		double xi = dx + ((x + z * zxr) * zm);
		double yi = h - dy - ((y + z * zyr) * zm);
		g2.draw(new Line2D.Double(xi, yi, xi, yi));
	}

	public void Line(double begx, double begy, double begz, double endx,
			double endy, double endz) {
		double x1 = dx + ((begx + begz * zxr) * zm);
		double y1 = h - dy - ((begy + begz * zyr) * zm);
		double x2 = dx + ((endx + endz * zxr) * zm);
		double y2 = h - dy - ((endy + endz * zyr) * zm);
		g2.draw(new Line2D.Double(x1, y1, x2, y2));
	}

	public void DrawPolygon(double Ax, double Ay, double Az, double Bx,
			double By, double Bz, double Cx, double Cy, double Cz, double Ddx,
			double Ddy, double Dz) {
		poly_x[0] = (int)(dx + ((Ax + Az * zxr) * zm));
		poly_y[0] = (int)(h - dy - ((Ay + Az * zyr) * zm));
		poly_x[1] = (int)(dx + ((Bx + Bz * zxr) * zm));
		poly_y[1] = (int)(h - dy - ((By + Bz * zyr) * zm));
		poly_x[2] = (int)(dx + ((Cx + Cz * zxr) * zm));
		poly_y[2] = (int)(h - dy - ((Cy + Cz * zyr) * zm));
		poly_x[3] = (int)(dx + ((Ddx + Dz * zxr) * zm));
		poly_y[3] = (int)(h - dy - ((Ddy + Dz * zyr) * zm));
		
		if (!fill) g2.drawPolygon(poly_x, poly_y, 4);
		else{
			g2.setPaint(new Color(bcolR, bcolG, bcolB));
			g2.fillPolygon(poly_x, poly_y, 4);
			g2.setPaint(new Color(fcolR, fcolG, fcolB));
			g2.drawPolygon(poly_x, poly_y, 4);
		}
	}

	public void Fill() {
		g2.setPaint(new Color(bcolR, bcolG, bcolB));
		g2.fillRect(0, 0, (w - 1), (h - 1));
		g2.setPaint(new Color(fcolR, fcolG, fcolB));
	}
}