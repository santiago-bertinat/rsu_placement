package ec.app.trafico.geometry;

import ec.app.trafico.geometry.LineSegment;
import ec.app.trafico.geometry.Point;

public class Circle {
  public Point center;
  public double radius;

  public Circle(Point center, double radius) {
    this.center = center;
    this.radius = radius;
  }

  public boolean belongsToCircle(Point x) {
    return radius >= Point.twoPointsDistance(center, x);
  }
}
