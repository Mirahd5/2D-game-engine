package engine.view;

import engine.model.Entity;
import engine.model.ISU;
import oop.graphics.BufferedImage;
import oop.graphics.Graphics;

/*
 * Avatar générique utilisant une ou plusieurs images.
 *
 * Il gère :
 * - les chemins vers les images ;
 * - leur chargement paresseux ;
 * - la frame actuellement affichée ;
 * - le positionnement et le redimensionnement.
 *
 * Il ne décide pas quand une animation doit avancer.
 */
public abstract class ImageAvatar extends Avatar {

	private final String[] paths;
	private final BufferedImage[] images;

	private int currentFrame;

	public ImageAvatar(View view, Entity entity, String[] paths) {
		super(view, entity);

		assert paths != null;
		assert paths.length > 0;

		this.paths = paths.clone();
		this.images = new BufferedImage[paths.length];

		for (String path : this.paths) {
			assert path != null;
			assert !path.isEmpty();
		}

		this.currentFrame = 0;
	}

	protected final int frameCount() {
		return this.paths.length;
	}

	protected final int currentFrame() {
		return this.currentFrame;
	}

	protected final void setCurrentFrame(int frameIndex) {
		assert frameIndex >= 0;
		assert frameIndex < frameCount();

		this.currentFrame = frameIndex;
	}

	protected final void nextFrame() {
		this.currentFrame = (this.currentFrame + 1) % frameCount();
	}

	protected final void resetFrame() {
		this.currentFrame = 0;
	}

	protected final String pathAt(int frameIndex) {
		assert frameIndex >= 0;
		assert frameIndex < frameCount();

		return this.paths[frameIndex];
	}

	protected BufferedImage image(Graphics graphics) {
		assert graphics != null;

		if (this.images[this.currentFrame] == null) {

			this.images[this.currentFrame] = graphics.load(this.paths[this.currentFrame]);
		}

		return this.images[this.currentFrame];
	}

	/*
	 * Permet à un avatar spécifique d'afficher une image plus grande ou plus petite
	 * que la taille physique de l'entité.
	 */
	protected double visualScale() {
		return 1.0;
	}

	/*
	 * true : l'image est tournée graphiquement selon l'orientation de l'entité.
	 *
	 * false : l'avatar possède déjà des images distinctes pour chaque direction.
	 */
	protected boolean rotateWithEntity() {
		return true;
	}

	@Override
	public void paint(Graphics graphics) {
		assert graphics != null;

		BufferedImage image = image(graphics);

		ISU.Dimension size = this.entity.size();

		ISU.Coord center = this.entity.center();

		double scale = visualScale();

		int width = this.view.toPixelWidth(size.x() * scale);

		int height = this.view.toPixelHeight(size.y() * scale);

		int centerX = this.view.toPixelX(center.x());

		int centerY = this.view.toPixelY(center.y());

		int x = centerX - width / 2;

		int y = centerY - height / 2;

		Object savedTransform = graphics.getTransform();

		if (rotateWithEntity()) {
			graphics.translate(centerX, centerY);

			graphics.rotate(Math.toRadians(this.entity.orientation()));

			graphics.translate(-centerX, -centerY);
		}

		graphics.drawImage(image, x, y, width, height);

		graphics.setTransform(savedTransform);
	}
}