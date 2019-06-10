package Serveur.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class ConnectionListener implements Runnable {
	private final ServerSocket socket;
	private ConnectionObserver observer;
	private volatile boolean active = false, running = false;

	public ConnectionListener(ServerSocket socket,
			ConnectionObserver observer) {
		this.socket = socket;
		this.observer = observer;
	}

	public void run() {
		active = running = true;

		while (active) {
			try {
				Socket client = socket.accept();
				observer.newClient(client);
			} catch (IOException e) {
				if (!(e instanceof SocketTimeoutException))
					e.printStackTrace();
			}
		}

		running = false;
	}

	public boolean isRunning() {
		return running;
	}

	public void close() {
		active = false;
	}
}
