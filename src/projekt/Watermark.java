package projekt;

import ij.ImagePlus;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;

public class Watermark {

    public int [][][] originalBits;
    public int [][][] watermarkBits;
    private int imageHeight;
    private int imageWidth;

    private int watermarkHeight;
    private int watermarkWidth;

    private BufferedImage originalImage;
    private BufferedImage watermarkImage;

    private int[][] red;
    private int[][] green;
    private int[][] blue;

    private String RGBcomponent;

    public static final int BLACK = 0;
    public static final int WHITE = 1;


    public int[][][] getOriginalBits() {
        return originalBits;
    }

    public int[][][] getWatermarkBits() {
        return watermarkBits;
    }

    public void setOriginalBits(int[][][] originalBites) {
        this.originalBits = originalBites;
    }

    public void setWatermarkBits(int[][][] watermarkBites) {
        this.watermarkBits = watermarkBites;
    }

    public Watermark(BufferedImage originalImage, BufferedImage watermarkImage, String selection) {
        this.originalImage = originalImage;
        this.watermarkImage = watermarkImage;
        this.imageHeight = originalImage.getHeight();
        this.imageWidth = originalImage.getWidth();
        this.watermarkHeight = watermarkImage.getHeight();
        this.watermarkWidth = watermarkImage.getWidth();
        this.RGBcomponent = selection;
    }

    public int[][][] bitsPreparationOrig (int h) {
        var colorModel = this.originalImage.getColorModel();
        this.red = new int [this.originalImage.getHeight()][this.originalImage.getWidth()];
        this.green = new int [this.originalImage.getHeight()][this.originalImage.getWidth()];
        this.blue = new int [this.originalImage.getHeight()][this.originalImage.getWidth()];
        var redInBites = new int [this.originalImage.getHeight()][this.originalImage.getWidth()][8];

        for (int i = 0; i < this.originalImage.getHeight(); i++) {
            for (int j = 0; j < this.originalImage.getWidth(); j++) {
                this.red[i][j] = colorModel.getRed(this.originalImage.getRGB(j, i)); //0-255
                this.green[i][j] = colorModel.getGreen(this.originalImage.getRGB(j, i));
                this.blue[i][j] = colorModel.getBlue(this.originalImage.getRGB(j, i));
                String decimaltoBinary;
                switch (this.RGBcomponent) {
                    //kazde cislo vyjadrit pomocou 8mich bitov
                    case "green":
                        decimaltoBinary = Integer.toBinaryString(green[i][j]);
                        break;
                    case "blue":
                        decimaltoBinary = Integer.toBinaryString(blue[i][j]);
                        break;
                    default:
                        decimaltoBinary = Integer.toBinaryString(red[i][j]);
                        break;
                }
                if(decimaltoBinary.length() != 8) {                 //ak chybaju na zaciatku nuly
                    int binaryLength = decimaltoBinary.length();
                    for (int u = binaryLength; u < 8; u++) {
                        decimaltoBinary = "0" + decimaltoBinary;
                    }
                }
                for(int k = 0; k < 8; k++) { //ak je h, vynuluj bit
                    if(i < watermarkHeight && j < watermarkWidth) {
                        if (k == h) {
                            redInBites[i][j][k] = 0;
                        } else {
                            redInBites[i][j][k] = decimaltoBinary.charAt(k) - 48;
                        }
                        } else {
                        redInBites[i][j][k] = decimaltoBinary.charAt(k) - 48;
                    }
                }
            }
        }
        return redInBites;
    }

    public int[][][] bitsPreparationMark (int h) {
        var colorModel = watermarkImage.getColorModel();
        var color = new int [watermarkImage.getHeight()][watermarkImage.getWidth()][8];
        var redInBites = new int [watermarkImage.getHeight()][watermarkImage.getWidth()][8];

        for (int i = 0; i < watermarkImage.getHeight(); i++) {
            for (int j = 0; j < watermarkImage.getWidth(); j++) {
                    for(int k = 0; k < 8; k++) {
                        if(watermarkImage.getRGB(j, i) ==-1) {
                        if (k == h) {
                            color[i][j][k] = WHITE;
                        } else {
                            color[i][j][k] = BLACK;
                        }
                    }
                else {
                    color[i][j][k] = BLACK;
                        }
                }
                //0-255
            }
        }
        return color;
    }

    public void insertWatermarkInImage() {
        var originalBits = this.getOriginalBits();
        var watermarkBits = this.getWatermarkBits();

        for (int i = 0; i < this.watermarkHeight; i++) {
            for (int j = 0; j < this.watermarkWidth; j++) {
                for (int k = 0; k < 8; k++) {
                    if(originalBits[i][j][k] == 1 || watermarkBits[i][j][k] == 1) {
                        originalBits[i][j][k] = 1;
                    }  else {
                            originalBits[i][j][k] = 0;
                        }
                }
            }
        }
    }

    public ImagePlus setImageFromBits (){

        for (int i = 0; i < this.watermarkHeight; i++) {
            for (int j = 0; j < this.imageWidth; j++) {
                var binaryToDecimal = "";
                for(int k = 0; k < 8; k++) {
                    binaryToDecimal = binaryToDecimal + originalBits[i][j][k];
                }
                switch (this.RGBcomponent) {
                    //kazde cislo vyjadrit pomocou 8mich bitov
                    case "green":
                        this.green[i][j] = Integer.parseInt(binaryToDecimal,2);
                        break;
                    case "blue":
                        this.blue[i][j] = Integer.parseInt(binaryToDecimal,2);
                        break;
                    default:
                        this.red[i][j] = Integer.parseInt(binaryToDecimal,2);
                        break;
                }
            }
        }

        BufferedImage bImage = new BufferedImage(this.imageWidth, this.imageHeight, BufferedImage.TYPE_INT_RGB);
        int [][] rgb = new int [this.imageHeight][this.imageWidth];
        for (int i = 0; i < this.imageHeight; i++) {
            for (int j = 0; j < this.imageWidth; j++) {
                rgb[i][j] = new Color(this.red[i][j], this.green[i][j], this.blue[i][j]).getRGB();
                bImage.setRGB(j, i, rgb[i][j]);
            }
        }
        return (new ImagePlus("Vlozeny watermark",bImage));
    }

    public ImagePlus extractWatermarkFromImage(int h) {
        var extractedBits = new int[watermarkImage.getHeight()][watermarkImage.getWidth()];
        for (int i = 0; i < this.watermarkHeight; i++) {
            for (int j = 0; j < this.watermarkWidth; j++) {
                var value = originalBits[i][j][h];
                    if(value == 0) {
                        extractedBits[i][j] = 0;
                    } else {
                        extractedBits[i][j] = 255;
                    }
            }
        }
        BufferedImage bImage = new BufferedImage(watermarkImage.getWidth(), watermarkImage.getHeight(), BufferedImage.TYPE_INT_RGB);
        int [][] rgb = new int [watermarkImage.getHeight()][watermarkImage.getWidth()];
        for (int i = 0; i < watermarkImage.getHeight(); i++) {
            for (int j = 0; j < watermarkImage.getWidth(); j++) {
                rgb[i][j] = new Color(extractedBits[i][j], extractedBits[i][j], extractedBits[i][j]).getRGB();
                bImage.setRGB(j, i, rgb[i][j]);
            }
        }
        return (new ImagePlus("Extraktovany vodoznak",bImage));
    }
}
