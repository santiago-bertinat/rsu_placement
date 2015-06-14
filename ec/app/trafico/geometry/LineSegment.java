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

  public void print() {
    System.out.println("#####");

    System.out.print("start x: ");
    System.out.println(start.x);
    System.out.print("start y: ");
    System.out.println(start.y);
    System.out.print("end x: ");
    System.out.println(end.x);
    System.out.print("end y: ");
    System.out.println(end.y);
  }
}
