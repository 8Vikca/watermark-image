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

    private ArrayList<Matrix> blocks;
    private int imageHeight;
    private int imageWidth;

    public WatermarkDCT(ImagePlus imagePlus, ImagePlus watermarkImage) {
        this.imagePlus = imagePlus;
        this.watermarkImage = watermarkImage;
        blocks = new ArrayList<Matrix>();
        loadImages();
        this.imageHeight = imagePlus.getBufferedImage().getHeight();
        this.imageWidth = imagePlus.getBufferedImage().getWidth();
    }

    public void loadImages () {
        this.colorTransformOrig = new ColorTransform(imagePlus.getBufferedImage());
        this.colorTransformOrig.getRGB();
        this.colorTransformOrig.convertRgbToYcbcr();

    }

    public ImagePlus extractWatermarkFromImage(int blockSize, int h) {
        // ziskani y
        Matrix yMatrix = imageWithWatermark.getY().copy();

        int numberOfBitsWatermark = workingWatermarkMini.getImageHeight() * workingWatermarkMini.getImageWidth();

        // prevod vytvoreni listu pro watermark
        ArrayList<Integer> watermarkList = new ArrayList<>();

        // rozdelit na bloky
        ArrayList<Matrix> blocks = TransformMatrix.getBlocks(8, yMatrix);

        // DCT transformace
        double bi, bj;

        Matrix dctMatrix = TransformMatrix.getDctMatrix(8);
        // DCT transformace
        for (int i = 0; i < numberOfBitsWatermark; i++) {
            // 2D-DCT
            Matrix helpMatrix = dctMatrix.times(blocks.get(i));
            Matrix matrixAfterDCT = helpMatrix.times(dctMatrix.transpose());

            // dodani hodnot
            bi = matrixAfterDCT.get(u1, v1);
            bj = matrixAfterDCT.get(u2, v2);
            // extrakce vodoznaku
            if (bi > bj) watermarkList.add(0);
            else watermarkList.add(255);
        }
        // ulozeni a navrat
        int[][] result = createWatermarkFromList(watermarkList, workingWatermarkMini.getRed().length, workingWatermarkMini.getRed()[1].length);
        workingWatermarkMini.setRed(result.clone());
        workingWatermarkMini.setGreen(result.clone());
        workingWatermarkMini.setBlue(result.clone());

        return workingWatermarkMini;
    }

    public ImagePlus insertWatermarkDCT(int blockSize, int h, int u1, int v1, int u2, int v2) {

        var yMatrix = this.colorTransformOrig.getY();

        // rozdelit na bloky
        blocks = this.getBlocks(blockSize, yMatrix);

        //zmena velkosti watermarku
        this.resizeWatermark(watermarkImage);

        this.colorTransformWatermarkMini = new ColorTransform(watermarkImageMini.getBufferedImage());
        this.colorTransformWatermarkMini.getRGB();

        // prevod watermarku do listu
        ArrayList<Integer> watermarkList = getWatermarkList(watermarkImageMini.getBufferedImage());

        // DCT transformacia
        Matrix dctMatrix = new TransformMatrix().getDctMatrix(blockSize);
        for (int i = 0; i < watermarkList.size(); i++) {
            // 2D-DCT
            Matrix helpMatrix = dctMatrix.times(blocks.get(i));
            Matrix matrixAfterDCT = helpMatrix.times(dctMatrix.transpose());
            blocks.set(i,matrixAfterDCT);

            //vytiahnutie blokov Bi a Bj
            var bi = blocks.get(i).get(u1, v1);
            var bj = blocks.get(i).get(u2, v2);

            // uprava h
            if (Math.abs(bi - bj) <= h) {
                if (bi > bj) {
                    bi += h/2.0;
                    bj -=h/2.0;
                } else {
                    bi -= h/2.0;
                    bj +=h/2.0;
                }
            }

            // aplikovanie nerovnic
            int watermarkPixel = watermarkList.get(i);
            if (watermarkPixel == 255) {
                if (bi > bj) {
                    var value = bi;
                    bi = bj;
                    bj = value;
                }
            } else {
                if (bi <= bj) {
                    var value = bi;
                    bi = bj;
                    bj = value;
                }
            }
            // prepisanie novych hodnot do blokov
            blocks.get(i).set(u1, v1, bi);
            blocks.get(i).set(u2, v2, bj);

            // Inverse 2D-DCT
            helpMatrix = dctMatrix.transpose().times(blocks.get(i));
            Matrix output = helpMatrix.times(dctMatrix);
            blocks.set(i, output);
        }
        // spojenie blokov do 1 matice
        Matrix newY = this.connectBlocks(blockSize, blocks, yMatrix.getRowDimension(), yMatrix.getColumnDimension());

        // ulozenie Y zlozky a vytvorenie noveho obrazku
        colorTransformOrig.setY(newY);
        colorTransformOrig.convertYcbcrToRgb();

        return (colorTransformOrig.setImageFromRGB(this.imageWidth, this. imageHeight));

    }

    public void resizeWatermark(ImagePlus watermarkImage) {
        var watermarkPixelSum = watermarkImage.getHeight()*watermarkImage.getWidth();
        if(watermarkPixelSum > blocks.size()) {
            watermarkImageMini = resize(this.watermarkImage.getBufferedImage(), (int) (this.watermarkImage.getWidth()*0.8),(int) (this.watermarkImage.getWidth()*0.8));
            watermarkPixelSum = watermarkImageMini.getHeight()*watermarkImageMini.getWidth();
            while(watermarkPixelSum > blocks.size()) {
                watermarkImageMini = resize(this.watermarkImageMini.getBufferedImage(), (int) (this.watermarkImageMini.getWidth()*0.8),(int) (this.watermarkImageMini.getWidth()*0.8));
                watermarkPixelSum = watermarkImageMini.getHeight()*watermarkImageMini.getWidth();
            }
        } else {
            watermarkImageMini = watermarkImage;
        }
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


    public  ArrayList<Integer> getWatermarkList(BufferedImage watermark) {
        ArrayList<Integer> watermarkList = new ArrayList<>();

        for (int i = 0; i < watermark.getHeight(); i++) {
            for (int j = 0; j < watermark.getWidth(); j++) {
                watermarkList.add(colorTransformWatermarkMini.getRed()[i][j]);
            }
        }
        return watermarkList;
    }

    public ArrayList<Matrix> getBlocks(int n, Matrix entryMatrix) {
        ArrayList<Matrix> blocks = new ArrayList<>();
        int row= (int)(entryMatrix.getRowDimension()/n);
        for (int i = 0; i < row; i++){
            for (int j = 0; j < row; j++){
                blocks.add(entryMatrix.getMatrix(n*i,n*i+n-1,n*j,n*j+n-1));
            }
        }

        return blocks;
    }

    public Matrix connectBlocks(int n, ArrayList<Matrix> blocks, int row, int column){
        int size = n * (int) Math.sqrt(blocks.size());
        Matrix returnMatrix = new Matrix(size,size);
        row = (int) row/n;
        column = (int) column/n;
        int blockCounter = 0;
        for (int i = 0; i < row; i++){
            for (int j = 0; j < column; j++){
                returnMatrix.setMatrix(n*i,n*i+n-1,n*j,n*j+n-1, blocks.get(blockCounter));
                blockCounter++;
            }
        }

        return returnMatrix;
    }
}
