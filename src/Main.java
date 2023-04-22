import data.DataLoader;
import data.MarchingCubes;
import renderEngine.WindowView;

import java.io.File;
import java.util.Objects;
import java.util.Scanner;

/**
 * Copyright 2023, Elia Pym
 * <h1>Marching Cubes implementation using OpenGL and LWJGL to visualise medical CT scan data in 3D space.</h1>
 * <p>
 * Final Year Project Dissertation at Swansea University (Bsc Computer Science).<br>
 * See <a href="https://github.com/EliaPym/Marching-Cubes-v4.0-Test-Build">Marching Cubes v4.0 Test Build Repository</a>
 * for executable .JAR file of application.
 * </p>
 *
 * @author Elia Pym
 * @version v4.0
 * @since 2023 -04-22
 */
public class Main {
    private static int windowWidth = 640;
    private static int windowHeight = 480;
    private static boolean windowFullscreen = false;
    private static String windowTitle;
    private static String dir;
    private static float isoLevel = 0.1f;
    private static boolean col = false;

    /**
     * The entry point of application.
     *
     * @param args the input arguments
     * @throws Exception Exception thrown if data can't be read from Data Loader class
     */
    public static void main(String[] args) throws Exception {
        userInput();

        // read data from directory
        DataLoader.Data[][][] data = DataLoader.getData(dir);

        // set parameters for Marching Cubes class
        MarchingCubes.isoLevel = isoLevel;
        MarchingCubes.enableColours = col;
        // generate vertices of mesh from the data array
        MarchingCubes.generateVertices(data);

        // gets data from Marching Cubes class
        float[] vertices = MarchingCubes.getVertices();
        int[] indices = MarchingCubes.getIndices();
        float[] normals = MarchingCubes.getNormals();
        float[] colours = MarchingCubes.getColours();

        // creates the GLFW window object
        WindowView window = new WindowView(windowWidth, windowHeight, windowFullscreen, windowTitle);
        // sets data in window view class
        window.data(vertices, indices, normals, colours);
        window.setPos(MarchingCubes.getWidth(), MarchingCubes.getHeight(), MarchingCubes.getDepth());
        // run window
        window.run();
    }

    // gets inputs from user to set up application
    private static void userInput() {
        Scanner in = new Scanner(System.in);
        int inInt;
        float inFlt;
        String inStr;

        // source directory where image slices are
        System.out.print("Enter source directory: ");
        inStr = in.next();
        if (Objects.equals(inStr, "0")) dir = System.getProperty("user.dir") + File.separator + "CT_Slices";
        else if (Objects.equals(inStr, "1")) dir = System.getProperty("user.dir") + File.separator + "CT_Bunny";
        else dir = inStr;

        // iso level for Marching Cubes class
        System.out.print("Enter ISO Level: ");
        inFlt = in.nextFloat();
        if (inFlt > 0.0f) isoLevel = inFlt;

        // enable fullscreen for the GLFW window
        System.out.print("Enable fullscreen (y/n): ");
        inStr = in.next();
        if (Objects.equals(inStr, "y")) windowFullscreen = true;
        else if (Objects.equals(inStr, "n")) windowFullscreen = false;

        // enable colours for the mesh
        System.out.print("Enable colours (y/n): ");
        inStr = in.next();
        if (Objects.equals(inStr, "y")) col = true;
        else if (Objects.equals(inStr, "n")) col = false;

        // gets window dimensions if fullscreen is not enabled
        if (!windowFullscreen) {
            System.out.print("Enter window width: ");
            inInt = in.nextInt();
            if (inInt != 0) windowWidth = inInt;

            System.out.print("Enter window height: ");
            inInt = in.nextInt();
            if (inInt != 0) windowHeight = inInt;
        }

        // sets title of the window
        windowTitle = "Marching Cubes v4.0 - " + dir + " - DEMO";
    }
}
