package data;

import java.util.ArrayList;
import java.util.Arrays;

public class MarchingCubes extends DataLoader{
    private static final float isoLevel = 0.9f;

    private static final int[] edgeTable = TriangulationTable.getEdgeTable();
    private static final int[][] triTable = TriangulationTable.getTriTable();

    private static final ArrayList<Float> vertices = new ArrayList<>();
    private static final ArrayList<Integer> indices = new ArrayList<>();

    public static void generateVertices(Data[][][] data){
        int vertexCount = 0;

        for (int x = 0; x < data.length - 1; x++){
            for (int y = 0; y < data[0].length - 1; y++){
                for (int z = 0; z < data[1].length - 1; z++){
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

                        for (int i = 0; triTable[edgeIndex][i] != -1; i++){
                            Vertex v = vertList[triTable[edgeIndex][i]];

                            vertices.add(v.x);
                            vertices.add(v.y);
                            vertices.add(v.z);

                            indices.add(vertexCount);
                            vertexCount++;
                        }

                    } catch(Exception e){
                        e.printStackTrace();
                    }
                }
            }
        }
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

    public static float[] getVertices(){
        float[] arr = new float[vertices.size()];
        for(int i = 0; i < vertices.size(); i++){
            arr[i] = vertices.get(i);
        }
        return arr;
    }

    public static int[] getIndices(){
        int[] arr = new int[indices.size()];
        for(int i = 0; i < indices.size(); i++){
            arr[i] = indices.get(i);
        }
        return arr;
    }
}
