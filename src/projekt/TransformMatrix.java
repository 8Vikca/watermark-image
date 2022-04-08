package projekt;

import Jama.Matrix;

public class TransformMatrix {
    public Matrix getDctMatrix (int size) {
        Matrix dctMatrix = new Matrix(size, size);
        double pom;
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (i == 0) {
                    dctMatrix.set(i, j,  1/Math.sqrt(size));
                }
                else {
                    pom = Math.sqrt(2/(double)size)*Math.cos((Math.PI*(2*j+1)*i)/(2*(double)size));
                    dctMatrix.set(i, j, pom);
                }
            }
        }
        return dctMatrix;

    }

    public Matrix getWhtMatrix (int size) {
        size = (int) (Math.log(size)/Math.log(2));
        Matrix oldMatrix = new Matrix(1, 1, 1);
        Matrix newMatrix;
        if (size !=0) {
            for (int i=1; i<=size; i++) {
                newMatrix = new Matrix((int) Math.pow(2, i), (int) Math.pow(2, i));
                int pomMax = (int) Math.pow(2, i);
                //System.out.println(pomMax);
                newMatrix.setMatrix(0, pomMax/2-1, 0, pomMax/2-1, oldMatrix);
                newMatrix.setMatrix(0, pomMax/2-1 , pomMax/2, pomMax-1, oldMatrix);
                newMatrix.setMatrix(pomMax/2, pomMax-1, 0, pomMax/2-1, oldMatrix);
                newMatrix.setMatrix(pomMax/2, pomMax-1, pomMax/2, pomMax-1, oldMatrix.times(-1));
                //newMatrix.print(2, 2);
                oldMatrix = newMatrix.copy();
            }
        }
        newMatrix = oldMatrix.times(1/Math.sqrt(Math.pow(2, size)));
        return newMatrix;
    }
}
