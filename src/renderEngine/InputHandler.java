package renderEngine;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWScrollCallback;
import org.lwjgl.system.MemoryStack;

import java.nio.DoubleBuffer;

public class InputHandler {
    private long window;
    private final int moveRate = 5;
    private final double scaleRate = 0.5;
    private final double scaleLimitLower = 1;
    private final double scaleLimitHigher = 10;

    DoubleBuffer mouseX = MemoryStack.stackMallocDouble(1);
    DoubleBuffer mouseY = MemoryStack.stackMallocDouble(1);

    double lastMouseX;
    double lastMouseY;

    private int moveX = 0;
    private int moveY = 0;
    private int moveZ = 0;

    private double inputScroll = 1;

    public InputHandler(long window){
        this.window = window;
    }

    public void update(){
        //updateMousePos();
        //updateMouseHold();
        updateKeyPress();
        updateScroll();
    }

    private void updateMousePos(){
        lastMouseX = mouseX.get();
        lastMouseY = mouseY.get();

        mouseX.clear();
        mouseY.clear();

        GLFW.glfwGetCursorPos(window, mouseX, mouseY);
    }

    private void updateMouseHold(){
        if (GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS){
            double x = mouseX.get() - lastMouseX;
            double y = mouseY.get() - lastMouseY;
            System.out.printf("Direction Vector | X: %f | Y: %f%n", x, y);
        }
    }

    private void updateKeyPress(){
        GLFW.glfwSetKeyCallback(window, new GLFWKeyCallback() {
            @Override
            public void invoke(long window, int key, int scancode, int action, int mods) {
                if (key == GLFW.GLFW_KEY_ESCAPE && action == GLFW.GLFW_PRESS){
                    GLFW.glfwSetWindowShouldClose(window, true);
                }
                else if (key == GLFW.GLFW_KEY_LEFT && action == GLFW.GLFW_PRESS){
                    moveX += moveRate;
                }
                else if (key == GLFW.GLFW_KEY_RIGHT && action == GLFW.GLFW_PRESS){
                    moveX -= moveRate;
                }
                else if (key == GLFW.GLFW_KEY_PAGE_UP && action == GLFW.GLFW_PRESS){
                    moveY += moveRate;
                }
                else if (key == GLFW.GLFW_KEY_PAGE_DOWN && action == GLFW.GLFW_PRESS){
                    moveY -= moveRate;
                }
                else if (key == GLFW.GLFW_KEY_UP && action == GLFW.GLFW_PRESS){
                    moveZ += moveRate;
                }
                else if (key == GLFW.GLFW_KEY_DOWN && action == GLFW.GLFW_PRESS){
                    moveZ -= moveRate;
                }
            }
        });
    }

    private void updateScroll(){
        GLFW.glfwSetScrollCallback(window, new GLFWScrollCallback() {
            @Override
            public void invoke(long window, double xoffset, double yoffset) {
                if (yoffset > 0) {
                    if ((inputScroll + scaleRate) <= scaleLimitHigher) {
                        inputScroll += scaleRate;
                    } else {
                        inputScroll = scaleLimitHigher;
                    }
                } else if (yoffset < 0) {
                    if ((inputScroll - scaleRate) >= scaleLimitLower) {
                        inputScroll -= scaleRate;
                    } else {
                        inputScroll = scaleLimitLower;
                    }
                }
            }
        });
    }

    public int getMoveX(){
        return moveX;
    }

    public int getMoveY(){
        return moveY;
    }

    public int getMoveZ(){
        return moveZ;
    }

    public float getInputScroll(){
        return (float) inputScroll;
    }
}
