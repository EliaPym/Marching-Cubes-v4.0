package data;

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
                            new Vertex(x, y, z),
                            (float) (r * 0.2126 + g * 0.7152 + b * 0.0722) / 255 // greyscale value of pixel
                    );
                }
            }
        }
    }

    /**
     * Vertex class as a 3D vector.
     */
    public static class Vertex {
        /**
         * X coordinate of vertex.
         */
        public float x;
        /**
         * Y coordinate of vertex.
         */
        public float y;
        /**
         * Z coordinate of vertex.
         */
        public float z;

        /**
         * Instantiates a new Vertex.
         *
         * @param x x component of position
         * @param y y component of position
         * @param z z component of position
         */
        public Vertex(float x, float y, float z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    /**
     * Data class containing vertex and value.
     */
    public static class Data {
        /**
         * Position of data in 3D space.
         */
        Vertex pos;
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
        public Data(Vertex pos, float val) {
            this.pos = pos;
            this.val = val;
        }
    }
}
