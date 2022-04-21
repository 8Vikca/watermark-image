package projekt;

import ij.ImagePlus;

import java.awt.*;
import java.awt.image.BufferedImage;

public class WatermarkLSB {

    private int imageHeight;
    private int imageWidth;

    private int watermarkHeight;
    private int watermarkWidth;

    private BufferedImage origWithWatermarkImage;

    private BufferedImage rotatedImage;

    private int[][] red;
    private int[][] green;
    private int[][] blue;

    private String RGBcomponent;

    public static final int BLACK = 0;
    public static final int WHITE = 1;


    public BufferedImage getRotatedImage() {
        return rotatedImage;
    }

    public void setRotatedImage(BufferedImage rotatedImage) {
        this.rotatedImage = rotatedImage;
    }



    public WatermarkLSB(BufferedImage originalImage, BufferedImage watermarkImage, String selection) {
        this.imageHeight = originalImage.getHeight();
        this.imageWidth = originalImage.getWidth();
        this.watermarkHeight = watermarkImage.getHeight();
        this.watermarkWidth = watermarkImage.getWidth();
        this.RGBcomponent = selection;
    }

    public void getRGB(BufferedImage image) {
        this.red = new int [image.getHeight()][image.getWidth()];
        this.green = new int [image.getHeight()][image.getWidth()];
        this.blue = new int [image.getHeight()][image.getWidth()];

        var colorModel = image.getColorModel();
        for (int i = 0; i < image.getHeight(); i++) {
            for (int j = 0; j < image.getWidth(); j++) {
                red[i][j] = colorModel.getRed(image.getRGB(j, i));
                green[i][j] = colorModel.getGreen(image.getRGB(j, i));
                blue[i][j] = colorModel.getBlue(image.getRGB(j, i));
            }
        }
    }

