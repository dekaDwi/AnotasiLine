package com.demeg.anotasiline;

/**
 * Updated by deka dwi on 01-Oct-15.
 */

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.util.TimingLogger;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.demeg.anotasiline.TouchImageViewNew.TouchImageView;
import com.demeg.anotasiline.matriks.Bezier;
import com.demeg.anotasiline.matriks.Matriks;
import com.demeg.anotasiline.matriks.Spline;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class MainActivity extends ActionBarActivity {



    final int RQS_IMAGE1 = 1;
    Button btnLoadImage, simpanMembran, simpanAnotasi;
    TextView textJudul, textLokasi, textResolusi, jumlahMembran;
    TouchImageView imageResult;
    List<float[]> l = new ArrayList<float[]>();
    List<int[]> st = new ArrayList<int[]>();
    List<int[]> l2 = new ArrayList<int[]>();
    Drawable drawable;
    Rect imageBounds;
    KelompokSel Membran;
    Sel s;
    int x, y, cnt = 0;
    String lks;
    double scalH, scalW;
    boolean boleh = false;
    Uri source;
    Bitmap bitmapMaster, b, label;
    Canvas canvasMaster, label2;
    Stack<Bitmap> undos = new Stack<Bitmap>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnLoadImage = (Button)findViewById(R.id.btnPilihBerkas);
        simpanMembran = (Button)findViewById(R.id.btnSimpanMembran);
        simpanMembran.setEnabled(false);
        simpanAnotasi = (Button)findViewById(R.id.btnSimpan);
        simpanAnotasi.setEnabled(false);
        textJudul = (TextView)findViewById(R.id.namaBerkas);
        textResolusi = (TextView)findViewById(R.id.resolusi);
        textLokasi = (TextView)findViewById(R.id.lokasiBerkas);
        jumlahMembran = (TextView)findViewById(R.id.infoJumlahMembran);
        imageResult = (TouchImageView)findViewById(R.id.gambar);
        s = new Sel();
        b = null;

        btnLoadImage.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, RQS_IMAGE1);
                imageResult.resetZoom();
                s = new Sel();
                boleh = false;
                l = new ArrayList<float[]>();
                simpanMembran.setEnabled(false);
                simpanAnotasi.setEnabled(false);
                undos.clear();
                Membran = new KelompokSel("Membran");
                jumlahMembran.setText("Jumlah membran : " + Membran.count);
                cnt = 0;
            }
        });

        imageResult.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //boleh ==> jika sudah melakukan cek berkas
                if (boleh) {
                    int action = event.getAction();
                    x = (int) event.getX();
                    y = (int) event.getY();
                    PointF bitmapPoint = imageResult.transformCoordTouchToBitmap(event.getX(), event.getY(), true);
                    PointF normalizedBitmapPoint = new PointF(bitmapPoint.x / bitmapMaster.getWidth(), bitmapPoint.y / bitmapMaster.getHeight());
                    int xN = Math.round(normalizedBitmapPoint.x * bitmapMaster.getWidth());
                    int yN = Math.round(normalizedBitmapPoint.y * bitmapMaster.getHeight());
//                    Bitmap b;
                    switch (action) {
                        case MotionEvent.ACTION_DOWN:
                            if (cnt == 0) {
                                //menyimpan bitmap awal, untuk semacam undo
                                l = new ArrayList<float[]>();
                                b = Bitmap.createBitmap(bitmapMaster);
                                undos.push(b);
                                b = null;
                                s = new Sel();
                                cnt = 1;
                            } else {
                                //mengembalikan bitmap yang telah disimpan, jika ada
                                cnt = 0;
                                b = undos.pop();
                                canvasMaster.drawBitmap(b, 0, 0, null);
                                l = new ArrayList<float[]>();
                                undos.push(b);
                                b = null;
                                s = new Sel();
                                imageResult.invalidate();
                            }
                            simpanMembran.setEnabled(false);
                            break;
                        case MotionEvent.ACTION_UP:
                            float[][] a = new float[l.size()][2];
                            for (int i = 0; i < l.size(); i++) {
                                for (int j = 0; j < 2; j++) {
                                    a[i][j] = l.get(i)[j];
                                }
                            }

                            closeSpline(a);

                            break;
                        case MotionEvent.ACTION_MOVE:
                            //menandai sel/membran
                            float[] titik = {xN, yN};
                            l.add(titik);
                            cnt = 1;
                            point2(xN, yN, 1);
                            simpanMembran.setEnabled(true);
                            break;
                    }
                }

            /*
             * Return 'true' to indicate that the event have been consumed.
             * If auto-generated 'false', your code can detect ACTION_DOWN only,
             * cannot detect ACTION_MOVE and ACTION_UP.
             */
                return true;
            }
        });

        simpanMembran.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                TimingLogger timings = new TimingLogger("TopicLogTag", "simpan membran");
                rapikan();
                Membran.addLast(s);
                jumlahMembran.setText("Jumlah membran : " + Membran.count);
                canvasMaster.drawBitmap(undos.pop(), 0, 0, null);
                imageResult.invalidate();
                undos.clear();
                cnt = 0;
                simpanAnotasi.setEnabled(true);
                Elemen temp = s.head;
                if (!s.isEmpty()) {
                    point2(temp.x, temp.y, 2);
//                    list.add(new int[]{x, y});
                    while (temp.next != null) {
                        temp = temp.next;
                        point2(temp.x, temp.y, 2);
//                        list.add(new int[]{x, y});
                    }
                }
                int[][] koor = sort(l2);
                label(koor, Membran.count);

                s = new Sel();
                timings.addSplit("simpan membran");
                timings.dumpToLog();
            }
        });

        simpanAnotasi.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                simpanFile();
                simpanLabel();
            }
        });

    }

    public void rapikan(){
        s.enQueue(st.get(0)[0], st.get(0)[1]);
        for (int i = 1; i < st.size(); i++) {
            if(st.get(i)[0] == st.get(i-1)[0] && st.get(i)[1] == st.get(i-1)[1]){
            } else {
                s.enQueue(st.get(i)[0], st.get(i)[1]);
            }
        }
    }

    public int[][] sort(List<int[]> koor) {
        TimingLogger timings = new TimingLogger("TopicLogTag", "sorting koordinat");
        int[][] hasil = new int[koor.size()][2];

        for (int i = 0; i < koor.size(); i++) {
            hasil[i] = koor.get(i);
        }

        for (int i = 1; i < koor.size(); i++) {
            for (int j = koor.size() - 1; j >= i; j--) {
                if (hasil[j - 1][1] > hasil[j][1]) {
                    int[] temp = hasil[j - 1];
                    hasil[j - 1] = hasil[j];
                    hasil[j] = temp;
                }
            }
        }
        timings.addSplit("sorting");
        timings.dumpToLog();
        return hasil;
    }

    public void label(int[][] koor, int obj) {
        TimingLogger timings = new TimingLogger("TopicLogTag", "labeling");
        int ymin = koor[0][1];
        int ymax = koor[koor.length - 1][1];
        for (int i = ymin; i <= ymax; i++) {
            int xmax = 0;
            for (int j = 0; j < koor.length; j++) {
                if (koor[j][1] == i) {
                    if (xmax < koor[j][0]) {
                        xmax = koor[j][0];
                    }
                }
            }
            int xmin = xmax;
            for (int j = 0; j < koor.length; j++) {
                if (koor[j][1] == i) {
                    if (xmin > koor[j][0]) {
                        xmin = koor[j][0];
                    }
                }
            }

            for (int j = xmin; j <= xmax; j++) {
                point3(j, i, obj);
            }
        }
        timings.addSplit("labelling");
        timings.dumpToLog();
    }

    public void closeSpline(float[][] a) {
        TimingLogger timings = new TimingLogger("TopicLogTag", "closeSpline");
        l2 = new ArrayList<int[]>();
        if (a.length <= 3) {
            return;
        }
        float[][] a2 = new float[a.length + 2][a[0].length];
        for (int i = 0; i < a.length; i++) {
            System.arraycopy(a[i], 0, a2[i], 0, 2); //1
        }
        System.arraycopy(a[1], 0, a2[a.length], 0, 2); //1
        System.arraycopy(a[2], 0, a2[a.length+1], 0, 2); //1
        float[][] m1 = Matriks.invers(Spline.m1(a2));
        float[][] m2 = Spline.m2(a2);
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
            float[][] t1 = Spline.tengah(s1, b1, b2, s2);
            float[][] t2 = Matriks.kaliMatrix(Matriks.kaliMatrix(t1, Bezier.basisBezier), Bezier.nilaiT());
            for (int b = 0; b < t2[0].length; b++) {
                int x = Math.round(t2[1][b]);
                int y = Math.round(t2[0][b]);
                point2(y,x,3);
                int[] titik = {y, x};
                st.add(titik);
                l2.add(titik);
                s.enQueue(y, x);
            }
        }
        timings.addSplit("Reconstruct the curve");
        timings.dumpToLog();

    }

    //membalikkan koordinat dari koordinat scale image menjadi koordinat asli
    final float[] getPointerCoords(ImageView view, MotionEvent e) {
        final int index = e.getActionIndex();
        final float[] coords = new float[] {e.getX(index), e.getY(index)};
        Matrix matrix = new Matrix();
        view.getImageMatrix().invert(matrix);
        matrix.postTranslate(view.getScrollX(), view.getScrollY());
        matrix.mapPoints(coords);
        return coords;
    }

    //membalikkan koordinat dari koordinat asli menjadi koordinat scale image
    final float[] coba(ImageView view, int x, int y) {
        double intrH = drawable.getIntrinsicHeight();
        double intrW = drawable.getIntrinsicWidth();
        scalW = imageResult.getWidth();
        scalH = imageResult.getHeight();
        double ratioH = scalH / intrH;
        double ratioW = scalW / intrW;
        x = (int)(x * ratioW);
        y = (int)(y * ratioH);
        final float[] coords = new float[] {x, y};
        return coords;
    }

    //penyimpanan file
    public void simpanFile() {
        TimingLogger timings = new TimingLogger("TopicLogTag", "simpanFile");
        String namaFile = textJudul.getText().toString();
        int name = namaFile.lastIndexOf(".");
        String nama = namaFile.substring(0, name) + ".txt";
        try {
            File root = new File(Environment.getExternalStorageDirectory(), "Notes");
            if(!root.exists()) {
                root.mkdirs();
            }
            File file = new File(root, nama);
            Writer wr = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF8"));
            wr.append("Anotasi dari file " + namaFile + "\n");
            wr.append(Membran.isi() + "\n");
            wr.flush();
            wr.close();
            Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
        timings.addSplit("simpan file");
        timings.dumpToLog();
    }

    public void simpanLabel() {
        String namaFile = textJudul.getText().toString();
        int name = namaFile.lastIndexOf(".");
        String nama = namaFile.substring(0, name) + ".png";
        try {
            File root = new File(Environment.getExternalStorageDirectory(), "Notes");
            if(!root.exists()) {
                root.mkdirs();
            }
            File file = new File(root, nama);
            label.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(file));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //pembacaan file anotasi dari file yang sama jika ada
    public void readFile(){
        TimingLogger timings = new TimingLogger("TopicLogTag", "membaca file");
        String namaFile = textJudul.getText().toString();
        int name = namaFile.lastIndexOf(".");
        String nama = namaFile.substring(0, name) + ".txt";
        File sdCard = new File(Environment.getExternalStorageDirectory(), "Notes");
        File file = new File(sdCard, nama);
        Sel s;
        if(file.exists()) {
            try {
                BufferedReader br = new BufferedReader(new FileReader(file));
                String line;
                int c = 1;
                while((line=br.readLine()) != null) {
                    if(c==1) {
                        c++;
                    } else {
                        if(line.indexOf("/") == -1) {
                            Membran = new KelompokSel(line);
                        } else if(line.indexOf("/")!=-1) {
                            s = new Sel();
                            String angka = line;
                            int awal = angka.indexOf("(");
                            int x = Integer.parseInt(angka.substring(awal + 1, angka.indexOf(",", awal)));
                            int y = Integer.parseInt(angka.substring(angka.indexOf(",", awal) + 2, angka.indexOf(")", awal + 1)));
                            s.enQueue(x, y);
                            while (angka.indexOf("(", awal + 1) != -1) {
                                awal = angka.indexOf("(", awal + 1);
                                x = Integer.parseInt(angka.substring(awal + 1, angka.indexOf(",", awal)));
                                y = Integer.parseInt(angka.substring(angka.indexOf(",", awal) + 2, angka.indexOf(")", awal + 1)));
                                s.enQueue(x, y);
                            }
                            Membran.addLast(s);
                        } else if(line.substring(0, 5).equals("Belum")) {
                        }
                    }
                }
                br.close();
                jumlahMembran.setText("Jumlah membran : " + Membran.count);
                gambarSel();
            } catch (IOException e) {
            }
        } else {
            Toast.makeText(this, "File belum pernah dianotasi", Toast.LENGTH_SHORT).show();
            simpanAnotasi.setEnabled(false);
            Membran = new KelompokSel("Membran");
            jumlahMembran.setText("Jumlah membran : " + Membran.count);
        }
        timings.addSplit("Membaca file");
        timings.dumpToLog();

    }

    //bagian dari pembacaan file, untuk menggambar yang telah dianotasi
    public void gambarSel(){
        int obj = 1;
        l2 = new ArrayList<int[]>();
        Elemen e;
        Sel s;
//        int x, y;
        s = Membran.head;
        if(s != null) {
            e = s.head;
            if(e != null) {
                point2(e.x, e.y, 2);
                l2.add(new int[]{e.x, e.y});
                while(e.next != null) {
                    e = e.next;
                    point2(e.x, e.y, 2);
                    l2.add(new int[]{e.x, e.y});
                }
                int[][] koor = sort(l2);
                label(koor, obj);
                obj++;
            }
            while(s.next != null) {
//                l2 = new ArrayList<int[]>();
                s = s.next;
                e = s.head;
                if(e != null) {
                    point2(e.x, e.y, 2);
                    l2.add(new int[]{e.x, e.y});
                    while(e.next != null) {
                        e = e.next;
                        point2(e.x, e.y, 2);
                        l2.add(new int[]{e.x, e.y});
                    }
                }
                int[][] koor = sort(l2);
                label(koor, obj);
                obj++;
            }
        }

    }

    //untuk menggambar titik sesuai dengan masukkan touch dari user
    public void point(int x, int y, int kondisi){
        if(x<0 || y<0 || x>imageResult.getWidth() || y>imageResult.getHeight()) {
            return;
        } else {
            int projectedX = (int)((double)x * ((double)bitmapMaster.getWidth()/(double)imageResult.getWidth()));
            int projectedY = (int)((double)y * ((double)bitmapMaster.getHeight()/(double)imageResult.getHeight()));
            Paint paint = new Paint();
            paint.setStyle(Paint.Style.FILL);
            switch (kondisi) {
                case 1 :
                    paint.setColor(Color.CYAN);
                    break;
                case 2 :
                    paint.setColor(Color.RED);
                    break;
                case 3 :
                    paint.setColor(Color.GREEN);
                    break;
            }
            paint.setStrokeWidth(3);
            canvasMaster.drawCircle(projectedX, projectedY, 3, paint);
            imageResult.invalidate();
        }
    }

    public void point2(int x, int y, int kondisi){
        if(x<0 || y<0 || x>bitmapMaster.getWidth() || y>bitmapMaster.getHeight()) {
            return;
        } else {
            int projectedX = x;
            int projectedY = y;
            Paint paint = new Paint();
            paint.setStyle(Paint.Style.FILL);
            switch (kondisi) {
                case 1 :
                    paint.setColor(Color.YELLOW);
                    break;
                case 2 :
                    paint.setARGB(255, 255, 0, 0);
                    break;
                case 3 :
                    paint.setColor(Color.BLUE);
                    break;
            }
            paint.setStrokeWidth(3);
            canvasMaster.drawCircle(projectedX, projectedY, 3, paint);
            imageResult.invalidate();
        }
    }

    public void point3(int x, int y, int obj){
        if (x < 0 || y < 0 || x >bitmapMaster.getWidth() || y>bitmapMaster.getHeight()) {
            return;
        } else {
            int projectedX = x;
            int projectedY = y;
            Paint paint = new Paint();
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.rgb(obj,obj,obj));
            paint.setStrokeWidth(3);
            label2.drawCircle(projectedX, projectedY, 3, paint);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        TimingLogger timings = new TimingLogger("TopicLogTag", "membukaGambar");
        super.onActivityResult(requestCode, resultCode, data);
        Bitmap tempBitmap;
        if(resultCode == RESULT_OK){
            switch (requestCode){
                case RQS_IMAGE1:
                    source = data.getData();
                    int a = source.getPath().lastIndexOf("/");
                    lks = source.getPath().substring(0, a);
                    String lokasi = ImageFilePath.getPath(getApplicationContext(), source);
                    int name = lokasi.lastIndexOf("/");
                    String nama = lokasi.substring(name + 1);
                    int b = lokasi.lastIndexOf("/");
                    lokasi = lokasi.substring(0, b+1);
                    textLokasi.setText(lokasi);
                    textJudul.setText(nama);
                    try {
                        tempBitmap = BitmapFactory.decodeStream(
                                getContentResolver().openInputStream(source));
                        Bitmap.Config config;
                        if(tempBitmap.getConfig() != null) {
                            config = tempBitmap.getConfig();
                        } else {
                            config = Bitmap.Config.ARGB_8888;
                        }
                        bitmapMaster = Bitmap.createBitmap(tempBitmap.getWidth(), tempBitmap.getHeight(), config);
                        label = Bitmap.createBitmap(tempBitmap.getWidth(), tempBitmap.getHeight(), config);
                        undos.add(bitmapMaster);
                        canvasMaster = new Canvas(bitmapMaster);
                        label2 = new Canvas(label);
                        canvasMaster.drawBitmap(tempBitmap, 0, 0, null);
                        label2.drawBitmap(label, 0, 0, null);
                        imageResult.setImageBitmap(bitmapMaster);
                        drawable = imageResult.getDrawable();
                        imageBounds = drawable.getBounds();
                        int x = bitmapMaster.getWidth();
                        int y = bitmapMaster.getHeight();
                        textResolusi.setText(x + " x " + y);
                        timings.addSplit("Membuka gambar");
                        timings.dumpToLog();
                        readFile();
                        boleh = true;
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}