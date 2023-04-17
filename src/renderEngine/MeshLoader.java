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

    public static Mesh createMesh(float[] vertices, int[] indices, float[] colours){
        vaoID = genVao();
        genVbo(vertices);
        genIbo(indices);
        assignColours(colours);

        return new Mesh(vaoID, vboID, iboID, indices.length);
    }

    private static int genVao(){
        int vao = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(vao);
        return vao;
    }

    private static void genVbo(float[] data){
        try {
            vboID = GL15.glGenBuffers();
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboID);
            GL15.glBufferData(GL15.GL_ARRAY_BUFFER, data, GL15.GL_STATIC_DRAW);
            GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 0, 0);
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

    private static void assignColours(float[] data){
        int colourVboID = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, colourVboID);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, data, GL15.GL_STATIC_DRAW);
        GL20.glVertexAttribPointer(1, 3, GL15.GL_FLOAT, false, 0, 0);
    }

    public static void cleanup(){
        GL30.glDeleteBuffers(vboID);
        GL30.glDeleteBuffers(iboID);
    }

    /*
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
    */
}