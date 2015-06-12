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

  public LineSegment lineIntersection(LineSegment segment) {
    boolean start_inside = belongsToCircle(segment.start);
    boolean end_inside = belongsToCircle(segment.end);

    // System.out.print("start insinde:");
    // System.out.println(start_inside);
    // System.out.print("end insinde:");
    // System.out.println(end_inside);

    double intersection_length;

    if(start_inside && end_inside) {
      // The segment is totally covered
      // System.out.println("Los dos adentro");
      return segment;
    }else if(start_inside || end_inside) {
      // One point of the segment is inside the circle
      // System.out.println("Un punto adentro");
      Point inside_point = start_inside ? segment.start : segment.end;
      Point outside_point = start_inside ? segment.end : segment.start;

      LineSegment aux_segment = new LineSegment(center, inside_point);
      segment = new LineSegment(inside_point, outside_point);
      double segments_angle = LineSegment.angleBetweenLines(aux_segment, segment);
      double center_point_distance = Point.twoPointsDistance(center, inside_point);
      // System.out.print("Angulo:");
      // System.out.println(segments_angle);
      // System.out.print("distancia centro:");
      // System.out.println(center_point_distance);

      if (segments_angle != 0) {
        double beta = Math.asin(center_point_distance * Math.sin(segments_angle) / radius);
        intersection_length = Math.sin(Math.PI - segments_angle - beta) * radius / Math.sin(segments_angle);
      }else {
        intersection_length = radius - Point.twoPointsDistance(center, inside_point);
      }
      // System.out.print("Length:");
      // System.out.println(intersection_length);

      return segment.subSegment(intersection_length);
    }else if (LineSegment.pointToSegmentDistance(center, segment) < radius) {
      // System.out.println("El segmento adentro");
      double distance = LineSegment.pointToSegmentDistance(center, segment);

      if (distance == 0) {
        intersection_length = 2 * radius;
        return segment.subSegment(intersection_length, center);
      }else {
        double lambda = Math.sqrt(Math.pow(radius, 2) - Math.pow(distance, 2));
        intersection_length  = 2 * lambda;
      }

      // System.out.print("Length:");
      // System.out.println(intersection_length);
      Point perpendicular_point = LineSegment.pointToSegmentPerpendicularPoint(center, segment);
      // System.out.println("Center: ");
      // System.out.println(perpendicular_point.x);
      // System.out.println(perpendicular_point.y);
      return segment.subSegment(intersection_length, perpendicular_point);
    }
    return null;
  }
}
