import renderEngine.WindowView;

public class Main {
    public static void main(String[] args){
        WindowView window = new WindowView(640, 480, "Marching Cubes v4.0 - DEMO");

        float[] cubeVertices = {
                -1.0f, -1.0f, -5.0f,  // 0
                1.0f, -1.0f, -5.0f,   // 1
                1.0f,  1.0f,  -5.0f,   // 2
                -1.0f,  1.0f,  -5.0f,  // 3
                -1.0f, -1.0f, -7.0f,  // 4
                1.0f, -1.0f, -7.0f,   // 5
                1.0f,  1.0f, -7.0f,   // 6
                -1.0f,  1.0f, -7.0f,  // 7
        };

        int[] cubeTriangles = {
                // Front face
                0, 1, 2,
                0, 2, 3,
                // Back face
                4, 5, 7,
                5, 6, 7,
                // Top face
                2, 3, 6,
                3, 6, 7,
                // Bottom face
                0, 1, 5,
                0, 4, 5,
                // Right face
                1, 5, 6,
                1, 2, 6,
                // Left face
                0, 4, 7,
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

        window.data(cubeVertices, cubeTriangles, cubeColours);
        window.run();
    }
}
