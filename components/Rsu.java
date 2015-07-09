package components;

import components.Segment;
import components.Point;

public class Rsu {
  public Point center;
  public double radius;

  public Rsu(Point center, double radius) {
    this.center = center;
    this.radius = radius;
  }

  public boolean belongsToRsu(Point x) {
    return radius >= Point.twoPointsDistance(center, x);
  }
}
