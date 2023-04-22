package renderEngine;

/**
 * Mesh class stores VAOs containing vertex, normal and colour VBOs.
 */
public class Mesh {
    private final int vaoID;
    private final int vertexCount;

    /**
     * Creates Mesh object.
     *
     * @param vao         vertex array object containing VBOs of vertices, normals, colours
     * @param vertexCount number of vertices defining the mesh
     */
    public Mesh(int vao, int vertexCount) {
        vaoID = vao;
        this.vertexCount = vertexCount;
    }

    /**
     * Returns ID of the vertex array object for mesh.
     *
     * @return integer VAO ID
     */
    public int getVaoID() {
        return vaoID;
    }

    /**
     * Returns vertex count.
     *
     * @return integer count of vertices
     */
    public int getVertexCount() {
        return vertexCount;
    }
}
