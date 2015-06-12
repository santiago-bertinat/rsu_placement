package ec.app.trafico.geometry;

import java.util.*;

import ec.app.trafico.geometry.Circle;
import ec.app.trafico.geometry.Point;
import ec.app.trafico.geometry.LineSegment;

public class Intersection {

  public static void main(String[] args) {
    // Circle circle = new Circle(new Point(3,3), 2);
    // LineSegment segment = new LineSegment(new Point(0,5), new Point(0,3));
    // LineSegment segment = new LineSegment(new Point(2.5,3.5), new Point(3.5,3.5));
    // LineSegment segment = new LineSegment(new Point(3,2), new Point(3,0));
    // LineSegment segment = new LineSegment(new Point(4,3), new Point(5,6));
    // LineSegment segment = new LineSegment(new Point(1,2), new Point(6,2));
    // LineSegment segment = new LineSegment(new Point(0,3), new Point(6,3));
    // LineSegment segment = new LineSegment(new Point(0,3), new Point(6,3));
    // LineSegment segment = new LineSegment(new Point(1,5), new Point(6,3));

    Circle[] circles = new Circle[6];
    circles[0] = new Circle(new Point(2,3), 2);
    circles[1] = new Circle(new Point(6,8), 3);
    circles[2] = new Circle(new Point(12,8), 4);
    circles[3] = new Circle(new Point(6,8), 2);
    circles[4] = new Circle(new Point(6,4), 2);
    circles[5] = new Circle(new Point(1,8), 1);
    // LineSegment segment = new LineSegment(new Point(3,4), new Point(11,9));
    LineSegment segment = new LineSegment(new Point(7,3), new Point(7,10));

    ArrayList<LineSegment> intersections = new ArrayList<LineSegment>();
    for (Circle circle : circles) {
      LineSegment intersection = circle.lineIntersection(segment);
      if (intersection != null) {
        intersections.add(intersection);
      }
    }

    ArrayList<LineSegment> combinations = LineSegment.combineSegments(intersections);

    double result = 0;
    for (LineSegment combination : combinations) {
      result += Point.twoPointsDistance(combination.start, combination.end);
    }

  }
}
