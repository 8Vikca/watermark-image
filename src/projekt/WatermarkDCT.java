package projekt;

import Jama.Matrix;
import ij.ImagePlus;

public class WatermarkDCT {
    private ImagePlus imagePlus;
    private ColorTransform colorTransform;

    private ColorTransform colorTransformOrig;

    public static final double[][] quantizationMatrix = {{16, 11, 10, 16, 24, 40, 51, 61},
            {12, 12, 14, 19, 26, 58, 60, 55},
            {14, 13, 16, 24, 40, 57, 69, 56},
            {14, 17, 22, 29, 51, 87, 80, 62},
            {18, 22, 37, 56, 68, 109, 103, 77},
            {24, 35, 55, 64, 81, 104, 113, 92},
            {49, 64, 78, 87, 103, 121, 120, 101},
            {72, 92, 95, 98, 112, 100, 103, 99}};


    public WatermarkDCT(ImagePlus imagePlus) {
        this.imagePlus = imagePlus;
        nactiOrigObraz(imagePlus);
    }

    public void nactiOrigObraz (ImagePlus imagePlus) {
        this.colorTransformOrig = new ColorTransform(imagePlus.getBufferedImage());
        this.colorTransform = new ColorTransform(imagePlus.getBufferedImage());
        this.colorTransformOrig.getRGB();
        this.colorTransform.getRGB();
        this.colorTransform.convertRgbToYcbcr();

    }

    public void transform(int blockSize, Matrix transformMatrix, int quality) {
        Matrix y = new Matrix(colorTransform.getY().getRowDimension(), colorTransform.getY().getColumnDimension());
        Matrix cB = new Matrix(colorTransform.getcB().getRowDimension(), colorTransform.getcB().getColumnDimension());
        Matrix cR = new Matrix(colorTransform.getcR().getRowDimension(), colorTransform.getcR().getColumnDimension());

        Matrix qMatY = new Matrix(quantizationMatrix);
        Matrix qMatC = new Matrix(quantizationMatrix);

        double alpha = 0.0;
        if (quality >= 1 && quality < 50) {
            alpha = 50.0 / quality;
            qMatY = qMatY.times(alpha);
            qMatC = qMatC.times(alpha);
        }
        if (quality > 50 && quality <= 99) {
            alpha = 2 - ((2 * quality) / 100.0);
            qMatY = qMatY.times(alpha);
            qMatC = qMatC.times(alpha);
        }

        for (int i = 0; i < colorTransform.getY().getRowDimension() - 1; i = i + blockSize) {
            for (int j = 0; j < colorTransform.getY().getColumnDimension() - 1; j = j + blockSize) {
                y.setMatrix(i, i + blockSize - 1, j, j + blockSize - 1, colorTransform.quantization(qMatY, colorTransform.transform(blockSize, transformMatrix, colorTransform.getY().getMatrix(i, i + blockSize - 1, j, j + blockSize - 1))));
            }
        }

        for (int i = 0; i < colorTransform.getcB().getRowDimension() - 1; i = i + blockSize) {
            for (int j = 0; j < colorTransform.getcB().getColumnDimension() - 1; j = j + blockSize) {
                cB.setMatrix(i, i + blockSize - 1, j, j + blockSize - 1, colorTransform.quantization(qMatC, colorTransform.transform(blockSize, transformMatrix, colorTransform.getcB().getMatrix(i, i + blockSize - 1, j, j + blockSize - 1))));
            }
        }

        for (int i = 0; i < colorTransform.getcR().getRowDimension() - 1; i = i + blockSize) {
            for (int j = 0; j < colorTransform.getcR().getColumnDimension() - 1; j = j + blockSize) {
                cR.setMatrix(i, i + blockSize - 1, j, j + blockSize - 1, colorTransform.quantization(qMatC, colorTransform.transform(blockSize, transformMatrix, colorTransform.getcR().getMatrix(i, i + blockSize - 1, j, j + blockSize - 1))));
            }
        }

        colorTransform.setY(y);
        colorTransform.setcB(cB);
        colorTransform.setcR(cR);

        this.addWatermark();
    }

    public void addWatermark() {

    }

    public void iTransform(int blockSize, Matrix transformMatrix, int quality) {
        Matrix y = new Matrix(colorTransform.getY().getRowDimension(), colorTransform.getY().getColumnDimension());
        Matrix cB = new Matrix(colorTransform.getcB().getRowDimension(), colorTransform.getcB().getColumnDimension());
        Matrix cR = new Matrix(colorTransform.getcR().getRowDimension(), colorTransform.getcR().getColumnDimension());

        Matrix qMatY = new Matrix(quantizationMatrix);
        Matrix qMatC = new Matrix(quantizationMatrix);

        double alpha = 0.0;
        quality = 50;

        for (int i = 0; i < colorTransform.getY().getRowDimension() - 1; i = i + blockSize) {
            for (int j = 0; j < colorTransform.getY().getColumnDimension() - 1; j = j + blockSize) {
                y.setMatrix(i, i + blockSize - 1, j, j + blockSize - 1, colorTransform.inverseTransform(blockSize, transformMatrix, colorTransform.inverseQuantization(qMatY, colorTransform.getY().getMatrix(i, i + blockSize - 1, j, j + blockSize - 1))));
            }
        }

        for (int i = 0; i < colorTransform.getcB().getRowDimension() - 1; i = i + blockSize) {
            for (int j = 0; j < colorTransform.getcB().getColumnDimension() - 1; j = j + blockSize) {
                cB.setMatrix(i, i + blockSize - 1, j, j + blockSize - 1, colorTransform.inverseTransform(blockSize, transformMatrix, colorTransform.inverseQuantization(qMatC, colorTransform.getcB().getMatrix(i, i + blockSize - 1, j, j + blockSize - 1))));
            }
        }

        for (int i = 0; i < colorTransform.getcR().getRowDimension() - 1; i = i + blockSize) {
            for (int j = 0; j < colorTransform.getcR().getColumnDimension() - 1; j = j + blockSize) {
                cR.setMatrix(i, i + blockSize - 1, j, j + blockSize - 1, colorTransform.inverseTransform(blockSize, transformMatrix, colorTransform.inverseQuantization(qMatC, colorTransform.getcR().getMatrix(i, i + blockSize - 1, j, j + blockSize - 1))));
            }
        }

        colorTransform.setY(y);
        colorTransform.setcB(cB);
        colorTransform.setcR(cR);
    }
}
