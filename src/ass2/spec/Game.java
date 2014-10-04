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
        //Terrain terrain = LevelIO.load(new File("/Users/sam/Documents/Programming/IdeaProjects/WorldEngineAssignment/src/ass2/spec/TestLevels/negativeLightTest.json"));
        Terrain terrain = LevelIO.load(new File("/Users/sam/Documents/Programming/IdeaProjects/WorldEngineAssignment/src/ass2/spec/TestLevels/simpleBezier.json"));
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

        GLU glu = new GLU();
        double[] pos = camera.getPosition();
        double[] dir = camera.getDirection();
        double[] up  = camera.getUpVector();
        glu.gluLookAt(pos[0],pos[1],pos[2],
                dir[0],dir[1],dir[2], up[0],up[1],up[2]);

        // Default camera movement controls
        gl.glRotated(dx,1,0,0);
        gl.glRotated(dy,0,1,0);
        gl.glScaled(scale, scale, scale);
        gl.glTranslated(tr,0,0);
        gl.glTranslated(0,0,fb);

        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
        // Render Terrain
        float terrain[] = {0.96f, 0.67f, 0.55f, 1f};
        //float green[] = {0f,1f,0f,1f};
        gl.glBegin(GL2.GL_TRIANGLES);
        {
            gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_AMBIENT_AND_DIFFUSE, terrain,0);
            ArrayList<Triangle> mesh = myTerrain.getTriangleMesh();
            for(Triangle t : mesh) {
                gl.glNormal3dv(t.getNormal(),0);
                for(Vertex v : t.getVertexList()) {
                    gl.glVertex3d(v.x, v.y, v.z);
                }
            }
        }
        gl.glEnd();
        // Render trees
        myTerrain.renderTrees(gl);
        myTerrain.renderRoads(gl, 30);
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
        GL2 gl = drawable.getGL().getGL2();
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();

        GLU glu = new GLU();
        glu.gluPerspective(80.0, (float)width/(float)height, 0.001, 60.0);
        gl.glMatrixMode(GL2.GL_MODELVIEW);
		
	}

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP:
                //if (step < 180) step++;
                if (dx < 360)
                    dx+=4;
                else dx=0;
                break;
            case KeyEvent.VK_DOWN:
                //if (step > 0) step--;
                if (dx > -360)
                    dx-=4;
                else dx=0;
                break;
            case KeyEvent.VK_LEFT:
                if (dy > -360)
                    dy-=4;
                else dy=0;
                break;
            case KeyEvent.VK_RIGHT:
                if (dy < 360)
                    dy+=4;
                else dy=0;
                break;
            case KeyEvent.VK_EQUALS:
                scale++;
                break;
            case KeyEvent.VK_MINUS:
                if(scale > 1) {
                    scale--;
                } else {
                    scale /= 1.5;
                }
                break;
            case KeyEvent.VK_A:
                tr++;
                break;
            case KeyEvent.VK_D:
                tr--;
                break;
            case KeyEvent.VK_W:
                fb++;
                break;
            case KeyEvent.VK_S:
                fb--;
                break;
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
