package ass2.spec;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sam on 15/09/2014.
 */
public class Triangle {
    private List<Vertex> vertexList;
    private double[] normal = null;

    public Triangle() {
        vertexList = new ArrayList<Vertex>(3);
    }

    public Triangle(Vertex v0, Vertex v1, Vertex v2) {
        vertexList = new ArrayList<Vertex>();
        vertexList.add(v0);
        vertexList.add(v1);
        vertexList.add(v2);
    }

    public List<Vertex> getVertexList() {
        return vertexList;
    }

    public void addVertex(Vertex v) {
        if(vertexList.size() < 3)
            vertexList.add(v);
        else System.err.println("Maximum 3 vertices in a triangle");
    }

    public double[] getNormal() {
        if (vertexList.size() != 3) {
            System.err.println("Incomplete triangle, no normal exists");
            return null;
        }
        Vertex v0 = vertexList.get(0);
        Vertex v1 = vertexList.get(1);
        Vertex v2 = vertexList.get(2);
        // From notes week 6
        double u[] = {v1.x - v0.x, v1.y - v0.y, v1.z - v0.z};
        double v[] = {v2.x - v0.x, v2.y - v0.y, v2.z - v0.z};
        double cross[] = crossProduct(u, v);
        double magnitude = cross[0]*cross[0] + cross[1]*cross[1] + cross[2]*cross[2];
        magnitude = Math.sqrt(magnitude);
        cross[0] = cross[0]/magnitude;
        cross[1] = cross[1]/magnitude;
        cross[2] = cross[2]/magnitude;
        return cross;
    }


    // from notes week 6
    private double[] crossProduct(double[] u, double[] v) {
        double crossProduct[] = new double[3];
        crossProduct[0] = u[1]*v[2] - u[2]*v[1];
        crossProduct[1] = u[2]*v[0] - u[0]*v[2];
        crossProduct[2] = u[0]*v[1] - u[1]*v[0];
        return crossProduct;
    }
}
