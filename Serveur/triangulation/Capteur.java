package Serveur.triangulation;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.io.Closeable;
import java.util.ArrayList;

import Serveur.maths.Utils;
import Serveur.maths.vectors.Vector2d;

public class Capteur implements Closeable {
	public static final int MAX_LENGTH_HISTORIQUE = 400;
	public static final int ALPHA_SMOOTH = 10;

	private ArrayList<Vector2d> historiquePos = new ArrayList<Vector2d>();
	private ArrayList<Double> historiqueDistance = new ArrayList<Double>();
	private ArrayList<Double> historiqueRSSI = new ArrayList<Double>();
	private ArrayList<Long> historiqueTempsRSSI = new ArrayList<Long>();
	private ArrayList<Double> historiqueSmoothRSSI = new ArrayList<Double>();
	private ArrayList<Double> historiqueFiltredRSSI = new ArrayList<Double>();
	private Vector2d pos;
	private double distance = 1;
	private double RSSI = 0;

	public Capteur(double x, double y) {
		setPosition(x, y);
	}

	public Vector2d getPosition() {
		return pos.clone();
	}

	public Vector2d[] getHistoriquePos() {
		return historiquePos.toArray(new Vector2d[historiquePos.size()]);
	}

	public void setPosition(double x, double y) {
		pos = new Vector2d(x, y);
		historiquePos.add(pos);

		while (historiquePos.size() > MAX_LENGTH_HISTORIQUE)
			historiquePos.remove(0);
	}

	public double getDistance() {
		return distance;
	}

	public Double[] getHistoriqueDistances() {
		return historiqueDistance
				.toArray(new Double[historiqueDistance.size()]);
	}

	public void setDistance(double distance) {
		this.distance = distance;
		historiqueDistance.add(distance);

		while (historiqueDistance.size() > MAX_LENGTH_HISTORIQUE)
			historiqueDistance.remove(0);
	}

	public double getRSSI() {
		return RSSI;
	}

	public Double[] getHistoriqueRSSI() {
		return historiqueRSSI.toArray(new Double[historiqueRSSI.size()]);
	}

	public Double[] getHistoriqueSmoothRSSI() {
		return historiqueSmoothRSSI
				.toArray(new Double[historiqueSmoothRSSI.size()]);
	}

	public Double[] getHistoriqueFiltredRSSI() {
		return historiqueFiltredRSSI
				.toArray(new Double[historiqueFiltredRSSI.size()]);
	}

	public void setRSSI(double RSSI) {
		this.RSSI = RSSI;
		historiqueRSSI.add(RSSI);
		historiqueTempsRSSI.add(System.currentTimeMillis());

		while (historiqueRSSI.size() > MAX_LENGTH_HISTORIQUE)
			historiqueRSSI.remove(0);
		while (historiqueTempsRSSI.size() > MAX_LENGTH_HISTORIQUE)
			historiqueTempsRSSI.remove(0);

		int size = historiqueRSSI.size();
		double smooth = RSSI;

		if (size >= ALPHA_SMOOTH) {
			smooth = 0;

			for (int i = 1; i <= ALPHA_SMOOTH; i++)
				smooth += historiqueRSSI.get(size - i);

			smooth /= ALPHA_SMOOTH;
			historiqueSmoothRSSI.add(smooth);

			while (historiqueSmoothRSSI.size() > MAX_LENGTH_HISTORIQUE)
				historiqueSmoothRSSI.remove(0);
		}
		
		double filtred = smooth;
		
		if (size == 1)
		{
			historiqueFiltredRSSI.add(RSSI);
		}
		else if (size >= 2)
		{
			double dt = (double) (historiqueTempsRSSI.get(size - 1) - historiqueTempsRSSI.get(size - 2)) / 1000;
			double tau = 5 * dt;
			double lastRSSI = historiqueRSSI.get(size - 2);
			double lastFiltred = historiqueFiltredRSSI.get(size - 2);
			double coeff = 1 / (tau + dt / 2);
			int method = 1;
			filtred = RSSI;
			
			if (method == 1)
				filtred = dt / tau * (RSSI - lastFiltred) + lastFiltred;
			else if (method == 2)
					filtred = coeff * ((tau - dt / 2) * lastFiltred + dt * (RSSI - lastRSSI));
			
			historiqueFiltredRSSI.add(filtred);
		}
		
		while (historiqueFiltredRSSI.size() > MAX_LENGTH_HISTORIQUE)
			historiqueFiltredRSSI.remove(0);

		double distance = 0.02 * Math.pow(10, -filtred / 20); // en metres
		setDistance(distance);
	}

	public void paintComponent(Graphics g, double xmin, double xmax,
			double ymin, double ymax) {
		Rectangle rect = g.getClipBounds();

		int x = (int) Utils.map(pos.x, xmin, xmax, rect.x, rect.x + rect.width);
		int y = (int) Utils.map(pos.y, ymin, ymax, rect.y + rect.height,
				rect.y);
		double distance = getDistance();

		int radius_x = (int) (distance * rect.width / (xmax - xmin));
		int radius_y = (int) (distance * rect.height / (ymax - ymin));

		int epaisseur = 3;

		for (int i = 0; i < epaisseur; i++)
			g.drawOval(x - (radius_x + i), y - (radius_y + i),
					2 * (radius_x + i), 2 * (radius_y + i));

		g.drawLine(x - 3, y - 3, x + 3, y + 3);
		g.drawLine(x - 3, y + 3, x + 3, y - 3);
	}

	public void close() {
		historiquePos.clear();
		historiqueRSSI.clear();
		historiqueTempsRSSI.clear();
		historiqueDistance.clear();
		historiqueSmoothRSSI.clear();
		historiqueFiltredRSSI.clear();
	}
}
