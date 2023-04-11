package renderEngine;

import org.lwjgl.glfw.*;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;
import java.util.Objects;

import static org.lwjgl.system.MemoryUtil.NULL;

public class WindowView {
    private static int windowWidth;
    private static int windowHeight;
    private static String windowTitle;
    private static long window;
    private static GLFWErrorCallback errorCallback;
    private static GLFWKeyCallback keyCallback;

    public WindowView(int width, int height, String title){
        windowWidth = width;
        windowHeight = height;
        windowTitle = title;

        run();
    }

    public void run(){
        try {
            init();
            loop();

            GLFW.glfwDestroyWindow(window);
            Callbacks.glfwFreeCallbacks(window);
            GLFW.glfwTerminate();
            errorCallback.free();

        } catch (Exception e){
            e.printStackTrace();
        } finally {
            cleanUp();
        }
    }

    public void init(){
        errorCallback = GLFWErrorCallback.createPrint(System.err).set();

        if (!GLFW.glfwInit()){
            throw new IllegalStateException("Unable to initialise GLFW");
        }

        createWindow();

        GL.createCapabilities();
    }

    private void createWindow(){
        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE);

        window = GLFW.glfwCreateWindow(windowWidth, windowHeight, windowTitle, NULL, NULL);

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

            GLFWVidMode vidMode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());
            assert vidMode != null;
            GLFW.glfwSetWindowPos(window, (vidMode.width() - pWidth.get(0)) / 2, (vidMode.height() - pHeight.get(0)) / 2);

            GLFW.glfwMakeContextCurrent(window);
            GLFW.glfwSwapInterval(1);
            GLFW.glfwShowWindow(window);
        }
    }

    public void loop(){
        while (!GLFW.glfwWindowShouldClose(window)){
            double time = GLFW.glfwGetTime();

            GLFW.glfwSwapBuffers(window);
            //GLFW.glfwWaitEvents();
            GLFW.glfwPollEvents();
        }
    }

    public void cleanUp(){

    }
}
