package components;

import java.util.*;

import components.Point;

public class Segment {
  public Point start;
  public Point end;
  public int importance;

  public Segment(Point start, Point end, int importance) {
    this.start = start;
    this.end = end;
    this.importance = importance;
  }

  public double distance() {
    return Point.twoPointsDistance(start, end);
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
