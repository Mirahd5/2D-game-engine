package game.model;

import engine.model.Game;

public class Pacman extends Game {

	public Pacman() {
		super(31, // largeur carrée
				31, // hauteur carrée
				true, // torus X
				true, // pas torus Y
				3.7, 7 // agrandissement affichage
		);
	}
}