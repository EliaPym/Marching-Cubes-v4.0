package data;

import org.joml.Vector3f;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

/**
 * Creates 3-dimensional data array from images in given directory.
 */
public class DataLoader {

    private static Data[][][] data;
    private static File directory;
    private static File[] slices;

    /**
     * Generates and returns 3D array of Data objects.
     *
     * @param dir source directory of images
     * @return generated Data array
     * @throws Exception ImageIO.read() exception
     */
    public static Data[][][] getData(String dir) throws Exception {
        directory = new File(dir);
        slices = getSlices();
        if (slices.length > 0) {
            generateData();
        } else {
            System.err.printf("No files found in %s%n", directory.getPath());
        }

        return data;
    }

    // returns list of files located in source directory with ".png" file extension
    private static File[] getSlices() {
        FilenameFilter filter = (dir, name) -> name.toLowerCase().endsWith(".png");

        return directory.listFiles(filter);
    }

    // read through each image slice
    private static void generateData() throws IOException {
        int height = slices.length;
        BufferedImage tempImage = ImageIO.read(slices[0]);
        int width = tempImage.getWidth();
        int depth = tempImage.getHeight();
        System.out.printf("Width: %d | Height: %d | Depth: %d%n", width, height, depth);

        data = new Data[width][height][depth];

        for (int y = 0; y < height; y++) {
            BufferedImage image = ImageIO.read(slices[y]);

            for (int x = 0; x < width; x++) {
                for (int z = 0; z < depth; z++) {
                    // get colour as RGB integer value
                    // bitwise AND comparison to extract red, green, blue values
                    int clr = image.getRGB(x, z);
                    int r = (clr & 0x00ff0000) >> 16;
                    int g = (clr & 0x0000ff00) >> 8;
                    int b = clr & 0x000000ff;

                    data[x][y][z] = new Data(
                            new Vector3f(x, y, z),
                            (float) (r * 0.2126 + g * 0.7152 + b * 0.0722) / 255 // brightness value of pixel
                    );
                }
            }
        }
    }

    /**
     * Vertex class as a 3D vector.
     */
    public static class Vertex {
        public Vector3f pos;

        public Vector3f colour;

        /**
         * Instantiates a new Vertex.
         *

         */
        public Vertex(Vector3f pos) {
            this.pos = pos;
        }
    }

    public static class Triangle {
        int v1;
        int v2;
        int v3;

        Vector3f normal;

        public Triangle(int v1, int v2, int v3){
            this.v1 = v1;
            this.v2 = v2;
            this.v3 = v3;
        }

        public int getV1() {
            return v1;
        }

        public int getV2() {
            return v2;
        }

        public int getV3() {
            return v3;
        }
    }

    /**
     * Data class containing vertex and value.
     */
    public static class Data {
        /**
         * Position of data in 3D space.
         */
        Vector3f pos;
        /**
         * Value of data.
         */
        float val;

        /**
         * Data constructor.
         *
         * @param pos vertex position of data
         * @param val value of data
         */
        public Data(Vector3f pos, float val) {
            this.pos = pos;
            this.val = val;
        }
    }
}
