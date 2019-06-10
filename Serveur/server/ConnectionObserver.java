package Serveur.server;

import java.net.Socket;

public interface ConnectionObserver {
	/**
	 * Cette m�thode est appel�e quand la classe ConnectionListener re�ois une
	 * demande de connection d'un nouveau client. L'observer est alert� du
	 * nouveau client.
	 * 
	 * @param socket
	 *            le socket du nouveau client.
	 */
	public void newClient(Socket socket);
}
