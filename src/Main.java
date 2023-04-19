import data.DataLoader;
import data.MarchingCubes;
import renderEngine.WindowView;

public class Main {
    public static void main(String[] args) throws Exception {
        WindowView window = new WindowView(640, 480, "Marching Cubes v4.0 - DEMO");

        String dir = "CT_Slices";
        DataLoader.Data[][][] data = DataLoader.getData(dir);

        MarchingCubes.enableColours = false;
        MarchingCubes.generateVertices(data);

        float[] vertices = MarchingCubes.getVertices();
        int[] indices = MarchingCubes.getIndices();
        float[] normals = MarchingCubes.getNormals();
        float [] colours = MarchingCubes.getColours();

        window.data(vertices, indices, normals, colours);
        window.setPos(MarchingCubes.getWidth(), MarchingCubes.getHeight(), MarchingCubes.getDepth());

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
