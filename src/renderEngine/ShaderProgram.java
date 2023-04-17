package renderEngine;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL20.*;

public class ShaderProgram {
    private final int programID;
    private int vertexShaderID;
    private int fragmentShaderID;
    private final Map<String, Integer> uniforms;

    public ShaderProgram() throws Exception {
        programID = GL20.glCreateProgram();
        System.out.println("Program ID: " + programID);

        if (programID == 0){
            throw new Exception("could not create Shader");
        }

        uniforms = new HashMap<>();
    }

    public void createVertexShader(String shaderCode){
        vertexShaderID = createShader(shaderCode, GL20.GL_VERTEX_SHADER);
        System.out.println("Vertex Shader created");
    }

    public void createFragmentShader(String shaderCode){
        fragmentShaderID = createShader(shaderCode, GL20.GL_FRAGMENT_SHADER);
        //GL30.glBindFragDataLocation(programID, 0, "fragColour");
        System.out.println("Fragment Shader created");
    }

    private int createShader(String shaderCode, int shaderType){
        int shaderID = GL20.glCreateShader(shaderType);

        if (shaderID == 0){
            throw new RuntimeException("Error creating shader. Type: " + shaderType);
        }

        GL20.glShaderSource(shaderID, shaderCode);
        GL20.glCompileShader(shaderID);

        if (GL20.glGetShaderi(shaderID, GL20.GL_COMPILE_STATUS) != GL20.GL_TRUE){
            throw new RuntimeException(GL20.glGetShaderInfoLog(shaderID));
        }

        GL20.glAttachShader(programID, shaderID);

        return shaderID;
    }

    public void link(){
        GL20.glLinkProgram(programID);

        if (GL20.glGetProgrami(programID, GL20.GL_LINK_STATUS) != GL20.GL_TRUE){
            throw new RuntimeException("Error linking shader code: " + GL20.glGetProgramInfoLog(programID));
        }

        if (vertexShaderID != 0){
            glDetachShader(programID, vertexShaderID);
        }

        if (fragmentShaderID != 0){
            glDetachShader(programID, fragmentShaderID);
        }

        glValidateProgram(programID);
        if (glGetProgrami(programID, GL_VALIDATE_STATUS) == 0){
            System.err.println("Warning validating shader code: " + glGetProgramInfoLog(programID));
        }
    }

    public void createUniform(String uniformName){
        int uniformLocation = glGetUniformLocation(programID, uniformName);

        if (uniformLocation < 0){
            throw new RuntimeException("Could not find uniform: " + uniformName);
        }

        uniforms.put(uniformName, uniformLocation);
        /*try (MemoryStack stack = MemoryStack.stackPush()) {
            int uniform = GL20.glGetUniformLocation(programID, uniformName);
            Matrix4f mat4 = new Matrix4f();
            FloatBuffer fb = stack.mallocFloat(16);

            if (uniformName.equals("projection")) {
                float ratio = 640f / 480f;
                // this might be wrong since Matrix4f.orthographic() has been deprecated
                //mat4 = new Matrix4f().ortho(-ratio, ratio, -1f, 1f, -1f, 1f);
                mat4 = new Matrix4f().perspective(60, ratio, -1f, 1000f);
            }

            // this might be wrong since .getBuffer() has been deprecated
            mat4.get(fb);
            GL20.glUniformMatrix4fv(uniform, false, fb);
        }*/
    }

    public void setUniform(String uniformName, Matrix4f value){
        try (MemoryStack stack = MemoryStack.stackPush()){
            FloatBuffer fb = stack.mallocFloat(16);
            value.get(fb);
            glUniformMatrix4fv(uniforms.get(uniformName), false, value.get(new float[16]));
        } catch (Exception e){
            System.err.println("Failed to set uniform for: " + uniformName);
            e.printStackTrace();
        }
    }

    public void bind(){
        GL20.glUseProgram(programID);
    }

    public void unbind(){
        GL20.glUseProgram(0);
    }

    public int getProgramID(){
        return programID;
    }

    public void cleanup(){
        unbind();
        if(programID != 0){
            glDeleteProgram(programID);
        }
    }
}
