package renderEngine;

public class Mesh {
    private final int vaoID;
    private final int vertexID;

    public Mesh(int vao, int vertex){
        vaoID = vao;
        vertexID = vertex;
    }

    public int getVaoID(){
        return vaoID;
    }

    public int getVertexID(){
        return vertexID;
    }
}
