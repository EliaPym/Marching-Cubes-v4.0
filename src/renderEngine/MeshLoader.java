package renderEngine;

import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;

public class MeshLoader {
    private static int vaoID;
    private static int vboID;
    private static int iboID;

    public static Mesh createMesh(float[] vertices, int[] indices, float[] colour){
        vaoID = genVao();
        storeData(vertices);
        return new Mesh(vaoID, vboID, iboID, indices.length);
    }

    private static int genVao(){
        int vao = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(vao);
        return vao;
    }

    private static void storeData(float[] data){
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buffer = stack.mallocFloat(data.length);
            buffer.put(data).flip();

            vboID = GL30.glGenBuffers();
            GL30.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboID);
            GL30.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public static void cleanup(){
        GL30.glDeleteVertexArrays(vaoID);
        GL30.glDeleteBuffers(vboID);
    }
}
