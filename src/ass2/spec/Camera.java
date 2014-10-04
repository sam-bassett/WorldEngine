package ass2.spec;

/**
 * Created by sam on 4/10/2014.
 */
public class Camera {
    // position, direction, up vector
    // forward, back, left, right fn.s
    // (map direction + movement to position/rotation changes)
    private double[] position, up;
    private double   rotation;

    public Camera() {
        position    = new double[]{0,0.3,0}; // x, y, z (place at height 0.2 above plane)
        rotation    = 45;                    // in degrees from x axis
        up          = new double[]{0,1,0};   // this probs shouldn't change in base game
    }

    public void forwardStep(double stepSize) {
        // only in x-z plane
        position[0] += Math.cos(rotation)*stepSize;
        position[2] += Math.sin(rotation)*stepSize;
    }

    public void backStep(double stepSize) {
        position[0] -= Math.cos(rotation)*stepSize;
        position[2] -= Math.sin(rotation)*stepSize;
    }

    public void rotate(double dTheta) {
        rotation += dTheta;
        if(rotation > 360) {
            rotation -= 360;
        }
    }

    public void updateHeight(double y) {
        position[1] = y;
    }



    // Getters and setters follow
    public void setPosition(double x, double y, double z) {
        position = new double[]{x, y, z};
    }

    public void setRotation(double theta) {
        rotation = theta;
    }

    public void setUpVector(double x, double y, double z) {
        up = new double[]{x, y, z};
    }

    public double[] getPosition() {
        return position;
    }

    public double getRotation() {
        return rotation;
    }
}
