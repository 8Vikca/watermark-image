package projekt;

import Jama.Matrix;
import ij.ImagePlus;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;

public class ColorTransform {
    private BufferedImage imageOrig;
    private ColorModel colorModelOrig;
    private int imageHeightOrig;
    private int imageWidthOrig;

    //barevne komponenty
    private int[][] red;
    private int[][] green;
    private int[][] blue;

    private Matrix y;
    private Matrix cB;
    private Matrix cR;

    public int[][] getRed() {
        return red;
    }

    public int[][] getGreen() {
        return green;
    }

    public int[][] getBlue() {
        return blue;
    }

    public Matrix getY() {
        return y;
    }

    public Matrix getcB() {
        return cB;
    }

    public Matrix getcR() {
        return cR;
    }


    public void setY(Matrix y) {
        this.y = y;
    }

    public void setcB(Matrix cB) {
        this.cB = cB;
    }

    public void setcR(Matrix cR) {
        this.cR = cR;
    }

    public int getImageHeightOrig() {
        return imageHeightOrig;
    }

    public int getImageWidthOrig() {
        return imageWidthOrig;
    }


    public ColorTransform(BufferedImage bImage) {
        this.imageOrig = bImage;
        this.colorModelOrig = bImage.getColorModel();
        this.imageHeightOrig = bImage.getHeight();
        this.imageWidthOrig = bImage.getWidth();
        red = new int[this.imageHeightOrig][this.imageWidthOrig];
        green = new int[this.imageHeightOrig][this.imageWidthOrig];
        blue = new int[this.imageHeightOrig][this.imageWidthOrig];
        y = new Matrix(this.imageHeightOrig, this.imageWidthOrig);
        cB = new Matrix(this.imageHeightOrig, this.imageWidthOrig);
        cR = new Matrix(this.imageHeightOrig, this.imageWidthOrig);
    }

    public void getRGB() {
        for (int i = 0; i < this.imageHeightOrig; i++) {
            for (int j = 0; j < this.imageWidthOrig; j++) {
                red[i][j] = colorModelOrig.getRed(this.imageOrig.getRGB(j, i));
                green[i][j] = colorModelOrig.getGreen(this.imageOrig.getRGB(j, i));
                blue[i][j] = colorModelOrig.getBlue(this.imageOrig.getRGB(j, i));
            }
        }
    }

