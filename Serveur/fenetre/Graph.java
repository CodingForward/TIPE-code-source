package Serveur.fenetre;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.function.Function;

import javax.swing.JFrame;
import javax.swing.JPanel;

import Serveur.Main;
import Serveur.maths.Utils;

/**
	Sert à tracer des courbes sur une fenêtre java.
*/
public class Graph extends JFrame {
	private static final long serialVersionUID = 1L;
	public int avancementPixel = 1;
	public ArrayList<Function<Float, Float>> foncts = new ArrayList<Function<Float, Float>>();
	public ArrayList<Color> colors = new ArrayList<Color>();
	public volatile boolean painted = true;
	private Closeable closer = null;

	public Graph(float xmin, float xmax, float ymin, float ymax) {
		super();
		super.setTitle("Graph");
		super.setSize(1600, 900);
		super.setContentPane(new JPanel() {
			private static final long serialVersionUID = 1L;

			public void paintComponent(Graphics g) {
				Rectangle rect = g.getClipBounds();
				Drawer drawer = new Drawer(g);
				drawer.setAntiAliasing(true);

				int zero_x = (int) Utils.map(0, xmin, xmax, rect.x, rect.x + rect.width - 1);
				int zero_y = (int) Utils.map(0, ymin, ymax, rect.y + rect.height - 1, rect.y);

				g.setColor(Color.WHITE);
				g.fillRect(0, 0, rect.width, rect.height);
				g.setColor(Color.BLACK);
				g.drawLine(0, zero_y, rect.width - 1, zero_y);
				g.drawLine(zero_x, 0, zero_x, rect.height - 1);

				double delta_x = (double) rect.width / (xmax - xmin);
				double delta_y = (double) rect.height / (ymax - ymin);

				for (double x = zero_x; x < rect.x + rect.width; x += delta_x)
					g.drawLine((int) x, zero_y + 5, (int) x, zero_y - 5);

				for (double x = zero_x; x > rect.x; x -= delta_x)
					g.drawLine((int) x, zero_y + 5, (int) x, zero_y - 5);

				for (double y = zero_y; y < rect.y + rect.height; y += delta_y)
					g.drawLine(zero_x + 5, (int) y, zero_x - 5, (int) y);

				for (double y = zero_y; y > rect.y; y -= delta_y)
					g.drawLine(zero_x + 5, (int) y, zero_x - 5, (int) y);

				int lasty = 0;
				float angle = 0;
				Iterator<Color> colorsIt = colors.iterator();
				
				for (Function<Float, Float> f : foncts)
				{
					if (colorsIt.hasNext())
						g.setColor(colorsIt.next());
					else
					{
						g.setColor(Color.getHSBColor((float) (angle / (2 * Math.PI)), 1, 0.9f));
						angle = (float) ((angle + 3.6) % (2 * Math.PI));
					}
					
					for (int x = 0; x < rect.width; x += avancementPixel) {
						float xmap = (float) Utils.map(x, 0, rect.width - 1, xmin, xmax);
						float ymap = f.apply(xmap);
						int y = (int) Utils.map(ymap, ymin, ymax, rect.height - 1, 0);

						if (x > 0)
							drawer.drawBigLine(x - 1, lasty, x, y, 3);

						lasty = y;
					}
					
				}

				painted = true;
				Main.main.painted = true;
			}
		});

		super.addWindowListener(new WindowListener() {
			public void windowOpened(WindowEvent e) {
			}
			public void windowIconified(WindowEvent e) {
			}
			public void windowDeiconified(WindowEvent e) {
			}
			public void windowDeactivated(WindowEvent e) {
			}

			public void windowClosing(WindowEvent e) {
				foncts.clear();
				colors.clear();
				
				if (Graph.this.closer != null)
					try {
						Graph.this.closer.close();
					} catch (IOException ex) {
						ex.printStackTrace();
					}
				System.exit(0);
			}

			public void windowClosed(WindowEvent e) {
			}
			public void windowActivated(WindowEvent e) {
			}
		});
		super.setVisible(true);
	}

	public void repaint() {
		painted = false;
		super.repaint();
		while (!painted);
	}

	public void setCloser(Closeable closer) {
		this.closer = closer;
	}
}