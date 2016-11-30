package detection;


import org.opencv.core.Point;
import java.util.Comparator;
import java.util.List;

public class ClockWiseComparator implements Comparator<Point> {

    private Point p0;

    public ClockWiseComparator(List<Point> points) {

        Point point;
        double min;
        double dist;

        p0=points.get(0);
        min=distance( p0);

        for (int i=1;i<points.size();i++) {

            point=points.get(i);
            dist = distance(point);

            if (dist < min) {

                points.set(i,points.get(0));
                points.set(0,point);
                min=dist;
                p0=point;
            }
        }

    }

    private double distance(Point point) {

        return (point.x * point.x) + (point.y * point.y);
    }

    @Override
    public int compare(Point p1, Point p2) {

        double ccw = (p1.x - p0.x) * (p2.y - p0.y) - (p1.y - p0.y) * (p2.x - p0.x);
    
        if (ccw < 0)       
            return -1;
          else if (ccw > 0)  
            return 1;
          else   
            return 0;
        
    }
}
