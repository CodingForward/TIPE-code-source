package Serveur;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.util.function.Function;

import javax.swing.JFrame;
import javax.swing.JPanel;

import Serveur.fenetre.Graph;
import Serveur.maths.Fonction;
import Serveur.maths.Utils;
import Serveur.server.ClientTriangulation;
import Serveur.server.ServerTriangulation;
import Serveur.triangulation.Balle;
import Serveur.triangulation.Capteur;
import Serveur.triangulation.Map;

public class Main {
	public static Main main;
	public static boolean FORCER_ARRET = true;
	public final boolean DEBUG = false;
	public Map map = new Map();
	public final double RAPPORT_HAUTEUR_LONGUEUR = 9. / 16;
	public double xmin = -10 * ClientTriangulation.ZOOM, xmax = 10 * ClientTriangulation.ZOOM;
	public double ymin = xmin * RAPPORT_HAUTEUR_LONGUEUR,
			ymax = xmax * RAPPORT_HAUTEUR_LONGUEUR;
	public float offset = 30;
	public JFrame fenetre;
	public volatile boolean painted = true;
	public volatile boolean continuer = true;
	public volatile boolean fenetreActive = true;

	public void lancerFenetre(ServerTriangulation server) {
		fenetre = new JFrame();
		fenetre.setSize(1600, 900);
		fenetre.setResizable(false);
		fenetre.setLocationRelativeTo(null);
		fenetre.setTitle("Serveur");
		fenetre.addWindowListener(new WindowListener() {
			public void windowOpened(WindowEvent e) {
			}
			public void windowIconified(WindowEvent e) {
			}
			public void windowDeiconified(WindowEvent e) {
			}
			public void windowDeactivated(WindowEvent e) {
			}

			public void windowClosing(WindowEvent e) {
				if (FORCER_ARRET)
					System.exit(0);

				server.close();
				continuer = false;
				painted = true;
				fenetreActive = false;
			}

			public void windowClosed(WindowEvent e) {
			}
			public void windowActivated(WindowEvent e) {
			}
		});

		fenetre.setVisible(true);

		fenetre.setContentPane(new JPanel() {
			private static final long serialVersionUID = 1L;

			@SuppressWarnings("unused")
			public void paintComponent(Graphics g) {
				int width = getWidth();
				int height = getHeight();

				map.paintComponent(g, xmin, xmax, ymin, ymax);

				painted = true;
			}
		});
	}

	public void lancerGraph(ServerTriangulation server) {
		Function<Float, Float> rssi = t -> 0f;
		fenetre = new Graph(0, 80, offset - 80, offset + 20);
		((Graph) fenetre).foncts.add(rssi);
		((Graph) fenetre).colors.add(new Color(255, 17, 76));
		((Graph) fenetre).colors.add(new Color(50, 80, 255));
		((Graph) fenetre).colors.add(new Color(70, 255, 13));
	}

	@SuppressWarnings("unchecked")
	public Function<Float, Float>[] createFunctionsRSSI() {
		if (map.capteurs.size() == 0)
			return new Function[]{x -> 0f};

		Double[][] listRSSI = {
				map.capteurs.get(0).getHistoriqueRSSI(),
				map.capteurs.get(0).getHistoriqueSmoothRSSI(),
				map.capteurs.get(0).getHistoriqueFiltredRSSI(),
		};

		Function<Float, Float>[] result = new Function[listRSSI.length];
		for (int i = 0; i < listRSSI.length; i++)
		{
			Double[] rssi = listRSSI[i];
			result[i] = x -> {
			switch (rssi.length) {
				case 0 :
					return offset;
				case 1 :
					return (float) (double) rssi[0] + offset;
				default :
					Float t = (float) Utils.map(x, 0, 80, 0, rssi.length - 2);
					int index = (int) (float) t;
					float blend_x = t - index;

					return (float) ((1 - blend_x) * rssi[index]
							+ blend_x * rssi[index + 1]) + offset;
			}};
		}

		return result;
	}

	public void updateRSSI() {
		if (fenetre instanceof Graph)
		{
			((Graph) fenetre).foncts.clear();
			
			for (Function<Float, Float> f : createFunctionsRSSI())
			((Graph) fenetre).foncts.add(f);
		}
	}

	public void repaint() {
		doModifications(() -> {
			updateRSSI();
			painted = false;
			fenetre.repaint();
			while (!painted && fenetreActive);
			return null;
		});
	}

	public void setupMapTest() {
		Capteur capteur1 = new Capteur(-1 - 3, -2 + 3);
		capteur1.setDistance(3);
		map.addCapteur(capteur1);

		Capteur capteur2 = new Capteur(1 - 3, -1.5 + 3);
		capteur2.setDistance(3);
		map.addCapteur(capteur2);

		Capteur capteur3 = new Capteur(3 - 3, 1 + 3);
		capteur3.setDistance(4);
		map.addCapteur(capteur3);

		for (int i = 0; i <= 16; i++) {
			double x = Utils.map(i, 0, 16, xmin, xmax);

			for (int j = 0; j <= 9; j++) {
				double y = Utils.map(j, 0, 9, ymin, ymax);
				map.addBalle(x, y);
			}
		}
	}

	public void resetBalles() {
		for (Balle balle : map.balles)
			balle.close();

		for (int i = 0; i <= 16; i++) {
			double x = Utils.map(i, 0, 16, xmin, xmax);

			for (int j = 0; j <= 9; j++) {
				double y = Utils.map(j, 0, 9, ymin, ymax);
				map.addBalle(x, y);
			}
		}
	}

	public void main() {
		resetBalles();

		try {
			// System.setOut(new PrintStream(new File("out.txt")));
			ServerTriangulation server = new ServerTriangulation(80);
			server.start();
			lancerFenetre(server);
			//lancerGraph(server);
		} catch (IOException e) {
			e.printStackTrace();
		}

		while (continuer) {
			map.getSource(1);
			repaint();
		}

		map.close();
		fenetre.dispose();
	}

	public static void main(String[] args) {
		main = new Main();
		main.main();
	}

	public synchronized Object doModifications(Fonction f) {
		return f.method();
	}
}
