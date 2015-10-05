package com.demeg.anotasiline.matriks;

import java.text.DecimalFormat;

/**
 * Created by deka dwi on 05-Oct-15.
 */
public class Bezier {
    public final static float[][] basisBezier = {
            {1, -3, 3, -1},
            {0, 3, -6, 3},
            {0, 0, 3, -3},
            {0, 0, 0, 1}
    };

    public static float[][] nilaiT() {
        DecimalFormat fd = new DecimalFormat("#.###");
        float[] t = new float[101];
        float nilai = 0;
        int x = 1;
        t[0] = nilai;
        nilai += 0.01;
        while (nilai <= 1) {
            t[x] = Float.valueOf(fd.format(nilai));
            nilai += 0.01;
            x++;
        }
        fd = new DecimalFormat("#.####");
        float[][] T = new float[4][101];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 101; j++) {
                T[i][j] = Float.valueOf(fd.format(Math.pow(t[j], i)));
            }
        }
        return T;
    }

    public static float[][] kurvaBezier(float[][] titik) {
        if (titik.length != 2 || titik[0].length != 4) {
            return null;
        }
        return Matriks.kaliMatrix(Matriks.kaliMatrix(titik, basisBezier), nilaiT());
    }
}
