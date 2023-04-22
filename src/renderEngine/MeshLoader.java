package renderEngine;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

/**
 * Builds Mesh object. Sets necessary VAO, VBO and IBOs.
 */
public class MeshLoader {
    private static int vaoID;

    /**
     * Return new Mesh object containing the vertex array object and number of vertices.
     *
     * @param vertices float array containing vertex coordinates
     * @param indices  integer array containing indices constructing each polygon from given vertices
     * @param normals  float array for normals of each vertex
     * @param colours  float array containing colours for each vertex
     * @return new Mesh object
     */
    public static Mesh createMesh(float[] vertices, int[] indices, float[] normals, float[] colours) {
        genVao();
        genVbo(vertices, GL20.glGetAttribLocation(ShaderProgram.getProgramID(), "position"));
        genIbo(indices);
        genVbo(normals, GL20.glGetAttribLocation(ShaderProgram.getProgramID(), "normal"));
        genVbo(colours, GL20.glGetAttribLocation(ShaderProgram.getProgramID(), "colour"));

        return new Mesh(vaoID, indices.length);
    }

    // generates vertex array object to load VBOs and IBO onto the GPU
    private static void genVao() {
        vaoID = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(vaoID);
    }

    // generates vertex buffer object to be added to the VAO
    private static void genVbo(float[] data, int index) {
        try {
            int vbo = GL15.glGenBuffers();
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
            GL15.glBufferData(GL15.GL_ARRAY_BUFFER, data, GL15.GL_STATIC_DRAW);
            GL20.glVertexAttribPointer(index, 3, GL11.GL_FLOAT, false, 0, GL11.GL_NONE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // generates index buffer object to be added to the VAO
    private static void genIbo(int[] data) {
        try {
            int iboID = GL15.glGenBuffers();
            GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, iboID);
            GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, data, GL15.GL_STATIC_DRAW);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}