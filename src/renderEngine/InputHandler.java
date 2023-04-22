package renderEngine;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWScrollCallback;

/**
 * Class to handle window inputs.
 */
public class InputHandler {
    private final long window;
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

    // translation
    private int transX = 0;
    private int transY = 0;
    private int transZ = 0;

    // rotation delta
    private float dx = 0;
    private float dy = 0;

    // scaling
    private double scaling = 1;

    /**
     * Instantiates Input handler class.
     *
     * @param window GLFW window ID
     * @param limitX translation limit for x-axis
     * @param limitY translation limit for y-axis
     * @param limitZ translation limit for z-axis
     */
    public InputHandler(long window, float limitX, float limitY, float limitZ) {
        this.window = window;
        this.limitX = limitX;
        this.limitY = limitY;
        this.limitZ = limitZ;
    }

    /**
     * Updates input handler to get window inputs.
     */
    public void update() {
        updateKeyPress();
        updateMousePos();
        updateScroll();
    }

    // update function for mouse position and button click
    private void updateMousePos() {
        // callback for mouse button click
        GLFW.glfwSetMouseButtonCallback(window, (long windowHandle, int button, int action, int mods) -> {
            if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                leftButtonDown = action == GLFW.GLFW_PRESS;
                // when left button released reset delta x and y to 0
                dx = 0;
                dy = 0;
            }
        });

        // callback for mouse position on screen
        GLFW.glfwSetCursorPosCallback(window, (long windowHandle, double x, double y) -> {
            if (leftButtonDown) {
                // delta is difference between current mouse position and mouse position from last frame
                dx = (float) x - lastMouseX;
                dy = (float) y - lastMouseY;
            }

            // stores current mouse position for next frame
            lastMouseX = (float) x;
            lastMouseY = (float) y;
        });
    }

    // update function for keyboard inputs
    private void updateKeyPress() {
        GLFW.glfwSetKeyCallback(window, new GLFWKeyCallback() {
            @Override
            public void invoke(long window, int key, int scancode, int action, int mods) {
                if (action == GLFW.GLFW_PRESS) {
                    switch (key) {
                        // ESC key closes window
                        case GLFW.GLFW_KEY_ESCAPE -> GLFW.glfwSetWindowShouldClose(window, true);
                        // R key resets translation, rotation and scaling of mesh
                        case GLFW.GLFW_KEY_R -> {
                            transX = 0;
                            transY = 0;
                            transZ = 0;
                            dx = 0;
                            dy = 0;
                            scaling = 1;
                        }
                        // W key toggles wireframe render of model
                        case GLFW.GLFW_KEY_W -> WindowView.renderWireframe = !WindowView.renderWireframe;
                        // LEFT key translates mesh right (simulates camera moving left)
                        case GLFW.GLFW_KEY_LEFT -> {
                            if (transX + moveRate <= limitX) transX += moveRate;
                        }
                        // RIGHT key translates mesh left (simulates camera moving right)
                        case GLFW.GLFW_KEY_RIGHT -> {
                            if (transX - moveRate >= -limitX) transX -= moveRate;
                        }
                        // DOWN key translates mesh up (simulates camera moving down)
                        case GLFW.GLFW_KEY_DOWN -> {
                            if (transY + moveRate <= limitY) transY += moveRate;
                        }
                        // UP key translates mesh down (simulates camera moving up)
                        case GLFW.GLFW_KEY_UP -> {
                            if (transY - moveRate >= -limitY) transY -= moveRate;
                        }
                        // PAGE DOWN key translates mesh forwards (simulates camera moving backwards)
                        case GLFW.GLFW_KEY_PAGE_DOWN -> {
                            if (transZ + moveRate <= limitZ * 2) transZ += moveRate;
                        }
                        // PAGE UP key translate mesh backwards (simulates camera moving forwards)
                        case GLFW.GLFW_KEY_PAGE_UP -> {
                            if (transZ - moveRate >= -limitZ) transZ -= moveRate;
                        }
                        // 1, 2, 3 keys enables auto rotation for x, y, z axes
                        case GLFW.GLFW_KEY_1 -> WindowView.autoRotateX = !WindowView.autoRotateX;
                        case GLFW.GLFW_KEY_2 -> WindowView.autoRotateY = !WindowView.autoRotateY;
                        case GLFW.GLFW_KEY_3 -> WindowView.autoRotateZ = !WindowView.autoRotateZ;
                    }
                    // sets rButtonDown to true if R key pressed
                    rButtonDown = key == GLFW.GLFW_KEY_R;
                }
            }
        });
    }

    // update function for scroll movement
    private void updateScroll() {
        GLFW.glfwSetScrollCallback(window, new GLFWScrollCallback() {
            @Override
            public void invoke(long window, double offsetX, double offsetY) {
                // offsetY is positive when scrolled forward and negative when scrolled backwards
                if (offsetY > 0) {
                    // increases the scale value up to scaling limit
                    if ((scaling + scaleRate) <= scaleLimitHigher) scaling += scaleRate;
                    else scaling = scaleLimitHigher;
                } else if (offsetY < 0) {
                    // decreases the scale value down to scaling limit
                    if ((scaling - scaleRate) >= scaleLimitLower) scaling -= scaleRate;
                    else scaling = scaleLimitLower;
                }
            }
        });
    }

    /**
     * Returns translation for x-axis.
     *
     * @return translation for x-axis
     */
    public int getTransX() {
        return transX;
    }

    /**
     * Returns translation for y-axis.
     *
     * @return translation for y-axis
     */
    public int getTransY() {
        return transY;
    }

    /**
     * Returns translation for z-axis.
     *
     * @return translation for z-axis
     */
    public int getTransZ() {
        return transZ;
    }

    /**
     * Returns true if LEFT button is pressed.
     *
     * @return boolean for LEFT button
     */
    public boolean getLeftButtonDown() {
        return leftButtonDown;
    }

    /**
     * Returns true if R button is pressed.
     *
     * @return boolean for R button
     */
    public boolean getRButtonDown() {
        return rButtonDown;
    }

    /**
     * Resets value for rButtonDown to false.
     */
    public void resetRButtonDown() {
        rButtonDown = false;
    }

    /**
     * Returns rotation for x-axis.
     *
     * @return rotation for x-axis
     */
    public float getRotX() {
        return dx;
    }

    /**
     * Returns rotation for y-axis.
     *
     * @return rotation for y-axis
     */
    public float getRotY() {
        return dy;
    }

    /**
     * Resets rotation deltas.
     */
    public void resetRot() {
        dx = 0;
        dy = 0;
    }

    /**
     * Returns scale value.
     *
     * @return float scaling value
     */
    public float getScaling() {
        return (float) scaling;
    }
}
