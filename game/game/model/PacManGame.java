package game.model;

import engine.model.Game;

public class PacManGame extends Game {

	public PacManGame(int w_ncell, int h_ncell, boolean torusOnXaxis, boolean torusOnYaxis, double cmPerCell,
			int pixelPerCm) {
		super(w_ncell, h_ncell, torusOnXaxis, torusOnYaxis, cmPerCell, pixelPerCm);
	}

	public PacManGame(double w_cm, double h_cm, boolean torusOnXaxis, boolean torusOnYaxis, double cmPerCell, int pixelPerCm) {
		super(w_cm, h_cm, torusOnXaxis, torusOnYaxis, cmPerCell, pixelPerCm);
	}

}
