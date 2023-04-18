package renderEngine;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.MemoryStack;

import java.io.File;
import java.nio.IntBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class WindowView {
    private static int windowWidth;
    private static int windowHeight;
    private static String windowTitle;
    private static long window;
    private static Mesh mesh;

    private static Matrix4f projectionMatrix;
    private static Matrix4f modelMatrix;
    private static Matrix4f viewMatrix;
    private static Matrix4f translationMatrix;
    private static Matrix4f rotationMatrix;
    private static Matrix4f scalingMatrix;
    private static final float FOV = (float) Math.toRadians(60.0f);
    private static final float Z_NEAR = 0.01f;
    private static final float Z_FAR = 1000.0f;
    private float aspectRatio;

    private float[] vertices;
    private int[] indices;
    private float[] colours;

    // translation
    float posX = 0f;
    float posY = 0f;
    float posZ = -10f;
    // rotation
    float axisX = 0f;
    float axisY = 1f;
    float axisZ = 0f;
    // scaling
    float scaleX = 1f;
    float scaleY = 1f;
    float scaleZ = 1f;

    private int uniformModel;
    private float angle = 0f;
    private final float anglePerSecond = 50f;
    private final Timer timer;

    private static GLFWErrorCallback errorCallback;
    private static GLFWKeyCallback keyCallback;
    private static ShaderProgram shaderProgram;

    public static boolean renderWireframe;

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

    public void setPos(int z){
        this.posZ = -z * 1.5f;
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

            aspectRatio = (float) pWidth.get(0) / (float) pHeight.get(0);
            projectionMatrix = new Matrix4f().perspective(FOV, aspectRatio, Z_NEAR, Z_FAR);

            translationMatrix = new Matrix4f().translation(0, 0, -5);
            scalingMatrix = new Matrix4f().scaling(1, 1, 1);
            rotationMatrix = new Matrix4f().rotation(0, 1, 1, 1);

            modelMatrix = new Matrix4f().identity();

            viewMatrix = new Matrix4f().lookAt(
                    new Vector3f(0, 0, 0),  // camera position
                    new Vector3f(0, 0, -1), // camera look direction
                    new Vector3f(0, -1, 0)   // camera up direction
            );

            timer.init();
        }

        GL11.glEnable(GL_DEPTH_TEST);
        GL11.glDepthFunc(GL11.GL_LESS);
        GL11.glEnable(GL_CULL_FACE);
    }

    private void setupShader() throws Exception {
        shaderProgram = new ShaderProgram();
        System.out.println("Shader Program created");

        String vertexShader = Files.readString(Path.of(System.getProperty("user.dir") + File.separator + "src\\renderEngine\\VertexShader.glsl"), Charset.defaultCharset());
        String fragmentShader = Files.readString(Path.of(System.getProperty("user.dir") + File.separator + "src\\renderEngine\\FragmentShader.glsl"), Charset.defaultCharset());

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
            float delta = timer.getDelta();

            update(delta);
            render();

            GLFW.glfwSwapBuffers(window);
            //GLFW.glfwWaitEvents();
            GLFW.glfwPollEvents();
        }
    }

    private void update(float delta){
        if (angle > 360) {
            angle = 0;
        }
        angle += delta * anglePerSecond;
    }

    private void render(){
        clear();

        if (renderWireframe) {
            glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
        }

        modelMatrix = new Matrix4f().identity();

        //System.out.println(angle);
        translationMatrix = new Matrix4f().translation(posX, posY, posZ);

        rotationMatrix = new Matrix4f().rotate((float)Math.toRadians(angle * axisX), 1f, 0f, 0f); // rotate along x-axis
        rotationMatrix = rotationMatrix.rotate((float)Math.toRadians(angle * axisY), 0f, 1f, 0f); // rotate along y-axis
        rotationMatrix = rotationMatrix.rotate((float)Math.toRadians(angle * axisZ), 0f, 0f, 1f); // rotate along z-axis
        scalingMatrix = new Matrix4f().scaling(scaleX, scaleY, scaleZ);

        //modelMatrix = modelMatrix.mul(scalingMatrix).mul(translationMatrix).mul(rotationMatrix);
        modelMatrix = modelMatrix.mul(translationMatrix).mul(rotationMatrix).mul(scalingMatrix);

        shaderProgram.setUniform("projection", projectionMatrix);
        shaderProgram.setUniform("view", viewMatrix);
        shaderProgram.setUniform("model", modelMatrix);

        GL30.glBindVertexArray(mesh.getVaoID());
        GL20.glEnableVertexAttribArray(0);
        GL20.glEnableVertexAttribArray(1);
        GL20.glDrawElements(GL_TRIANGLES, mesh.getVertexID(), GL_UNSIGNED_INT, 0);

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
