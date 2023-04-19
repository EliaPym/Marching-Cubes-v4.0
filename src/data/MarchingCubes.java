package data;

import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Arrays;

public class MarchingCubes extends DataLoader{
    private static final float isoLevel = 0.1f;
    public static final boolean enableColours = false;

    private static final int[] edgeTable = TriangulationTable.getEdgeTable();
    private static final int[][] triTable = TriangulationTable.getTriTable();

    private static final ArrayList<Float> vertices = new ArrayList<>();
    private static final ArrayList<Integer> indices = new ArrayList<>();
    private static final ArrayList<Float> normals = new ArrayList<>();
    private static float[] colours;

    private static Data[][][] data;

    public static void generateVertices(Data[][][] in_data){
        data = in_data;
        normaliseVertices();

        int vertexCount = 0;

        for (int x = 0; x < data.length - 1; x++){
            for (int y = 0; y < data[0].length - 1; y++){
                for (int z = 0; z < data[0][0].length - 1; z++){
                    try{
                        int edgeIndex = 0;
                        Vertex[] vertList = new Vertex[12];
                        Arrays.fill(vertList, new Vertex(0, 0, 0));

                        Data dp0, dp1, dp2, dp3, dp4, dp5, dp6, dp7;
                        dp0 = data[x    ][y    ][z    ];
                        dp1 = data[x    ][y    ][z + 1];
                        dp2 = data[x + 1][y    ][z + 1];
                        dp3 = data[x + 1][y    ][z    ];
                        dp4 = data[x    ][y + 1][z    ];
                        dp5 = data[x    ][y + 1][z + 1];
                        dp6 = data[x + 1][y + 1][z + 1];
                        dp7 = data[x + 1][y + 1][z    ];

                        if (dp0.val < isoLevel) { edgeIndex +=   1; }
                        if (dp1.val < isoLevel) { edgeIndex +=   2; }
                        if (dp2.val < isoLevel) { edgeIndex +=   4; }
                        if (dp3.val < isoLevel) { edgeIndex +=   8; }
                        if (dp4.val < isoLevel) { edgeIndex +=  16; }
                        if (dp5.val < isoLevel) { edgeIndex +=  32; }
                        if (dp6.val < isoLevel) { edgeIndex +=  64; }
                        if (dp7.val < isoLevel) { edgeIndex += 128; }

                        if ((edgeTable[edgeIndex] & 1) == 1){
                            vertList[0] = VertexInterpolation(dp0, dp1);
                        }
                        if ((edgeTable[edgeIndex] & 2) == 2){
                            vertList[1] = VertexInterpolation(dp1, dp2);
                        }
                        if ((edgeTable[edgeIndex] & 4) == 4){
                            vertList[2] = VertexInterpolation(dp2, dp3);
                        }
                        if ((edgeTable[edgeIndex] & 8) == 8){
                            vertList[3] = VertexInterpolation(dp3, dp0);
                        }
                        if ((edgeTable[edgeIndex] & 16) == 16){
                            vertList[4] = VertexInterpolation(dp4, dp5);
                        }
                        if ((edgeTable[edgeIndex] & 32) == 32){
                            vertList[5] = VertexInterpolation(dp5, dp6);
                        }
                        if ((edgeTable[edgeIndex] & 64) == 64){
                            vertList[6] = VertexInterpolation(dp6, dp7);
                        }
                        if ((edgeTable[edgeIndex] & 128) == 128){
                            vertList[7] = VertexInterpolation(dp7, dp4);
                        }
                        if ((edgeTable[edgeIndex] & 256) == 256){
                            vertList[8] = VertexInterpolation(dp0, dp4);
                        }
                        if ((edgeTable[edgeIndex] & 512) == 512){
                            vertList[9] = VertexInterpolation(dp1, dp5);
                        }
                        if ((edgeTable[edgeIndex] & 1024) == 1024){
                            vertList[10] = VertexInterpolation(dp2, dp6);
                        }
                        if ((edgeTable[edgeIndex] & 2048) == 2048){
                            vertList[11] = VertexInterpolation(dp3, dp7);
                        }

                        int triCount = 0;
                        for (int i = 0; triTable[edgeIndex][i] != -1; i++){
                            Vertex v = vertList[triTable[edgeIndex][i]];

                            vertices.add(v.x);
                            vertices.add(v.y);
                            vertices.add(v.z);

                            triCount++;

                            if (triCount == 3){
                                indices.add(vertexCount + 2);
                                indices.add(vertexCount + 1);
                                indices.add(vertexCount);

                                vertexCount += 3;
                                triCount = 0;
                            }
                        }

                    } catch(Exception e){
                        e.printStackTrace();
                    }
                }
            }
        }
        calculateNormals();
    }

