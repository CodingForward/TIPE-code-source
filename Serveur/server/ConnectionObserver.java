package Serveur.server;

import java.net.Socket;

public interface ConnectionObserver {
	/**
	 * Cette méthode est appelée quand la classe ConnectionListener reçois une
	 * demande de connection d'un nouveau client. L'observer est alerté du
	 * nouveau client.
	 * 
	 * @param socket
	 *            le socket du nouveau client.
	 */
	public void newClient(Socket socket);
}
