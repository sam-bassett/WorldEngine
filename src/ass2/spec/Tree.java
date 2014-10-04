package ass2.spec;

/**
 * COMMENT: Comment Tree 
 *
 * @author sdba660, malcolmr
 */
public class Tree {
    public final int DEFAULT_HEIGHT = 4;
    public final int DEFAULT_WIDTH  = 1;

    private double[] myPos;
    private double height, width;
    private float[] trunkColour, leafColour;
    
    public Tree(double x, double y, double z) {
        myPos = new double[3];
        myPos[0] = x;
        myPos[1] = y;
        myPos[2] = z;
        height   = DEFAULT_HEIGHT;
        width    = DEFAULT_WIDTH;
        trunkColour = new float[]{0.35f, 0.31f, 0.27f, 1f};
        leafColour  = new float[]{0.45f, 0.65f, 0.26f, 1f};
    }
    
    public double[] getPosition() {
        return myPos;
    }

    public void setHeight(double newHeight) {
        this.height = newHeight;
    }

    public void setWidth(double newWidth) {
        width = newWidth;
    }

    public double getHeight() {
        return height;
    }

    public double getWidth() {
        return width;
    }

    public float[] getTrunkColour() {
        return trunkColour;
    }

    public float[] getLeafColour() {
        return leafColour;
    }

    public void setTrunkColour(float r, float g, float b, float a) {
        trunkColour = new float[]{r, g, b, a};
    }

    public void setLeafColour(float r, float g, float b, float a) {
        leafColour = new float[]{r, g, b, a};
    }
}
