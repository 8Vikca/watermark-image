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

    private ColorTransform colorTransformOrig;
    private ColorTransform colorTransformWatermarkMini;

    private ArrayList<Matrix> blocksOrig;
    private int imageHeight;
    private int imageWidth;

    public static final int BLACK = 0;

     public WatermarkDCT(ImagePlus imagePlus, ImagePlus watermarkImage) {
        this.imagePlus = imagePlus;
        this.watermarkImage = watermarkImage;
        loadImages();
        this.imageHeight = imagePlus.getBufferedImage().getHeight();
        this.imageWidth = imagePlus.getBufferedImage().getWidth();
    }

    public void loadImages () {
        this.colorTransformOrig = new ColorTransform(imagePlus.getBufferedImage());
        this.colorTransformOrig.getRGB();
        this.colorTransformOrig.convertRgbToYcbcr();

    }

    public ImagePlus extractWatermarkFromImage(int blockSize,  int u1, int v1, int u2, int v2) {
        var extractedBits = new int[watermarkImageMini.getHeight()][watermarkImageMini.getWidth()];

        Matrix tMat = new TransformMatrix().getDctMatrix(blockSize);
        this.transform(blockSize, tMat);

        int block = 0;

        for (int i = 0; i < watermarkImageMini.getHeight(); i++) {
            for (int j = 0; j < watermarkImageMini.getWidth(); j++) {
                var origY = blocksOrig.get(block).getArray();
                if (origY[u1][v1] > origY[u2][v2]) {
                    extractedBits[i][j] = 0;
                } else {
                    extractedBits[i][j] = 255;
                }
                block ++;
            }
        }

        BufferedImage bImage = new BufferedImage(watermarkImageMini.getWidth(), watermarkImageMini.getHeight(), BufferedImage.TYPE_INT_RGB);
        int [][] rgb = new int [watermarkImageMini.getHeight()][watermarkImageMini.getWidth()];
        for (int i = 0; i < watermarkImageMini.getHeight(); i++) {
            for (int j = 0; j < watermarkImageMini.getWidth(); j++) {
                rgb[i][j] = new Color(extractedBits[i][j], extractedBits[i][j], extractedBits[i][j]).getRGB();
                bImage.setRGB(j, i, rgb[i][j]);
            }
        }

        return (new ImagePlus("Extraktovany vodoznak (DCT)",bImage));
    }

    public ImagePlus insertWatermarkDCT(int blockSize, int h, int u1, int v1, int u2, int v2) {
        // DCT transformacia s rozdelenim na bloky
        Matrix tMat = new TransformMatrix().getDctMatrix(blockSize);
        this.transform(blockSize, tMat);

        //zmena velkosti watermarku
        this.resizeWatermark(watermarkImage);

        //pridat vodoznak
        this.addWatermark(h, u1, v1, u2, v2, blockSize);

        //inverzna DCT
        this.iTransform(blockSize, tMat);

        colorTransformOrig.convertYcbcrToRgb();

        return (colorTransformOrig.setImageFromRGB(this.imageWidth, this. imageHeight));

    }
        public void resizeWatermark(ImagePlus watermarkImage) {
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
        }

    public void addWatermark(int h, int u1, int v1, int u2, int v2, int blockSize) {
        this.colorTransformWatermarkMini = new ColorTransform(watermarkImageMini.getBufferedImage());
        this.colorTransformWatermarkMini.getRGB();
        var miniRed = this.colorTransformWatermarkMini.getRed();
        var origYMatrix = this.colorTransformOrig.getY();

        int block = 0;

        for (int i = 0; i < watermarkImageMini.getHeight(); i++) {
            for (int j = 0; j < watermarkImageMini.getWidth(); j++) {
                var origY = blocksOrig.get(block).getArray();
                if (miniRed[i][j] == BLACK) {
                    if(Math.abs(origY[u1][v1] - origY[u2][v2]) <= h) {
                        if(origY[u1][v1] > origY[u2][v2]) {
                            origY[u1][v1] += h/2.0;
                            origY[u2][v2] -= h/2.0;
                        } else {
                            origY[u1][v1] -= h/2.0;
                            origY[u2][v2] += h/2.0;
                        }

                    }
                    if(origY[u1][v1] <= origY[u2][v2]) {
                        var value = origY[u1][v1];
                        origY[u1][v1] = origY[u2][v2];
                        origY[u2][v2] = value;
                    }
                } else {
                    if(Math.abs(origY[u1][v1] - origY[u2][v2]) <= h) {
                        if(origY[u1][v1] > origY[u2][v2]) {
                            origY[u1][v1] += h/2.0;
                            origY[u2][v2] -= h/2.0;
                        } else {
                            origY[u1][v1] -= h/2.0;
                            origY[u2][v2] += h/2.0;
                        }
                    }
                    if(origY[u1][v1] > origY[u2][v2]) {
                        var value = origY[u1][v1];
                        origY[u1][v1] = origY[u2][v2];
                        origY[u2][v2] = value;
                    }
                }
                blocksOrig.set(block, new Matrix(origY));
                block ++;
            }
        }

        //zlepenie matrixov do 1
        block = 0;
        for (int i = 0; i < colorTransformOrig.getY().getRowDimension(); i = i + blockSize) {
            for (int j = 0; j < colorTransformOrig.getY().getColumnDimension(); j = j + blockSize) {
                origYMatrix.setMatrix(i,i + blockSize - 1, j, j + blockSize - 1,blocksOrig.get(block));
                block ++;
            }
        }
        colorTransformOrig.setY(origYMatrix);
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
        return (new ImagePlus("Zmenseny obrazok",newerBf));
    }

    public void transform(int blockSize, Matrix transformMatrix) {
        Matrix y = new Matrix(colorTransformOrig.getY().getRowDimension(), colorTransformOrig.getY().getColumnDimension());
        blocksOrig = new ArrayList<>();

        for (int i = 0; i < colorTransformOrig.getY().getRowDimension() - 1; i = i + blockSize) {
            for (int j = 0; j < colorTransformOrig.getY().getColumnDimension() - 1; j = j + blockSize) {
                y.setMatrix(i, i + blockSize - 1, j, j + blockSize - 1, colorTransformOrig.transform(blockSize, transformMatrix, colorTransformOrig.getY().getMatrix(i, i + blockSize - 1, j, j + blockSize - 1)));
                blocksOrig.add(y.getMatrix(i, i + blockSize - 1, j, j + blockSize - 1));
            }
        }

        colorTransformOrig.setY(y);
    }

    public void iTransform(int blockSize, Matrix transformMatrix) {
        Matrix y = new Matrix(colorTransformOrig.getY().getRowDimension(), colorTransformOrig.getY().getColumnDimension());

        for (int i = 0; i < colorTransformOrig.getY().getRowDimension() - 1; i = i + blockSize) {
            for (int j = 0; j < colorTransformOrig.getY().getColumnDimension() - 1; j = j + blockSize) {
                y.setMatrix(i, i + blockSize - 1, j, j + blockSize - 1, colorTransformOrig.inverseTransform(blockSize, transformMatrix, colorTransformOrig.getY().getMatrix(i, i + blockSize - 1, j, j + blockSize - 1)));
            }
        }

        colorTransformOrig.setY(y);
    }
}
