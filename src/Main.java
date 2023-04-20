import data.DataLoader;
import data.MarchingCubes;
import renderEngine.WindowView;

import java.io.File;
import java.util.Objects;
import java.util.Scanner;

public class Main {
    private static int windowWidth = 640;
    private static int windowHeight = 480;
    private static boolean windowFullscreen = false;
    private static String windowTitle;
    private static String dir;
    private static float isoLevel = 0.1f;
    private static boolean col = false;

    public static void main(String[] args) throws Exception {
        userInput();

        DataLoader.Data[][][] data = DataLoader.getData(dir);

        MarchingCubes.isoLevel = isoLevel;
        MarchingCubes.enableColours = col;
        MarchingCubes.generateVertices(data);

        float[] vertices = MarchingCubes.getVertices();
        int[] indices = MarchingCubes.getIndices();
        float[] normals = MarchingCubes.getNormals();
        float [] colours = MarchingCubes.getColours();

        WindowView window = new WindowView(windowWidth, windowHeight, windowFullscreen, windowTitle);
        window.data(vertices, indices, normals, colours);
        window.setPos(MarchingCubes.getWidth(), MarchingCubes.getHeight(), MarchingCubes.getDepth());
        window.run();
    }

    private static void userInput(){
        Scanner in = new Scanner(System.in);
        int inInt;
        float inFlt;
        String inStr;

        System.out.print("Enter source directory: ");
        inStr = in.next();
        if (Objects.equals(inStr, "0")) dir = System.getProperty("user.dir") + File.separator + "CT_Slices";
        else if (Objects.equals(inStr, "1")) dir = System.getProperty("user.dir") + File.separator + "CT_Bunny";
        else dir = inStr;

        System.out.print("Enter ISO Level: ");
        inFlt = in.nextFloat();
        if (inFlt > 0.0f) isoLevel = inFlt;

        System.out.print("Enable fullscreen (y/n): ");
        inStr = in.next();
        if (Objects.equals(inStr, "y")) windowFullscreen = true;
        else if (Objects.equals(inStr, "n")) windowFullscreen = false;

        System.out.print("Enable colours (y/n): ");
        inStr = in.next();
        if (Objects.equals(inStr, "y")) col = true;
        else if (Objects.equals(inStr, "n")) col = false;

        if (!windowFullscreen) {
            System.out.print("Enter window width: ");
            inInt = in.nextInt();
            if (inInt != 0) windowWidth = inInt;

            System.out.print("Enter window height: ");
            inInt = in.nextInt();
            if (inInt != 0) windowHeight = inInt;
        }

        windowTitle = "Marching Cubes v4.0 - " + dir + " - DEMO";
    }
}
