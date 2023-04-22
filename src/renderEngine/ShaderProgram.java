package renderEngine;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL20;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL20.*;

/**
 * Creates and handles the shader program for the 3D engine.
 */
public class ShaderProgram {
    private static int programID;
    private final Map<String, Integer> uniforms;
    private int vertexShaderID;
    private int fragmentShaderID;

    /**
     * Instantiates a new Shader program.<br>
     * Order of operation is as follows:
     * <ol>
     * <li>glCreateProgram</li>
     * <li>glCreateShader</li>
     * <li>glShaderSource</li>
     * <li>glCompileShader</li>
     * <li>glAttachShader</li>
     * <li>glLinkProgram</li>
     * <li>glDetachShader</li>
     * <li>glDeleteShader</li>
     * <li>glUseProgram</li>
     * </ol>
     *
     * @throws Exception Exception if shader fails to instantiate
     */
    public ShaderProgram() throws Exception {
        programID = GL20.glCreateProgram();

        if (programID == 0) {
            throw new Exception("could not create Shader");
        }
        System.out.println("Shader Program ID: " + programID);

        uniforms = new HashMap<>();
    }

    /**
     * Returns ID of the shader program.
     *
     * @return shader program ID
     */
    public static int getProgramID() {
        return programID;
    }

    /**
     * Creates shader for vertex.
     *
     * @param shaderCode vertex shader code as string written in GLSL
     */
    public void createVertexShader(String shaderCode) {
        vertexShaderID = createShader(shaderCode, GL20.GL_VERTEX_SHADER);
        System.out.println("Vertex Shader created");
    }

    /**
     * Creates shader for fragment.
     *
     * @param shaderCode fragment shader code as string written in GLSL
     */
    public void createFragmentShader(String shaderCode) {
        fragmentShaderID = createShader(shaderCode, GL20.GL_FRAGMENT_SHADER);
        System.out.println("Fragment Shader created");
    }

    // method to create and compile shader
    private int createShader(String shaderCode, int shaderType) {
        int shaderID = GL20.glCreateShader(shaderType);

        if (shaderID == 0) {
            throw new RuntimeException("Error creating shader. Type: " + shaderType);
        }

        GL20.glShaderSource(shaderID, shaderCode);
        GL20.glCompileShader(shaderID);

        if (GL20.glGetShaderi(shaderID, GL20.GL_COMPILE_STATUS) != GL20.GL_TRUE) {
            throw new RuntimeException(GL20.glGetShaderInfoLog(shaderID));
        }

        GL20.glAttachShader(programID, shaderID);

        return shaderID;
    }

    /**
     * Link program and clean up vertex and fragment shaders.
     */
    public void link() {
        GL20.glLinkProgram(programID);

        if (GL20.glGetProgrami(programID, GL20.GL_LINK_STATUS) != GL20.GL_TRUE) {
            throw new RuntimeException("Error linking shader code: " + GL20.glGetProgramInfoLog(programID));
        }

        // detach and delete vertex shader
        // frees up memory after shader has been used
        if (vertexShaderID != 0) {
            glDetachShader(programID, vertexShaderID);
            glDeleteShader(vertexShaderID);
        }

        // detach and delete fragment shader
        // frees up memory after shader has been used
        if (fragmentShaderID != 0) {
            glDetachShader(programID, fragmentShaderID);
            glDeleteShader(fragmentShaderID);
        }

        // validate program for assistance with debugging
        glValidateProgram(programID);
        if (glGetProgrami(programID, GL_VALIDATE_STATUS) == 0) {
            System.err.println("Warning validating shader code: " + glGetProgramInfoLog(programID));
        }
    }

    /**
     * Creates uniform and sets location.
     *
     * @param uniformName uniform name
     */
    public void createUniform(String uniformName) {
        int uniformLocation = glGetUniformLocation(programID, uniformName);

        if (uniformLocation < 0) {
            throw new RuntimeException("Could not find uniform: " + uniformName);
        }

        uniforms.put(uniformName, uniformLocation);
    }

    /**
     * Sets uniform for Matrix4f.
     *
     * @param uniformName uniform name
     * @param value       Matrix (4 float) value
     */
    public void setUniform(String uniformName, Matrix4f value) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer fb = stack.mallocFloat(16);
            value.get(fb);
            glUniformMatrix4fv(uniforms.get(uniformName), false, value.get(new float[16]));
        } catch (Exception e) {
            System.err.println("Failed to set uniform for: " + uniformName);
            e.printStackTrace();
        }
    }

    /**
     * Sets uniform for Vector3f.
     *
     * @param uniformName uniform name
     * @param value       Vector (3 float) value
     */
    public void setUniform(String uniformName, Vector3f value) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer fb = stack.mallocFloat(12);
            value.get(fb);
            glUniform3f(uniforms.get(uniformName), value.x, value.y, value.z);
        } catch (Exception e) {
            System.err.println("Failed to set uniform for: " + uniformName);
            e.printStackTrace();
        }
    }

    /**
     * Sets uniform for float.
     *
     * @param uniformName uniform name
     * @param value       float value
     */
    public void setUniform(String uniformName, float value) {
        try {
            glUniform1f(uniforms.get(uniformName), value);
        } catch (Exception e) {
            System.err.println("Failed to set uniform for: " + uniformName);
            e.printStackTrace();
        }
    }

    /**
     * Sets the current program to shader program.
     */
    public void bind() {
        GL20.glUseProgram(programID);
    }

    /**
     * Sets the current program to 0 (null).
     */
    public void unbind() {
        GL20.glUseProgram(0);
    }

    /**
     * Cleanup shader program on window close.
     */
    public void cleanup() {
        unbind();
        if (programID != 0) {
            glDeleteProgram(programID);
        }
    }
}
