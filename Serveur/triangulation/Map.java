package Serveur.triangulation;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.io.Closeable;
import java.util.ArrayList;
import java.util.Iterator;

import Serveur.fenetre.Drawer;
import Serveur.maths.Fonction;
import Serveur.maths.Utils;
import Serveur.maths.vectors.Vector2d;
import Serveur.maths.vectors.Vector3d;

public class Map implements Closeable {
	public ArrayList<Capteur> capteurs = new ArrayList<Capteur>();
	public ArrayList<Balle> balles = new ArrayList<Balle>();

	public Map() {

	}

	public void addCapteur(Capteur capteur) {
		capteurs.add(capteur);
	}

	public void addCapteur(double x, double y) {
		capteurs.add(new Capteur(x, y));
	}

	public boolean removeCapteur(Capteur capteur) {
		boolean succes = capteurs.remove(capteur);
		return succes;
	}

	public void addBalle(double x, double y) {
		balles.add(new Balle(x, y));
	}

	public void addBalle(Balle balle) {
		balles.add(balle);
	}

	public boolean removeBalle(Balle balle) {
		System.out.println("remove balle " + balle);
		boolean succes = balles.remove(balle);
		return succes;
	}

	public ArrayList<Vector3d> getCercles() {
		ArrayList<Vector3d> cercles = new ArrayList<Vector3d>();

		for (Capteur capteur : capteurs) {
			Vector2d pos = capteur.getPosition();
			cercles.add(new Vector3d(pos.x, pos.y, capteur.getDistance()));
		}

		return cercles;
	}

	public Vector2d getSource() {
		return getSource(1000);
	}

	public Vector2d getSource(int iterations) {
		Vector2d sumPos = (Vector2d) doModifications(() -> {
			ArrayList<Vector3d> cercles = getCercles();
			Vector2d sumPos2 = new Vector2d();

			for (Balle balle : balles) {
				Vector2d pos;

				if (iterations > 0)
					pos = balle.train(cercles, iterations);
				else
					pos = balle.getPosition();

				sumPos2 = sumPos2.add(pos);
			}

			cercles.clear();

			return sumPos2;
		});

		return sumPos.div(balles.size());
	}

	public int removeExtremesBalles(Vector2d source) {
		Iterator<Balle> it = balles.iterator();
		double variance = variance(source);
		int nb = 0;

		while (it.hasNext()) {
			if (it.next().getPosition().distanceSquared(source) > variance) {
				it.remove();
				nb++;
			}
		}

		return nb;
	}

	public double variance(Vector2d source) {
		double v = 0;

		for (Balle balle : balles) {
			Vector2d pos = balle.getPosition();
			v += pos.distanceSquared(source);
		}

		return v / balles.size();
	}

	private void drawAxis(Graphics g, double xmin, double xmax, double ymin,
			double ymax) {
		Rectangle rect = g.getClipBounds();

		int x0 = (int) Utils.map(0, xmin, xmax, rect.x, rect.x + rect.width);
		int y0 = (int) Utils.map(0, ymin, ymax, rect.y, rect.y + rect.height);

		g.drawLine(x0, rect.y, x0, rect.y + rect.height);
		g.drawLine(rect.x, y0, rect.x + rect.width, y0);

		double delta_x = (double) rect.width / (xmax - xmin);
		double delta_y = (double) rect.height / (ymax - ymin);

		for (double x = x0; x < rect.x + rect.width; x += delta_x)
			g.drawLine((int) x, y0 + 5, (int) x, y0 - 5);

		for (double x = x0; x > rect.x; x -= delta_x)
			g.drawLine((int) x, y0 + 5, (int) x, y0 - 5);

		for (double y = y0; y < rect.y + rect.height; y += delta_y)
			g.drawLine(x0 + 5, (int) y, x0 - 5, (int) y);

		for (double y = y0; y > rect.y; y -= delta_y)
			g.drawLine(x0 + 5, (int) y, x0 - 5, (int) y);
	}

	public void paintComponent(Graphics g, double xmin, double xmax,
			double ymin, double ymax) {
		g.setColor(Color.BLACK);
		drawAxis(g, xmin, xmax, ymin, ymax);

		new Drawer(g).setAntiAliasing(true);
		g.setColor(new Color(134, 95, 255));

		for (Capteur capteur : capteurs)
			capteur.paintComponent(g, xmin, xmax, ymin, ymax);

		g.setColor(new Color(34, 177, 76));

		for (Balle balle : balles)
			balle.paintComponent(g, xmin, xmax, ymin, ymax);
	}

	public void close() {
		for (Balle balle : balles)
			balle.close();

		for (Capteur capteur : capteurs)
			capteur.close();

		balles.clear();
		capteurs.clear();
	}

	public synchronized Object doModifications(Fonction f) {
		return f.method();
	}
}
