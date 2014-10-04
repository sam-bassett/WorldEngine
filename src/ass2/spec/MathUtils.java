package ass2.spec;

/**
 * A collection of useful math methods
 * Extended from malcolmr's code in Ass1 by Sam Bassett
 */
public class MathUtils {

    /**
     * Normalise an angle to the range (-180, 180]
     *
     * @param angle
     * @return
     */
    public static double normaliseAngle(double angle) {
        return ((angle + 180.0) % 360.0 + 360.0) % 360.0 - 180.0;
    }

    /**
     * Clamp a value to the given range
     *
     * @param value
     * @param min
     * @param max
     * @return
     */

    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    /**
     * Multiply two matrices
     *
     * @param p A 3x3 matrix
     * @param q A 3x3 matrix
     * @return
     */
    public static double[][] multiply(double[][] p, double[][] q) {

        double[][] m = new double[3][3];

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                m[i][j] = 0;
                for (int k = 0; k < 3; k++) {
                    m[i][j] += p[i][k] * q[k][j];
                }
            }
        }

        return m;
    }

    /**
     * Multiply a vector by a matrix
     *
     * @param m A 3x3 matrix
     * @param v A 3x1 vector
     * @return
     */
    public static double[] multiply(double[][] m, double[] v) {

        double[] u = new double[3];

        for (int i = 0; i < 3; i++) {
            u[i] = 0;
            for (int j = 0; j < 3; j++) {
                u[i] += m[i][j] * v[j];
            }
        }

        return u;
    }

    /******************************************************************
     *********** Methods below from example lecture code **************
     ******************************************************************/

    /**
     * Get Cross Product of vectors u and v
     * @param u
     * @param v
     * @return cross product
     */
    public static double[] cross(double[] u, double[] v) {
        double[] crossProduct = new double[3];
        crossProduct[0] = u[1]*v[2] - u[2]*v[1];
        crossProduct[1] = u[2]*v[0] - u[0]*v[2];
        crossProduct[2] = u[0]*v[1] - u[1]*v[0];
        return crossProduct;
    }

    /**
     * Find normal for planar polygon
     * @param p0
     * @param p1
     * @param p2
     * @return
     */
    public static double[] normal(double[] p0, double p1[], double p2[]) {
        double[] u = {p1[0] - p0[0], p1[1] - p0[1], p1[2] - p0[2]};
        double[] v = {p2[0] - p0[0], p2[1] - p0[1], p2[2] - p0[2]};
        double[] normal = cross(u,v);
        return   normalise(normal);
    }

    /**
     * Normalise vector n
     * @param n
     * @return
     */
    public static double[] normalise(double[] n) {
        double   mag = getMagnitude(n);
        double[] norm = {n[0]/mag,n[1]/mag,n[2]/mag};
        return   norm;
    }

    /**
     * Get magnitude of vector n
     * @param n
     * @return
     */
    public static double getMagnitude(double[] n) {
        double mag = n[0]*n[0] + n[1]*n[1] + n[2]*n[2];
        mag = Math.sqrt(mag);
        return mag;
    }


    // ===========================================
    // COMPLETE THE METHODS BELOW
    // ===========================================


    /**
     * returns a 2D translation matrix for the given offset vector
     * [ 1  0  v0 ]
     * [ 0  1  v1 ]
     * [ 0  0   1 ]
     *
     * @param v
     * @return
     */
    public static double[][] translationMatrix(double[] v) {
        double tMatrix[][] = new double[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (i == j) {
                    tMatrix[i][j] = 1;
                } else {
                    tMatrix[i][j] = 0;
                }
            }
        }
        tMatrix[0][2] = v[0];
        tMatrix[1][2] = v[1];
        return tMatrix;
    }

    /**
     * returns a 2D rotation matrix for the given angle:
     * [ cos(x) -sin(x)  0 ]
     * [ sin(x)  cos(x)  0 ]
     * [   0       0     1 ]
     *
     * @param angle
     * @return
     */
    public static double[][] rotationMatrix(double angle) {
        double rotMatrix[][] = new double[3][3];
        angle = Math.toRadians(angle);
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                rotMatrix[i][j] = 0;
            }
        }
        rotMatrix[0][0] = Math.cos(angle);
        rotMatrix[0][1] =-Math.sin(angle);
        rotMatrix[1][0] = Math.sin(angle);
        rotMatrix[1][1] = Math.cos(angle);
        rotMatrix[2][2] = 1;
        return rotMatrix;
    }

    /**
     * returns a 2D scale matrix that scales both axes by the same factor:
     * [ s  0  0 ]
     * [ 0  s  0 ]
     * [ 0  0  1 ]
     *
     * @param scale
     * @return
     */
    public static double[][] scaleMatrix(double scale) {
        double scaleMatrix[][] = new double[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                scaleMatrix[i][j] = 0;
            }
        }
        scaleMatrix[0][0] = scale;
        scaleMatrix[1][1] = scale;
        scaleMatrix[2][2] = 1;
        return scaleMatrix;
    }


}

