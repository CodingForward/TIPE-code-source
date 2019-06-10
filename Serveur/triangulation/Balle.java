package Serveur.triangulation;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.io.Closeable;
import java.util.ArrayList;

import Serveur.maths.Utils;
import Serveur.maths.vectors.Vector2d;
import Serveur.maths.vectors.Vector3d;

public class Balle implements Closeable {
	private ArrayList<Vector2d> gradients = new ArrayList<Vector2d>();
	private Vector2d pos;

	public double learningRate = 0.1;
	public double momentum = 0.5;

	private int nbGradientsMax;
	private volatile boolean training = false;

	public Balle(double x, double y) {
		pos = new Vector2d(x, y);

		setMomentum(momentum);
	}

	public Vector2d getPosition() {
		return pos.clone();
	}

	public void setPosition(Vector2d pos) {
		this.pos = pos.clone();

		gradients.clear();
	}

	private void setMomentum(double momentum) {
		this.momentum = momentum;
		nbGradientsMax = (int) (Math.log(0.02) / Math.log(momentum));

		while (gradients.size() > nbGradientsMax)
			gradients.remove(0);
	}

	private void gradientDescent(final ArrayList<Vector3d> cercles) {
		training = true;
		Vector3d[] tangentes = calculateTangentes(cercles);

		Vector2d gradient = new Vector2d();
		int nbCercles = cercles.size();

		for (Vector3d t : tangentes) {
			double cte = 2 * (t.x * pos.x + t.y * pos.y + t.z)
					/ (nbCercles * (t.x * t.x + t.y * t.y));

			gradient.x += cte * t.x;
			gradient.y += cte * t.y;
		}

		for (Vector2d g : gradients) {
			g.x *= momentum;
			g.y *= momentum;
		}

		gradients.add(gradient);

		while (gradients.size() > nbGradientsMax)
			gradients.remove(0);

		Vector2d totalGradient = new Vector2d();

		for (Vector2d g : gradients)
			totalGradient = totalGradient.add(g);

		training = false;

		pos = pos.sub(totalGradient.mult(learningRate));
	}

	private Vector3d[] calculateTangentes(final ArrayList<Vector3d> cercles) {
		ArrayList<Vector3d> tangentes = new ArrayList<Vector3d>(); // (a, b, c)
																	// tel que a
																	// * x + b *
																	// y + c = 0

		for (Vector3d cercle : cercles) {
			if (cercle.z == 0)
				continue;

			Vector3d inter = cercle.clone();
			Vector2d centre = new Vector2d(cercle.x, cercle.y);
			double distance = centre.distance(pos);

			if (distance == 0)
				continue;

			double coeff = cercle.z / distance; // rayon / distance

			inter.x += coeff * (pos.x - cercle.x);
			inter.y += coeff * (pos.y - cercle.y);

			double a = pos.x - inter.x;
			double b = pos.y - inter.y;
			double c = -a * inter.x - b * inter.y;

			if (Math.abs(a) > Math.abs(b) && Math.abs(a) > Math.abs(c)) {
				b /= a;
				c /= a;
				a = 1;
			} else if (Math.abs(b) > Math.abs(c)) {
				a /= b;
				c /= b;
				b = 1;
			} else {
				a /= c;
				b /= c;
				c = 1;
			}

			Vector3d tangente = new Vector3d(a, b, c);

			if (tangente.normSquared() > 0)
				tangentes.add(tangente);
		}

		Vector3d[] list = tangentes.toArray(new Vector3d[tangentes.size()]);
		tangentes.clear();

		return list;
	}

	public Vector2d train(final ArrayList<Vector3d> cercles, int iterations) {

		for (int i = 0; i < iterations; i++)
			gradientDescent(cercles);

		return pos.clone();
	}

	public void paintComponent(Graphics g, double xmin, double xmax,
			double ymin, double ymax) {
		Rectangle rect = g.getClipBounds();

		int x = (int) Utils.map(pos.x, xmin, xmax, rect.x, rect.x + rect.width);
		int y = (int) Utils.map(pos.y, ymin, ymax, rect.y + rect.height,
				rect.y);
		int radius = 5;

		g.fillOval(x - radius, y - radius, 2 * radius, 2 * radius);
	}

	public void close() {
		while (training);
		gradients.clear();
	}
}
