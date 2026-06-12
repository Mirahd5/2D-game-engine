package game.view;

import engine.model.BasicStunt;
import engine.model.Picture;
import engine.view.Avatar;
import game.model.Friend;
import oop.graphics.BufferedImage;
import oop.graphics.Graphics;

public class FriendAvatar extends Avatar {

	private final Friend friend;

	private BufferedImage down;
	private BufferedImage up;
	private BufferedImage left;
	private BufferedImage right;

	public FriendAvatar(Friend friend) {
		super(friend);

		assert friend != null;

		this.friend = friend;
	}

	@Override
	public void paint(Graphics g, Picture picture) {
		assert g != null;
		assert picture != null;

		loadImages(g);

		BufferedImage image = imageForOrientation();

		int centerX = picture.toPixelX(friend.center().x());
		int centerY = picture.toPixelY(friend.center().y());

		double size_cm = friend.isu().cmPerCell();

		int width = picture.toPixelLength(size_cm);
		int height = picture.toPixelLength(size_cm);

		g.drawImage(
			image,
			centerX - width / 2,
			centerY - height / 2,
			width,
			height
		);
	}

	private void loadImages(Graphics g) {
		if (down != null) {
			return;
		}

		down = g.load("pengo_rsrc/friend_down.png");
		up = g.load("pengo_rsrc/friend_up.png");
		left = g.load("pengo_rsrc/friend_left.png");
		right = g.load("pengo_rsrc/friend_right.png");
	}

	private BufferedImage imageForOrientation() {
		int orientation = friend.orientation();

		if (orientation == BasicStunt.EAST) {
			return right;
		}

		if (orientation == BasicStunt.WEST) {
			return left;
		}

		if (orientation == BasicStunt.NORTH) {
			return up;
		}

		if (orientation == BasicStunt.SOUTH) {
			return down;
		}

		return down;
	}
}