    //pro vytvoření modelu RGB
    public ImagePlus setImageFromRGB(int width, int height) {
        BufferedImage bImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        int[][] rgb = new int[height][width];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                rgb[i][j] = new Color(red[i][j], green[i][j], blue[i][j]).getRGB();
                bImage.setRGB(j, i, rgb[i][j]);
            }
        }
        return (new ImagePlus("Vlozeny watermark (DCT)", bImage));
    }

    // Pro vytvoření modelu jedné komponenty R G B z pole int
    public ImagePlus setImageFromRGB(int width, int height, int[][] x, String component) {
        BufferedImage bImage = new BufferedImage(width, height,
                BufferedImage.TYPE_INT_RGB);
        int[][] rgb = new int[height][width];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                rgb[i][j] = new Color(x[i][j], x[i][j], x[i][j]).getRGB();
                bImage.setRGB(j, i, rgb[i][j]);
            }
        }
        return (new ImagePlus(component, bImage));
    }

    // Pro vytvoření modelu jedné komponenty Y Cb Cr z pole Matrix
    public ImagePlus setImageFromRGB(int width, int height, Matrix x,
                                     String component) {
        BufferedImage bImage = new BufferedImage(width, height,
                BufferedImage.TYPE_INT_RGB);
        int[][] rgb = new int[height][width];
        // x.print(8, 2);
        //if (afterTransform == false) {
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {

                rgb[i][j] = new Color((int) x.get(i, j), (int) x.get(i, j),
                        (int) x.get(i, j)).getRGB();
                bImage.setRGB(j, i, rgb[i][j]);
            }
        }
        return (new ImagePlus(component, bImage));
    }


    public void convertRgbToYcbcr() {
        for (int i = 0; i < this.imageHeightOrig; i++) {
            for (int j = 0; j < this.imageWidthOrig; j++) {
                y.set(i, j, 0.257 * red[i][j] + 0.504 * green[i][j] + 0.098 * blue[i][j] + 16);
                cB.set(i, j, -0.148 * red[i][j] - 0.291 * green[i][j] + 0.439 * blue[i][j] + 128);
                cR.set(i, j, 0.439 * red[i][j] - 0.368 * green[i][j] - 0.071 * blue[i][j] + 128);
            }
        }
    }

    public void convertYcbcrToRgb() {
        for (int i = 0; i < this.imageHeightOrig; i++) {
            for (int j = 0; j < this.imageWidthOrig; j++) {
                red[i][j] = (int) Math.round(1.164 * (y.get(i, j) - 16) + 1.596 * (cR.get(i, j) - 128));
                if (red[i][j] > 255) red[i][j] = 255;
                if (red[i][j] < 0) red[i][j] = 0;
                green[i][j] = (int) Math.round(1.164 * (y.get(i, j) - 16) - 0.813 * (cR.get(i, j) - 128) - 0.391 * (cB.get(i, j) - 128));
                if (green[i][j] > 255) green[i][j] = 255;
                if (green[i][j] < 0) green[i][j] = 0;
                blue[i][j] = (int) Math.round(1.164 * (y.get(i, j) - 16) + 2.018 * (cB.get(i, j) - 128));
                if (blue[i][j] > 255) blue[i][j] = 255;
                if (blue[i][j] < 0) blue[i][j] = 0;
            }
        }
    }

    //roznásobenie matice obrázku a dct matice
    public Matrix transform(int size, Matrix transformMatrix, Matrix inputMatrix) {
        Matrix newMatrix = new Matrix(inputMatrix.getRowDimension(), inputMatrix.getColumnDimension());
        for (int i = 0; i < inputMatrix.getRowDimension(); i+=size) {
            for (int j = 0; j < inputMatrix.getColumnDimension(); j+=size) {
                Matrix block = inputMatrix.getMatrix(i, Math.min(inputMatrix.getRowDimension()-1, i+size-1),
                        j, Math.min(inputMatrix.getColumnDimension()-1, j+size-1));

                Matrix transformedBlock = transformMatrix.times(block).times(transformMatrix.transpose());

                newMatrix.setMatrix(i, Math.min(inputMatrix.getRowDimension()-1, i+size-1),
                        j, Math.min(inputMatrix.getColumnDimension()-1, j+size-1), transformedBlock);

            }
        }
        return (newMatrix);
    }

    public Matrix inverseTransform(int size, Matrix transformMatrix, Matrix inputMatrix) {
        Matrix newMatrix = new Matrix(inputMatrix.getRowDimension(), inputMatrix.getColumnDimension());
        for (int i = 0; i < inputMatrix.getRowDimension(); i+=size) {
            for (int j = 0; j < inputMatrix.getColumnDimension(); j+=size) {
                Matrix block = inputMatrix.getMatrix(i, Math.min(inputMatrix.getRowDimension()-1, i+size-1),
                        j, Math.min(inputMatrix.getColumnDimension()-1, j+size-1));

                Matrix transformedBlock = transformMatrix.transpose().times(block).times(transformMatrix);

                newMatrix.setMatrix(i, Math.min(inputMatrix.getRowDimension()-1, i+size-1),
                        j, Math.min(inputMatrix.getColumnDimension()-1, j+size-1), transformedBlock);

            }
        }
        return (newMatrix);

    }

    //zaokruhlenie hodnot podla tabulky
    public Matrix quantization(Matrix qMat, Matrix input) {
        Matrix res = new Matrix(input.getRowDimension(), input.getColumnDimension());
        for (int i = 0; i < input.getRowDimension(); i++) {
            for (int j = 0; j < input.getColumnDimension(); j++) {
                res.set(i, j, (int) (input.get(i, j) / qMat.get(i, j)));
            }
        }
        return res;
    }

    public Matrix inverseQuantization(Matrix qMat, Matrix input) {
        Matrix res = new Matrix(input.getRowDimension(), input.getColumnDimension());
        for (int i = 0; i < input.getRowDimension(); i++) {
            for (int j = 0; j < input.getColumnDimension(); j++) {
                res.set(i, j, (int) (input.get(i, j) * qMat.get(i, j)));
            }
        }
        return res;
    }

}
