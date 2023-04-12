package renderEngine;

public class Mesh {
    private final int vaoID;
    private final int vboID;
    private final int iboID;
    private final int vertexID;

    public Mesh(int vao, int vbo, int ibo, int vertex){
        vaoID = vao;
        vboID = vbo;
        iboID = ibo;
        vertexID= vertex;
    }

    public int getVaoID(){
        return vaoID;
    }

    public int getVboID(){
        return vboID;
    }

    public int getIboID(){
        return iboID;
    }

    public int getVertexID(){
        return vertexID;
    }
}
