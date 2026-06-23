package game.view;

import engine.view.Avatar;
import engine.view.SpawnAnimation;
import engine.view.View;
import game.model.Friend;
import oop.graphics.BufferedImage;
import oop.graphics.Graphics;

/** Avatar directionnel de l'allié. */
public final class FriendAvatar extends Avatar {

	private final Friend friend;
	private final SpawnAnimation spawnAnimation;

	private BufferedImage down;
	private BufferedImage up;
	private BufferedImage left;
	private BufferedImage right;

	public FriendAvatar(View view, Friend friend) {
		super(view, friend);

		this.friend = friend;
		this.spawnAnimation = new SpawnAnimation();
	}

	@Override
	public void tick(long elapsed) {
		assert elapsed >= 0L;

		this.spawnAnimation.tick(elapsed);
	}

	@Override
	public void paint(Graphics graphics) {
		assert graphics != null;

		loadImages(graphics);

		BufferedImage image = imageForOrientation(this.friend.orientation());

		int centerX = this.view.toPixelX(this.friend.center().x());
		int centerY = this.view.toPixelY(this.friend.center().y());

		double spawnScale = this.spawnAnimation.scale();

		int width = this.view.toPixelWidth(this.friend.size().x() * spawnScale);
		int height = this.view.toPixelHeight(this.friend.size().y() * spawnScale);

		graphics.drawImage(image, centerX - width / 2, centerY - height / 2, width, height);
	}

	private void loadImages(Graphics graphics) {
		if (this.down != null) {
			return;
		}

		this.down = graphics.load("pengo_rsrc/friend_down.png");
		this.up = graphics.load("pengo_rsrc/friend_up.png");
		this.left = graphics.load("pengo_rsrc/friend_left.png");
		this.right = graphics.load("pengo_rsrc/friend_right.png");
	}

	private BufferedImage imageForOrientation(int orientation) {
		switch (orientation) {
		case 0:
			return this.right;
		case 90:
			return this.down;
		case 180:
			return this.left;
		case 270:
			return this.up;
		default:
			return this.down;
		}
	}
}