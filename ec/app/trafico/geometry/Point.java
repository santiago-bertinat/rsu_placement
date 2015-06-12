package ec.app.trafico.geometry;

public class Point {
  public double x;
  public double y;

  public Point(double x, double y) {
    this.x = x;
    this.y = y;
  }

  public static double twoPointsDistance(Point point1, Point point2){
    double deglen = 110.25 * 1000;
    double x = point1.x - point2.x;
    double y = (point1.y - point2.y) * Math.cos(point1.x);
    return deglen * Math.sqrt(x * x + y * y);

    // double x = point2.x - point1.x;
    // double y = point2.y - point1.y;
    // return Math.sqrt(x * x + y * y);
  }
}
