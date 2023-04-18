package data;

import javax.imageio.ImageIO;
import java.awt.desktop.SystemSleepEvent;
import java.awt.image.BufferedImage;
import java.io.*;

public class DataLoader {

    public static class Vertex{
        public float x;
        public float y;
        public float z;

        public Vertex(float x, float y, float z){
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    public static class Data{
        Vertex pos;
        float val;

        public Data(Vertex pos, float val){
            this.pos = pos;
            this.val = val;
        }
    }

    private static Data[][][] data;

    private static File directory;
    private static File[] slices;

    public static Data[][][] getData(String dir) throws IOException {
        directory = new File(System.getProperty("user.dir") + File.separator + dir);
        slices = getSlices();
        generateData();

        return data;
    }

    private static File[] getSlices(){
        FilenameFilter filter = (dir, name) -> name.toLowerCase().endsWith(".png");

        return directory.listFiles(filter);
    }

    private static void generateData() throws IOException {
        int height = slices.length;
        BufferedImage tempImage = ImageIO.read(slices[0]);
        int width = tempImage.getWidth();
        int depth = tempImage.getHeight();
        System.out.printf("Width: %d | Height: %d | Depth: %d%n", width, height, depth);

        data = new Data[width][height][depth];

        for (int y = 0; y < slices.length; y++){
            BufferedImage image = ImageIO.read(slices[y]);

            for (int x = 0; x < width; x++){
                for (int z = 0; z < depth; z++){
                    int clr = image.getRGB(x, z);
                    int r = (clr & 0x00ff0000) >> 16;
                    int g = (clr & 0x0000ff00) >> 8;
                    int b = clr & 0x000000ff;

                    data[x][y][z] = new Data(
                            new Vertex(x, y, z),
                            ((float)(r + g + b) / 3) / 255
                    );
                }
            }
        }
    }
}
