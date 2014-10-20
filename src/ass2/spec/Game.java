package ass2.spec;

import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.gl2.GLUT;

import javax.media.opengl.*;
import javax.media.opengl.awt.GLJPanel;
import javax.media.opengl.glu.GLU;
import javax.swing.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;


/**
 * Game class, renders and controls
 *
 * @author sdba660, malcolmr
 */
public class Game extends JFrame implements GLEventListener {

    private Terrain myTerrain;
    private Camera camera;
    private GameController control;
    private Texture terrainTex, roadTex, treeTex;

    public Game(Terrain terrain, Camera c, GameController gc) {
    	super("Assignment 2");
        myTerrain = terrain;
        camera = c;
        control = gc;
    }
    
    /** 
     * Run the game.
     *
     */
    public void run() {
        GLProfile glp = GLProfile.getDefault();
        GLJPanel panel = new GLJPanel();
        panel.addGLEventListener(this);
        panel.addKeyListener(control);

        // Add an animator to call 'display' at 60fps
        FPSAnimator animator = new FPSAnimator(60);
        animator.add(panel);
        animator.start();

        getContentPane().add(panel);
        setSize(800, 600);
        setVisible(true);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }
    
    /**
     * Load a level file and display it.
     * 
     * @param args - The first argument is a level file in JSON format
     * @throws FileNotFoundException
     */
    public static void main(String[] args) throws FileNotFoundException {
        Terrain terrain = LevelIO.load(new File(args[0]));
        Camera c = new Camera();
        GameController gc = new GameController();
        Game game = new Game(terrain, c, gc);
        game.run();
    }

    @Override
    public void init(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glClearColor(1f, 1f, 1f, 0f);

        gl.glEnable(GL2.GL_DEPTH_TEST);
        gl.glEnable(GL2.GL_LIGHTING);
        // White diffuse, specular lighting (from notes)
        float lightDifAndSpec[] = { 1.0f, 1.0f, 1.0f, 1.0f };
        float lightDir[] = myTerrain.getSunlight();
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, lightDir, 0);

