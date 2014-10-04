package ass2.spec;

/**
 * COMMENT: Comment Tree 
 *
 * @author malcolmr
 */
public class Tree {
    public final int DEFAULT_HEIGHT = 4;
    public final int DEFAULT_WIDTH  = 1;

    private double[] myPos;
    private double height, width;
    
    public Tree(double x, double y, double z) {
        myPos = new double[3];
        myPos[0] = x;
        myPos[1] = y;
        myPos[2] = z;
        height   = DEFAULT_HEIGHT;
        width    = DEFAULT_WIDTH;
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

}
