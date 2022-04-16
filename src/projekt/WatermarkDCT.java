package projekt;

import Jama.Matrix;
import ij.ImagePlus;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.util.ArrayList;

public class WatermarkDCT {
    private ImagePlus imagePlus;
    private ImagePlus watermarkImage;
    private ImagePlus watermarkImageMini;

    private ColorTransform colorTransformWatermark;
    private ColorTransform colorTransformOrig;

    private ArrayList<Matrix> blocksOrig;

    /**Pro jasovou slozku */
    public static final double[][] quantizationMatrix = {{16, 11, 10, 16, 24, 40, 51, 61},
            {12, 12, 14, 19, 26, 58, 60, 55},
            {14, 13, 16, 24, 40, 57, 69, 56},
            {14, 17, 22, 29, 51, 87, 80, 62},
            {18, 22, 37, 56, 68, 109, 103, 77},
            {24, 35, 55, 64, 81, 104, 113, 92},
            {49, 64, 78, 87, 103, 121, 120, 101},
            {72, 92, 95, 98, 112, 100, 103, 99}};


    public WatermarkDCT(ImagePlus imagePlus, ImagePlus watermarkImage) {
        this.imagePlus = imagePlus;
        this.watermarkImage = watermarkImage;
        blocksOrig = new ArrayList<>();
        loadImages();
    }

    public void loadImages () {
        this.colorTransformOrig = new ColorTransform(imagePlus.getBufferedImage());
        this.colorTransformWatermark = new ColorTransform(watermarkImage.getBufferedImage());

        this.colorTransformOrig.getRGB();
        this.colorTransformWatermark.getRGB();

        this.colorTransformOrig.convertRgbToYcbcr();
        this.colorTransformWatermark.convertRgbToYcbcr();

    }

    public void insertWatermarkDCT(int blockSize, int quality, int h, int u, int v) {
        // DCT transformacia s rozdelenim na bloky
        this.transform(blockSize, quality);

        //zmena velkosti watermarku
        var watermarkPixelSum = watermarkImage.getHeight()*watermarkImage.getWidth();
        if(watermarkPixelSum > blocksOrig.size()) {
                watermarkImageMini = resize(this.watermarkImage.getBufferedImage(), (int) (this.watermarkImage.getWidth()*0.8),(int) (this.watermarkImage.getWidth()*0.8));
                watermarkPixelSum = watermarkImageMini.getHeight()*watermarkImageMini.getWidth();
            while(watermarkPixelSum > blocksOrig.size()) {
                watermarkImageMini = resize(this.watermarkImageMini.getBufferedImage(), (int) (this.watermarkImageMini.getWidth()*0.8),(int) (this.watermarkImageMini.getWidth()*0.8));
                watermarkPixelSum = watermarkImageMini.getHeight()*watermarkImageMini.getWidth();
            }
        } else {
            watermarkImageMini = watermarkImage;
        }
        watermarkImageMini.show();

        //pridat vodoznak
        this.addWatermark();


    }

    public void addWatermark() {

    }
    public ImagePlus resize(BufferedImage img, int newW, int newH) {
        Image tmp = img.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
        BufferedImage newBf = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = newBf.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();
        ColorModel colorModel = newBf.getColorModel();
        int imageWidth = newBf.getWidth();
        int imageHeight = newBf.getHeight();
        BufferedImage newerBf= new BufferedImage(imageWidth,imageHeight,BufferedImage.TYPE_INT_RGB);
        int red,green,blue;
        for (int i = 0; i < imageHeight; i++) {
            for (int j = 0; j < imageWidth; j++) {
                red = colorModel.getRed(newBf.getRGB(j, i));
                green = colorModel.getGreen(newBf.getRGB(j, i));
                blue = colorModel.getBlue(newBf.getRGB(j, i));
                if (red < 150) red=0;
                else red=255;
                if (green < 150) green=0;
                else green=255;
                if (blue < 150) blue=0;
                else blue=255;
                newerBf.setRGB(j,i,new Color(red,green,blue).getRGB());
            }
        }
        return (new ImagePlus("Prevrateny obrazok",newerBf));
    }

    public void transform(int blockSize, int quality) {
        Matrix dctMatrix = new TransformMatrix().getDctMatrix(blockSize);

        Matrix y = new Matrix(colorTransformOrig.getY().getRowDimension(), colorTransformOrig.getY().getColumnDimension());
        Matrix qMatY = new Matrix(quantizationMatrix);

        double alpha = 0.0;
        if (quality >= 1 && quality < 50) {
            alpha = 50.0 / quality;
            qMatY = qMatY.times(alpha);
        }
        if (quality > 50 && quality <= 99) {
            alpha = 2 - ((2 * quality) / 100.0);
            qMatY = qMatY.times(alpha);
        }

        for (int i = 0; i < colorTransformOrig.getY().getRowDimension() - 1; i = i + blockSize) {
            for (int j = 0; j < colorTransformOrig.getY().getColumnDimension() - 1; j = j + blockSize) {
                y.setMatrix(i, i + blockSize - 1, j, j + blockSize - 1, colorTransformOrig.quantization(qMatY, colorTransformOrig.transform(blockSize, dctMatrix, colorTransformOrig.getY().getMatrix(i, i + blockSize - 1, j, j + blockSize - 1))));
                blocksOrig.add(colorTransformOrig.getY().getMatrix(i, i + blockSize - 1, j, j + blockSize - 1));
            }
        }

        colorTransformOrig.setY(y);
    }

    public void iTransform(int blockSize, Matrix transformMatrix, int quality) {
        Matrix y = new Matrix(colorTransformOrig.getY().getRowDimension(), colorTransformOrig.getY().getColumnDimension());

        Matrix qMatY = new Matrix(quantizationMatrix);

        double alpha = 0.0;
        quality = 50;

        for (int i = 0; i < colorTransformOrig.getY().getRowDimension() - 1; i = i + blockSize) {
            for (int j = 0; j < colorTransformOrig.getY().getColumnDimension() - 1; j = j + blockSize) {
                y.setMatrix(i, i + blockSize - 1, j, j + blockSize - 1, colorTransformOrig.inverseTransform(blockSize, transformMatrix, colorTransformOrig.inverseQuantization(qMatY, colorTransformOrig.getY().getMatrix(i, i + blockSize - 1, j, j + blockSize - 1))));
            }
        }

        colorTransformOrig.setY(y);
    }
}
