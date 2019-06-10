package Serveur.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;

import Serveur.Main;
import Serveur.maths.Fonction;
import Serveur.triangulation.Capteur;

public class ClientTriangulation implements Runnable {
	private volatile boolean active = false, running = false;
	private final Socket socket;
	private final ServerTriangulation server;
	private InputStream in = null;
	private OutputStream out = null;
	public Capteur capteur = null;
	public static final double ZOOM = 2;

	public ClientTriangulation(Socket socket, ServerTriangulation server)
			throws IOException {
		this.socket = socket;
		this.server = server;
		in = socket.getInputStream();
		out = socket.getOutputStream();
	}

	public void start() {
		new Thread(this).start();
	}

	private String receive() throws IOException {
		byte[] buf = new byte[1024];
		StringBuffer strBuf = new StringBuffer();
		int len, i;

		do {
			len = in.read(buf);

			if (len > 0) {
				for (i = 0; i < len; i++)
					strBuf.append((char) buf[i]);
			}
		} while (len == buf.length);

		String msg = strBuf.toString();
		return msg == "" ? null : msg;
	}

	private void send(String msg) throws IOException {
		byte[] buffer = msg.getBytes("UTF-8");
		out.write(buffer);
		out.flush();
	}

	private boolean setup() {
		String msg = null;

		try {
			msg = receive();
		} catch (IOException e) {
			if (!(e instanceof SocketTimeoutException))
				e.printStackTrace();
		}

		boolean clientIsCarteArduino = false;

		if (msg == null)
			clientIsCarteArduino = true;
		else if (msg.startsWith("GET / HTTP/1.1"))
			clientIsCarteArduino = false;
		else if (msg.startsWith("arduino"))
			clientIsCarteArduino = true;

		if (!clientIsCarteArduino) {
			try {
				envoyerLeSite();
			} catch (IOException e) {
				e.printStackTrace();
			}

			try {
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return clientIsCarteArduino;
	}

	@Override
	public void run() {
		active = running = true;
		boolean clientIsCarteArduino = setup();

		if (clientIsCarteArduino) {
			System.out.println(" => arduino");
		} else {
			System.out.println(" => pas arduino");
			active = false;
		}

		while (active) {
			try {
				String msg = receive();

				if (Main.main.DEBUG)
					System.out.println(socket.getInetAddress() + " > " + msg);

				performRequest(msg);
			} catch (IOException e) {
				if (!(e instanceof SocketTimeoutException))
					e.printStackTrace();
				else
					active = false;
			}
		}

		System.out.println(
				"Le client " + socket.getInetAddress() + " est déconnecté.("
						+ server.clients.size() + " clients restants)");

			server.doModifications(() -> {
				server.clients.remove(this);
				return null;
			});

			if (clientIsCarteArduino)
			{
				Main.main.doModifications(() -> {
					Main.main.map.removeCapteur(capteur);
					Main.main.resetBalles();
					return null;
				});

				capteur.close();
			}

		running = false;
	}

	public static boolean isDigit(char c) {
		return c >= 45 && c <= 57 && c != 47;
	}

	/**
	 * renvoie un vecteur (x = le nombre, y = l'indice de fin + 1)
	 */
	public static double nextNumber(String str, int index) {
		int length = str.length();
		int start = index;

		while (start < length && !isDigit(str.charAt(start)))
			start++;

		int end = start;

		while (end < length && isDigit(str.charAt(end)))
			end++;

		return new Double(str.substring(start, end));
	}

	protected void performRequest(String msg) {
		String msgUpper = msg.toUpperCase();

		if (msgUpper.contains("CARD_ID")) {
			capteur = new Capteur(0, 0);

			Main.main.doModifications(() -> {
				Main.main.map.addCapteur(capteur);
				return null;
			});
			
			Main.main.map.doModifications(() -> {
				Main.main.resetBalles();
				return null;
			});
		}

		if (msgUpper.contains("RSSI")) {
			capteur.setRSSI(nextNumber(msgUpper, msgUpper.indexOf("RSSI")));
		}

		if (msgUpper.contains("DISTANCE")) {
			capteur.setDistance(
					nextNumber(msgUpper, msgUpper.indexOf("DISTANCE")));
		}

		if (msgUpper.contains("X")) {
			double x = nextNumber(msgUpper, msgUpper.indexOf("X")) * ZOOM;
			capteur.setPosition(x, capteur.getPosition().y);
		}

		if (msgUpper.contains("Y")) {
			double y = nextNumber(msgUpper, msgUpper.indexOf("Y")) * ZOOM;
			capteur.setPosition(capteur.getPosition().x, y);
		}

		if (msgUpper.contains("STOP"))
			active = false;
	}

	public void envoyerLeSite() throws IOException {
		String code = server.constructHtml();
		send(code);
	}

	public boolean isRunning() {
		return running;
	}

	public void close() {
		active = false;
	}

	public synchronized Object doModifications(Fonction f) {
		return f.method();
	}
}
