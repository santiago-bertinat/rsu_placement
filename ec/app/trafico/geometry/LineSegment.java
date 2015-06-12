package ec.app.trafico.geometry;

import java.util.*;

import ec.app.trafico.geometry.Point;

public class LineSegment {
  public Point start;
  public Point end;

  public LineSegment(Point start, Point end) {
    this.start = start;
    this.end = end;
  }

  public static double angleBetweenLines(LineSegment line1, LineSegment line2) {
    // Apply cosine theorem
    double a = Point.twoPointsDistance(line1.start, line1.end);
    double b = Point.twoPointsDistance(line2.start, line2.end);
    double c = Point.twoPointsDistance(line1.start, line2.end);

    if((a + b == c) || (a + c == b) || (c + b == a)) {
      return 0;
    }

    System.out.print("a: ");
    System.out.println(a);
    System.out.print("b: ");
    System.out.println(b);
    System.out.print("c: ");
    System.out.println(c);
    double cosine = (c*c - a*a - b*b) / (-2 * a * b);
    if (cosine > 1) {
      return 0;
    }
    System.out.print("cosine: ");
    System.out.println(cosine);
    System.out.println(Math.acos(cosine));
    return Math.acos(cosine);
  }

  public static double pointToSegmentDistance(Point point, LineSegment segment) {
    // Using Heron formula
    double dps, dpe, dse;
    dps = Point.twoPointsDistance(point, segment.start);
    dpe = Point.twoPointsDistance(point, segment.end);
    dse = Point.twoPointsDistance(segment.start, segment.end);

    double s = (dps + dpe + dse) / (double)2;
    double area= Math.sqrt(s*(s-dps)*(s-dpe)*(s-dse));
    return 2*area/(double)dse;
  }

  public static Point pointToSegmentPerpendicularPoint(Point point, LineSegment segment) {
    double dps = Point.twoPointsDistance(point, segment.start);
    double pts = pointToSegmentDistance(point, segment);
    // System.out.println("Distancias: ");
    // System.out.println(dps);
    // System.out.println(pts);
    double dsc = Math.sqrt(Math.abs(pts * pts - dps * dps));

    LineSegment subsegment = segment.subSegment(dsc);
    return subsegment.end;
  }

  public static ArrayList<LineSegment> combineSegments(ArrayList<LineSegment> segments) {
    boolean combination_made = true;
    ArrayList<LineSegment> combination = new ArrayList<LineSegment>();
    ArrayList<LineSegment> new_combination = new ArrayList<LineSegment>();

    new_combination.addAll(segments);
    while (combination_made) {
      combination.clear();
      combination.addAll(new_combination);
      new_combination.clear();

      int i = 0;
      int j = 0;
      // System.out.println("Size");
      // System.out.println(combination.size());
      combination_made = combination.size() == 1;
      for (i = 0; i < combination.size() - 1; i++) {
        for (j = i + 1; j < combination.size(); j++) {
          // System.out.println(i);
          // System.out.println(j);
          // System.out.println("before:");
          // for (LineSegment s : combination) {
          //   s.print();
          // }
          ArrayList<LineSegment> partial = LineSegment.combineTwoSegments(combination.get(i), combination.get(j));
          // System.out.println("after:");
          // for (LineSegment s : partial) {
          //   s.print();
          // }
          if (partial.size() == 1) {
            // System.out.println("combination:");
            // System.out.println(i);
            // System.out.println(j);

            combination.remove(i);
            combination.remove(j - 1);
            new_combination.addAll(combination);
            new_combination.addAll(partial);

            combination_made = true;
            break;
          } else {
            combination_made = false;
          }
        }
        if (j < combination.size()) {
          break;
        }
      }
    }

    return combination;
  }

