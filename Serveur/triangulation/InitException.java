package Serveur.triangulation;

public class InitException extends Exception {
	private static final long serialVersionUID = 1L;

	public InitException() {
		super("Erreur d'initialisation.");
	}

	public InitException(String msg) {
		super(msg);
	}
}
