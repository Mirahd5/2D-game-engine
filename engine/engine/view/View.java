package engine.view;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import engine.model.Entity;
import engine.model.ISU;
import engine.model.Model;
import engine.model.ViewPort;
import oop.graphics.Graphics;

/*
 * Vue générique du moteur.
 *
 * Elle :
 * - transforme les coordonnées ISU en pixels ;
 * - récupère les entités visibles ;
 * - les trie selon zOrder et sOrder ;
 * - délègue leur dessin aux Avatar ;
 * - délègue éventuellement le HUD à un Overlay.
 *
 * Elle ne contient aucune règle propre à un jeu.
 */
public class View {

	private final Model model;

	/*
	 * Dimensions en pixels de la zone représentant le monde. Elles n'incluent pas
	 * l'éventuel Overlay.
	 */
	private int widthPixel;
	private int heightPixel;

	private Overlay overlay;

	public View(Model model, int widthPixel, int heightPixel) {
		this(model, widthPixel, heightPixel, null);
	}

	public View(Model model, int widthPixel, int heightPixel, Overlay overlay) {
		assert model != null;
		assert widthPixel > 0;
		assert heightPixel > 0;

		this.model = model;
		this.widthPixel = widthPixel;
		this.heightPixel = heightPixel;
		this.overlay = overlay;
	}

	public Model model() {
		return this.model;
	}

	public ViewPort viewPort() {
		return this.model.viewPort();
	}

	public int widthPixel() {
		return this.widthPixel;
	}

	public int heightPixel() {
		return this.heightPixel;
	}

	public int overlayWidthPixel() {
		if (this.overlay == null) {
			return 0;
		}

		return this.overlay.widthPixel();
	}

	/*
	 * Largeur totale nécessaire au Canvas.
	 */
	public int totalWidthPixel() {
		return this.widthPixel + overlayWidthPixel();
	}

	public Overlay overlay() {
		return this.overlay;
	}

	public void setOverlay(Overlay overlay) {
		this.overlay = overlay;
	}

	public void removeOverlay() {
		this.overlay = null;
	}

	public void resize(int widthPixel, int heightPixel) {
		assert widthPixel > 0;
		assert heightPixel > 0;

		this.widthPixel = widthPixel;
		this.heightPixel = heightPixel;
	}

	public double scaleX() {
		return this.widthPixel / this.viewPort().width();
	}

	public double scaleY() {
		return this.heightPixel / this.viewPort().height();
	}

	public int toPixelX(double xIsu) {
		double translated = translatedX(xIsu);

		return (int) Math.round(translated * scaleX());
	}

	public int toPixelY(double yIsu) {
		double translated = translatedY(yIsu);

		return (int) Math.round(translated * scaleY());
	}

	/*
	 * Distance orientée depuis le bord gauche du ViewPort.
	 */
	private double translatedX(double xIsu) {
		double translated = xIsu - this.viewPort().x();

		if (this.model.isu().xAxis().onTorus()) {

			translated = this.model.isu().xAxis().modp(translated, this.model.isu().width());
		}

		return translated;
	}

	/*
	 * Distance orientée depuis le bord supérieur du ViewPort.
	 */
	private double translatedY(double yIsu) {
		double translated = yIsu - this.viewPort().y();

		if (this.model.isu().yAxis().onTorus()) {

			translated = this.model.isu().yAxis().modp(translated, this.model.isu().height());
		}

		return translated;
	}

	public int toPixelWidth(double widthIsu) {
		return (int) Math.round(widthIsu * scaleX());
	}

	public int toPixelHeight(double heightIsu) {
		return (int) Math.round(heightIsu * scaleY());
	}

	/*
	 * Transformation inverse :
	 *
	 * pixel → suppression de l'échelle → ajout de l'origine du ViewPort.
	 */
	public ISU.Coord toISUCoord(int xPixel, int yPixel) {
		assert xPixel >= 0;
		assert xPixel < this.widthPixel;

		assert yPixel >= 0;
		assert yPixel < this.heightPixel;

		double xIsu = this.viewPort().x() + xPixel / scaleX();

		double yIsu = this.viewPort().y() + yPixel / scaleY();

		/*
		 * Le constructeur de Coord normalise automatiquement les axes toriques.
		 */
		return this.model.isu().new Coord(xIsu, yIsu);
	}

	public void paint(Graphics graphics) {
		assert graphics != null;

		Object savedTransform = graphics.getTransform();

		/*
		 * Le monde est limité à sa propre zone. Les avatars ne peuvent pas dessiner
		 * dans le HUD.
		 */
		graphics.setClip(0, 0, this.widthPixel, this.heightPixel);

		List<Entity> visible = new ArrayList<Entity>(this.model.visibleEntities());

		visible.sort(new Comparator<Entity>() {
			@Override
			public int compare(Entity first, Entity second) {
				if (first.zOrder() != second.zOrder()) {
					return Integer.compare(first.zOrder(), second.zOrder());
				}

				return Integer.compare(first.sOrder(), second.sOrder());
			}
		});

		for (Entity entity : visible) {
			if (!entity.alive() || !entity.installed() || entity.avatar() == null) {
				continue;
			}

			entity.avatar().paint(graphics);
		}

		/*
		 * Les avatars ont pu appliquer des rotations ou des translations graphiques.
		 */
		graphics.setTransform(savedTransform);

		/*
		 * L'overlay est dessiné après le monde.
		 */
		if (this.overlay != null) {
			graphics.setClip(0, 0, totalWidthPixel(), this.heightPixel);

			this.overlay.paint(graphics, this, this.widthPixel, 0);
		}
	}

	public void tick(long elapsed) {
		assert elapsed >= 0;

		for (Entity entity : this.model.visibleEntities()) {

			if (!entity.alive() || !entity.installed() || entity.avatar() == null) {
				continue;
			}

			entity.avatar().tick(elapsed);
		}

		if (this.overlay != null) {
			this.overlay.tick(elapsed);
		}
	}
}