    private static Vertex VertexInterpolation(Data p1, Data p2){
        double mu;

        if (Math.abs(isoLevel - p1.val) < 0.00001){
            return p1.pos;
        }
        if (Math.abs(isoLevel - p2.val) < 0.00001){
            return p2.pos;
        }
        if (Math.abs(p1.val - p2.val) < 0.00001){
            return p1.pos;
        }

        mu = (isoLevel - p1.val) / (p2.val - p1.val);

        float x = (float)(p1.pos.x + mu * (p2.pos.x - p1.pos.x));
        float y = (float)(p1.pos.y + mu * (p2.pos.y - p1.pos.y));
        float z = -(float)(p1.pos.z + mu * (p2.pos.z - p1.pos.z));

        return new Vertex(x, y, z);
    }

    private static void normaliseVertices(){
        float max_x = 0, max_y = 0, max_z = 0;

        for (int x = 0; x < data.length; x++){
            for (int y = 0; y < data[0].length; y++){
                for (int z = 0; z < data[0][0].length; z++) {
                    if (data[x][y][z].pos.x > max_x){
                        max_x = data[x][y][z].pos.x;
                    }
                    if (data[x][y][z].pos.y > max_y){
                        max_y = data[x][y][z].pos.y;
                    }
                    if (data[x][y][z].pos.z > max_z){
                        max_z = data[x][y][z].pos.z;
                    }
                }
            }
        }
        System.out.printf("MAX_X: %f | MAX_Y: %f | MAX_Z: %f%n", max_x, max_y, max_z);

        for (int x = 0; x < data.length; x++) {
            for (int y = 0; y < data[0].length; y++) {
                for (int z = 0; z < data[0][0].length; z++) {
                    data[x][y][z].pos.x = data[x][y][z].pos.x - (max_x / 2);
                    data[x][y][z].pos.y = data[x][y][z].pos.y - (max_y / 2);
                    data[x][y][z].pos.z = data[x][y][z].pos.z - (max_z / 2);
                }
            }
        }
    }

    private static void calculateNormals(){
        Vector3f o = new Vector3f(0, 0, 0);
        for (int i = 0; i < (vertices.size() + 1) / 3; i++){
            Vector3f v = new Vector3f(vertices.get(i * 3), vertices.get(i * 3 + 1), vertices.get(i * 3 + 2));

            normals.add(v.x - o.x);
            normals.add(v.y - o.y);
            normals.add(v.z - o.z);
        }
    }

    private static void assignColours(){
        colours = new float[vertices.size() * 3];

        if (enableColours) {
            for (int i = 0; i < (colours.length + 1) / 9; i++) {
                colours[i * 9] = 1f; // R
                colours[i * 9 + 1] = 0f; // G
                colours[i * 9 + 2] = 0f; // B
                colours[i * 9 + 3] = 0f; // R
                colours[i * 9 + 4] = 1f; // G
                colours[i * 9 + 5] = 0f; // B
                colours[i * 9 + 6] = 0f; // R
                colours[i * 9 + 7] = 0f; // G
                colours[i * 9 + 8] = 1f; // B
            }
        } else {
            Arrays.fill(colours, 1.0f);
        }
    }

    public static float[] getVertices(){
        float[] arr = new float[vertices.size()];
        for (int i = 0; i < vertices.size(); i++){
            arr[i] = vertices.get(i);
        }
        return arr;
    }

    public static int[] getIndices(){
        int[] arr = new int[indices.size()];
        for (int i = 0; i < indices.size(); i++){
            arr[i] = indices.get(i);
        }
        return arr;
    }

    public static float[] getNormals(){
        float[] arr = new float[normals.size()];
        for (int i = 0; i < normals.size(); i++){
            arr[i] = normals.get(i);
        }
        return arr;
    }

    public static float[] getColours(){
        return colours;
    }

    public static int getWidth(){
        return data.length;
    }

    public static int getHeight(){
        return data[0].length;
    }

    public static int getDepth(){
        return data[0][0].length;
    }
}
