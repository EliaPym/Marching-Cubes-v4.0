package renderEngine;

import data.MarchingCubes;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

/**
 * Main window class that handles all the logic and setup for rendering the mesh.
 */
public class WindowView {
    private static final float FOV = (float) Math.toRadians(60.0f);
    private static final float Z_NEAR = 0.01f;
    private static final float Z_FAR = 10000.0f;
    /**
     * Toggles the mesh to render in wireframe mode.
     */
    public static boolean renderWireframe = false;
    /**
     * Automatically applies rotation to x-axis.
     */
    public static boolean autoRotateX = false;
    /**
     * Automatically applies rotation to y-axis.
     */
    public static boolean autoRotateY = false;
    /**
     * Automatically applies rotation to z-axis.
     */
    public static boolean autoRotateZ = false;
    private static int windowWidth;
    private static int windowHeight;
    private static boolean windowFullscreen;
    private static String windowTitle;
    private static long window;
    private static Mesh mesh;
    private static InputHandler inputHandler;
    private static Matrix4f projectionMatrix;
    private static Matrix4f modelMatrix;
    private static Matrix4f viewMatrix;
    private static Matrix4f translationMatrix;
    private static Matrix4f rotationMatrix;
    private static Matrix4f scalingMatrix;
    private static Vector3f lightPos;
    private static GLFWErrorCallback errorCallback;
    private static ShaderProgram shaderProgram;
    private final Timer timer;
    private static Vector3f cameraPos = new Vector3f(0, 0, 0);
    /**
     * Position x of mesh.
     */
    float posX = 0f;
    /**
     * Position y of mesh.
     */
    float posY = 0f;
    /**
     * Position z of mesh.
     */
    float posZ = -10f;
    /**
     * Rotation of mesh along x-axis.
     */
    float axisX = 0f;
    /**
     * Rotation of mesh along y-axis.
     */
    float axisY = 0f;
    /**
     * Rotation of mesh along z-axis.
     */
    float axisZ = 0f;
    /**
     * Scale of mesh along x-axis.
     */
    float scaleX = 1f;
    /**
     * Scale of mesh along y-axis.
     */
    float scaleY = 1f;
    /**
     * Scale of mesh along z-axis.
     */
    float scaleZ = 1f;
    private float angleX = 0f;
    private float angleY = 0f;
    private float angleZ = 0f;
    private float[] vertices;
    private int[] indices;
    private float[] normals;
    private float[] colours;

    /**
     * Constructs a GLFW window using prerequisite parameters.
     *
     * @param width      width of the window in pixels
     * @param height     height of the window in pixels
     * @param fullscreen enable fullscreen mode of the window
     * @param title      sets title of the window
     */
    public WindowView(int width, int height, boolean fullscreen, String title) {
        windowWidth = width;
        windowHeight = height;
        windowFullscreen = fullscreen;
        windowTitle = title;

        // creates new timer object at beginning of window creation
        timer = new Timer();
    }

    /**
     * Sets data of mesh for window to render.
     *
     * @param vertices float array of vertices
     * @param indices  integer array of indices for vertices
     * @param normals  float array of normals for vertices
     * @param colours  float array of colours for vertices
     */
    public void data(float[] vertices, int[] indices, float[] normals, float[] colours) {
        this.vertices = vertices;
        this.indices = indices;
        this.normals = normals;
        this.colours = colours;


        // outputs information about mesh before creation
        // assists with debugging
        System.out.printf("Generating mesh with: %n  - Vertices: %d%n  - Indices: %d%n  - Polygons: %d%n  - Normals: %d%n  - Colours: %d%n",
                vertices.length / 3,
                indices.length,
                vertices.length / 9,
                normals.length / 3,
                colours.length / 3);
    }

    /**
     * Sets initial starting position for mesh.
     *
     * @param x x coordinate
     * @param y y coordinate
     * @param z z coordinate
     */
    public void setPos(int x, int y, int z) {
        this.posX = (float) x / 2;
        this.posY = (float) y / 2;
        this.posZ = -z * 0.8f;
    }

