package ass2.spec;

import java.util.ArrayList;
import java.util.List;

/**
 * Road class for world engine, uses bezier curve to describe road
 * and includes functions for working with curve (such as tangent)
 *
 * @author sdba660, malcolmr
 */
public class Road {

    private List<Double> myPoints;
    private double myWidth;
    private float[] roadCol;
    
    /** 
     * Create a new road starting at the specified point
     */
    public Road(double width, double x0, double y0) {
        myWidth = width/5;
        myPoints = new ArrayList<Double>();
        myPoints.add(x0);
        myPoints.add(y0);
        roadCol = new float[]{0.22f,0.22f,0.22f,1f};
    }

    /**
     * Create a new road with the specified spine 
     *
     * @param width
     * @param spine
     */
    public Road(double width, double[] spine) {
        myWidth = width/5;
        myPoints = new ArrayList<Double>();
        for (int i = 0; i < spine.length; i++) {
            myPoints.add(spine[i]);
        }
        roadCol = new float[]{0.22f,0.22f,0.22f,1f};
    }

    public void setRoadCol(float r, float g, float b, float a) {
        roadCol = new float[]{r, g, b, a};
    }

    public float[] getRoadCol() {
        return roadCol;
    }

    /**
     * The width of the road.
     * 
     * @return
     */
    public double width() {
        return myWidth;
    }

    /**
     * Add a new segment of road, beginning at the last point added and ending at (x3, y3).
     * (x1, y1) and (x2, y2) are interpolated as bezier control points.
     * 
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     * @param x3
     * @param y3
     */
    public void addSegment(double x1, double y1, double x2, double y2, double x3, double y3) {
        myPoints.add(x1);
        myPoints.add(y1);
        myPoints.add(x2);
        myPoints.add(y2);
        myPoints.add(x3);
        myPoints.add(y3);        
    }
    
    /**
     * Get the number of segments in the curve
     * 
     * @return
     */
    public int size() {
        return myPoints.size() / 6;
    }

    /**
     * Get the specified control point.
     * 
     * @param i
     * @return
     */
    public double[] controlPoint(int i) {
        double[] p = new double[2];
        p[0] = myPoints.get(i*2);
        p[1] = myPoints.get(i*2+1);
        return p;
    }

    /**
     * Get the derivative to Bezier at point t (0 ≤ t ≤ size())
     * P(t) = (1-t)^3*P0 + 3t(1-t)^2*P1 + 3t^2(1-t)*P2 + t^3*P3
     * @param t
     * @return
     */
    public double[] derivative(double t) {
        // Get control points
        int i = (int)Math.floor(t);
        t = t - i;
        i *= 6;
        double x0 = myPoints.get(i++);
        double y0 = myPoints.get(i++);
        double x1 = myPoints.get(i++);
        double y1 = myPoints.get(i++);
        double x2 = myPoints.get(i++);
        double y2 = myPoints.get(i++);
        double x3 = myPoints.get(i++);
        double y3 = myPoints.get(i++);
        double[] p = new double[3];
        /*
        Derivative:
        P'(t)= -3(t-1)^2*P0      + 3(1-t)^2*P1      - 6t(1-t)*P1   - 3t^2*P2  + 6t(1-t)*P2   + 3t^2*P3
         */
        p[0] = -3*(t-1)*(t-1)*x0 + 3*(1-t)*(1-t)*x1 - 6*t*(1-t)*x1 - 3*t*t*x2 + 6*t*(1-t)*x2 + 3*t*t*x3;
        p[1] = -3*(t-1)*(t-1)*y0 + 3*(1-t)*(1-t)*y1 - 6*t*(1-t)*y1 - 3*t*t*y2 + 6*t*(1-t)*y2 + 3*t*t*y3;
        p[2] = 0;
        p = MathUtils.normalise(p);
        return p;
    }
    
    /**
     * Get a point on the spine. The parameter t may vary from 0 to size().
     * Points on the kth segment take have parameters in the range (k, k+1).
     * 
     * @param t
     * @return
     */
    public double[] point(double t) {
        int i = (int)Math.floor(t);
        t = t - i;
        
        i *= 6;
        
        double x0 = myPoints.get(i++);
        double y0 = myPoints.get(i++);
        double x1 = myPoints.get(i++);
        double y1 = myPoints.get(i++);
        double x2 = myPoints.get(i++);
        double y2 = myPoints.get(i++);
        double x3 = myPoints.get(i++);
        double y3 = myPoints.get(i++);
        
        double[] p = new double[2];

        p[0] = b(0, t) * x0 + b(1, t) * x1 + b(2, t) * x2 + b(3, t) * x3;
        p[1] = b(0, t) * y0 + b(1, t) * y1 + b(2, t) * y2 + b(3, t) * y3;        
        
        return p;
    }
    
    /**
     * Calculate the Bezier coefficients
     * 
     * @param i
     * @param t
     * @return
     */
    private double b(int i, double t) {
        
        switch(i) {
        
        case 0:
            return (1-t) * (1-t) * (1-t);

        case 1:
            return 3 * (1-t) * (1-t) * t;
            
        case 2:
            return 3 * (1-t) * t * t;

        case 3:
            return t * t * t;
        }
        
        // this should never happen
        throw new IllegalArgumentException("" + i);
    }


}
