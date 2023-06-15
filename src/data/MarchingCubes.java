package data;

import org.joml.Vector3f;

import java.util.*;

/**
 * Constructs array of vertices, indices, normals and colours from given data.
 */
public class MarchingCubes extends DataLoader {
    private static final int[] edgeTable = TriangulationTable.getEdgeTable();
    private static final int[][] triTable = TriangulationTable.getTriTable();
    private static final ArrayList<Vector3f> normals = new ArrayList<>();

    private static final ArrayList<Triangle> triangles = new ArrayList<>();

    private static final ArrayList<Integer> hashList = new ArrayList<>();
    private static final Hashtable<Integer, Vertex> vertHash = new Hashtable<>();
    private static final Hashtable<Integer, Integer> indHash = new Hashtable<>();

    /**
     * Brightness threshold of image.
     */
    public static float isoLevel = 0.1f;
    /**
     * Enable colours for vertices, else default to white.
     */
    public static boolean enableColours = false;
    private static Data[][][] data;
    private static float max_x = 0, max_y = 0, max_z = 0;

    /**
     * Generate array of vertices.
     *
     * @param in_data 3D data array to construct vertices of mesh.
     */
    public static void generateVertices(Data[][][] in_data) {
        data = in_data;
        normaliseVertices();

        int vertexCount = 0;

        for (int x = 0; x < data.length - 1; x++) {
            for (int y = 0; y < data[0].length - 1; y++) {
                for (int z = 0; z < data[0][0].length - 1; z++) {
                    try {
                        int edgeIndex = 0;
                        Vector3f[] vertList = new Vector3f[12];
                        Arrays.fill(vertList, new Vector3f(0, 0, 0));

                        // defines each vertex of a cube to march over
                        Data dp0, dp1, dp2, dp3, dp4, dp5, dp6, dp7;
                        dp0 = data[x    ][y    ][z    ];
                        dp1 = data[x    ][y    ][z + 1];
                        dp2 = data[x + 1][y    ][z + 1];
                        dp3 = data[x + 1][y    ][z    ];
                        dp4 = data[x    ][y + 1][z    ];
                        dp5 = data[x    ][y + 1][z + 1];
                        dp6 = data[x + 1][y + 1][z + 1];
                        dp7 = data[x + 1][y + 1][z    ];

                        // checks each vertex for value above ISO threshold
                        // edge index represents values of each vertex of cube
                        // marked as 'true' if above the threshold
                        if (dp0.val < isoLevel) edgeIndex += 1;
                        if (dp1.val < isoLevel) edgeIndex += 2;
                        if (dp2.val < isoLevel) edgeIndex += 4;
                        if (dp3.val < isoLevel) edgeIndex += 8;
                        if (dp4.val < isoLevel) edgeIndex += 16;
                        if (dp5.val < isoLevel) edgeIndex += 32;
                        if (dp6.val < isoLevel) edgeIndex += 64;
                        if (dp7.val < isoLevel) edgeIndex += 128;

                        // edge index can be represented as an 8-bit binary value
                        // for each vertex that is represented as 'true' in the edge index,
                        // find the corresponding value in the precomputed edge table and apply interpolation with connecting vertex
                        // stores result of the interpolation in vertex list array
                        if ((edgeTable[edgeIndex] & 1   ) == 1   ) vertList[ 0] = VertexInterpolation(dp0, dp1);
                        if ((edgeTable[edgeIndex] & 2   ) == 2   ) vertList[ 1] = VertexInterpolation(dp1, dp2);
                        if ((edgeTable[edgeIndex] & 4   ) == 4   ) vertList[ 2] = VertexInterpolation(dp2, dp3);
                        if ((edgeTable[edgeIndex] & 8   ) == 8   ) vertList[ 3] = VertexInterpolation(dp3, dp0);
                        if ((edgeTable[edgeIndex] & 16  ) == 16  ) vertList[ 4] = VertexInterpolation(dp4, dp5);
                        if ((edgeTable[edgeIndex] & 32  ) == 32  ) vertList[ 5] = VertexInterpolation(dp5, dp6);
                        if ((edgeTable[edgeIndex] & 64  ) == 64  ) vertList[ 6] = VertexInterpolation(dp6, dp7);
                        if ((edgeTable[edgeIndex] & 128 ) == 128 ) vertList[ 7] = VertexInterpolation(dp7, dp4);
                        if ((edgeTable[edgeIndex] & 256 ) == 256 ) vertList[ 8] = VertexInterpolation(dp0, dp4);
                        if ((edgeTable[edgeIndex] & 512 ) == 512 ) vertList[ 9] = VertexInterpolation(dp1, dp5);
                        if ((edgeTable[edgeIndex] & 1024) == 1024) vertList[10] = VertexInterpolation(dp2, dp6);
                        if ((edgeTable[edgeIndex] & 2048) == 2048) vertList[11] = VertexInterpolation(dp3, dp7);

                        ArrayList<Integer> indexList = new ArrayList<>();
                        // iterate over the precomputed triangulation table at index of the edge index until a '-1' is found
                        for (int i = 0; triTable[edgeIndex][i] != -1; i++) {
                            Vector3f v = vertList[triTable[edgeIndex][i]];
                            Vertex vertex = new Vertex(new Vector3f(v.x, v.y, v.z));
                            vertex.colour = assignColours(x, y, z);

                            int hash = vertex.pos.toString().hashCode();
                            indexList.add(hash);

                            if (indHash.get(hash) == null) {
                                vertHash.put(hash, vertex);
                                indHash.put(hash, vertexCount);
                                hashList.add(hash);
                                vertexCount++;
                            }

                            if ((i + 1) % 3 == 0) {
                                Triangle t = new Triangle(
                                        indHash.get(indexList.get(0)),
                                        indHash.get(indexList.get(1)),
                                        indHash.get(indexList.get(2)));
                                t.normal = calculateTriangleNormals(
                                        vertHash.get(indexList.get(0)),
                                        vertHash.get(indexList.get(1)),
                                        vertHash.get(indexList.get(2)));
                                triangles.add(t);
                                indexList.clear();
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        calculateVertexNormals();
    }

    // interpolates position between vertex pair based off values
    private static Vector3f VertexInterpolation(Data p1, Data p2) {
        if (p2.val < p1.val) {
            Data temp = p1;
            p1 = p2;
            p2 = temp;
        }

        Vector3f p;
        if (Math.abs(p1.val - p2.val) > 0.00001) {
            float px = p1.pos.x + (p2.pos.x - p1.pos.x) / (p2.val - p1.val) * (isoLevel - p1.val);
            float py = p1.pos.y + (p2.pos.y - p1.pos.y) / (p2.val - p1.val) * (isoLevel - p1.val);
            float pz = p1.pos.z + (p2.pos.z - p1.pos.z) / (p2.val - p1.val) * (isoLevel - p1.val);

            p = new Vector3f(px, py, -pz);
        } else {
            p = new Vector3f(p1.pos.x, p1.pos.y, p1.pos.z);
        }

        return p;
    }

    // normalise vertices to centre around origin
    private static void normaliseVertices() {
        // calculate maximum x, y, z positions
        for (Data[][] dataX : data) {
            for (Data[] dataXY : dataX) {
                for (Data dataXYZ : dataXY) {
                    if (dataXYZ.pos.x > max_x) max_x = dataXYZ.pos.x;
                    if (dataXYZ.pos.y > max_y) max_y = dataXYZ.pos.y;
                    if (dataXYZ.pos.z > max_z) max_z = dataXYZ.pos.z;
                }
            }
        }
        System.out.printf("MAX_X: %f | MAX_Y: %f | MAX_Z: %f%n", max_x, max_y, max_z);

        // translate each vertex by half the negative maximum value
        for (Data[][] dataX : data) {
            for (Data[] dataXY : dataX) {
                for (Data dataXYZ : dataXY) {
                    dataXYZ.pos.x = dataXYZ.pos.x - (max_x / 2);
                    dataXYZ.pos.y = dataXYZ.pos.y - (max_y / 2);
                    dataXYZ.pos.z = dataXYZ.pos.z - (max_z / 2);
                }
            }
        }
    }

    // calculate normals of each vertex
    private static Vector3f calculateTriangleNormals(Vertex vertex1, Vertex vertex2, Vertex vertex3) {
        Vector3f v1 = new Vector3f(vertex1.pos.x, vertex1.pos.y, vertex1.pos.z);
        Vector3f v2 = new Vector3f(vertex2.pos.x, vertex2.pos.y, vertex2.pos.z);
        Vector3f v3 = new Vector3f(vertex3.pos.x, vertex3.pos.y, vertex3.pos.z);

        Vector3f e1 = v2.sub(v1);
        Vector3f e2 = v3.sub(v1);

        return (e1.cross(e2)).normalize();
    }

    private static void calculateVertexNormals() {
        HashMap<Integer, List<Triangle>> vertexTriangles = new HashMap<>();

        for (Triangle t : triangles) {
            addToVertexTriangles(vertexTriangles, t.v1, t);
            addToVertexTriangles(vertexTriangles, t.v2, t);
            addToVertexTriangles(vertexTriangles, t.v3, t);
        }

        for (Integer h : hashList) {
            Vector3f sum = new Vector3f();
            int count = 0;
            List<Triangle> vTriList = vertexTriangles.get(indHash.get(h));
            if (vTriList != null) {
                for (Triangle t : vTriList) {
                    sum = sum.add(t.normal);
                    count++;
                }
            }
            normals.add((sum.div(count)).normalize());
        }
    }

    private static void addToVertexTriangles(
            HashMap<Integer, List<Triangle>> vertexTriangles,
            int index,
            Triangle t) {
        List<Triangle> triList = vertexTriangles.computeIfAbsent(index, k -> new ArrayList<>());
        triList.add(t);
    }

    // assign colours to each vertex
    private static Vector3f assignColours(int x, int y, int z) {
        float r, g, b;
        if (enableColours) {
            r = (float) x / data.length;
            g = (float) y / data[0].length;
            b = (float) z / data[0][0].length;
        } else {
            r = 0.6f;
            g = 0.6f;
            b = 0.6f;
        }
        return new Vector3f(r, g, b);
    }

    /**
     * Returns float array of vertices.
     *
     * @return float array of vertices
     */
    public static float[] getVertices() {
        float[] arr = new float[hashList.size() * 3];
        for (int i = 0; i < hashList.size(); i++) {
            arr[i * 3    ] = vertHash.get(hashList.get(i)).pos.x;
            arr[i * 3 + 1] = vertHash.get(hashList.get(i)).pos.y;
            arr[i * 3 + 2] = vertHash.get(hashList.get(i)).pos.z - 2;
        }
        return arr;
    }

    /**
     * Returns integer array of indices.
     *
     * @return integer array of indices
     */
    public static int[] getIndices() {
        int[] arr = new int[triangles.size() * 3];
        for (int i = 0; i < triangles.size(); i++) {
            arr[i * 3    ] = triangles.get(i).v3;
            arr[i * 3 + 1] = triangles.get(i).v2;
            arr[i * 3 + 2] = triangles.get(i).v1;
        }
        return arr;
    }

    /**
     * Returns float array of normals.
     *
     * @return float array of normals
     */
    public static float[] getNormals() {
        float[] arr = new float[normals.size() * 3];
        for (int i = 0; i < normals.size(); i++) {
            arr[i * 3] = normals.get(i).x;
            arr[i * 3 + 1] = normals.get(i).y;
            arr[i * 3 + 2] = normals.get(i).z;
        }
        return arr;
    }

    /**
     * Returns float array of colours.
     *
     * @return float array of colours
     */
    public static float[] getColours() {
        float[] arr = new float[hashList.size() * 3];
        for (int i = 0; i < hashList.size(); i++) {
            arr[i * 3] = vertHash.get(hashList.get(i)).colour.x;
            arr[i * 3 + 1] = vertHash.get(hashList.get(i)).colour.y;
            arr[i * 3 + 2] = vertHash.get(hashList.get(i)).colour.z;
        }
        return arr;
    }

    /**
     * Returns width of mesh.
     *
     * @return width width
     */
    public static int getWidth() {
        return data.length;
    }

    /**
     * Returns height of mesh.
     *
     * @return height height
     */
    public static int getHeight() {
        return data[0].length;
    }

    /**
     * Gets depth of mesh.
     *
     * @return depth depth
     */
    public static int getDepth() {
        return data[0][0].length;
    }
}
