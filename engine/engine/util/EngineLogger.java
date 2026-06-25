package engine.util;

import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Configuration centrale du logger du moteur.
 *
 * À appeler une seule fois au démarrage du jeu, par exemple :
 * EngineLogger.configure(Level.INFO);
 */
public final class EngineLogger {

	private EngineLogger() {
		// Classe utilitaire.
	}

	public static void configure(Level level) {
		assert level != null;

		Logger root = Logger.getLogger("");

		for (Handler handler : root.getHandlers()) {
			root.removeHandler(handler);
		}

		ConsoleHandler console = new ConsoleHandler();
		console.setLevel(level);

		root.addHandler(console);
		root.setLevel(level);

		Logger.getLogger("engine").setLevel(level);
		Logger.getLogger("pengo").setLevel(level);
		Logger.getLogger("game").setLevel(level);
	}
}
