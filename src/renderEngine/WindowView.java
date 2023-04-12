package renderEngine;

import org.joml.Matrix4f;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Objects;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class WindowView {
    private static int windowWidth;
    private static int windowHeight;
    private static String windowTitle;
    private static long window;
    private static Mesh mesh;

    private float[] vertices;
    private int[] indices;
    private float[] colours;

    private int uniformModel;
    private float angle = 0f;
    private float anglePerSecond = 50f;
    private Timer timer;

    private static GLFWErrorCallback errorCallback;
    private static GLFWKeyCallback keyCallback;
    private static ShaderProgram shaderProgram;

    public static boolean renderWireframe = false;

    public WindowView(int width, int height, String title){
        windowWidth = width;
        windowHeight = height;
        windowTitle = title;

        timer = new Timer();
    }

    public void data(float[] vertices, int[] indices, float[] colours){
        this.vertices = vertices;
        this.indices = indices;
        this.colours = colours;
    }

    public void run(){
        try {
            init(); // initialise the program
            loop(); // run loop as long as program remains open
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            cleanUp(); // free allocated memory
        }
    }

    private void init() throws Exception {
        errorCallback = GLFWErrorCallback.createPrint(System.err).set();

        if (!GLFW.glfwInit()){
            throw new IllegalStateException("Unable to initialise GLFW");
        }

        createWindow();
        mesh = MeshLoader.createMesh(vertices, indices, colours);
        setupShader();
    }

    private void createWindow(){
        // set default window hints
        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE);
        // creates an OpenGL context of version 3.2
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 2);
        // select core functionality of OpenGL context
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);
        // OpenGL context is forwards compatible
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, GLFW.GLFW_TRUE);

        // creates new GLFW window context
        window = GLFW.glfwCreateWindow(windowWidth, windowHeight, windowTitle, NULL, NULL);

        // check if window has successfully been created
        if (window == NULL){
            GLFW.glfwTerminate();
            throw new RuntimeException("Failed to create the GLFW window");
        }

        keyCallback = new GLFWKeyCallback() {
            @Override
            public void invoke(long window, int key, int scancode, int action, int mods) {
                if (key == GLFW.GLFW_KEY_ESCAPE && action == GLFW.GLFW_PRESS){
                    GLFW.glfwSetWindowShouldClose(window, true);
                }
            }
        };

        GLFW.glfwSetKeyCallback(window, keyCallback);

        try (MemoryStack stack = MemoryStack.stackPush()){
            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);

            GLFW.glfwGetWindowSize(window, pWidth, pHeight);

            // align window at centre of screen
            GLFWVidMode vidMode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());
            assert vidMode != null;
            GLFW.glfwSetWindowPos(window, (vidMode.width() - pWidth.get(0)) / 2, (vidMode.height() - pHeight.get(0)) / 2);

            // create OpenGL context
            GLFW.glfwMakeContextCurrent(window);
            GL.createCapabilities();

            // enable v-sync
            GLFW.glfwSwapInterval(1);
            GLFW.glfwShowWindow(window);
        }
    }

    private void setupShader() throws Exception {
        shaderProgram = new ShaderProgram();
        System.out.println("Shader Program created");

        String vertexShader = "#version 150 core\n" +
                "\n" +
                "in vec3 position;\n" +
                "in vec3 colour;\n" +
                "\n" +
                "out vec3 vertexColour;\n" +
                "\n" +
                "uniform mat4 model;\n" +
                "uniform mat4 view;\n" +
                "uniform mat4 projection;\n" +
                "\n" +
                "void main(){\n" +
                "    vertexColour = colour;\n" +
                "    mat4 pvm = projection * view * model;\n" +
                "    gl_Position = pvm * vec4(position, 1.0);\n" +
                "}";
        String fragmentShader = "#version 150 core\n" +
                "\n" +
                "in vec3 vertexColour;\n" +
                "\n" +
                "out vec4 fragColour;\n" +
                "\n" +
                "void main(){\n" +
                "    fragColour = vec4(vertexColour, 1.0);\n" +
                "}";

        shaderProgram.createVertexShader(vertexShader);
        shaderProgram.createFragmentShader(fragmentShader);
        shaderProgram.link();
        shaderProgram.bind();

        shaderProgram.createUniform("model");
        shaderProgram.createUniform("view");
        shaderProgram.createUniform("projection");
    }

    private void loop(){
        while (!GLFW.glfwWindowShouldClose(window)){
            //double time = GLFW.glfwGetTime();
            float delta = timer.getDelta();

            update(delta);
            render();

            GLFW.glfwSwapBuffers(window);
            //GLFW.glfwWaitEvents();
            GLFW.glfwPollEvents();
        }
    }

    private void update(float delta){
        angle += delta * anglePerSecond;
    }

    private void render(){
        clear();

        if (renderWireframe) {
            glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
        }

        //Matrix4f model = new Matrix4f().rotate(angle, 0f, 0f, 1f);
        //MemoryStack stack = MemoryStack.stackPush();
        //FloatBuffer fb = stack.mallocFloat(16);
        //model.get(fb);
        //GL20.glUniformMatrix4fv(uniformModel, false, fb);

        //GL20.glDrawElements(GL_TRIANGLES, mesh.getVertexID(), GL_UNSIGNED_INT, 0);
        GL20.glDrawArrays(GL11.GL_TRIANGLES, 0, 3);

        GL20.glDisableVertexAttribArray(0);
        GL30.glBindVertexArray(0);
    }

    private void clear(){
        GL20.glClear(GL20.GL_COLOR_BUFFER_BIT|GL20.GL_DEPTH_BUFFER_BIT);
    }

    private void cleanUp(){
        MeshLoader.cleanup();
        shaderProgram.cleanup();

        GLFW.glfwDestroyWindow(window);
        Callbacks.glfwFreeCallbacks(window);
        GLFW.glfwTerminate();
        errorCallback.free();

        System.out.println("Cleanup Success");
    }
}
