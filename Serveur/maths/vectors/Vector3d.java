package Serveur.maths.vectors;

/**
 * Aucune fonction ne change l'état du vecteur. Les fonctions de transformations
 * renvoient un nouveau vecteur. Cette classe travaille avec des double. Cette
 * classe agit à la fois comme un vecteur et comme un point.
 */
public class Vector3d implements Cloneable {
	public double x;
	public double y;
	public double z;

	/**
	 * Créé un vecteur nul en 3D.
	 */
	public Vector3d() {
		x = 0;
		y = 0;
		z = 0;
	}

	/**
	 * Créé un vecteur en 3D.
	 */
	public Vector3d(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	/**
	 * Créé un vecteur aléatoire de norme 1
	 */
	public static Vector3d random() {
		Vector3d result = new Vector3d();
		double norm;

		do {
			result.x = Math.random() * 2 - 1;
			result.y = Math.random() * 2 - 1;
			result.z = Math.random() * 2 - 1;
			norm = result.norm();
		} while (norm > 1);

		return new Vector3d(result.x / norm, result.y / norm, result.z / norm);
	}

	/**
	 * Créé un vecteur avec des coordonnées sphériques.
	 */
	public static Vector3d spherique(double norm, double theta, double phi) {
		double sinPhi = Math.sin(phi);

		return new Vector3d(norm * sinPhi * Math.cos(theta),
				norm * sinPhi * Math.sin(theta), norm * Math.cos(phi));
	}

	/**
	 * @return La norme du vecteur.
	 */
	public double norm() {
		return Math.sqrt(x * x + y * y + z * z);
	}

	/**
	 * @return La norme du vecteur au carré.
	 */
	public double normSquared() {
		return x * x + y * y + z * z;
	}

	/**
	 * @return La distance par rapport à un point.
	 */
	public double distance(double x, double y) {
		return Math.sqrt((this.x - x) * (this.x - x)
				+ (this.y - y) * (this.y - y) + (this.z - z) * (this.z - z));
	}

	/**
	 * @return La distance par rapport à un point.
	 */
	public double distance(final Vector3d v) {
		return Math.sqrt((x - v.x) * (x - v.x) + (y - v.y) * (y - v.y)
				+ (z - v.z) * (z - v.z));
	}

	/**
	 * @return La distance au carré par rapport à un point.
	 */
	public double distanceSquared(double x, double y) {
		return (this.x - x) * (this.x - x) + (this.y - y) * (this.y - y)
				+ (this.z - z) * (this.z - z);
	}

	/**
	 * @return La distance au carré par rapport à un point.
	 */
	public double distanceSquared(final Vector3d v) {
		return (x - v.x) * (x - v.x) + (y - v.y) * (y - v.y)
				+ (z - v.z) * (z - v.z);
	}

	/**
	 * @return Le produit scalaire avec le vecteur (x, y, z).
	 */
	public double dotProduct(double x, double y, double z) {
		return this.x * x + this.y * y + this.z * z;
	}

	/**
	 * @return Le produit scalaire avec le vecteur v.
	 */
	public double dotProduct(final Vector3d v) {
		return x * v.x + y * v.y + z * v.z;
	}

	/**
	 * Ajoute un autre vecteur.
	 */
	public Vector3d add(double x, double y) {
		return new Vector3d(this.x + x, this.y + y, this.z + z);
	}

	/**
	 * Ajoute un autre vecteur.
	 */
	public Vector3d add(final Vector3d v) {
		return new Vector3d(x + v.x, y + v.y, z + v.z);
	}

	/**
	 * Soustrait un autre vecteur.
	 */
	public Vector3d sub(double x, double y, double z) {
		return new Vector3d(this.x - x, this.y - y, this.z - z);
	}

	/**
	 * Soustrait un autre vecteur.
	 */
	public Vector3d sub(final Vector3d v) {
		return new Vector3d(x - v.x, y - v.y, z - v.z);
	}

	/**
	 * Multiplie par un scalaire.
	 */
	public Vector3d mult(double k) {
		return new Vector3d(x * k, y * k, z * k);
	}

	/**
	 * Divise par un scalaire.
	 */
	public Vector3d div(double k) {
		return new Vector3d(x / k, y / k, z / k);
	}

	/**
	 * Applique une homothétie.
	 * 
	 * @param k
	 *            : Le rapport de l'homothétie.
	 * @param origine
	 *            : Le point de l'origine de l'homothétie.
	 * @return Le nouveau vecteur auquel on a appliqué l'homothétie.
	 */
	public Vector3d homothetie(double k, final Vector3d origine) {
		return new Vector3d((x - origine.x) * k + origine.x,
				(y - origine.y) * k + origine.y,
				(z - origine.z) * k + origine.z);
	}

	/**
	 * @return Un nouveau vecteur de même sens mais de norme 1.
	 */
	public Vector3d normalize() {
		double norm = norm();
		return new Vector3d(x / norm, y / norm, z / norm);
	}

	/**
	 * @return Un nouveau vecteur identique.
	 */
	public Vector3d clone() {
		return new Vector3d(x, y, z);
	}

	/**
	 * @return Une chaine indiquant les composantes du vecteur en ligne.
	 */
	public String toString() {
		return "(" + x + ", " + y + ", " + z + ")";
	}

	/**
	 * @return Une chaine indiquant les composantes du vecteur en colonne plutot
	 *         qu'en ligne.
	 */
	public String toStringVertical() {
		return "[" + x + "]\n[" + y + "]\n[" + z + "]";
	}
}
