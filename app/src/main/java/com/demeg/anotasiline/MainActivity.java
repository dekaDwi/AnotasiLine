package com.demeg.anotasiline;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Stack;

//import com.demeg.anotasiline.ImageViewNew.GestureImageView;
//import com.demeg.anotasiline.MultiTouch.TouchImageView;


public class MainActivity extends ActionBarActivity {

    final int RQS_IMAGE1 = 1;
    Button btnLoadImage, simpanMembran, simpanAnotasi, cekBerkas;
    TextView textJudul, textLokasi, textResolusi, jumlahMembran, xy1, xy2, skala;
//    GestureImageView imageResult;
//    TouchImageView imageResult;
    ImageView imageResult;
    Drawable drawable;
    Rect imageBounds;
    KelompokSel Membran;
    Sel s, sTemp;
    int x, y, cnt = 0;
    String lks;
    double scalH, scalW;
    boolean boleh;
    Uri source;
    Bitmap bitmapMaster;
    Canvas canvasMaster;
    Stack<Bitmap> undos = new Stack<Bitmap>();
    Stack<Bitmap> undos2 = new Stack<Bitmap>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnLoadImage = (Button)findViewById(R.id.btnPilihBerkas);
        cekBerkas = (Button)findViewById(R.id.btnCekBerkas);
        cekBerkas.setEnabled(false);
        simpanMembran = (Button)findViewById(R.id.btnSimpanMembran);
        simpanMembran.setEnabled(false);
        simpanAnotasi = (Button)findViewById(R.id.btnSimpan);
        simpanAnotasi.setEnabled(false);
        textJudul = (TextView)findViewById(R.id.namaBerkas);
        textResolusi = (TextView)findViewById(R.id.resolusi);
        textLokasi = (TextView)findViewById(R.id.lokasiBerkas);
        jumlahMembran = (TextView)findViewById(R.id.infoJumlahMembran);
//        imageResult = (GestureImageView)findViewById(R.id.gambar);
//        imageResult = (TouchImageView)findViewById(R.id.gambar);
        imageResult = (ImageView)findViewById(R.id.gambar);
        xy1 = (TextView)findViewById(R.id.XY1);
        xy2 = (TextView)findViewById(R.id.XY2);
        skala = (TextView)findViewById(R.id.skala);
        s = new Sel();
        sTemp = new Sel();
        boleh = false;

