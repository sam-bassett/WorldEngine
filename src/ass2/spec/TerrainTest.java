package ass2.spec;

import junit.framework.TestCase;

public class TerrainTest extends TestCase {
    public static double EPSILON = 0.05;
    public void testAltitude() throws Exception {
        Terrain t = new Terrain(2,2);
        t.setGridAltitude(0,0,0);
        t.setGridAltitude(1,0,0.5);
        t.setGridAltitude(0,1,0);
        t.setGridAltitude(1,1,0.3);
        double i = t.altitude(0.4,0);
        double j = t.altitude(1,0.5);
        double k = t.altitude(0.3,0.8);
        double l = t.altitude(0.9, 0.8);

        assertEquals(i, 0.2, EPSILON);
        assertEquals(j, 0.4, EPSILON);
        assertEquals(k, 0.102, EPSILON);
        assertEquals(l, 0.306, EPSILON);
    }
}