    public int[][][] convertComponentIntoBits(int h, BufferedImage imageforPreparation) {
        var bits = new int [imageforPreparation.getHeight()][imageforPreparation.getWidth()][8];

        for (int i = 0; i < imageforPreparation.getHeight(); i++) {
            for (int j = 0; j < imageforPreparation.getWidth(); j++) {
                String decimaltoBinary = "";
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
                            bits[i][j][k] = 0;
                        } else {
                            bits[i][j][k] = decimaltoBinary.charAt(k) - 48;
                        }
                        } else {
                        bits[i][j][k] = decimaltoBinary.charAt(k) - 48;
                    }
                }
            }
        }
        return bits;
    }

    public int[][][] convertWatermarkIntoToBits (int h, BufferedImage watermark) {
        var bits = new int [watermark.getHeight()][watermark.getWidth()][8];
        for (int i = 0; i < watermark.getHeight(); i++) {
            for (int j = 0; j < watermark.getWidth(); j++) {
                    for(int k = 0; k < 8; k++) {
                        if(watermark.getRGB(j, i) ==-1) {
                        if (k == h) {
                            bits[i][j][k] = WHITE;
                        } else {
                            bits[i][j][k] = BLACK;
                        }
                    }
                    else {
                        bits[i][j][k] = BLACK;
                    }
                }
            }
        }
        return bits;
    }

    public int[][][] convertImageToBits (int h, BufferedImage image) {
        var bits = new int [image.getHeight()][image.getWidth()][8];

        for (int i = 0; i < image.getHeight(); i++) {
            for (int j = 0; j < image.getWidth(); j++) {
                String decimaltoBinary = "";
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
                        bits[i][j][k] = decimaltoBinary.charAt(k) - 48;
                }
            }
        }
        return bits;
    }

    public int[][][] insertWatermarkInImage(int[][][] imageBits, int[][][] watermarkBits) {
        for (int i = 0; i < this.watermarkHeight; i++) {
            for (int j = 0; j < this.watermarkWidth; j++) {
                for (int k = 0; k < 8; k++) {
                    if(imageBits[i][j][k] == 1 || watermarkBits[i][j][k] == 1) {
                        imageBits[i][j][k] = 1;
                    }  else {
                        imageBits[i][j][k] = 0;
                        }
                }
            }
        }
        return imageBits;
    }

    public ImagePlus setImageFromBits(int[][][] bitsForImage){

        for (int i = 0; i < this.watermarkHeight; i++) {
            for (int j = 0; j < this.imageWidth; j++) {
                var binaryToDecimal = "";
                for(int k = 0; k < 8; k++) {
                    binaryToDecimal = binaryToDecimal + bitsForImage[i][j][k];
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

        var image = new BufferedImage(this.imageWidth, this.imageHeight, BufferedImage.TYPE_INT_RGB);
        int [][] rgb = new int [this.imageHeight][this.imageWidth];
        for (int i = 0; i < this.imageHeight; i++) {
            for (int j = 0; j < this.imageWidth; j++) {
                rgb[i][j] = new Color(this.red[i][j], this.green[i][j], this.blue[i][j]).getRGB();
                image.setRGB(j, i, rgb[i][j]);
            }
        }
        return (new ImagePlus("Vlozeny watermark",image));
    }

    public ImagePlus extractWatermarkFromImage(int h, int[][][] bitForExtraction) {

        var extractedBits = new int[watermarkHeight][watermarkWidth];
        for (int i = 0; i < this.watermarkHeight; i++) {
            for (int j = 0; j < this.watermarkWidth; j++) {
                var value = bitForExtraction[i][j][h];
                    if(value == 0) {
                        extractedBits[i][j] = 0;
                    } else {
                        extractedBits[i][j] = 255;
                    }
            }
        }
        BufferedImage bImage = new BufferedImage(watermarkWidth, watermarkHeight, BufferedImage.TYPE_INT_RGB);
        int [][] rgb = new int [watermarkHeight][watermarkWidth];
        for (int i = 0; i < watermarkHeight; i++) {
            for (int j = 0; j < watermarkWidth; j++) {
                rgb[i][j] = new Color(extractedBits[i][j], extractedBits[i][j], extractedBits[i][j]).getRGB();
                bImage.setRGB(j, i, rgb[i][j]);
            }
        }
        return (new ImagePlus("Extraktovany vodoznak",bImage));
    }

    public ImagePlus mirrorImage(BufferedImage imageForMirroring) {
        int height = imageForMirroring.getHeight();
        int width = imageForMirroring.getWidth();

        BufferedImage res = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        for(int j = 0; j < height; j++){
            for(int i = 0, w = width - 1; i < width; i++, w--){
                int p = imageForMirroring.getRGB(i, j);
                //set mirror image pixel value - both left and right
                res.setRGB(w, j, p);
            }
        }
        return (new ImagePlus("Prevrateny obrazok",res));

    }

    public ImagePlus rotate(double angle, BufferedImage imageForRotating){
        double sin = Math.abs(Math.sin(Math.toRadians(angle))),
                cos = Math.abs(Math.cos(Math.toRadians(angle)));
        int height = imageForRotating.getHeight();
        int width = imageForRotating.getWidth();
        int newWidth, newHeight;
        if(angle > 0) {
            newWidth = (int) Math.floor(width*cos + height*sin);
            newHeight = (int) Math.floor(height*cos + width*sin);
        } else {
            newWidth = (int) this.imageWidth;
            newHeight = (int) this.imageHeight;
        }

        System.out.println("new W:"+newWidth+", new H"+newHeight);
        BufferedImage rotated = new BufferedImage(newWidth, newHeight, imageForRotating.getType());
        Graphics2D graphic = rotated.createGraphics();
        graphic.translate((newWidth-width)/2, (newHeight-height)/2);
        graphic.rotate(Math.toRadians(angle), width/2.0, height/2.0);
        graphic.drawRenderedImage(imageForRotating, null);
        graphic.dispose();
        return (new ImagePlus("Zrotovany obrazok",rotated));
    }

}