        btnLoadImage.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, RQS_IMAGE1);
                s = new Sel();
                sTemp = new Sel();
                cekBerkas.setEnabled(true);
                boleh = false;
                simpanMembran.setEnabled(false);
                simpanAnotasi.setEnabled(false);
                undos.clear();
                undos2.clear();
                Membran = new KelompokSel("Membran");
                jumlahMembran.setText("Jumlah membran : " + Membran.count);
            }
        });

        cekBerkas.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                readFile();
                boleh = true;
                cekBerkas.setEnabled(false);
            }
        });

        imageResult.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //boleh ==> jika sudah melakukan cek berkas
                if(boleh) {
                    int action = event.getAction();
//                    float skl = imageResult.getScale();
                    int ww = bitmapMaster.getWidth();
                    int hh = bitmapMaster.getHeight();
                    x = (int) event.getX();
                    y = (int) event.getY();
                    int originalX = (int) getPointerCoords(imageResult, event)[0];
                    int originalY = (int) getPointerCoords(imageResult, event)[1];
                    xy1.setText("X1: " + originalX + "   || Y1: " + originalY);
                    xy2.setText("X1: " + x + "   || Y1: " + y);
//                    skala.setText("TX : " + xb + " || TY : " + yb);
                    Bitmap b;
                    switch (action) {
                        case MotionEvent.ACTION_DOWN:
                            if (cnt == 0) {
                                //menyimpan bitmap awal, untuk semacam undo
                                b = Bitmap.createBitmap(bitmapMaster);
                                undos.push(b);
                                b = null;
                                s = new Sel();
                                sTemp = new Sel();
                                cnt = 1;
                            } else {
                                //mengembalikan bitmap yang telah disimpan, jika ada
                                cnt = 0;
                                b = undos.pop();
                                canvasMaster.drawBitmap(b, 0, 0, null);
                                undos.push(b);
                                b = null;
                                s = new Sel();
                                sTemp = new Sel();
                                imageResult.invalidate();
                            }
                            simpanMembran.setEnabled(false);
                            break;
                        case MotionEvent.ACTION_MOVE:
                            //menandai sel/membran
                            cnt = 1;
                            point(originalX, originalY, 1);
                            if (originalX >= 0 && originalY >= 0 && originalX <= bitmapMaster.getWidth() && originalY <= bitmapMaster.getHeight()) {
                                s.enQueue(originalX, originalY);
                            }
                            sTemp.enQueue(x, y);
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
                Membran.addLast(s);
                jumlahMembran.setText("Jumlah membran : " + Membran.count);
                canvasMaster.drawBitmap(undos.pop(), 0, 0, null);
                imageResult.invalidate();
                undos.clear();
                cnt = 0;
                simpanAnotasi.setEnabled(true);
                Elemen temp = sTemp.head;
                if (!s.isEmpty()) {
                    point(temp.x, temp.y, 2);
                    while (temp.next != null) {
                        temp = temp.next;
                        point(temp.x, temp.y, 2);
                    }
                }
                s = new Sel();
                sTemp = new Sel();
            }
        });

        simpanAnotasi.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                simpanFile();
            }
        });
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
    }

    //pembacaan file anotasi dari file yang sama jika ada
    public void readFile(){
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

    }

    //bagian dari pembacaan file, untuk menggambar yang telah dianotasi
    public void gambarSel(){
        Elemen e;
        Sel s;
        int x, y;
        s = Membran.head;
        if(s != null) {
            e = s.head;
            if(e != null) {
                x = (int)coba(imageResult, e.x, e.y)[0];/* e.x;*/
                y = (int)coba(imageResult, e.x, e.y)[1];/* e.y;*/
                point(x, y, 2);
                while(e.next != null) {
                    e = e.next;
                    x = (int)coba(imageResult, e.x, e.y)[0];/* e.x;*/
                    y = (int)coba(imageResult, e.x, e.y)[1];/* e.y;*/
                    point(x, y, 2);
                }
            }
            while(s.next != null) {
                s = s.next;
                e = s.head;
                if(e != null) {
                    x = (int)coba(imageResult, e.x, e.y)[0];/* e.x;*/
                    y = (int)coba(imageResult, e.x, e.y)[1];/* e.y;*/
                    point(x, y, 2);
                    while(e.next != null) {
                        e = e.next;
                        x = (int)coba(imageResult, e.x, e.y)[0];/* e.x;*/
                        y = (int)coba(imageResult, e.x, e.y)[1];/* e.y;*/
                        point(x, y, 2);
                    }
                }
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
//            int projectedX = x;
//            int projectedY = y;
//            skala.setText("projcetedX : " + projectedX + " -||- projectedY : " + projectedY);
            Paint paint = new Paint();
            paint.setStyle(Paint.Style.FILL);
            switch (kondisi) {
                case 1 :
                    paint.setColor(Color.CYAN);
                    break;
                case 2 :
                    paint.setColor(Color.RED);
                    break;
            }
            paint.setStrokeWidth(3);
            canvasMaster.drawCircle(projectedX, projectedY, 3, paint);
            imageResult.invalidate();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
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
                        undos.add(bitmapMaster);
                        canvasMaster = new Canvas(bitmapMaster);
                        canvasMaster.drawBitmap(tempBitmap, 0, 0, null);
                        imageResult.setImageBitmap(bitmapMaster);
                        drawable = imageResult.getDrawable();
                        imageBounds = drawable.getBounds();
                        int x = bitmapMaster.getWidth();
                        int y = bitmapMaster.getHeight();
                        textResolusi.setText(x + " x " + y);
//                        xy1.setText("X1: " + imageResult.getTopLeft()[1] + "   || Y1: " + imageResult.getTopLeft()[0]);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    }
//
////    @Override
//    public void onWindowFocusChanged(boolean hasFocus) {
//        super.onWindowFocusChanged(hasFocus);
//        readFile();
//    }

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