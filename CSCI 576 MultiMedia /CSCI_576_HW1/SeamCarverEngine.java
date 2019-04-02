import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.*;

public class SeamCarverEngine {

    private final float[] soebelOne = {1, 2, 1, 0, 0, 0, -1, -2, -1};
    private final ConvolveOp convolveOpsOne = new ConvolveOp(new Kernel(3, 3, soebelOne));
    private final float[] soebelTwo = {-1, 0, 1, -2, 0, 2, -1, 0, 1};
    private final ConvolveOp convolveOpsTwo = new ConvolveOp(new Kernel(3, 3, soebelTwo));
    private final ColorConvertOp greyscale;
    private BufferedImage bufferedImage;
    private BufferedImage energyAddonBufImage = null;
    private BufferedImage energyBufImage = null;
    private float[][][] seamCarveInfo = null;

    public SeamCarverEngine(BufferedImage bufferedImage, BufferedImage energyAddonBufImage) {
        this.bufferedImage = bufferedImage;
        this.energyAddonBufImage = energyAddonBufImage;
        greyscale = new ColorConvertOp(bufferedImage.getColorModel().getColorSpace(), ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
    }

    public SeamCarverEngine(BufferedImage bufferedImage) {
        this.bufferedImage = bufferedImage;
        greyscale = new ColorConvertOp(bufferedImage.getColorModel().getColorSpace(), ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
    }

    private static float[][][] createHorizontalSeamTable(BufferedImage energyImage, float[][][] seamTable) {

        float[] pixelCol = new float[energyImage.getHeight()];
        float[][][] result = seamTable == null ? new float[energyImage.getWidth()][energyImage.getHeight()][2] : seamTable;

        int height = energyImage.getHeight();
        int width = energyImage.getWidth();
        energyImage.getRaster().getPixels(0, 0, 1, height, pixelCol);

        for (int ydx = 0; ydx < height; ydx++) {
            result[0][ydx][0] = pixelCol[ydx];
        }

        float middle;
        float currentPixelEnergy;
        int nextStep;


        float right;
        float left;
        float min;

        for (int xdx = 1; xdx < width; xdx++) {
            energyImage.getRaster().getPixels(xdx, 0, 1, height, pixelCol);
            for (int ydx = 0; ydx < height; ydx++) {
                middle = result[xdx - 1][ydx][0];
                currentPixelEnergy = pixelCol[ydx];
                nextStep = 0;
                if (ydx > 1 && ydx < height - 2) {
                    left = result[xdx - 1][ydx - 1][0];
                    right = result[xdx - 1][ydx + 1][0];
                    nextStep = 0;
                    if (right < middle || left < middle) {
                        if (right <= left) {
                            min = right;
                            nextStep = 1;
                        } else {
                            min = left;
                            nextStep = -1;
                        }
                    } else {
                        min = middle;
                    }
                    currentPixelEnergy = currentPixelEnergy + min;
                } else if (ydx > 1) {
                    right = result[xdx - 1][ydx - 1][0];
                    if (right < middle) {
                        min = right;
                        nextStep = -1;
                    } else {
                        min = middle;
                        nextStep = 0;
                    }
                    currentPixelEnergy = currentPixelEnergy + min;
                } else if (ydx < height - 2) {
                    left = result[xdx - 1][ydx + 1][0];
                    if (left < middle) {
                        min = left;
                        nextStep = 1;
                    } else {
                        min = middle;
                        nextStep = 0;
                    }
                    currentPixelEnergy = currentPixelEnergy + min;
                }
                result[xdx][ydx][0] = currentPixelEnergy;
                result[xdx][ydx][1] = nextStep;
            }
        }
        return result;
    }

    private static float[][][] createVerticalSeamTable(BufferedImage energyImage, float[][][] seamTable) {
        float[] pixelRow = new float[energyImage.getWidth()];
        float[][][] result = seamTable == null ? new float[energyImage.getWidth()][energyImage.getHeight()][2] : seamTable;
        int height = energyImage.getHeight();
        int width = energyImage.getWidth();
        energyImage.getRaster().getPixels(0, 0, energyImage.getWidth(), 1, pixelRow);
        for (int xdx = 0; xdx < width; xdx++) {
            result[xdx][0][0] = pixelRow[xdx];
        }
        float right;
        float left;
        float min;
        float middle;
        float currentEnergy;
        int nextStep;
        for (int ydx = 1; ydx < height; ydx++) {
            energyImage.getRaster().getPixels(0, ydx, energyImage.getWidth(), 1, pixelRow);
            for (int xdx = 0; xdx < width; xdx++) {
                middle = result[xdx][ydx - 1][0];
                currentEnergy = pixelRow[xdx];
                nextStep = 0;
                if (xdx > 1 && xdx < width - 2) {
                    left = result[xdx - 1][ydx - 1][0];
                    right = result[xdx + 1][ydx - 1][0];
                    nextStep = 0;
                    if (right < middle || left < middle) {
                        if (right < left) {
                            min = right;
                            nextStep = 1;
                        } else {
                            min = left;
                            nextStep = -1;
                        }
                    } else {
                        min = middle;
                    }
                    currentEnergy = currentEnergy + min;
                } else if (xdx > 1) {
                    left = result[xdx - 1][ydx - 1][0];
                    if (left < middle) {
                        min = left;
                        nextStep = -1;
                    } else {
                        min = middle;
                        nextStep = 0;
                    }
                    currentEnergy = currentEnergy + min;
                } else if (xdx < width - 2) {
                    // left border of the image, no right possible
                    right = result[xdx + 1][ydx - 1][0];
                    if (right < middle) {
                        min = right;
                        nextStep = 1;
                    } else {
                        min = middle;
                        nextStep = 0;
                    }
                    currentEnergy = currentEnergy + min;
                }
                result[xdx][ydx][0] = currentEnergy;
                result[xdx][ydx][1] = nextStep;
            }
        }
        return result;
    }

    private VerticalSeamCarve findOptimalVerticalSeam() {
        if (energyBufImage == null) {
            energyBufImage = generateEnergyImage();
        }
        float[][][] table = createVerticalSeamTable(energyBufImage, seamCarveInfo);
        float energy = Float.MAX_VALUE;
        int seamstart = 0;
        int height = energyBufImage.getHeight();

        for (int i = 1; i < table.length - 1; i++) {
            if (table[i][height - 1][0] < energy) {
                energy = table[i][height - 1][0];
                seamstart = i;
            }
        }
        return new VerticalSeamCarve(energy, seamstart, table, energyBufImage.getWidth(), energyBufImage.getHeight());
    }

    private HorizontalSeamCarve findOptimalHorizontalSeam() {

        if (energyBufImage == null) energyBufImage = generateEnergyImage();
        float[][][] table = createHorizontalSeamTable(energyBufImage, seamCarveInfo);

        float energy = Float.MAX_VALUE;
        int seamstart = 0;
        int width = energyBufImage.getWidth();
        for (int i = 1; i < table[0].length - 1; i++) {
            if (table[width - 1][i][0] < energy) {
                energy = table[width - 1][i][0];
                seamstart = i;
            }
        }
        return new HorizontalSeamCarve(energy, seamstart, table, energyBufImage.getWidth(), energyBufImage.getHeight());
    }

    public void carveVerticalSeam() {
        VerticalSeamCarve verticalSeamCarve = findOptimalVerticalSeam();
        int height = bufferedImage.getHeight();
        int[] seamLookup = new int[height];
        int nextX = verticalSeamCarve.startX;
        int minX = bufferedImage.getWidth();
        int maxX = 0;
        seamLookup[height - 1] = nextX;
        seamLookup[height - 2] = nextX;
        for (int y = height - 2; y >= 1; y--) {
            nextX += (int) verticalSeamCarve.table[nextX][y][1];
            seamLookup[y - 1] = nextX;
            minX = Math.min(nextX, minX);
            maxX = Math.max(nextX, maxX);
        }
        WritableRaster raster = bufferedImage.getRaster();
        Graphics2D g2 = bufferedImage.createGraphics();
        int[] pixel = new int[3];
        for (int y = 0; y < bufferedImage.getHeight(); y++) {
            for (int x = seamLookup[y]; x <= maxX; x++) {
                raster.getPixel(x + 1, y, pixel);
                raster.setPixel(x, y, pixel);
            }
        }
        if (maxX < bufferedImage.getWidth() - 1) {
            g2.drawImage(bufferedImage, maxX + 1, 0, bufferedImage.getWidth() - 1, bufferedImage.getHeight(), maxX + 2, 0, bufferedImage.getWidth(), bufferedImage.getHeight(), null);
        }
        bufferedImage = bufferedImage.getSubimage(0, 0, bufferedImage.getWidth() - 1, bufferedImage.getHeight());
        carveGradientImageVertical(seamLookup, maxX, raster);


        if (energyAddonBufImage != null) {
            pixel = new int[4];
            raster = energyAddonBufImage.getRaster();
            g2 = energyAddonBufImage.createGraphics();
            for (int y = 0; y < energyAddonBufImage.getHeight(); y++) {
                for (int x = seamLookup[y]; x <= maxX; x++) {
                    raster.getPixel(x + 1, y, pixel);
                    raster.setPixel(x, y, pixel);
                }
            }

            if (maxX < energyAddonBufImage.getWidth() - 1) {
                g2.drawImage(energyAddonBufImage, maxX - 1, 0, energyAddonBufImage.getWidth() - 1, energyAddonBufImage.getHeight(), maxX, 0, energyAddonBufImage.getWidth(), energyAddonBufImage.getHeight(), null);
            }
            energyAddonBufImage = energyAddonBufImage.getSubimage(0, 0, energyAddonBufImage.getWidth() - 1, energyAddonBufImage.getHeight());
        }
    }

    private void carveGradientImageVertical(int[] seamLookup, int maxX, WritableRaster raster) {
        int[] ePixel = new int[1];
        int[] pixel = new int[3];
        Graphics2D graphics2D = energyBufImage.createGraphics();
        WritableRaster eRaster = energyBufImage.getRaster();
        for (int ydx = 0; ydx < energyBufImage.getHeight(); ydx++) {
            for (int xdx = seamLookup[ydx]; xdx <= maxX; xdx++) {
                eRaster.getPixel(xdx + 1, ydx, ePixel);
                eRaster.setPixel(xdx, ydx, ePixel);
            }
        }
        if (maxX < energyBufImage.getWidth() - 1) {
            graphics2D.drawImage(energyBufImage, maxX - 1, 0, energyBufImage.getWidth() - 1, energyBufImage.getHeight(), maxX, 0, energyBufImage.getWidth(), energyBufImage.getHeight(), null);
        }
        energyBufImage = energyBufImage.getSubimage(0, 0, energyBufImage.getWidth() - 1, energyBufImage.getHeight());

        eRaster = energyBufImage.getRaster();
        for (int yindex = 0; yindex < energyBufImage.getHeight(); yindex++) {
            for (int xIndex = seamLookup[yindex] - 1; xIndex <= seamLookup[yindex] + 1; xIndex++) {
                int greyone = 0;
                int greyTwo = 0;
                int grey;
                for (int gx = 0; gx <= 2; gx++) {
                    for (int gy = 0; gy <= 2; gy++) {
                        int xCordinate = xIndex + (gx - 1);
                        int yCodedinates = yindex + (gy - 1);
                        if (xCordinate < 0) {
                            xCordinate = 0;
                        }
                        if (xCordinate >= bufferedImage.getWidth()) {
                            xCordinate = bufferedImage.getWidth() - 1;
                        }
                        if (yCodedinates < 0) {
                            yCodedinates = 0;
                        }
                        if (yCodedinates >= bufferedImage.getHeight()) {
                            yCodedinates = bufferedImage.getHeight() - 1;
                        }
                        raster.getPixel(xCordinate, yCodedinates, pixel);
                        grey = (int) (0.299 * pixel[0] + 0.587 * pixel[1] + 0.114 * pixel[2]);
                        greyone += soebelOne[gx + 3 * gy] * grey;
                        greyTwo += soebelTwo[gx + 3 * gy] * grey;
                    }
                }
                pixel[0] = (Math.abs(greyone) + Math.abs(greyTwo)) >> 1;
                eRaster.setPixel(xIndex, yindex, pixel);
            }
        }
    }

    private void carveGradientImageHorizontal(int[] seamLookup, int maxY, WritableRaster raster) {
        int[] ePixel = new int[1];
        int[] pixel = new int[3];
        Graphics2D graphics2D = energyBufImage.createGraphics();
        WritableRaster eRaster = energyBufImage.getRaster();
        for (int xIndex = 0; xIndex < energyBufImage.getWidth(); xIndex++) {
            for (int yIndex = seamLookup[xIndex]; yIndex <= maxY; yIndex++) {
                eRaster.getPixel(xIndex, yIndex + 1, ePixel);
                eRaster.setPixel(xIndex, yIndex, ePixel);
            }
        }
        if (maxY < energyBufImage.getHeight() - 1) {
            graphics2D.drawImage(energyBufImage, 0, maxY - 1, energyBufImage.getWidth(), energyBufImage.getHeight() - 1, 0, maxY, energyBufImage.getWidth(), energyBufImage.getHeight(), null);
        }
        energyBufImage = energyBufImage.getSubimage(0, 0, energyBufImage.getWidth(), energyBufImage.getHeight() - 1);

        eRaster = energyBufImage.getRaster();
        for (int xIndex = 0; xIndex < energyBufImage.getWidth(); xIndex++) {
            for (int yIndex = seamLookup[xIndex] - 1; yIndex <= seamLookup[xIndex] + 1; yIndex++) {
                int gr1 = 0, gr2 = 0, grey;
                for (int gxIndex = 0; gxIndex <= 2; gxIndex++) {
                    for (int gyIndex = 0; gyIndex <= 2; gyIndex++) {
                        int xCordinate = xIndex + (gxIndex - 1);
                        int yCordinate = yIndex + (gyIndex - 1);
                        if (xCordinate < 0) {
                            xCordinate = 0;
                        }
                        if (xCordinate >= bufferedImage.getWidth()) {
                            xCordinate = bufferedImage.getWidth() - 1;
                        }
                        if (yCordinate < 0) {
                            yCordinate = 0;
                        }
                        if (yCordinate >= bufferedImage.getHeight()) {
                            yCordinate = bufferedImage.getHeight() - 1;
                        }
                        raster.getPixel(xCordinate, yCordinate, pixel);
                        grey = (int) (0.299 * pixel[0] + 0.587 * pixel[1] + 0.114 * pixel[2]);
                        gr1 += soebelOne[gxIndex + 3 * gyIndex] * grey;
                        gr2 += soebelTwo[gxIndex + 3 * gyIndex] * grey;
                    }
                }
                pixel[0] = (Math.abs(gr1) + Math.abs(gr2)) >> 1;
                eRaster.setPixel(xIndex, yIndex, pixel);
            }
        }
    }

    public void carveHorizontalSeam() {
        HorizontalSeamCarve horizontalSeamCarve = findOptimalHorizontalSeam();
        int width = bufferedImage.getWidth();
        int[] seamLookup = new int[width];
        int nextY = horizontalSeamCarve.startY;
        int minY = bufferedImage.getHeight() - 1;
        int maxY = 0;
        seamLookup[width - 1] = nextY;
        seamLookup[width - 2] = nextY;
        for (int xIndex = width - 2; xIndex >= 1; xIndex--) {
            nextY += (int) horizontalSeamCarve.table[xIndex][nextY][1];
            seamLookup[xIndex - 1] = nextY;
            minY = Math.min(nextY, minY);
            maxY = Math.max(nextY, maxY);
        }
        WritableRaster raster = bufferedImage.getRaster();
        Graphics2D graphics2D = bufferedImage.createGraphics();
        int[] pixel = new int[3];
        for (int xIndex = 0; xIndex < raster.getWidth(); xIndex++) {
            for (int y = seamLookup[xIndex]; y <= maxY; y++) {
                raster.getPixel(xIndex, y + 1, pixel);
                raster.setPixel(xIndex, y, pixel);
            }
        }

        if (maxY < bufferedImage.getHeight() - 1) {
            graphics2D.drawImage(bufferedImage, 0, maxY + 1, bufferedImage.getWidth(), bufferedImage.getHeight() - 1, 0, maxY + 2, bufferedImage.getWidth(), bufferedImage.getHeight(), null);
        }
        bufferedImage = bufferedImage.getSubimage(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight() - 1);

        carveGradientImageHorizontal(seamLookup, maxY, raster);

        if (energyAddonBufImage != null) {
            raster = energyAddonBufImage.getRaster();
            pixel = new int[4];
            graphics2D = energyAddonBufImage.createGraphics();

            for (int xIndex = 0; xIndex < raster.getWidth(); xIndex++) {
                for (int yIndex = seamLookup[xIndex]; yIndex <= maxY; yIndex++) {
                    raster.getPixel(xIndex, yIndex + 1, pixel);
                    raster.setPixel(xIndex, yIndex, pixel);
                }
            }

            if (maxY < energyAddonBufImage.getHeight() - 1) {
                graphics2D.drawImage(energyAddonBufImage, 0, maxY + 1, energyAddonBufImage.getWidth(), energyAddonBufImage.getHeight() - 1, 0, maxY + 2, energyAddonBufImage.getWidth(), energyAddonBufImage.getHeight(), null);
            }
            energyAddonBufImage = energyAddonBufImage.getSubimage(0, 0, energyAddonBufImage.getWidth(), energyAddonBufImage.getHeight() - 1);

        }
    }

    private BufferedImage generateEnergyImage() {
        return getGradientImage();
    }

    private BufferedImage getGradientImage() {

        BufferedImage img_grey = greyscale.filter(bufferedImage, null);

        BufferedImage gradBuffImageOne = convolveOpsOne.filter(img_grey, null);
        BufferedImage gradeBuffImageTwo = convolveOpsTwo.filter(img_grey, null);

        WritableRaster rasterOne = gradBuffImageOne.getRaster();
        WritableRaster rasterTwo = gradeBuffImageTwo.getRaster();

        int height = bufferedImage.getHeight();
        int[] temp = new int[1];
        int[] checkEnergy = new int[4];

        int[] colOne = new int[height];
        int[] colTwo = new int[height];


        for (int xIndex = 0; xIndex < rasterOne.getWidth(); xIndex++) {
            rasterOne.getPixels(xIndex, 0, 1, height, colOne);
            rasterTwo.getPixels(xIndex, 0, 1, height, colTwo);

            for (int yIndex = 0; yIndex < height; yIndex++) {
                temp[0] = (Math.abs(colOne[yIndex]) + Math.abs(colTwo[yIndex])) >> 1;
                if (energyAddonBufImage != null) {
                    if (energyAddonBufImage.getRaster().getPixel(xIndex, yIndex, checkEnergy)[0] > 0) {
                        temp[0] = 255;
                    } else if (energyAddonBufImage.getRaster().getPixel(xIndex, yIndex, checkEnergy)[1] > 0) {
                        temp[0] = 0;
                    }
                }
                rasterOne.setPixel(xIndex, yIndex, temp);
            }
        }
        return gradBuffImageOne;
    }

    public BufferedImage getBufferedImage() {
        return bufferedImage;
    }
}

class VerticalSeamCarve implements Comparable {

    public int startX;
    public float[][][] table;
    private float energy;
    private int tableWidth;
    private int tableHeight;

    public VerticalSeamCarve(float energy, int startX, float[][][] table, int tableWidth, int tableHeight) {
        this.energy = energy;
        this.startX = startX;
        this.table = table;
        this.tableWidth = tableWidth;
        this.tableHeight = tableHeight;
    }

    public int compareTo(Object object) {
        return object instanceof VerticalSeamCarve ? (int) Math.signum(((VerticalSeamCarve) object).energy - energy) : 0;
    }
}

class HorizontalSeamCarve implements Comparable {
    private float energy;
    private int tableHeight;
    private int tableWidth;
    public float[][][] table;
    public int startY;


    public HorizontalSeamCarve(float energy, int startY, float[][][] table, int tableWidth, int tableHeight) {
        this.startY = startY;
        this.tableWidth = tableWidth;
        this.tableHeight = tableHeight;
        this.table = table;
        this.energy = energy;
    }

    public int compareTo(Object object) {
        return object instanceof HorizontalSeamCarve ? (int) Math.signum(((HorizontalSeamCarve) object).energy - energy) : 0;
    }
}