    /**
     * Runs GLFW window. Initialises shader, loads mesh data onto GPU and starts main loop.<br>
     * Automatically runs cleanup on program termination.
     */
    public void run() {
        try {
            init(); // initialise the program
            System.out.printf("Successfully created GLFW Window with ID: %d%n", window);
            System.out.printf("GLFW Window created with title \"%s\"%n", windowTitle);
            loop(); // run loop as long as program remains open
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cleanUp(); // free allocated memory
        }
    }

    // Initialisation method to create new GLFW window, input handler class and shader program.<br>
    // Loads mesh data onto the GPU to be rendered in the loop method.
    private void init() throws Exception {
        errorCallback = GLFWErrorCallback.createPrint(System.err).set();

        if (!GLFW.glfwInit()) {
            throw new IllegalStateException("Unable to initialise GLFW");
        }

        createWindow();
        inputHandler = new InputHandler(
                window,
                (float) MarchingCubes.getWidth() / 2,
                (float) MarchingCubes.getHeight() / 2,
                (float) MarchingCubes.getDepth() / 2
        );
        setupShader();
        mesh = MeshLoader.createMesh(vertices, indices, normals, colours);
    }

    // Creates and sets up GLFW window
    private void createWindow() {
        // set default window hints
        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE);

        // creates an OpenGL context of version 3.2
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 2);

        // select core functionality of OpenGL context
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);

        // Enable forward compatibility for OpenGL context
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, GLFW.GLFW_TRUE);

        GLFWVidMode vidMode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());
        assert vidMode != null;

        // creates new GLFW window context
        if (windowFullscreen)
            window = GLFW.glfwCreateWindow(vidMode.width(), vidMode.height(), windowTitle, GLFW.glfwGetPrimaryMonitor(), NULL);
        else window = GLFW.glfwCreateWindow(windowWidth, windowHeight, windowTitle, NULL, NULL);

        // check if window has successfully been created
        if (window == NULL) {
            GLFW.glfwTerminate();
            throw new RuntimeException("Failed to create the GLFW window");
        }

        GLFWKeyCallback keyCallback = new GLFWKeyCallback() {
            @Override
            public void invoke(long window, int key, int scancode, int action, int mods) {
                if (key == GLFW.GLFW_KEY_ESCAPE && action == GLFW.GLFW_PRESS) {
                    GLFW.glfwSetWindowShouldClose(window, true);
                }
            }
        };

        GLFW.glfwSetKeyCallback(window, keyCallback);

        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);

            // gets size of window
            GLFW.glfwGetWindowSize(window, pWidth, pHeight);

            // align window at centre of screen
            GLFW.glfwSetWindowPos(window, (vidMode.width() - pWidth.get(0)) / 2, (vidMode.height() - pHeight.get(0)) / 2);

            // create OpenGL context
            GLFW.glfwMakeContextCurrent(window);
            GL.createCapabilities();

            // enable v-sync
            GLFW.glfwSwapInterval(1);
            GLFW.glfwShowWindow(window);

            // sets up transformation matrices
            translationMatrix = new Matrix4f().translation(posX, posY, posZ);
            scalingMatrix = new Matrix4f().scaling(scaleX, scaleY, scaleZ);
            rotationMatrix = new Matrix4f().rotation(0, 1, 1, 1);

            // sets up projection matrix
            float aspectRatio = (float) pWidth.get(0) / (float) pHeight.get(0);
            projectionMatrix = new Matrix4f().perspective(FOV, aspectRatio, Z_NEAR, Z_FAR);

            // sets up model matrix
            modelMatrix = new Matrix4f().identity();

            // sets up view matrix
            viewMatrix = new Matrix4f().lookAt(
                    cameraPos,  // camera position
                    new Vector3f(0, 0, -1), // camera look direction
                    new Vector3f(0, 1, 0)   // camera up direction
            );

            // sets diffuse lighting position
            lightPos = new Vector3f(200, 200, -posZ * 0);

            // initialises timer
            timer.init();
        }

        // sets prerequisites for rendering
        GL11.glEnable(GL_DEPTH_TEST);
        GL11.glDepthFunc(GL11.GL_LESS);
        GL11.glEnable(GL_CULL_FACE);

        // creates and sets window icon
        ByteBuffer icon128;
        int iconWidth, iconHeight;

        // try to get icon from src directory
        // used when program is run from source code
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer comp = stack.mallocInt(1);
            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);

            icon128 = STBImage.stbi_load(System.getProperty("user.dir") + "\\src\\icons\\cube_icon128.png", w, h, comp, 4);
            iconWidth = w.get();
            iconHeight = h.get();
        }
        // try to get icon from resources directory
        // used when program is run from .jar file
        if (icon128 == null) {
            try (MemoryStack stack = MemoryStack.stackPush()) {
                IntBuffer comp = stack.mallocInt(1);
                IntBuffer w = stack.mallocInt(1);
                IntBuffer h = stack.mallocInt(1);

                icon128 = STBImage.stbi_load(System.getProperty("user.dir") + "\\Resources\\Icons\\cube_icon128.png", w, h, comp, 4);
                iconWidth = w.get();
                iconHeight = h.get();
            }
        }

        // sets icon if not null
        if (icon128 != null) {
            GLFWImage image = GLFWImage.malloc();
            GLFWImage.Buffer imageBuffer = GLFWImage.malloc(1);
            image.set(iconWidth, iconHeight, icon128);
            imageBuffer.put(0, image);
            GLFW.glfwSetWindowIcon(window, imageBuffer);
        }
    }

    // Creates and sets up shader program
    private void setupShader() throws Exception {
        // initialises shader program
        shaderProgram = new ShaderProgram();
        System.out.println("Shader Program created");

        String vertexShader = null;
        String fragmentShader = null;
        // try to read shader code from src directory
        // used when program is run from source code
        try {
            vertexShader = Files.readString(Path.of(System.getProperty("user.dir") + File.separator + "src\\renderEngine\\VertexShader.glsl"), Charset.defaultCharset());
            fragmentShader = Files.readString(Path.of(System.getProperty("user.dir") + File.separator + "src\\renderEngine\\FragmentShader.glsl"), Charset.defaultCharset());
        } catch (NoSuchFileException e) {
            // try to read shader code from resources directory
            // used when program is run from .jar file
            try {
                vertexShader = Files.readString(Path.of(System.getProperty("user.dir") + File.separator + "\\Resources\\Shaders\\VertexShader.glsl"), Charset.defaultCharset());
                fragmentShader = Files.readString(Path.of(System.getProperty("user.dir") + File.separator + "\\Resources\\Shaders\\FragmentShader.glsl"), Charset.defaultCharset());
            } catch (NoSuchFileException f) {
                // print stack trace if shader code cannot be found/read from
                e.printStackTrace();
                f.printStackTrace();
            }
        }

        // creates vertex and fragment shaders
        shaderProgram.createVertexShader(vertexShader);
        shaderProgram.createFragmentShader(fragmentShader);
        // links shader program
        // cleans up vertex and fragment shaders
        shaderProgram.link();
        // sets current program to shader program
        shaderProgram.bind();

        // create uniforms for vertex position
        shaderProgram.createUniform("model");
        shaderProgram.createUniform("view");
        shaderProgram.createUniform("projection");

        // create uniforms for lighting
        shaderProgram.createUniform("lightPos");
        shaderProgram.createUniform("viewPos");
        shaderProgram.createUniform("ambientStrength");
        shaderProgram.createUniform("specularStrength");

        shaderProgram.setUniform("viewPos", cameraPos);

        // sets ambient and diffuse for shading
        shaderProgram.setUniform("ambientStrength", 0.5f);
        shaderProgram.setUniform("specularStrength", 0.3f);
    }

    // main loop that contains calls to the methods: update, input handler update, render
    private void loop() {
        while (!GLFW.glfwWindowShouldClose(window)) {
            float delta = timer.getDelta();

            update(delta);
            inputHandler.update();
            render();

            GLFW.glfwSwapBuffers(window);
            // doesn't make calls to render function if there is no user input and auto rotate isn't enabled
            if (!autoRotateX && !autoRotateY && !autoRotateZ) GLFW.glfwWaitEvents();
            GLFW.glfwPollEvents();
        }
    }

    // handles updates every frame that are not directly being used to render mesh
    private void update(float delta) {
        float anglePerSecond = 50f;
        // sets autorotation for x-axis if autoRotateX is true
        if (autoRotateX) {
            if (angleX > 360) angleX = 0;
            angleX += delta * anglePerSecond;
        }
        // sets autorotation for y-axis if autoRotateY is true
        if (autoRotateY) {
            if (angleY > 360) angleY = 0;
            angleY += delta * anglePerSecond;
        }
        // sets autorotation for z-axis if autoRotateZ is true
        if (autoRotateZ) {
            if (angleZ > 360) angleZ = 0;
            angleZ += delta * anglePerSecond;
        }

        // applies rotation delta to rotation if left mouse button is pressed
        if (inputHandler.getLeftButtonDown()) {
            axisX += inputHandler.getRotY();
            axisY += inputHandler.getRotX();
        }

        // resets all transformations when R button is pressed
        if (inputHandler.getRButtonDown()) {
            axisX = 0;
            axisY = 0;
            axisZ = 0;
            angleX = 0;
            angleY = 0;
            angleZ = 0;
            autoRotateX = false;
            autoRotateY = false;
            autoRotateZ = false;
            inputHandler.resetRButtonDown();
        }
    }

    // render method that is called every frame for handling all render code
    private void render() {
        // clears last frame
        clear();

        // sets wireframe render
        if (renderWireframe) glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
        else {
            // sets polygon render
            glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
            GL11.glEnable(GL_CULL_FACE);
        }

        modelMatrix = new Matrix4f().identity();

        // sets translation matrix
        translationMatrix = new Matrix4f().translation(
                inputHandler.getTransX(),
                inputHandler.getTransY(),
                inputHandler.getTransZ() + posZ);

        // sets rotation matrix
        Matrix4f rotationX = new Matrix4f().rotate((float) Math.toRadians(axisX + angleX), 1.0f, 0.0f, 0.0f);
        Matrix4f rotationY = new Matrix4f().rotate((float) Math.toRadians(axisY + angleY), 0.0f, 1.0f, 0.0f);
        Matrix4f rotationZ = new Matrix4f().rotate((float) Math.toRadians(0), 0.0f, 0.0f, 1.0f);
        rotationMatrix = rotationZ.mul(rotationY.mul(rotationX));
        inputHandler.resetRot();

        // sets scaling matrix
        scalingMatrix = new Matrix4f().scaling(
                inputHandler.getScaling(),
                inputHandler.getScaling(),
                inputHandler.getScaling());

        // sets model matrix using transformation matrices
        modelMatrix = modelMatrix.mul(translationMatrix).mul(rotationMatrix).mul(scalingMatrix);

        // sets uniforms for projection, view and model matrices
        // used for determining final position each frame for every vertex
        shaderProgram.setUniform("projection", projectionMatrix);
        shaderProgram.setUniform("view", viewMatrix);
        shaderProgram.setUniform("model", modelMatrix);

        // sets uniform for lighting position
        shaderProgram.setUniform("lightPos", lightPos);

        // bind vertex array object from Mesh object
        GL30.glBindVertexArray(mesh.getVaoID());
        // enabled vertex arrays for each VBO stored in VAO
        GL20.glEnableVertexAttribArray(0); // vertex VBO
        GL20.glEnableVertexAttribArray(1); // normal VBO
        GL20.glEnableVertexAttribArray(2); // colour VBO
        // draw mesh to screen
        GL20.glDrawElements(GL_TRIANGLES, mesh.getVertexCount(), GL_UNSIGNED_INT, 0);

        // unbinds VAO and VBOs
        GL20.glDisableVertexAttribArray(0);
        GL20.glDisableVertexAttribArray(1);
        GL20.glDisableVertexAttribArray(2);
        GL30.glBindVertexArray(0);
    }

    // clears anything drawn on screen
    private void clear() {
        GL20.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
    }

    // on window termination run cleanup to free memory
    private void cleanUp() {
        if (shaderProgram != null) {
            shaderProgram.cleanup();
        }

        Callbacks.glfwFreeCallbacks(window); // free all callbacks associated with the current GLFW window
        GLFW.glfwDestroyWindow(window); // destroys the current window
        errorCallback.free(); // free error callback from memory
        GLFW.glfwTerminate(); // terminates current session

        System.out.println("Cleanup Success");
    }
}
