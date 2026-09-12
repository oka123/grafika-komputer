package com.example.grafikakomputer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * View kustom untuk menampilkan bidang koordinat Cartesius (sumbu + grid)
 * dan menggambar semua objek Garis yang sudah dibuat (DDA maupun Bresenham)
 * sekaligus dalam satu bidang gambar.
 * <p>
 * Mendukung ZOOM (cubit dua jari / pinch-to-zoom) dan GESER (geser satu jari),
 * serta double-tap untuk reset ke posisi semula.
 */
public class CanvasGarisView extends View {

    // Daftar semua garis yang sudah ditambahkan pengguna
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
        paintGrid.setColor(Color.parseColor("#E0E0E0"));
        paintGrid.setStrokeWidth(1f);

        paintSumbu = new Paint();
        paintSumbu.setColor(Color.parseColor("#37474F"));
        paintSumbu.setStrokeWidth(3f);
        paintSumbu.setAntiAlias(true);

        paintDDA = new Paint();
        paintDDA.setColor(Color.parseColor("#1976D2")); // Biru untuk DDA
        paintDDA.setStrokeWidth(6f);
        paintDDA.setAntiAlias(true);

        paintBresenham = new Paint();
        paintBresenham.setColor(Color.parseColor("#D32F2F")); // Merah untuk Bresenham
        paintBresenham.setStrokeWidth(6f);
        paintBresenham.setAntiAlias(true);

        paintTeks = new Paint();
        paintTeks.setColor(Color.parseColor("#263238"));
        paintTeks.setTextSize(26f);
        paintTeks.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        paintTeks.setAntiAlias(true);

        paintLabelAngka = new Paint();
        paintLabelAngka.setColor(Color.parseColor("#546E7A"));
        paintLabelAngka.setTextSize(20f);
        paintLabelAngka.setAntiAlias(true);
        paintLabelAngka.setTextAlign(Paint.Align.CENTER);

