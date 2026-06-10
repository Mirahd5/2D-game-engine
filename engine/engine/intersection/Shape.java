package engine.intersection;

import engine.model.ISU;

abstract class Shape implements iShape{

	// FIELD

	protected ISU isu;
	protected ISU.Coord center;

	// CONSTRUCTOR

	Shape(ISU.Coord center) {
		this.center = center.mkCopy();
		this.isu = center.isu();
	}
	
	public boolean intersects(iShape shape) {
		return shape.intersects(this);
	}



	

}
