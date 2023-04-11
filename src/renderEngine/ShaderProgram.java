package renderEngine;

import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import static org.lwjgl.opengl.GL20.glDeleteProgram;

public class ShaderProgram {
    int programID;

    public ShaderProgram() throws Exception {
        programID = GL20.glCreateProgram();

        if (programID == 0){
            throw new Exception("could not create Shader");
        }
    }

    public void createVertexShader(String source){
        createShaders(source, GL20.GL_VERTEX_SHADER);
    }

    public void createFragmentShader(String source){
        createShaders(source, GL20.GL_FRAGMENT_SHADER);
        GL30.glBindFragDataLocation(programID, 0, "fragColour");
    }

    private void createShaders(String shaderSource, int shaderType){
        int shader = GL20.glCreateShader(shaderType);
        GL20.glShaderSource(shader, shaderSource);
        GL20.glCompileShader(shader);

        if (GL20.glGetShaderi(shader, GL20.GL_COMPILE_STATUS) != GL20.GL_TRUE){
            throw new RuntimeException(GL20.glGetShaderInfoLog(shader));
        }

        GL20.glAttachShader(programID, shader);
    }

    public void link(){
        GL20.glLinkProgram(programID);

        if (GL20.glGetProgrami(programID, GL20.GL_LINK_STATUS) != GL20.GL_TRUE){
            throw new RuntimeException(GL20.glGetProgramInfoLog(programID));
        }
    }

    public void bind(){
        GL20.glUseProgram(programID);
    }

    public void unbind(){
        GL20.glUseProgram(0);
    }

    public void cleanup(){
        unbind();
        if(programID != 0){
            glDeleteProgram(programID);
        }
    }
}
