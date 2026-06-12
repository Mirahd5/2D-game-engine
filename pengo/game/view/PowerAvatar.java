package game.view;

import java.util.EnumMap;
import java.util.Map;

import engine.model.Picture;
import engine.view.Avatar;
import game.model.Power;
import game.model.PowerType;
import oop.graphics.BufferedImage;
import oop.graphics.Graphics;

public class PowerAvatar extends Avatar {
	
	private final Power power;

	private final Map<PowerType, BufferedImage> images;

	private boolean imagesLoaded;

	public PowerAvatar(Power power) {
		super(power);

		assert power != null;

		this.power = power;
		this.images = new EnumMap<>(PowerType.class);
		this.imagesLoaded = false;
	}

	@Override
	public void paint(Graphics g, Picture picture) {
		assert g != null;
		assert picture != null;

		loadImages(g);

		BufferedImage image = imageForPower();

		if (image == null) {
			return;
		}

		int centerX_pixel = picture.toPixelX(power.center().x());

		int centerY_pixel = picture.toPixelY(power.center().y());

		// 0.65 changable si trop petit ou trop grand
		double size_cm = power.isu().cmPerCell() * 0.65;

		int width_pixel = picture.toPixelLength(size_cm);

		int height_pixel = picture.toPixelLength(size_cm);

		g.drawImage(image, centerX_pixel - width_pixel / 2, centerY_pixel - height_pixel / 2, width_pixel,
				height_pixel);
	}

	// Charge toutes les images une seule fois.
	private void loadImages(Graphics g) {
		if (imagesLoaded) {
			return;
		}

		images.put(PowerType.SHIELD, g.load("pengo_rsrc/powers/shield.png"));

		images.put(PowerType.SPEED, g.load("pengo_rsrc/powers/speed.png"));

		images.put(PowerType.FREEZE, g.load("pengo_rsrc/powers/freeze.png"));

		images.put(PowerType.STAR, g.load("pengo_rsrc/powers/star.png"));

		images.put(PowerType.EXTRA_LIFE, g.load("pengo_rsrc/powers/extra_life.png"));

		images.put(PowerType.BOOMFISH, g.load("pengo_rsrc/powers/boomfish.png"));

		images.put(PowerType.FRIEND_ACTIVATOR, g.load("pengo_rsrc/powers/friend_activator.png"));

		imagesLoaded = true;
	}

	// Renvoie l'image correspondant au type du pouvoir.
	private BufferedImage imageForPower() {
		return images.get(power.getType());
	}
}