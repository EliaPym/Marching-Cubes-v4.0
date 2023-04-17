import data.DataLoader;
import data.MarchingCubes;
import renderEngine.WindowView;

import java.io.IOException;
import java.util.Arrays;

public class Main {
    public static void main(String[] args) throws IOException {
        WindowView window = new WindowView(640, 480, "Marching Cubes v4.0 - DEMO");

        float[] cubeVertices = {
                -1.0f, -1.0f, 1.0f,   // 0
                1.0f, -1.0f, 1.0f,    // 1
                1.0f,  1.0f,  1.0f,   // 2
                -1.0f,  1.0f,  1.0f,  // 3
                -1.0f, -1.0f, -1.0f,   // 4
                1.0f, -1.0f, -1.0f,    // 5
                1.0f,  1.0f, -1.0f,    // 6
                -1.0f,  1.0f, -1.0f,   // 7
        };

        int[] cubeTriangles = {
                // Front face
                0, 1, 2,
                0, 2, 3,
                // Back face
                7, 5, 4,
                7, 6, 5,
                // Top face
                6, 3, 2,
                3, 6, 7,
                // Bottom face
                5, 1, 0,
                0, 4, 5,
                // Right face
                1, 5, 6,
                6, 2, 1,
                // Left face
                7, 4, 0,
                0, 3, 7
        };

        float[] cubeColours = new float[cubeVertices.length * 3];

        for (int i = 0; i < (cubeColours.length+1) / 9; i++){
            cubeColours[i * 9    ] = 1f; // R
            cubeColours[i * 9 + 1] = 0f; // G
            cubeColours[i * 9 + 2] = 0f; // B
            cubeColours[i * 9 + 3] = 0f; // R
            cubeColours[i * 9 + 4] = 1f; // G
            cubeColours[i * 9 + 5] = 0f; // B
            cubeColours[i * 9 + 6] = 0f; // R
            cubeColours[i * 9 + 7] = 0f; // G
            cubeColours[i * 9 + 8] = 1f; // B
        }

        String dir = "TestSpheres";
        DataLoader.Data[][][] data = DataLoader.getData(dir);

        MarchingCubes.generateVertices(data);
        float[] vertices = MarchingCubes.getVertices();
        int[] indices = MarchingCubes.getIndices();

        float[] colours = new float[vertices.length * 3];

        for (int i = 0; i < (colours.length+1) / 9; i++){
            colours[i * 9    ] = 1f; // R
            colours[i * 9 + 1] = 0f; // G
            colours[i * 9 + 2] = 0f; // B
            colours[i * 9 + 3] = 0f; // R
            colours[i * 9 + 4] = 1f; // G
            colours[i * 9 + 5] = 0f; // B
            colours[i * 9 + 6] = 0f; // R
            colours[i * 9 + 7] = 0f; // G
            colours[i * 9 + 8] = 1f; // B
        }

        //System.out.println(Arrays.toString(vertices));
        //System.out.println(Arrays.toString(indices));

        System.out.printf("Indices: %d%n", indices.length);
        System.out.printf("Vertices: %d%n", vertices.length / 3);
        System.out.printf("Polygons: %d%n", vertices.length / 9);

        WindowView.renderWireframe = false;

        window.data(vertices, indices, colours);
        //window.data(cubeVertices, cubeTriangles, cubeColours);

        window.run();
    }
}
