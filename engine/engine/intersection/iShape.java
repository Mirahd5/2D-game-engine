package engine.intersection;

interface iShape {

	boolean intersects(iShape shape);

	boolean intersects(Circle circle);

	boolean intersects(Rect rect);

	Box box(); // retourne la box correspondante

}
