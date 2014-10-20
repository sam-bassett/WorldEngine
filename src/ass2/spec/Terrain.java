package ass2.spec;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Terrain model class for world engine
 *
 * @author sdba660, malcolmr
 */
public class Terrain {

    private Dimension mySize;
    private double[][] myAltitude;
    private List<Tree> myTrees;
    private List<Road> myRoads;
    private float[] mySunlight;

    public boolean isNight = false;
    private double time = 12.00;

    /**
     * Create a new terrain
     *
     * @param width The number of vertices in the x-direction
     * @param depth The number of vertices in the z-direction
     */
    public Terrain(int width, int depth) {
        mySize = new Dimension(width, depth);
        myAltitude = new double[width][depth];
        myTrees = new ArrayList<Tree>();
        myRoads = new ArrayList<Road>();
        mySunlight = new float[3];
    }
    
    public Terrain(Dimension size) {
        this(size.width, size.height);
    }

    public Dimension size() {
        return mySize;
    }

    public List<Tree> trees() {
        return myTrees;
    }

    public List<Road> roads() {
        return myRoads;
    }

    public float[] getSunlight() {
        double sVec[] = timeToSunVector(time);
        return new float[]{(float)sVec[0], (float)sVec[1], (float)sVec[2]};
    }

    /**
     * Set the sunlight direction.
     *
     * Note: the sun should be treated as a directional light, without a position
     *
     * @param dx
     * @param dy
     * @param dz
     */
    public void setSunlightDir(float dx, float dy, float dz) {
        mySunlight[0] = dx;
        mySunlight[1] = dy;
        mySunlight[2] = dz;
        double nTime = sunVectorToTime(new double[]{(double) dx, (double) dy, (double) dz});
        setTime(nTime);
    }

    public void setTime(double newTime) {
        time = newTime;
        if (time > 24.0) {
            time -= 24;
        }
        if (time < 0.0) {
            time += 24;
        }
        // set night if time is during evening
        isNight = (time > 18.0 || time < 6.0);
    }

    public double getTime() {
        return time;
    }

    public float[] getSunlightColour() {
        // At 1700, light sunset colours. 1730, dark.
        // At 0600, dark. 0630, light.
        float light[] = new float[]{1f,0.83f,0.53f,1f};
        float dark[]  = new float[]{0.84f, 0.52f, 0.39f, 1f};
        if(time > 7.0 && time < 17.00) {
            return new float[]{1f,1f,1f,1f};
        } else if (time >= 17.00 && time <= 17.50) {
            return light;
        } else if (time > 17.50 && time <= 18.00) {
            return dark;
        } else if (time >= 6.00 && time <= 6.20) {
            return dark;
        } else if (time > 6.20 && time <= 7.20) {
            return light;
        }
        return new float[]{1f,1f,1f,1f};
    }

    /**
     * Add <increment> hours to clock
     * @param increment number of hours to add
     */
    public void tickClock(double increment) {
        setTime(time + increment);
    }

    /**
     * From an input light vector, get the current time of day
     * @param lightPos the vector to the sun
     * @return a double containing the current time (note that time is of format hh.mm where mm range from 0..99
     *              rather than 0..59)
     */
    public double sunVectorToTime(double[] lightPos) {
        double vTime;
        // directly overhead = 12.00
        if (lightPos[0] == 0) {
            return 12.00;
        } else {
            // tan(x) = o/a
            double angle = Math.toDegrees(Math.atan(lightPos[1]/lightPos[0]));
            if (angle > 0) {
                vTime = 6.0+(angle/90.0)*6; // 6 hours in morning
            } else {
                vTime = 18.0+(angle/90.0)*6; // since angle negative
            }
        }
        return vTime;
    }

    public double[] timeToSunVector(double time) {
        if (Math.floor(time) == 12.0) {
            return new double[]{0,1,0};
        } else if (isNight) {
            return new double[]{0,-1,0};
        } else {
            double angle, rAngle, x, y;
            if (time < 12.0 && time > 6.0) {
                angle = ((time - 6.0)/6.0)*90;
                rAngle = Math.toRadians(angle);
                x = Math.sqrt(2)*Math.cos(rAngle);
                y = Math.sqrt(2)*Math.sin(rAngle);
            } else {
                angle = ((time - 18.0)/6.0)*90;
                rAngle = Math.toRadians(angle);
                x = -Math.sqrt(2)*Math.cos(rAngle);
                y = -Math.sqrt(2)*Math.sin(rAngle);
            }
            return new double[]{x, y, 0};
        }
    }
    
    /**
     * Resize the terrain, copying any old altitudes. 
     * 
     * @param width
     * @param height
     */
    public void setSize(int width, int height) {
        mySize = new Dimension(width, height);
        double[][] oldAlt = myAltitude;
        myAltitude = new double[width][height];
        
        for (int i = 0; i < width && i < oldAlt.length; i++) {
            for (int j = 0; j < height && j < oldAlt[i].length; j++) {
                myAltitude[i][j] = oldAlt[i][j];
            }
        }
    }

    /**
     * Get the altitude at a grid point
     * 
     * @param x
     * @param z
     * @return
     */
    public double getGridAltitude(int x, int z) {
        return myAltitude[x][z];
    }

