package renderEngine;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;

public class MeshLoader {
    private static int vaoID;
    private static int vboID;
    private static int iboID;

    public static Mesh createMesh(float[] vertices, int[] indices, float[] normals, float[] colours){
        genVao();
        genVbo(vertices, 0);
        genIbo(indices);
        genVbo(normals, 1);
        genVbo(colours, 2);

        return new Mesh(vaoID, indices.length);
    }

    private static void genVao(){
        vaoID = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(vaoID);
    }

    private static void genVbo(float[] data, int index){
        try {
            int vbo = GL15.glGenBuffers();
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
            GL15.glBufferData(GL15.GL_ARRAY_BUFFER, data, GL15.GL_STATIC_DRAW);
            GL20.glVertexAttribPointer(index, 3, GL11.GL_FLOAT, false, 0, 0);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private static void genIbo(int[] data){
        try {
            iboID = GL15.glGenBuffers();
            GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, iboID);
            GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, data, GL15.GL_STATIC_DRAW);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void cleanup(){
        GL30.glDeleteBuffers(vboID);
        GL30.glDeleteBuffers(iboID);
    }
}