package projekt;

import ij.ImagePlus;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;

public class Watermark {

    public int [][][] originalBites;
    public int [][][] watermarkBites;

    public int[][][] getOriginalBites() {
        return originalBites;
    }

    public int[][][] getWatermarkBites() {
        return watermarkBites;
    }

    public void setOriginalBites(int[][][] originalBites) {
        this.originalBites = originalBites;
    }

    public void setWatermarkBites(int[][][] watermarkBites) {
        this.watermarkBites = watermarkBites;
    }

    public Watermark() {
        //this.original = original.getBufferedImage();
        //this.watermark = watermark.getBufferedImage();
        //red = new int [this.imageHeight][this.imageWidth];
        //redInBites = new int [this.imageHeight][this.imageWidth][8];
    }
    public int[][][] getRGBinBits (BufferedImage image) {
        var colorModel = image.getColorModel();
        var red = new int [image.getHeight()][image.getWidth()];
        var redInBites = new int [image.getHeight()][image.getWidth()][8];

        for (int i = 0; i < image.getHeight(); i++) {
            for (int j = 0; j < image.getWidth(); j++) {
                red[i][j] = colorModel.getRed(image.getRGB(j, i)); //0-255
                var decimaltoBinary = Integer.toBinaryString(red[i][j]); //kazde cislo vyjadrit pomocou 8mich bitov
                if(decimaltoBinary.length() != 8) {                 //ak chybaju na zaciatku nuly
                    int binaryLength = decimaltoBinary.length();
                    for (int u = binaryLength; u < 8; u++) {
                        decimaltoBinary = "0" + decimaltoBinary;
                    }
                }
                for(int k = 0; k < 8; k++) {
                    redInBites[i][j][k] = decimaltoBinary.charAt(k) - 48;
                }
            }
        }
        return redInBites;
    }

    public void getBitFromBlackWhie () {

    }

    public ImagePlus setImageFromRGB (int width, int height, int[][] r, int[][] g, int[][] b){
        BufferedImage bImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        int [][] rgb = new int [height][width];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                rgb[i][j] = new Color(r[i][j], g[i][j], b[i][j]).getRGB();
                bImage.setRGB(j, i, rgb[i][j]);
            }
        }
        return (new ImagePlus("",bImage));
    }
}
