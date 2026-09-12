package com.example.grafikakomputer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * View kustom untuk menampilkan bidang koordinat Cartesius (sumbu + grid)
 * dan menggambar semua objek Garis yang sudah dibuat (DDA maupun Bresenham)
 * sekaligus dalam satu bidang gambar.
 *
 * Mendukung ZOOM (cubit dua jari / pinch-to-zoom) dan GESER (geser satu jari),
 * supaya tiap titik/koordinat pada garis bisa dilihat lebih jelas.
 */
public class CanvasGarisView extends View {

    // Daftar semua garis yang sudah ditambahkan user
    private final List<Garis> daftarGaris = new ArrayList<>();

    // Ukuran dasar satu kotak grid dalam pixel layar (sebelum dikali skalaZoom)
    private static final float SKALA_DASAR = 20f;

    // Batas zoom supaya tidak kelewat kecil atau kelewat besar
    private static final float ZOOM_MIN = 0.4f;
    private static final float ZOOM_MAX = 8f;

    // Faktor zoom saat ini (1 = normal), dan pergeseran (pan) titik pusat
    private float skalaZoom = 1f;
    private float offsetX = 0f;
    private float offsetY = 0f;

    private Paint paintGrid;
    private Paint paintSumbu;
    private Paint paintDDA;
    private Paint paintBresenham;
    private Paint paintTeks;
    private Paint paintLabelAngka;

    private ScaleGestureDetector scaleGestureDetector;
    private GestureDetector gestureDetector;

    public CanvasGarisView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        paintGrid = new Paint();
        paintGrid.setColor(Color.LTGRAY);
        paintGrid.setStrokeWidth(1f);

        paintSumbu = new Paint();
        paintSumbu.setColor(Color.DKGRAY);
        paintSumbu.setStrokeWidth(3f);

        paintDDA = new Paint();
        paintDDA.setColor(Color.parseColor("#1976D2")); // biru untuk DDA
        paintDDA.setStrokeWidth(6f);
        paintDDA.setAntiAlias(true);

        paintBresenham = new Paint();
        paintBresenham.setColor(Color.parseColor("#D32F2F")); // merah untuk Bresenham
        paintBresenham.setStrokeWidth(6f);
        paintBresenham.setAntiAlias(true);

        paintTeks = new Paint();
        paintTeks.setColor(Color.BLACK);
        paintTeks.setTextSize(24f);

        paintLabelAngka = new Paint();
        paintLabelAngka.setColor(Color.DKGRAY);
        paintLabelAngka.setTextSize(16f);
        paintLabelAngka.setAntiAlias(true);
        paintLabelAngka.setTextAlign(Paint.Align.CENTER);

