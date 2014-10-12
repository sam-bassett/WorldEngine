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
    private Texture terrainTex;

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
        //Terrain terrain = LevelIO.load(new File(args[0]));
        //Terrain terrain = LevelIO.load(new File("/Users/sam/Documents/Programming/IdeaProjects/WorldEngineAssignment/src/ass2/spec/TestLevels/fiveByFive.json"));
        Terrain terrain = LevelIO.load(new File("/Users/sam/Documents/Programming/IdeaProjects/WorldEngineAssignment/src/ass2/spec/TestLevels/exampleLevel.json"));
        //Terrain terrain = LevelIO.load(new File("/Users/sam/Documents/Programming/IdeaProjects/WorldEngineAssignment/src/ass2/spec/TestLevels/treeTest.json"));
        //Terrain terrain = LevelIO.load(new File("/Users/sam/Documents/Programming/IdeaProjects/WorldEngineAssignment/src/ass2/spec/TestLevels/basicLightTest.json"));
        //Terrain terrain = LevelIO.load(new File("/Users/sam/Documents/Programming/IdeaProjects/WorldEngineAssignment/src/ass2/spec/TestLevels/negativeLightTest.json"));
        //Terrain terrain = LevelIO.load(new File("/Users/sam/Documents/Programming/IdeaProjects/WorldEngineAssignment/src/ass2/spec/TestLevels/simpleBezier.json"));
        //Terrain terrain = LevelIO.load(new File("/Users/sam/Documents/Programming/IdeaProjects/WorldEngineAssignment/src/ass2/spec/TestLevels/twinRoads.json"));
        //Terrain terrain = LevelIO.load(new File("/Users/sam/Documents/Programming/IdeaProjects/WorldEngineAssignment/src/ass2/spec/TestLevels/twoSpline.json"));
        Camera c = new Camera();
        GameController gc = new GameController(c);
        Game game = new Game(terrain, c, gc);
        game.run();
    }

	@Override
	public void display(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glLoadIdentity();
        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);

        // Camera movement/update
        control.update();
        GLU glu = new GLU();
        double[] pos = camera.getPosition();
        camera.updateHeight(camera.CAMERA_HEIGHT + myTerrain.altitude(pos[0],pos[2]));
        pos = camera.getPosition();
        double[] dir = camera.getDirection();
        double[] up  = camera.getUpVector();
        glu.gluLookAt(pos[0],pos[1],pos[2],
                dir[0],dir[1],dir[2], up[0],up[1],up[2]);

        // Render Terrain
        renderTerrain(gl);
        renderTrees(gl);
        renderRoads(gl, 30);
	}

    private void renderTerrain(GL2 gl) {
        float colour[] = {0.96f, 0.67f, 0.55f, 1f};
        gl.glBegin(GL2.GL_TRIANGLES);
        {
            gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_AMBIENT_AND_DIFFUSE, colour,0);
            gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_R, GL2.GL_REPEAT);
            gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_S, GL2.GL_REPEAT);
            gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_T, GL2.GL_REPEAT);
            ArrayList<Triangle> mesh = myTerrain.getTriangleMesh();
            for(Triangle t : mesh) {
                gl.glNormal3dv(t.getNormal(), 0);
                for(Vertex v : t.getVertexList()) {
                    gl.glTexCoord2d(v.x, v.z);
                    gl.glVertex3d(v.x, v.y, v.z);
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
        // iterate through trees in Terrain, translating and drawing as required
        for(Tree t : treeList) {
            float trunk[] = t.getTrunkColour();
            float leaves[]= t.getLeafColour();
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
            glut.glutSolidCylinder(0.1, 0.5, 20, 20);
            gl.glPopMatrix();
            gl.glPushMatrix();
            gl.glTranslated(0, 0.5, 0);
            // Leaves
            gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_AMBIENT_AND_DIFFUSE, leaves,0);
            glut.glutSolidSphere(0.2, 20, 20);
            // Undo height transform
            gl.glPopMatrix();
            // Undo tree location transform
            gl.glPopMatrix();
        }
        // Undo final transform to return matrix to previous state
        gl.glPopMatrix();
    }

    private void renderRoads(GL2 gl, int segments) {
        //myTerrain.renderRoads(gl, segments);
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
                // topFace
                gl.glBegin(GL2.GL_QUADS);
                // compute normal:
                gl.glNormal3dv(MathUtils.normal(vertices[0], vertices[1], vertices[4]), 0);
                for (int d : topFace) {
                    gl.glVertex3dv(vertices[d], 0);
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
	public void init(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glClearColor(1f, 1f, 1f, 0f);

        gl.glEnable(GL2.GL_DEPTH_TEST);
        gl.glEnable(GL2.GL_LIGHTING);
        // White diffuse, specular lighting (from notes)
        float lightDifAndSpec[] = { 1.0f, 1.0f, 1.0f, 1.0f };
        float lightDir[] = myTerrain.getSunlight();
        // TODO not a hunjie on this light position, see negativeSunlightTest
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, lightDir, 0);
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_DIFFUSE, lightDifAndSpec,0);
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_SPECULAR, lightDifAndSpec,0);

        // Turn on light0
        gl.glEnable(GL2.GL_LIGHT0);
        gl.glLightModeli(GL2.GL_LIGHT_MODEL_TWO_SIDE, GL2.GL_TRUE);
        gl.glLightModeli(GL2.GL_LIGHT_MODEL_LOCAL_VIEWER, GL2.GL_TRUE);
        // Enable normalisation
        gl.glEnable(GL2.GL_NORMALIZE);

        terrainTex = new Texture(gl, "/Users/sam/Documents/Programming/IdeaProjects/WorldEngineAssignment/res/terrain.bmp");
        /*
        // Load textures - terrain, trunk, leaf, road
        Texture textures[] = new Texture[4];
        textures[0] = new Texture(gl, "/Users/sam/Documents/Programming/IdeaProjects/WorldEngineAssignment/res/terrain.bmp");
        myTerrain.setTerrainTexture(textures[0].getTextureID());

        textures[1] = new Texture(gl, "/Users/sam/Documents/Programming/IdeaProjects/WorldEngineAssignment/res/trunk.bmp");
        textures[2] = new Texture(gl, "/Users/sam/Documents/Programming/IdeaProjects/WorldEngineAssignment/res/leaf.bmp");
        myTerrain.setTreeTexture(textures[1].getTextureID(), textures[2].getTextureID());
        textures[3] = new Texture(gl, "/Users/sam/Documents/Programming/IdeaProjects/WorldEngineAssignment/res/road.bmp");
        myTerrain.setRoadTexture(textures[3].getTextureID());
        */
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
