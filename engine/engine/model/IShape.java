package engine.model;

/**
 * Forme géométrique utilisée dans un Bounding.
 */
public interface IShape {

	ISU isu();

	boolean intersects(IShape shape);

	boolean intersects(Circle circle);

	boolean intersects(Rect rect);

	Hitbox hitbox();
}