        float globAmb[] = { 0.2f, 0.2f, 0.2f, 1.0f };
        gl.glLightModelfv(GL2.GL_LIGHT_MODEL_AMBIENT, globAmb,0); // Global ambient light.

        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_DIFFUSE, lightDifAndSpec,0);
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_SPECULAR, lightDifAndSpec,0);

        // Turn on light0
        gl.glEnable(GL2.GL_LIGHT0);
        gl.glLightModeli(GL2.GL_LIGHT_MODEL_TWO_SIDE, GL2.GL_TRUE);
        gl.glLightModeli(GL2.GL_LIGHT_MODEL_LOCAL_VIEWER, GL2.GL_TRUE);
        // Enable normalisation
        gl.glEnable(GL2.GL_NORMALIZE);
        // Enable blending
        gl.glEnable(GL.GL_BLEND);
        gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);

        // Load textures
        terrainTex = new Texture(gl, "res/terrain.bmp");
        roadTex = new Texture(gl, "res/road.bmp");
        treeTex = new Texture(gl, "res/lamp.bmp");
        gl.glTexEnvf(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, GL2.GL_MODULATE);
        gl.glEnable(GL.GL_TEXTURE_2D);
    }

	@Override
	public void display(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glLoadIdentity();
        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);

        // Camera movement/update
        control.update(camera, myTerrain);
        GLU glu = new GLU();
        double[] pos = camera.getPosition();
        camera.updateHeight(camera.CAMERA_HEIGHT + myTerrain.altitude(pos[0],pos[2]));
        pos = camera.getPosition();
        double[] dir = camera.getDirection();
        double[] up  = camera.getUpVector();
        glu.gluLookAt(pos[0],pos[1],pos[2],
                dir[0],dir[1],dir[2], up[0],up[1],up[2]);

        handleLight(gl, pos);
        // Render Terrain
        renderTerrain(gl);
        renderTrees(gl);
        renderRoads(gl, 30);
	}

    private void handleLight(GL2 gl, double[] pos) {
        if(myTerrain.isNight) {
            // Enable torch light
            float torchLight[] = new float[]{0.99f, 0.82f, 0.29f, 1.0f};
            gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_DIFFUSE, torchLight,0);
            double rotation = camera.getRotation();
            float fPos[] = new float[]{(float) (pos[0] + 0.1*Math.cos(Math.toRadians(rotation))),
                    (float) pos[1], (float) (pos[2] + 0.1*Math.sin(Math.toRadians(rotation))), 1.0f};
            gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, fPos, 0);
            gl.glLightModelfv(GL2.GL_LIGHT_MODEL_AMBIENT, new float[]{0.1f,0.1f,0.1f,1.0f},0);
            /*
            float spotlight[] = new float[]{(float) (pos[0] + Math.cos(Math.toRadians(rotation))),
                    0f, (float) (pos[2] + Math.sin(Math.toRadians(rotation))), 1.0f};
            gl.glLightf(GL2.GL_LIGHT0, GL2.GL_SPOT_CUTOFF, 60);
            gl.glLightf(GL2.GL_LIGHT0, GL2.GL_SPOT_EXPONENT, 20);
            gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_SPOT_DIRECTION, spotlight, 0);
            */
            gl.glLightf(GL2.GL_LIGHT0, GL2.GL_QUADRATIC_ATTENUATION, 0.2f);
            gl.glClearColor(0f,0f,0f,1f);
            //renderLight(gl, fPos);
        } else {
            // If not night mode, restore settings.
            float col[] = myTerrain.getSunlightColour();
            // Sunset colour? Dark: 215,81,48; Light: 255,170,15 (day 255,255,255)
            // As floats: d(0.84,0.32,0.19), l(1.00,0.67,0.06)
            float lightDir[] = myTerrain.getSunlight();
            gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, lightDir, 0);
            gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_DIFFUSE, col,0);
            gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_SPECULAR, col,0);
            gl.glClearColor(col[0], col[1], col[2],1f);
        }
    }

    private void renderLight(GL2 gl, float[] lightPos) {
        GLUT glut = new GLUT();
        // rotate and translate accordingly
        gl.glPushMatrix();
        gl.glTranslated(lightPos[0],lightPos[1],lightPos[2]);
        gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_AMBIENT_AND_DIFFUSE,
                new float[]{0.99f, 0.82f, 0.29f, 1.0f},0);
        glut.glutSolidSphere(0.01,20,20);
        gl.glPopMatrix();
    }

    private void renderTerrain(GL2 gl) {
        float colour[] = {0.96f, 0.67f, 0.55f, 1f};
        // Bind terrain texture
        gl.glBindTexture(GL.GL_TEXTURE_2D, terrainTex.getTextureID());
        gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_AMBIENT_AND_DIFFUSE, colour,0);
        gl.glBegin(GL2.GL_TRIANGLES);
        {
            ArrayList<Triangle> mesh = myTerrain.getTriangleMesh();
            int i = 0;
            for(Triangle t : mesh) {
                if (i > 5) {
                    i = 0;
                }
                gl.glNormal3dv(t.getNormal(), 0);
                for(Vertex v : t.getVertexList()) {
                    /* for vertices in triangle quad:
                        v0  v1
                        v2  v3
                        order is (v1, v0, v2), (v1, v2, v3)
                        so texture should be:
                        (1,0)(0,0)(0,1), (1,0)(0,1)(1,1)
                        */
                    // Yes, this is a bad way to do it.
                    // No, I don't have time to do it better.
                    switch (i) {
                        case 0:
                            gl.glTexCoord2d(0, 1);
                            break;
                        case 1:
                            gl.glTexCoord2d(0, 0);
                            break;
                        case 2:
                            gl.glTexCoord2d(1, 0);
                            break;
                        case 3:
                            gl.glTexCoord2d(0, 1);
                            break;
                        case 4:
                            gl.glTexCoord2d(1, 0);
                            break;
                        case 5:
                            gl.glTexCoord2d(1, 1);
                            break;
                        default:
                            System.err.println("Something wrong in terrain texturing");
                            break;
                    }
                    gl.glVertex3d(v.x, v.y, v.z);
                    i++;
                }
            }

        }
        gl.glEnd();
    }

    private void renderTrees(GL2 gl) {
        //myTerrain.renderTrees(gl);
        List<Tree> treeList = myTerrain.trees();
        gl.glPushMatrix();
        GLUT glut = new GLUT();
        gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2.GL_FILL);
        int lightArray[] = new int[]{GL2.GL_LIGHT1, GL2.GL_LIGHT2,
                GL2.GL_LIGHT3, GL2.GL_LIGHT4, GL2.GL_LIGHT5, GL2.GL_LIGHT6, GL2.GL_LIGHT7};
        int i = 1;
        // iterate through trees in Terrain, translating and drawing as required
        for(Tree t : treeList) {
            float trunk[] = t.getTrunkColour();
            // get location
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
            gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_AMBIENT_AND_DIFFUSE, trunk,0);
            // enable texture coordinate generation
            // Thanks to http://stackoverflow.com/questions/327043/how-to-apply-texture-to-glutsolidcube
            gl.glEnable(GL2.GL_TEXTURE_GEN_S);
            gl.glEnable(GL2.GL_TEXTURE_GEN_T);
            gl.glBindTexture(GL.GL_TEXTURE_2D, treeTex.getTextureID());
            glut.glutSolidCylinder(0.1, 1.0, 20, 20);
            gl.glPopMatrix();
            gl.glPushMatrix();
            // translate up lamp post
            gl.glTranslated(0, 0.9, 0);
            double deg = 45;// t.getRotation();
            gl.glRotated(deg, 0, 1, 0);
            glut.glutSolidCylinder(0.1, 0.5, 20,20);
            if (myTerrain.shineLight) {
                // lamp posts will shine some light at night.
                // Light should come from end of horizontal arm:
                float spotlight[] = new float[]{0f,-1f,0.2f,1f};
                gl.glTranslated(0,-0.01,0.5);
                gl.glEnable(lightArray[i]);
                gl.glLightfv(lightArray[i], GL2.GL_POSITION, new float[]{0,1,0,1}, 0);
                float lamp[] = new float[]{1f,1f,1f,1f};
                gl.glLightfv(lightArray[i], GL2.GL_DIFFUSE, lamp,0);
                gl.glLightfv(lightArray[i], GL2.GL_SPECULAR, lamp,0);
                gl.glLightf(lightArray[i], GL2.GL_SPOT_CUTOFF, 60f);
                gl.glLightf(lightArray[i], GL2.GL_SPOT_EXPONENT, 20f);
                gl.glLightfv(lightArray[i], GL2.GL_SPOT_DIRECTION, spotlight,0);
                //gl.glLightf(lightArray[i], GL2.GL_QUADRATIC_ATTENUATION, 0.2f);
                i++;
            } else {
                for (int l : lightArray) {
                    gl.glDisable(l);
                }
            }
            gl.glPopMatrix();
            // Undo tree location transform
            gl.glPopMatrix();
            gl.glDisable(GL2.GL_TEXTURE_GEN_S); // disable texture coordinate generation
            gl.glDisable(GL2.GL_TEXTURE_GEN_T);
        }
        // Undo final transform to return matrix to previous state
        gl.glPopMatrix();
    }

    private void renderRoads(GL2 gl, int segments) {
        gl.glBindTexture(GL.GL_TEXTURE_2D, roadTex.getTextureID());
        if (segments <= 0) {
            System.out.println("Segments must be a positive (nonzero) integer");
            return;
        }

        List<Road> roads = myTerrain.roads();
        for(Road r : roads) {
            double roadWidth = r.width();
            float roadCol[]  = r.getRoadCol();
            // set road material + colour
            gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_AMBIENT_AND_DIFFUSE, roadCol,0);
            int roadSize = r.size();
            // split road into segments
            double segmentSize = (double)roadSize/(double)segments;
            // record previous point and previous derivative, starting with 0
            double previous[] = r.point(0);
            double prevDerv[] = r.derivative(0);
            // Altitude guaranteed to be constant, make roads v. thin
            double y = myTerrain.altitude(previous[0], previous[1])+0.02;
            // iterate through segments in road, building vertex list each time
            for(double i = segmentSize; i < roadSize; i+=segmentSize) {
                double point[] = r.point(i);
                // derivative
                double derv[] = r.derivative(i);
                // list of vertices for rendering road
                double vertices[][] = {
                        {previous[0] - prevDerv[1] * roadWidth, y, previous[1] + prevDerv[0] * roadWidth},
                        {previous[0] + prevDerv[1] * roadWidth, y, previous[1] - prevDerv[0] * roadWidth},
                        {previous[0] - prevDerv[1] * roadWidth, y - 0.002, previous[1] + prevDerv[0] * roadWidth},
                        {previous[0] + prevDerv[1] * roadWidth, y - 0.002, previous[1] - prevDerv[0] * roadWidth},
                        {point[0] - derv[1] * roadWidth, y, point[1] + derv[0] * roadWidth},
                        {point[0] + derv[1] * roadWidth, y, point[1] - derv[0] * roadWidth},
                        {point[0] - derv[1] * roadWidth, y - 0.002, point[1] + derv[0] * roadWidth},
                        {point[0] + derv[1] * roadWidth, y - 0.002, point[1] - derv[0] * roadWidth}
                };
                // Face lists; underside not shown therefore not rendered
                int topFace[] = {0, 1, 5, 4};
                int frontFace[] = {0, 2, 3, 1};
                int posSideFace[] = {1, 3, 7, 5};
                int negSideFace[] = {2, 0, 4, 6};
                int backFace[] = {5, 7, 6, 4};
                // Render all faces of extrusion
                // topFace - only one needing texture
                gl.glBegin(GL2.GL_QUADS);
                gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_AMBIENT_AND_DIFFUSE, new float[]{0.6f,0.6f,0.6f,1f},0);
                // compute normal:
                gl.glNormal3dv(MathUtils.normal(vertices[0], vertices[1], vertices[4]), 0);
                int j = 0;
                // bottom left, bottom right, top right, top left
                for (int d : topFace) {
                    switch (j) {
                        case 0:
                            gl.glTexCoord2d(0,1);
                            break;
                        case 1:
                            gl.glTexCoord2d(1,1);
                            break;
                        case 2:
                            gl.glTexCoord2d(1,0);
                            break;
                        case 3:
                            gl.glTexCoord2d(0,0);
                            break;
                        default:
                            System.err.println("Error in road texturing");
                            break;
                    }
                    gl.glVertex3dv(vertices[d], 0);
                    j++;
                }
                gl.glEnd();
                // frontFace:
                gl.glBegin(GL2.GL_QUADS);
                gl.glNormal3dv(MathUtils.normal(vertices[0], vertices[2], vertices[3]), 0);
                for (int d : frontFace) {
                    gl.glVertex3dv(vertices[d], 0);
                }
                gl.glEnd();
                // posSide:
                gl.glBegin(GL2.GL_QUADS);
                gl.glNormal3dv(MathUtils.normal(vertices[1], vertices[3], vertices[7]), 0);
                for (int d : posSideFace) {
                    gl.glVertex3dv(vertices[d], 0);
                }
                gl.glEnd();
                // negSide
                gl.glBegin(GL2.GL_QUADS);
                gl.glNormal3dv(MathUtils.normal(vertices[2], vertices[0], vertices[4]), 0);
                for (int d : negSideFace) {
                    gl.glVertex3dv(vertices[d], 0);
                }
                gl.glEnd();
                // rearFace
                gl.glBegin(GL2.GL_QUADS);
                gl.glNormal3dv(MathUtils.normal(vertices[5], vertices[7], vertices[6]), 0);
                for (int d : backFace) {
                    gl.glVertex3dv(vertices[d], 0);
                }
                gl.glEnd();
                previous = point;
                prevDerv = derv;
            }
        }
    }

	@Override
	public void dispose(GLAutoDrawable drawable) {
		// Nothing to see here
	}

	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width,
			int height) {
        // Thought about refactoring this into Camera class, but that seems to
        // unnecessarily break abstraction - Camera doesn't know about GL
        // currently, this is the rendering class, reshape stays here.
        GL2 gl = drawable.getGL().getGL2();
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();

        GLU glu = new GLU();
        glu.gluPerspective(60.0, (float)width/(float)height, 0.001, 60.0);
        gl.glMatrixMode(GL2.GL_MODELVIEW);
	}
}
