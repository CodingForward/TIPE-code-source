package Serveur.maths;

public abstract class Utils {
	/**
	 * réalise un mappage de value entre min inclue et max exclue c'est une
	 * sorte de modulo amélioré
	 * 
	 * exemple: <br>
	 * map(angle, -PI, PI) réalise ramène un angle entre -PI et PI
	 */
	public static double map(double value, double min, double max) {
		double modulo = max - min;

		if (value < min)
			return ((value - min) % modulo + modulo) % modulo + min;

		return (value - min) % modulo + min;
	}

	public static double map(double x, double xmin, double xmax, double ymin,
			double ymax) {
		return (x - xmin) / (xmax - xmin) * (ymax - ymin) + ymin;
	}

	/**
	 * Créer une liste avec un nombre de dimensions spécifiable. <br>
	 * Exemple avec sizes = {4, 2, 3, 1} <br>
	 * La fonction va renvoyer : new Object[4][2][3][1]
	 */
	public static Object[] createList(final int[] sizes) {
		Object[] res = new Object[sizes[0]];

		if (sizes.length > 1) {
			int[] subSizes = new int[sizes.length - 1];

			for (int i = 0; i < subSizes.length; i++)
				subSizes[i] = sizes[i + 1];

			for (int i = 0; i < res.length; i++)
				res[i] = createList(subSizes);
		} else {
			for (int i = 0; i < res.length; i++)
				res[i] = null;
		}

		return res;
	}

	public static int dimensions(Object liste) {
		try {
			return 1 + dimensions(((Object[]) liste)[0]);
		} catch (ClassCastException | NullPointerException e) {
			return 0;
		}
	}

	public static String listStr(Object list, int nbDims) {
		return listStr(list, nbDims, 1);
	}

	public static String listStr(Object list, int nbDims, int sousCouche) {
		String res = "[";
		int length = ((Object[]) list).length;

		if (nbDims > 1) {
			String spaces = "";

			for (int i = 0; i < sousCouche; i++)
				spaces += " ";

			for (int i = 0; i < length; i++) {
				res += listStr(((Object[]) list)[i], nbDims - 1,
						sousCouche + 1);

				if (i < length - 1)
					res += "\n" + spaces;
			}
		} else {
			for (int i = 0; i < length; i++) {
				res += ((Object[]) list)[i];

				if (i < length - 1)
					res += ", ";
			}
		}

		return res + "]";
	}
}
