package renderEngine;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.system.MemoryStack;

import java.nio.DoubleBuffer;

public class InputHandler {
    private long window;

    DoubleBuffer mouseX;
    DoubleBuffer mouseY;

    double lastMouseX;
    double lastMouseY;

    public InputHandler(long window){
        this.window = window;

        MemoryStack stack = MemoryStack.stackPush();
        mouseX = stack.mallocDouble(1);
        mouseY = stack.mallocDouble(1);
    }

    public void update(){
        updateMousePos();
        lastMouseX = mouseX.get();
        lastMouseY = mouseY.get();
        updateMouseHold();
    }

    private void updateMousePos(){
        mouseX.clear();
        mouseY.clear();

        GLFW.glfwGetCursorPos(window, mouseX, mouseY);
    }

    private void updateMouseHold(){
        System.out.printf("Last mouse X: %f | Current Mouse X: %f%n", lastMouseX, mouseX.get());
        if (GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS){
            System.out.println("hold");
        }
    }
}
