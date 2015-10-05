package com.demeg.anotasiline.matriks;

/**
 * Created by deka dwi on 01-Oct-15.
 */
public class Matriks {
    public static float[][] invers(float[][] matrix) {
        int row = matrix.length;
        int col = matrix[0].length;
        if (row != col) {
            return null;
        }
        //membuat matrix identitas
        float[][] identitas = identitas(matrix);

        //augmented matrix
        float[][] augM = new float[row][row * 2];
        for (int i = 0; i < row; i++) {
            //System.arraycopy(mA[i], kolomAwalmA, mT[i], kolomAwalmT, jmlhKolomygDicopy);
            System.arraycopy(matrix[i], 0, augM[i], 0, row);
            System.arraycopy(identitas[i], 0, augM[i], row, row);
        }

        //Gauss Jordan Elimination
        int colA = augM[0].length;
        for (int j = 0; j < row; j++) {
            float star = augM[j][j];
            for (int i = 0; i < colA; i++) {
                augM[j][i] = (float) augM[j][i] / star;
            }
            star = augM[j][j];
            if (j == 0) {
                for (int i = j + 1; i < row; i++) {
                    float k = -(augM[i][j]) / star;
                    float[] temp = new float[colA];
                    for (int l = 0; l < colA; l++) {
                        temp[l] = k * augM[j][l];
                    }
                    for (int l = 0; l < colA; l++) {
                        augM[i][l] = augM[i][l] + temp[l];
                    }
                }
            }
            if (j > 0 || j < row - 2) {
                for (int i = 0; i < row; i++) {
                    if (i < j || i > j) {
                        float k = -(augM[i][j]) / star;
                        float[] temp = new float[colA];
                        for (int l = 0; l < colA; l++) {
                            temp[l] = k * augM[j][l];
                        }
                        for (int l = 0; l < colA; l++) {
                            augM[i][l] = augM[i][l] + temp[l];
                        }
                    }
                }
            }
            if (j == row - 1) {
                for (int i = row - 2; i >= 0; i--) {
                    float k = -(augM[i][j]) / star;
                    float[] temp = new float[colA];
                    for (int l = 0; l < colA; l++) {
                        temp[l] = k * augM[j][l];
                    }
                    for (int l = 0; l < colA; l++) {
                        augM[i][l] = augM[i][l] + temp[l];
                    }
                }
            }
        }

        float[][] hasil = new float[row][row];

        for (int i = 0; i < row; i++) {
            System.arraycopy(augM[i], row, hasil[i], 0, row);
        }

        return hasil;
    }

    public static float[][] kaliMatrix(float[][] m1, float[][] m2) {
        int row1 = m1.length;
        int col1 = m1[0].length;
        int row2 = m2.length;
        int col2 = m2[0].length;
        if (col1 != row2) {
            return null;
        }
        float[][] hasil = new float[row1][col2];
        for (int i = 0; i < row1; i++) {
            for (int j = 0; j < col2; j++) {
                for (int k = 0; k < col1; k++) {
                    hasil[i][j] += m1[i][k] * m2[k][j];
                }
            }
        }
        return hasil;
    }

    public static float[][] kaliMatrix(float angka, float[][] m2) {
        float[][] m1 = {{angka}};
        int row1 = m1.length;
        int col1 = m1[0].length;
        int row2 = m2.length;
        int col2 = m2[0].length;
        if (col1 != row2) {
            return null;
        }
        float[][] hasil = new float[row1][col2];
        for (int i = 0; i < row1; i++) {
            for (int j = 0; j < col2; j++) {
                for (int k = 0; k < col1; k++) {
                    hasil[i][j] += m1[i][k] * m2[k][j];
                }
            }
        }
        return hasil;
    }

    public static float[] kaliMatrix(float angka, float[] m2) {
        int n = m2.length;
        float[] hasil = new float[n];
        for (int i = 0; i < n; i++) {
            hasil[i] = angka * m2[i];
        }
        return hasil;
    }

    public static float[] tambah(float[] m1, float[] m2) {
        if(m1.length != m2.length) {
            return null;
        }
        float[] hasil = new float[m1.length];
        for (int i = 0; i < m1.length; i++) {
            hasil[i] = m1[i] + m2[i];
        }
        return hasil;
    }

    public static float[][] identitas(float[][] matrix) {
        int row = matrix.length;
        float[][] temp = new float[row][row];
        for (int i = 0; i < row; i++) {
            temp[i][i] = 1;
        }
        return temp;
    }

    public static float[][] transpose(float[][] matrix) {
        int row = matrix.length;
        int col = matrix[0].length;
        float[][] temp = new float[col][row];
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < col; j++) {
                temp[j][i] = matrix[i][j];
            }
        }
        return temp;
    }
}
