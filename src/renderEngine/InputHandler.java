package renderEngine;

import org.lwjgl.glfw.*;

import java.util.concurrent.atomic.AtomicReference;

public class InputHandler {
    private long window;
    private final float limitX;
    private final float limitY;
    private final float limitZ;
    private final int moveRate = 5;
    private final double scaleRate = 0.5;
    private final double scaleLimitLower = 1;
    private final double scaleLimitHigher = 10;

    private boolean rButtonDown = false;

    private boolean leftButtonDown = false;
    private float lastMouseX = 0;
    private float lastMouseY = 0;

    private int transX = 0;
    private int transY = 0;
    private int transZ = 0;

    private float dx = 0;
    private float dy = 0;

    private double scaling = 1;

    public InputHandler(long window, float limitX, float limitY, float limitZ){
        this.window = window;
        this.limitX = limitX;
        this.limitY = limitY;
        this.limitZ = limitZ;
    }

    public void update(){
        updateKeyPress();
        updateMousePos();
        updateScroll();
    }

    private void updateMousePos(){
        GLFW.glfwSetMouseButtonCallback(window, (long windowHandle, int button, int action, int mods) -> {
            if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                leftButtonDown = action == GLFW.GLFW_PRESS;
                dx = 0;
                dy = 0;
            }
        });

        GLFW.glfwSetCursorPosCallback(window, (long windowHandle, double x, double y) -> {
            if (leftButtonDown) {
                dx = (float) x - lastMouseX;
                dy = (float) y - lastMouseY;
            }

            lastMouseX = (float) x;
            lastMouseY = (float) y;
        });
    }

    private void updateKeyPress(){
        GLFW.glfwSetKeyCallback(window, new GLFWKeyCallback() {
            @Override
            public void invoke(long window, int key, int scancode, int action, int mods) {
                if (action == GLFW.GLFW_PRESS) {
                    switch (key) {
                        case GLFW.GLFW_KEY_ESCAPE:
                            GLFW.glfwSetWindowShouldClose(window, true);
                            break;
                        case GLFW.GLFW_KEY_R:
                            transX = 0;
                            transY = 0;
                            transZ = 0;
                            dx = 0;
                            dy = 0;
                            scaling = 1;
                            break;
                        case GLFW.GLFW_KEY_E:
                            WindowView.autoRotate = !WindowView.autoRotate;
                            break;
                        case GLFW.GLFW_KEY_W:
                            WindowView.renderWireframe = !WindowView.renderWireframe;
                            break;
                        case GLFW.GLFW_KEY_LEFT:
                            if (transX + moveRate <= limitX) {
                                transX += moveRate;
                            }
                            break;
                        case GLFW.GLFW_KEY_RIGHT:
                            if (transX - moveRate >= -limitX) {
                                transX -= moveRate;
                            }
                            break;
                        case GLFW.GLFW_KEY_DOWN:
                            if (transY + moveRate <= limitY) {
                                transY += moveRate;
                            }
                            break;
                        case GLFW.GLFW_KEY_UP:
                            if (transY - moveRate >= -limitY) {
                                transY -= moveRate;
                            }
                            break;
                        case GLFW.GLFW_KEY_PAGE_DOWN:
                            if (transZ + moveRate <= limitZ * 2) {
                                transZ += moveRate;
                            }
                            break;
                        case GLFW.GLFW_KEY_PAGE_UP:
                            if (transZ - moveRate >= -limitZ) {
                                transZ -= moveRate;
                            }
                            break;
                    }
                    rButtonDown = key == GLFW.GLFW_KEY_R;
                }
            }
        });
    }

    private void updateScroll(){
        GLFW.glfwSetScrollCallback(window, new GLFWScrollCallback() {
            @Override
            public void invoke(long window, double offsetX, double offsetY) {
                if (offsetY > 0) {
                    if ((scaling + scaleRate) <= scaleLimitHigher) {
                        scaling += scaleRate;
                    } else {
                        scaling = scaleLimitHigher;
                    }
                } else if (offsetY < 0) {
                    if ((scaling - scaleRate) >= scaleLimitLower) {
                        scaling -= scaleRate;
                    } else {
                        scaling = scaleLimitLower;
                    }
                }
            }
        });
    }

    public int getTransX(){
        return transX;
    }

    public int getTransY(){
        return transY;
    }

    public int getTransZ(){
        return transZ;
    }

    public boolean getLeftButtonDown(){
        return leftButtonDown;
    }

    public void setRButtonDown(boolean b){
        rButtonDown = b;
    }

    public boolean getRButtonDown(){
        return rButtonDown;
    }

    public float getRotX(){
        return dx;
    }

    public float getRotY(){
        return dy;
    }

    public void resetRot(){
        dx = 0;
        dy = 0;
    }

    public float getScaling(){
        return (float) scaling;
    }
}