        // Detector untuk pinch-to-zoom (cubit dua jari)
        scaleGestureDetector = new ScaleGestureDetector(context, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                skalaZoom *= detector.getScaleFactor();
                skalaZoom = Math.max(ZOOM_MIN, Math.min(ZOOM_MAX, skalaZoom));
                invalidate();
                return true;
            }
        });

        // Detector untuk geser satu jari (pan) dan double-tap reset zoom
        gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                offsetX -= distanceX;
                offsetY -= distanceY;
                invalidate();
                return true;
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                // Double-tap untuk reset tampilan ke posisi & zoom awal
                skalaZoom = 1f;
                offsetX = 0f;
                offsetY = 0f;
                invalidate();
                return true;
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scaleGestureDetector.onTouchEvent(event);
        gestureDetector.onTouchEvent(event);
        return true;
    }

    /** Tambah garis baru lalu gambar ulang view */
    public void tambahGaris(Garis garis) {
        daftarGaris.add(garis);
        invalidate();
    }

    /** Bersihkan seluruh garis dari kanvas */
    public void bersihkanSemua() {
        daftarGaris.clear();
        invalidate();
    }

    /** Reset zoom dan posisi geser ke kondisi awal (dipanggil dari tombol jika perlu) */
    public void resetTampilan() {
        skalaZoom = 1f;
        offsetX = 0f;
        offsetY = 0f;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float lebar = getWidth();
        float tinggi = getHeight();
        // Titik pusat (0,0) Cartesius = tengah layar + hasil geseran (pan) user
        float pusatX = lebar / 2f + offsetX;
        float pusatY = tinggi / 2f + offsetY;
        // Skala grid efektif setelah dikali faktor zoom
        float skala = SKALA_DASAR * skalaZoom;

        // Ketebalan garis grid & sumbu ikut zoom, biar tetap terlihat jelas
        // saat di-zoom in maupun zoom out (tidak jadi tipis/hilang).
        paintGrid.setStrokeWidth(Math.max(1f, 1f * skalaZoom));
        paintSumbu.setStrokeWidth(Math.max(2f, 3f * skalaZoom));
        paintLabelAngka.setTextSize(16f * skalaZoom);
        paintTeks.setTextSize(24f * skalaZoom);

        gambarGrid(canvas, lebar, tinggi, pusatX, pusatY, skala);
        gambarSumbu(canvas, lebar, tinggi, pusatX, pusatY);
        gambarLabelAngka(canvas, lebar, tinggi, pusatX, pusatY, skala);

        for (Garis garis : daftarGaris) {
            Paint paint = (garis.getAlgoritma() == Garis.Algoritma.DDA) ? paintDDA : paintBresenham;
            for (int[] titik : garis.generateTitik()) {
                float sx = pusatX + titik[0] * skala;
                // sumbu y layar terbalik (ke bawah), jadi dibalik agar Cartesius normal (atas positif)
                float sy = pusatY - titik[1] * skala;
                canvas.drawRect(sx - skala / 2f + 1, sy - skala / 2f + 1,
                        sx + skala / 2f - 1, sy + skala / 2f - 1, paint);
            }
        }
    }

    private void gambarGrid(Canvas canvas, float lebar, float tinggi, float pusatX, float pusatY, float skala) {
        for (float x = pusatX; x < lebar; x += skala) canvas.drawLine(x, 0, x, tinggi, paintGrid);
        for (float x = pusatX; x > 0; x -= skala) canvas.drawLine(x, 0, x, tinggi, paintGrid);
        for (float y = pusatY; y < tinggi; y += skala) canvas.drawLine(0, y, lebar, y, paintGrid);
        for (float y = pusatY; y > 0; y -= skala) canvas.drawLine(0, y, lebar, y, paintGrid);
    }

    /**
     * Menampilkan angka koordinat di sepanjang sumbu X dan Y,
     * supaya posisi tiap titik garis mudah dibaca.
     * Jarak antar label (lompat) menyesuaikan skala saat ini,
     * makin di-zoom in, makin rapat label yang muncul (sampai per 1 unit).
     */
    private void gambarLabelAngka(Canvas canvas, float lebar, float tinggi, float pusatX, float pusatY, float skala) {
        int lompat = 1;
        if (skala < 16f) lompat = 5;
        else if (skala < 28f) lompat = 2;

        // Label sumbu X (di bawah garis sumbu X)
        int unitKanan = (int) ((lebar - pusatX) / skala) + 1;
        int unitKiri = (int) (pusatX / skala) + 1;
        for (int i = -unitKiri; i <= unitKanan; i += lompat) {
            if (i == 0) continue; // 0 sudah diwakili label O
            float sx = pusatX + i * skala;
            canvas.drawText(String.valueOf(i), sx, pusatY + 18, paintLabelAngka);
        }

        // Label sumbu Y (di kiri garis sumbu Y)
        int unitAtas = (int) (pusatY / skala) + 1;
        int unitBawah = (int) ((tinggi - pusatY) / skala) + 1;
        for (int i = -unitBawah; i <= unitAtas; i += lompat) {
            if (i == 0) continue;
            float sy = pusatY - i * skala;
            canvas.drawText(String.valueOf(i), pusatX - 16, sy + 5, paintLabelAngka);
        }
    }

    private void gambarSumbu(Canvas canvas, float lebar, float tinggi, float pusatX, float pusatY) {
        canvas.drawLine(0, pusatY, lebar, pusatY, paintSumbu); // sumbu X
        canvas.drawLine(pusatX, 0, pusatX, tinggi, paintSumbu); // sumbu Y
        canvas.drawText("X", lebar - 30, pusatY - 10, paintTeks);
        canvas.drawText("Y", pusatX + 10, 30, paintTeks);
        canvas.drawText("O", pusatX + 6, pusatY + 24, paintTeks);
    }
}