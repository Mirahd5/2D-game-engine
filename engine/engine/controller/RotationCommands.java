package engine.controller;

/**
 * Commandes de rotation produites par un contrôleur.
 */
public interface RotationCommands {

	void startTurnLeft();

	void stopTurnLeft();

	void startTurnRight();

	void stopTurnRight();
}