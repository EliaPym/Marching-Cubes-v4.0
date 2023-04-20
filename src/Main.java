import data.DataLoader;
import data.MarchingCubes;
import renderEngine.WindowView;

public class Main {
    public static void main(String[] args) throws Exception {
        String dir = "CT_Bunny";
        DataLoader.Data[][][] data = DataLoader.getData(dir);

        MarchingCubes.enableColours = true;
        MarchingCubes.generateVertices(data);

        float[] vertices = MarchingCubes.getVertices();
        int[] indices = MarchingCubes.getIndices();
        float[] normals = MarchingCubes.getNormals();
        float [] colours = MarchingCubes.getColours();

        WindowView window = new WindowView(640, 480, "Marching Cubes v4.0 - DEMO");
        window.data(vertices, indices, normals, colours);
        window.setPos(MarchingCubes.getWidth(), MarchingCubes.getHeight(), MarchingCubes.getDepth());
        window.run();
    }
}
