package com.demeg.anotasiline.matriks;

/**
 * Created by demeg on 05-Oct-15.
 */
public class Spline {

    public static float[][] m1(float[][] matrix) {
        int row = matrix.length - 2;
        float[][] hasil = new float[row][row];
        for (int i = 0; i < row; i++) {
            hasil[i][i] = 4;
            if (i == 0) {
                hasil[i][i + 1] = 1;
            } else if (i > 0 && i < row - 1) {
                hasil[i][i + 1] = 1;
                hasil[i][i - 1] = 1;
            } else {
                hasil[i][i - 1] = 1;
            }
        }
        return hasil;
    }

    public static float[][] m2(float[][] matrix) {
        int row = matrix.length; //7
        int col = matrix[0].length; //2
        float[][] hasil = new float[row - 2][col]; //5,2
        hasil[0][0] = 6 * matrix[1][0] - matrix[0][0];
        hasil[0][1] = 6 * matrix[1][1] - matrix[0][1];
        for (int i = 1; i < row - 3; i++) { //mulai baris kedua sampai baris keempat
            for (int j = 0; j < col; j++) {
                hasil[i][j] = 6 * matrix[i + 1][j];
            }
        }
        hasil[row - 3][0] = 6 * matrix[row - 2][0] - matrix[row - 1][0]; //baris kelima (S(6) s(7)
        hasil[row - 3][1] = 6 * matrix[row - 2][1] - matrix[row - 1][1]; //baris kelima (S(6) s(7)
        return hasil;
    }

    public static float[][] tengah(float[] s1, float[] b1, float[] b2, float[] s2) {
        float[] a = Matriks.tambah(Matriks.kaliMatrix(2f / 3f, b1), Matriks.kaliMatrix(1f / 3f, b2));
        float[] b = Matriks.tambah(Matriks.kaliMatrix(1f / 3f, b1), Matriks.kaliMatrix(2f / 3f, b2));
        float[][] hasil = {s1, a, b, s2};
        return Matriks.transpose(hasil);
    }

    public static void openSpline(float[][] a) {
        if (a.length <= 3) {
            return;
        }
        float[][] m1 = Matriks.invers(m1(a));
        float[][] m2 = m2(a);
        float[][] m3 = Matriks.kaliMatrix(m1, m2); //3
        float[][] m3m = new float[m3.length + 2][2]; //5
        System.arraycopy(a[0], 0, m3m[0], 0, 2); //1
        for (int i = 1; i < m3m.length - 1; i++) { //2-4
            System.arraycopy(m3[i - 1], 0, m3m[i], 0, 2); //1
        }
        System.arraycopy(a[a.length - 1], 0, m3m[m3m.length - 1], 0, 2); //1

        for (int i = 0; i < a.length - 1; i++) {
            float[] s1 = a[i];
            float[] b1 = m3m[i];
            float[] b2 = m3m[i + 1];
            float[] s2 = a[i + 1];
            float[][] t1 = tengah(s1, b1, b2, s2);
            float[][] t2 = Matriks.kaliMatrix(Matriks.kaliMatrix(t1, Bezier.basisBezier), Bezier.nilaiT());
        }
    }

    public static void closeSpline(float[][] a) {
        if (a.length <= 3) {
            return;
        }
        float[][] a2 = new float[a.length + 2][a[0].length];
        for (int i = 0; i < a.length; i++) {
            System.arraycopy(a[i], 0, a2[i], 0, 2); //1
        }
        System.arraycopy(a[1], 0, a2[a.length], 0, 2); //1
        System.arraycopy(a[2], 0, a2[a.length+1], 0, 2); //1
        float[][] m1 = Matriks.invers(m1(a2));
        float[][] m2 = m2(a2);
        float[][] m3 = Matriks.kaliMatrix(m1, m2); //3
        float[][] m3m = new float[m3.length + 2][2]; //5
        System.arraycopy(a2[0], 0, m3m[0], 0, 2); //1
        for (int i = 1; i < m3m.length - 1; i++) { //2-4
            System.arraycopy(m3[i - 1], 0, m3m[i], 0, 2); //1
        }
        System.arraycopy(a2[a2.length - 1], 0, m3m[m3m.length - 1], 0, 2); //1

        for (int i = 1; i < a2.length - 2; i++) {
            float[] s1 = a2[i];
            float[] b1 = m3m[i];
            float[] b2 = m3m[i + 1];
            float[] s2 = a2[i + 1];
            float[][] t1 = tengah(s1, b1, b2, s2);
            float[][] t2 = Matriks.kaliMatrix(Matriks.kaliMatrix(t1, Bezier.basisBezier), Bezier.nilaiT());
        }
    }
}
