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

}