        // Detector untuk pinch-to-zoom (cubit dua jari)
        scaleGestureDetector = new ScaleGestureDetector(context, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(@NonNull ScaleGestureDetector detector) {
                skalaZoom *= detector.getScaleFactor();
                skalaZoom = Math.max(ZOOM_MIN, Math.min(ZOOM_MAX, skalaZoom));
                invalidate();
                return true;
            }
        });

        // Detector untuk geser satu jari (pan) dan double-tap reset zoom
        gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onScroll(MotionEvent e1, @NonNull MotionEvent e2, float distanceX, float distanceY) {
                offsetX -= distanceX;
                offsetY -= distanceY;
                invalidate();
                return true;
            }

            @Override
            public boolean onDoubleTap(@NonNull MotionEvent e) {
                resetTampilan();
                return true;
            }
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scaleGestureDetector.onTouchEvent(event);
        // Cegah konflik jitter scroll saat pinch zoom sedang aktif
        if (!scaleGestureDetector.isInProgress()) {
            gestureDetector.onTouchEvent(event);
        }
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

    /** Reset zoom dan posisi geser ke kondisi awal */
    public void resetTampilan() {
        skalaZoom = 1f;
        offsetX = 0f;
        offsetY = 0f;
        invalidate();
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        float lebar = getWidth();
        float tinggi = getHeight();
        // Titik pusat (0,0) Cartesius = tengah layar + hasil geseran (pan) user
        float pusatX = lebar / 2f + offsetX;
        float pusatY = tinggi / 2f + offsetY;
        // Skala grid efektif setelah dikali faktor zoom
        float skala = SKALA_DASAR * skalaZoom;

        // Ketebalan garis grid & sumbu
        float density = getResources().getDisplayMetrics().density;
        paintGrid.setStrokeWidth(Math.max(1f, density));
        paintSumbu.setStrokeWidth(Math.max(1.5f * density, Math.min(3.5f * density, 2f * density * skalaZoom)));

        // Ukuran teks angka koordinat rapi & proporsional (10-11sp)
        float ukuranLabel = 11f * density;
        paintLabelAngka.setTextSize(ukuranLabel);
        paintLabelAngka.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        paintLabelAngka.setColor(Color.parseColor("#455A64"));

        float ukuranTeksSumbu = 14f * density;
        paintTeks.setTextSize(ukuranTeksSumbu);
        paintTeks.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

        gambarGrid(canvas, lebar, tinggi, pusatX, pusatY, skala);
        gambarSumbu(canvas, lebar, tinggi, pusatX, pusatY, density);
        gambarLabelAngka(canvas, lebar, tinggi, pusatX, pusatY, skala, ukuranLabel, density);

        // Render pixel garis (mengambil data dari cache objek Garis)
        for (Garis garis : daftarGaris) {
            Paint paint = (garis.getAlgoritma() == Garis.Algoritma.DDA) ? paintDDA : paintBresenham;
            for (int[] titik : garis.generateTitik()) {
                float sx = pusatX + titik[0] * skala;
                // Sumbu y layar terbalik (ke bawah), jadi dibalik agar Cartesius normal (atas positif)
                float sy = pusatY - titik[1] * skala;
                canvas.drawRect(sx - skala / 2f + 0.5f, sy - skala / 2f + 0.5f,
                        sx + skala / 2f - 0.5f, sy + skala / 2f - 0.5f, paint);
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
     * Menghitung interval lompatan angka sumbu koordinat agar angka tidak saling bertumpuk.
     */
    private int hitungLompat(float skala, float density) {
        float minJarakPixel = 38f * density;
        float raw = minJarakPixel / skala;
        if (raw <= 1f) return 1;
        if (raw <= 2f) return 2;
        if (raw <= 5f) return 5;
        if (raw <= 10f) return 10;
        if (raw <= 20f) return 20;
        if (raw <= 50f) return 50;
        return 100;
    }

    /**
     * Menampilkan angka koordinat di sepanjang sumbu X dan Y.
     */
    private void gambarLabelAngka(Canvas canvas, float lebar, float tinggi, float pusatX, float pusatY, float skala, float ukuranLabel, float density) {
        int lompat = hitungLompat(skala, density);
        float offsetLabelY = ukuranLabel + 4f * density;
        float offsetLabelX = 6f * density;

        // Label sumbu X (di bawah garis sumbu X)
        paintLabelAngka.setTextAlign(Paint.Align.CENTER);
        int unitKanan = (int) ((lebar - pusatX) / skala) + 1;
        int unitKiri = (int) (pusatX / skala) + 1;
        for (int i = -unitKiri; i <= unitKanan; i++) {
            if (i == 0 || i % lompat != 0) continue;
            float sx = pusatX + i * skala;
            if (sx >= -50 && sx <= lebar + 50) {
                canvas.drawText(String.valueOf(i), sx, pusatY + offsetLabelY, paintLabelAngka);
            }
        }

        // Label sumbu Y (di kiri garis sumbu Y)
        paintLabelAngka.setTextAlign(Paint.Align.RIGHT);
        int unitAtas = (int) (pusatY / skala) + 1;
        int unitBawah = (int) ((tinggi - pusatY) / skala) + 1;
        for (int i = -unitBawah; i <= unitAtas; i++) {
            if (i == 0 || i % lompat != 0) continue;
            float sy = pusatY - i * skala;
            if (sy >= -50 && sy <= tinggi + 50) {
                canvas.drawText(String.valueOf(i), pusatX - offsetLabelX, sy + (ukuranLabel * 0.35f), paintLabelAngka);
            }
        }
    }

    private void gambarSumbu(Canvas canvas, float lebar, float tinggi, float pusatX, float pusatY, float density) {
        canvas.drawLine(0, pusatY, lebar, pusatY, paintSumbu); // Sumbu X
        canvas.drawLine(pusatX, 0, pusatX, tinggi, paintSumbu); // Sumbu Y

        paintTeks.setTextAlign(Paint.Align.LEFT);
        float teksSize = paintTeks.getTextSize();
        canvas.drawText("X", lebar - (teksSize + 8f * density), Math.max(teksSize + 4f, Math.min(tinggi - 10f, pusatY - 6f * density)), paintTeks);
        canvas.drawText("Y", Math.max(8f * density, Math.min(lebar - (teksSize + 8f * density), pusatX + 8f * density)), teksSize + 4f * density, paintTeks);
        canvas.drawText("O", pusatX + 6f * density, pusatY + teksSize + 2f * density, paintTeks);
    }
}