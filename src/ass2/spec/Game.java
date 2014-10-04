package ass2.spec;

import com.jogamp.opengl.util.FPSAnimator;

import javax.media.opengl.*;
import javax.media.opengl.awt.GLJPanel;
import javax.media.opengl.glu.GLU;
import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;


/**
 * COMMENT: Comment Game 
 *
 * @author malcolmr
 */
public class Game extends JFrame implements GLEventListener, KeyListener {

    private Terrain myTerrain;
    private Camera camera;
    private int dx = 0;
    private int dy = 0;
    private double scale = 1;
    private int tr = 0;
    private int fb = 0;

    public Game(Terrain terrain, Camera c) {
    	super("Assignment 2");
        myTerrain = terrain;
        camera = c;
    }
    
    /** 
     * Run the game.
     *
     */
    public void run() {
        GLProfile glp = GLProfile.getDefault();
        GLJPanel panel = new GLJPanel();
        panel.addGLEventListener(this);
        panel.addKeyListener(this);

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
        //Terrain terrain = LevelIO.load(new File("/Users/sam/Documents/Programming/IdeaProjects/WorldEngineAssignment/src/ass2/spec/TestLevels/exampleLevel.json"));
        //Terrain terrain = LevelIO.load(new File("/Users/sam/Documents/Programming/IdeaProjects/WorldEngineAssignment/src/ass2/spec/TestLevels/treeTest.json"));
        //Terrain terrain = LevelIO.load(new File("/Users/sam/Documents/Programming/IdeaProjects/WorldEngineAssignment/src/ass2/spec/TestLevels/basicLightTest.json"));
        Terrain terrain = LevelIO.load(new File("/Users/sam/Documents/Programming/IdeaProjects/WorldEngineAssignment/src/ass2/spec/TestLevels/negativeLightTest.json"));
        //Terrain terrain = LevelIO.load(new File("/Users/sam/Documents/Programming/IdeaProjects/WorldEngineAssignment/src/ass2/spec/TestLevels/simpleBezier.json"));
        //Terrain terrain = LevelIO.load(new File("/Users/sam/Documents/Programming/IdeaProjects/WorldEngineAssignment/src/ass2/spec/TestLevels/twinRoads.json"));
        //Terrain terrain = LevelIO.load(new File("/Users/sam/Documents/Programming/IdeaProjects/WorldEngineAssignment/src/ass2/spec/TestLevels/twoSpline.json"));
        Camera c = new Camera();
        Game game = new Game(terrain, c);
        game.run();
    }

	@Override
	public void display(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glLoadIdentity();
        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);

        GLU glu = new GLU();
        double[] pos = camera.getPosition();
        camera.updateHeight(camera.CAMERA_HEIGHT + myTerrain.altitude(pos[0],pos[2]));
        pos = camera.getPosition();
        double[] dir = camera.getDirection();
        double[] up  = camera.getUpVector();
        glu.gluLookAt(pos[0],pos[1],pos[2],
                dir[0],dir[1],dir[2], up[0],up[1],up[2]);

        // Render Terrain
        float terrain[] = {0.96f, 0.67f, 0.55f, 1f};
        renderTerrain(gl, terrain);
        renderTrees(gl);
        renderRoads(gl, 30);
	}

    private void renderTerrain(GL2 gl, float colour[]) {
        gl.glBegin(GL2.GL_TRIANGLES);
        {
            gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_AMBIENT_AND_DIFFUSE, colour,0);
            ArrayList<Triangle> mesh = myTerrain.getTriangleMesh();
            for(Triangle t : mesh) {
                gl.glNormal3dv(t.getNormal(),0);
                for(Vertex v : t.getVertexList()) {
                    gl.glVertex3d(v.x, v.y, v.z);
                }
            }
        }
        gl.glEnd();
    }

    private void renderTrees(GL2 gl) {
        myTerrain.renderTrees(gl);
    }

    private void renderRoads(GL2 gl, int segments) {
        myTerrain.renderRoads(gl, segments);
    }

	@Override
	public void dispose(GLAutoDrawable drawable) {
		// TODO Auto-generated method stub
		
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

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP:
                camera.forwardStep(0.1);
                break;
            case KeyEvent.VK_DOWN:
                camera.backStep(0.1);
                break;
            case KeyEvent.VK_LEFT:
                camera.rotate(-10);
                break;
            case KeyEvent.VK_RIGHT:
                camera.rotate(10);
                break;
            case KeyEvent.VK_A:
                camera.sideStep(0.1);
                break;
            case KeyEvent.VK_D:
                camera.sideStep(-0.1);
                break;
//            case KeyEvent.VK_W:
//                fb++;
//                break;
//            case KeyEvent.VK_S:
//                fb--;
//                break;
            case KeyEvent.VK_ESCAPE:
                System.exit(0);
            default:
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }
}
