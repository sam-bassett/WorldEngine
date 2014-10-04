package ass2.spec;

import com.jogamp.opengl.util.gl2.GLUT;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;


//import sailing.objects.Island;

/**
 * COMMENT: Comment HeightMap 
 *
 * @author malcolmr
 */

//TODO Ensure world coordinates are homogeneous for rendering/camera purposes
public class Terrain {

    private Dimension mySize;
    private double[][] myAltitude;
    private List<Tree> myTrees;
    private List<Road> myRoads;
    private float[] mySunlight;

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
        return mySunlight;
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
     * Render trees stored in Terrain using current GL context
     * @param gl
     */
    public void renderTrees(GL2 gl) {
        // TODO generalise rendering function for all terrain
        float trunkCol[] = {0.35f, 0.31f, 0.27f, 1f};
        float leavesCol[]= {0.45f, 0.65f, 0.26f, 1f};
        gl.glPushMatrix();
        GLUT glut = new GLUT();
        gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2.GL_FILL);
        for(Tree t : myTrees) {
            // TODO replace hard-coded values with something better
            // get tree location
            double x = t.getPosition()[0];
            double y = t.getPosition()[1];
            double z = t.getPosition()[2];
            // Save base matrix
            gl.glPushMatrix();
            // Translate to location
            gl.glTranslated(x, y, z);
            // Draw trunk of height t.getHeight(),
            // push, translate up t.getHeight(), draw leaves, pop
            // rotate cylinder:
            gl.glPushMatrix();
            gl.glRotated(-90, 1, 0, 0);
            // width, height, ...
            gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_AMBIENT_AND_DIFFUSE, trunkCol,0);
            glut.glutSolidCylinder(0.1, 0.5, 20, 20);
            gl.glPopMatrix();
            gl.glPushMatrix();
            gl.glTranslated(0, 0.5, 0);
            // Leaves
            gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_AMBIENT_AND_DIFFUSE, leavesCol,0);
            glut.glutSolidSphere(0.2, 20, 20);
            // Undo height transform
            gl.glPopMatrix();
            // Undo tree location transform
            gl.glPopMatrix();
            //System.out.println("(x, y, z) = " + x + ", " + y + ", " + z);
        }
        //System.exit(0);
        gl.glPopMatrix();
    }

    /**
     * Render roads in Terrain based on current gl, plus give level of detail
     * @param gl GL context
     * @param segments Number of segments to draw
     */
    public void renderRoads(GL2 gl, int segments) {
        float roadCol[] = {0.22f,0.22f,0.22f,1f};
        gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_AMBIENT_AND_DIFFUSE, roadCol,0);
        double roadWidth = 0.05;
        if(segments <= 0) {
            System.out.println("Segments must be a positive whole number");
            return;
        }
        // For each road, split into detail segments and draw at required location
        for(Road r : myRoads) {
            int roadSize = r.size();
            double segmentSize = (double)roadSize/(double)segments;
            //gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2.GL_FILL);
            double previous[] = r.point(0);
            double prevDerv[] = r.derivative(0);
            // Altitude guaranteed to be constant
            double y = altitude(previous[0], previous[1])+0.02;
            for(double i = segmentSize; i < roadSize; i+=segmentSize) {
                double point[] = r.point(i);
                // derivative
                double derv[] = r.derivative(i);
                // list of vertices for rendering road
                //TODO While this method works ok, gaps are formed in outside of curve between segments
                // (irregardless of segment size). Presumably the segments also overlap on inside of curve.

                // Save previous absolute points, only recalculate new derivatives
                double vertices[][] = {
                        {previous[0] - prevDerv[1]*roadWidth,y,     previous[1] + prevDerv[0]*roadWidth},
                        {previous[0] + prevDerv[1]*roadWidth,y,     previous[1] - prevDerv[0]*roadWidth},
                        {previous[0] - prevDerv[1]*roadWidth,y-0.02,previous[1] + prevDerv[0]*roadWidth},
                        {previous[0] + prevDerv[1]*roadWidth,y-0.02,previous[1] - prevDerv[0]*roadWidth},
                        {point[0] - derv[1]*roadWidth,y,        point[1] + derv[0]*roadWidth},
                        {point[0] + derv[1]*roadWidth,y,        point[1] - derv[0]*roadWidth},
                        {point[0] - derv[1]*roadWidth,y-0.02,   point[1] + derv[0]*roadWidth},
                        {point[0] + derv[1]*roadWidth,y-0.02,   point[1] - derv[0]*roadWidth}
                };
                int topFace[] = {0,1,5,4};
                int frontFace[] = {0,2,3,1};
                int posSideFace[] = {1,3,7,5};
                int negSideFace[] = {2,0,4,6};
                int backFace[] = {5,7,6,4};
                // don't need underside
                gl.glBegin(GL2.GL_QUADS);
                // topFace normal:
                gl.glNormal3dv(MathUtils.normal(vertices[0],vertices[1],vertices[4]),0);
                for(int d : topFace) {
                    gl.glVertex3dv(vertices[d],0);
                }
                System.out.println();
                gl.glEnd();
                // frontFace:
                gl.glBegin(GL2.GL_QUADS);
                gl.glNormal3dv(MathUtils.normal(vertices[0],vertices[2],vertices[3]),0);
                for(int d : frontFace) {
                    gl.glVertex3dv(vertices[d],0);
                }
                gl.glEnd();
                // posSide:
                gl.glBegin(GL2.GL_QUADS);
                gl.glNormal3dv(MathUtils.normal(vertices[1],vertices[3],vertices[7]),0);
                for(int d : posSideFace) {
                    gl.glVertex3dv(vertices[d],0);
                }
                gl.glEnd();
                // negSide
                gl.glBegin(GL2.GL_QUADS);
                gl.glNormal3dv(MathUtils.normal(vertices[2],vertices[0],vertices[4]),0);
                for(int d : negSideFace) {
                    gl.glVertex3dv(vertices[d],0);
                }
                gl.glEnd();
                // rearFace
                gl.glBegin(GL2.GL_QUADS);
                gl.glNormal3dv(MathUtils.normal(vertices[5],vertices[7],vertices[6]),0);
                for(int d : backFace) {
                    gl.glVertex3dv(vertices[d],0);
                }
                gl.glEnd();
                previous = point;
                prevDerv = derv;
            }
            gl.glPopMatrix();
        }
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
