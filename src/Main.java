import data.DataLoader;
import data.MarchingCubes;
import renderEngine.WindowView;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        WindowView window = new WindowView(640, 480, "Marching Cubes v4.0 - DEMO");

        float[] cubeVertices = {
                -1.0f, -1.0f, 1.0f,   // 0
                1.0f, -1.0f, 1.0f,    // 1
                1.0f, 1.0f, 1.0f,   // 2
                -1.0f, 1.0f, 1.0f,  // 3
                -1.0f, -1.0f, -1.0f,   // 4
                1.0f, -1.0f, -1.0f,    // 5
                1.0f, 1.0f, -1.0f,    // 6
                -1.0f, 1.0f, -1.0f,   // 7
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

        for (int i = 0; i < (cubeColours.length + 1) / 9; i++) {
            cubeColours[i * 9] = 1f; // R
            cubeColours[i * 9 + 1] = 0f; // G
            cubeColours[i * 9 + 2] = 0f; // B
            cubeColours[i * 9 + 3] = 0f; // R
            cubeColours[i * 9 + 4] = 1f; // G
            cubeColours[i * 9 + 5] = 0f; // B
            cubeColours[i * 9 + 6] = 0f; // R
            cubeColours[i * 9 + 7] = 0f; // G
            cubeColours[i * 9 + 8] = 1f; // B
        }

        String dir = "CT_Slices";
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

        //colours = assignColorsToPolygons(vertices);

        //System.out.println(Arrays.toString(vertices));
        //System.out.println(Arrays.toString(indices));

        System.out.printf("Indices: %d%n", indices.length);
        System.out.printf("Vertices: %d%n", vertices.length / 3);
        System.out.printf("Polygons: %d%n", vertices.length / 9);

        WindowView.renderWireframe = false;

        window.data(vertices, indices, colours);
        window.setPos(
                MarchingCubes.getDepth()
        );
        //window.data(cubeVertices, cubeTriangles, cubeColours);

        window.run();
    }

    public static float[] assignColorsToPolygons(float[] meshData) {
        float[] coloredMeshData = new float[meshData.length + 3];
        for (int i = 0; i < meshData.length; i += 9) {
            // Calculate normal vector
            float[] v1 = {meshData[i], meshData[i + 1], meshData[i + 2]};
            float[] v2 = {meshData[i + 3], meshData[i + 4], meshData[i + 5]};
            float[] v3 = {meshData[i + 6], meshData[i + 7], meshData[i + 8]};
            float[] edge1 = {v2[0] - v1[0], v2[1] - v1[1], v2[2] - v1[2]};
            float[] edge2 = {v3[0] - v2[0], v3[1] - v2[1], v3[2] - v2[2]};
            float[] normal = {edge1[1] * edge2[2] - edge1[2] * edge2[1],
                    edge1[2] * edge2[0] - edge1[0] * edge2[2],
                    edge1[0] * edge2[1] - edge1[1] * edge2[0]};
            // Determine which axis the normal is pointing towards
            float absX = Math.abs(normal[0]);
            float absY = Math.abs(normal[1]);
            float absZ = Math.abs(normal[2]);
            if (absX > absY && absX > absZ) {
                coloredMeshData[i] = normal[0] > 0 ? 1 : 0;
                coloredMeshData[i + 1] = absY > 0 ? (normal[1] > 0 ? 1 : 0) : 0;
                coloredMeshData[i + 2] = absZ > 0 ? (normal[2] > 0 ? 1 : 0) : 0;
            } else if (absY > absX && absY > absZ) {
                coloredMeshData[i] = absX > 0 ? (normal[0] > 0 ? 1 : 0) : 0;
                coloredMeshData[i + 1] = normal[1] > 0 ? 1 : 0;
                coloredMeshData[i + 2] = absZ > 0 ? (normal[2] > 0 ? 1 : 0) : 0;
            } else {
                coloredMeshData[i] = absX > 0 ? (normal[0] > 0 ? 1 : 0) : 0;
                coloredMeshData[i + 1] = absY > 0 ? (normal[1] > 0 ? 1 : 0) : 0;
                coloredMeshData[i + 2] = normal[2] > 0 ? 1 : 0;
            }
// Copy the vertex data for the polygon
            coloredMeshData[i + 3] = meshData[i + 3];
            coloredMeshData[i + 4] = meshData[i + 4];
            coloredMeshData[i + 5] = meshData[i + 5];
            coloredMeshData[i + 6] = meshData[i + 6];
            coloredMeshData[i + 7] = meshData[i + 7];
            coloredMeshData[i + 8] = meshData[i + 8];
        }
// Return the colored mesh data
        return coloredMeshData;
    }
}
