package engine.view;

import oop.graphics.Canvas;
import oop.graphics.Graphics;

/**
 * Adaptateur entre le Canvas graphique et la View du moteur.
 */
public final class Painter implements Canvas.PaintListener {

	private final View view;
	private final PerformanceMonitor performanceMonitor;

	private Canvas canvas;

	public Painter(View view) {
		this(view, null);
	}

	public Painter(View view, PerformanceMonitor performanceMonitor) {
		this.view = view;
		this.performanceMonitor = performanceMonitor;
	}

	public Canvas canvas() {
		return this.canvas;
	}

	@Override
	public void visible(Canvas canvas) {
		this.canvas = canvas;
		this.canvas.repaint();
	}

	@Override
	public void paint(Canvas canvas, Graphics graphics) {
		this.canvas = canvas;

		if (this.performanceMonitor != null) {
			this.performanceMonitor.recordPaintEvent();
		}

		clear(graphics, canvas);

		this.view.paint(graphics);
	}

	@Override
	public void revoked(Canvas canvas) {
		if (this.canvas == canvas) {
			this.canvas = null;
		}
	}

	private void clear(Graphics graphics, Canvas canvas) {
		graphics.setColor(Graphics.Colors.black);
		graphics.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
	}
}