    /**
     * Set the altitude at a grid point
     * 
     * @param x
     * @param z
     * @return
     */
    public void setGridAltitude(int x, int z, double h) {
        myAltitude[x][z] = h;
    }

    /**
     * Get the altitude at an arbitrary point. 
     * Non-integer points should be interpolated from neighbouring grid points
     *
     * a(0,0,0)----i-----b(1,0.5,0)  i = (0.4,?,0)   ? = 0.2   (0.0 + (0.5 - 0.0)*0.4)
     *        |          |           j = (1,?,0.5)   ? = 0.4   (0.5 + (0.3 - 0.5)*0.5)
     *        |          j           k = (0.3,?,0.8) ? = 0.102
     *        |   k     l|           l = (0.9,?,0.8) ? = 0.306
     * c(0,0,1)__________d(1,0.3,1)
     *
     * Using http://en.wikipedia.org/wiki/Bilinear_interpolation, since abcd form a
     * unit square, we can use:
     * f(x,y) = h(a)(1-x)(1-y) + h(b)x(1-y) + h(c)(1-x)y + h(d)xy
     *        = a0... + a1... + a2... + a3...
     * 
     * @param x
     * @param z
     * @return
     */
    public double altitude(double x, double z) {
        double altitude = 0;
        if(!inMesh(x, z)) {
            return 0;
        }
        // If x,y integer values
        if(x % 1 == 0 && z % 1 == 0) {
            altitude = getGridAltitude((int) x, (int) z);
        } else if (z % 1 == 0) {
            // if z int
            double a0 = getGridAltitude((int)Math.floor(x), (int) z);
            double a1 = getGridAltitude((int)Math.ceil(x), (int) z);
            double xP = x % 1;
            altitude = a0 + (a1 - a0)*xP;
        } else if (x % 1 == 0) {
            // if x int
            double a0 = getGridAltitude((int) x, (int) Math.floor(z));
            double a1 = getGridAltitude((int) x, (int) Math.ceil(z));
            double zP = z % 1;
            altitude = a0 + (a1 - a0) * zP;
        } else if ((x + z) % 1 == 0) {
            // Special edge case to take care of triangle mesh graphics
            // (xFloor, zCeil) and (xCeil, zFloor)
            int zCeil = (int) Math.ceil(z);
            int xFloor= (int) Math.floor(x);
            int xCeil = (int) Math.ceil(x);
            int zFloor= (int) Math.floor(z);
            double a0 = getGridAltitude(xFloor, zCeil);
            double a2 = getGridAltitude(xCeil, zFloor);
            // use x since triangles tessellated bottom left -> top right
            double xP = x % 1;
            altitude = a0 + (a2 - a0) * xP;
        } else {
            double a0 = getGridAltitude((int)Math.floor(x), (int)Math.floor(z));
            double a1 = getGridAltitude((int)Math.ceil(x), (int)Math.floor(z));
            double a2 = getGridAltitude((int)Math.floor(x), (int)Math.ceil(z));
            double a3 = getGridAltitude((int)Math.ceil(x), (int)Math.ceil(z));
            double xP = x % 1;
            double zP = z % 1;
            altitude  = a0*(1-xP)*(1-zP) + a1*xP*(1-zP) + a2*zP*(1-xP) + a3*xP*zP;
        }
        return altitude;
    }

    private boolean inMesh(double x, double z) {
        if (Math.ceil(x) >= mySize.width || x < 0) {
            return false;
        }
        if (Math.ceil(z) >= mySize.height || z < 0) {
            return false;
        }
        return true;
    }

    public ArrayList<Triangle> getTriangleMesh() {
        // loop over each quad, generate two triangles for each
        int qWidth = mySize.width - 1;
        int qHeight= mySize.height- 1;
        int numTriangles = qWidth*qHeight*2;
        ArrayList<Triangle> mesh = new ArrayList<Triangle>(numTriangles);
        for (int i = 0; i < qWidth; i++) {
            for (int j = 0; j < qHeight; j++) {
                /* Four vertices for the quad:
                    v0      v1
                    v2      v3
                 */
                Vertex v0 = new Vertex(i, getGridAltitude(i, j),j);
                Vertex v1 = new Vertex(i, getGridAltitude(i, j+1),j+1);
                Vertex v2 = new Vertex(i+1, getGridAltitude(i+1, j),j);
                Vertex v3 = new Vertex(i+1, getGridAltitude(i+1, j+1),j+1);
                // To ensure ccw-ness:
                Triangle t0 = new Triangle(v1,v0,v2);
                Triangle t1 = new Triangle(v1,v2,v3);
                // Add triangles to list
                mesh.add(t0);
                mesh.add(t1);
            }
        }
        return mesh;
    }

    /**
     * Add a tree at the specified (x,z) point. 
     * The tree's y coordinate is calculated from the altitude of the terrain at that point.
     * 
     * @param x
     * @param z
     */
    public void addTree(double x, double z) {
        double y = altitude(x, z);
        Tree tree = new Tree(x, y, z);
        myTrees.add(tree);
    }


    /**
     * Add a road. 
     * 
     * @param width
     * @param spine
     */
    public void addRoad(double width, double[] spine) {
        Road road = new Road(width, spine);
        myRoads.add(road);        
    }


}
