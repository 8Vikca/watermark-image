package projekt;

import Jama.Matrix;

public class Quality {

    public double getMae(int[][] original, int[][] edited)
    {
        int width = original.length;
        int height = original[0].length;
        double mae = 0;
        for (int i = 0; i < width; i++)
        {
            for (int j = 0; j < height; j++)
            {
                mae += Math.abs(original[i][j]-edited[i][j]);
            }
        }
        mae = mae/(width*height);
        return mae;
    }

    public double getMse (int[][] original, int[][] edited)
    {
        int width = original.length;
        int height = original[0].length;
        double mse = 0;
        for (int i = 0; i < width; i++)
        {
            for (int j = 0; j < height; j++)
            {
                mse += Math.pow(original[i][j]-edited[i][j],2);
            }
        }
        mse = mse/(width*height);
        return mse;
    }

    public double getPsnr (int[][] original, int[][] edited) {
        double mse = getMse (original, edited);
        double psnr = 10*Math.log10(Math.pow(255, 2)/mse);
        return psnr;
    }

    public double entropy (Matrix matrix){
        double cetnost [] = new double [256];
        double pocetSymbolu = matrix.getRowDimension()*matrix.getColumnDimension();
        double entropie = 0;
        for (int i = 0; i < matrix.getColumnDimension(); i++) {
            for (int j = 0; j < matrix.getRowDimension(); j++) {
                int x  =(int) matrix.get(i, j);
                cetnost[x] = cetnost[x] + 1/pocetSymbolu;
            }
        }
        for (int i = 0; i < 256; i++) {
            if (cetnost [i] > 0)
                entropie = entropie + cetnost[i]*Math.log(cetnost[i])/Math.log(2);
        }
        return (-entropie);


    }
}