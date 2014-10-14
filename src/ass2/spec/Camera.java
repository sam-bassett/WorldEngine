package ass2.spec;

/**
 * Camera class for perspective and movement
 *
 * Created by sdba660 on 4/10/2014.
 */
public class Camera {
    public final double CAMERA_HEIGHT = 0.5;
    // position, direction, up vector
    // forward, back, left, right fn.s
    // (map direction + movement to position/rotation changes)
    private double[] position, up;
    private double   rotation;

    public Camera() {
        position    = new double[]{0,CAMERA_HEIGHT,0}; // x, y, z (place at height 0.2 above plane)
        rotation    = 0;                    // in degrees from x axis
        up          = new double[]{0,1,0};   // this probs shouldn't change in base game
    }

    public void forwardStep(double stepSize) {
        // only in x-z plane
        double angle = Math.toRadians(rotation);
        position[0] += Math.cos(angle)*stepSize;
        position[2] += Math.sin(angle)*stepSize;
    }

    public void backStep(double stepSize) {
        double angle = Math.toRadians(rotation);
        position[0] -= Math.cos(angle)*stepSize;
        position[2] -= Math.sin(angle)*stepSize;
    }

    public void sideStep(double stepSize) {
        double angle = Math.toRadians(rotation);
        position[0] += Math.sin(angle)*stepSize;
        position[2] -= Math.cos(angle)*stepSize;
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

    public double[] getUpVector() {
        return up;
    }

    public double[] getDirection() {
        // look at 1 unit in front of camera's nose
        double angle = Math.toRadians(rotation);
        return new double[]{position[0] + Math.cos(angle), position[1], position[2] + Math.sin(angle)};
    }

    public double getRotation() {
        return rotation;
    }
}