  public void print() {
    System.out.println("#####");
    // System.out.print("start x: ");
    // System.out.println(start.x );
    // System.out.print("start y: ");
    // System.out.println(start.y);
    // System.out.print("end x: ");
    // System.out.println(end.x);
    // System.out.print("end y: ");
    // System.out.println(end.y);

    System.out.print("start x: ");
    System.out.println((start.x - 36.7) * 100);
    System.out.print("start y: ");
    System.out.println((start.y + 4.43) * -100);
    System.out.print("end x: ");
    System.out.println((end.x - 36.7) * 100);
    System.out.print("end y: ");
    System.out.println((end.y + 4.43) * -100);
  }

  public static ArrayList<LineSegment> combineTwoSegments(LineSegment segment1, LineSegment segment2) {
    ArrayList<LineSegment> results = new ArrayList<LineSegment>();
    double d1 = Point.twoPointsDistance(segment1.start, segment1.end);
    double d2 = Point.twoPointsDistance(segment2.start, segment2.end);

    LineSegment aux_segment1 = segment1.changeDirection();
    LineSegment aux_segment2 = segment2.changeDirection();

    Point inside_point;
    Point outside_point;
    if ((aux_segment1.start.x < aux_segment2.start.x && aux_segment1.end.x > aux_segment2.end.x) ||
      (aux_segment1.start.y <= aux_segment2.start.y && aux_segment1.end.y >= aux_segment2.end.y)) {
      // segment1 contains segment2
      results.add(aux_segment1);
      return results;
    }else if ((aux_segment2.start.x < aux_segment1.start.x && aux_segment2.end.x > aux_segment1.end.x) ||
      (aux_segment2.start.y < aux_segment1.start.y && aux_segment2.end.y > aux_segment1.end.y)) {
      // segment2 contains segment1
      results.add(aux_segment2);
      return results;
    }else if ((aux_segment1.start.x < aux_segment2.start.x) ||
      ((aux_segment1.start.x == aux_segment2.start.x) && (aux_segment1.start.y <= aux_segment2.start.y))) {
      inside_point = aux_segment1.start;
      outside_point = aux_segment2.end;
    }else {
      inside_point = aux_segment2.start;
      outside_point = aux_segment1.end;
    }

    double d12 = Point.twoPointsDistance(inside_point, outside_point);

    if (d1 + d2 > d12) {
      results.add(new LineSegment(inside_point, outside_point));
    }else {
      results.add(aux_segment1);
      results.add(segment2);
    }
    return results;
  }

  public LineSegment subSegment(double new_module) {

    double module = Point.twoPointsDistance(start, end);
    double percentage = new_module / module;
    // System.out.print("New Module: ");
    // System.out.println(new_module);
    // System.out.print("Module: ");
    // System.out.println(module);
    // System.out.print("Percentage: ");
    // System.out.println(percentage);

    double new_x;
    double new_y;
    if (end.x == start.x) {
      new_x = end.x;
    }else {
      new_x = (end.x > start.x) ? start.x + (end.x - start.x) * percentage : start.x - (start.x - end.x) * percentage;
    }

    if (end.y == start.y) {
      new_y = end.y;
    }else {
      new_y = (end.y > start.y) ? start.y + (end.y - start.y) * percentage : start.y - (start.y - end.y) * percentage;
    }

    // System.out.print("new x: ");
    // System.out.println(new_x);
    // System.out.print("new y: ");
    // System.out.println(new_y);
    Point new_end = new Point(new_x, new_y);

    return new LineSegment(start, new_end);
  }

  public LineSegment subSegment(double new_module, Point center) {
    LineSegment segment1 = new LineSegment(center, start);
    LineSegment segment2 = new LineSegment(center, end);
    LineSegment sub_segment =
      new LineSegment(segment1.subSegment(new_module / 2).end, segment2.subSegment(new_module / 2).end);

    return sub_segment;
  }

  public LineSegment changeDirection() {
    if ((start.x > end.x) || ((start.x == end.x) && (start.y > end.y))) {
      return new LineSegment(end, start);
    }else {
      return this;
    }
  }
}
