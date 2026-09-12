package com.example.grafikakomputer;

import java.util.ArrayList;
import java.util.List;

/**
 * Kelas Garis
 * Berisi method penggambaran garis menggunakan algoritma DDA dan Bresenham.
 * Masukan: dua titik koordinat (x1,y1) dan (x2,y2)
 * Keluaran: daftar titik (pixel) yang membentuk garis tersebut.
 */
public class Garis {

    // Titik ujung garis
    private int x1, y1, x2, y2;

    // Jenis algoritma yang dipakai, disimpan biar bisa dipakai info/label
    public enum Algoritma {
        DDA,
        BRESENHAM
    }

    private Algoritma algoritma;

    public Garis(int x1, int y1, int x2, int y2, Algoritma algoritma) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        this.algoritma = algoritma;
    }

    public Algoritma getAlgoritma() {
        return algoritma;
    }

    public int getX1() { return x1; }
    public int getY1() { return y1; }
    public int getX2() { return x2; }
    public int getY2() { return y2; }

    /**
     * Menghasilkan daftar titik pixel garis sesuai algoritma yang dipilih.
     */
    public List<int[]> generateTitik() {
        if (algoritma == Algoritma.DDA) {
            return hitungDDA();
        } else {
            return hitungBresenham();
        }
    }

    /**
     * Algoritma DDA (Digital Differential Analyzer)
     * Sesuai materi slide 5 & 10:
     * dx = x2-x1, dy = y2-y1
     * step = max(|dx|, |dy|)
     * x_inc = dx/step, y_inc = dy/step
     * plot titik dibulatkan setiap iterasi
     */
    private List<int[]> hitungDDA() {
        List<int[]> titikList = new ArrayList<>();

        float dx = x2 - x1;
        float dy = y2 - y1;

        int step;
        if (Math.abs(dx) > Math.abs(dy)) {
            step = Math.abs((int) dx);
        } else {
            step = Math.abs((int) dy);
        }

        // Kasus dua titik sama persis
        if (step == 0) {
            titikList.add(new int[]{x1, y1});
            return titikList;
        }

        float xInc = dx / step;
        float yInc = dy / step;

        float x = x1;
        float y = y1;

        titikList.add(new int[]{Math.round(x), Math.round(y)});

        for (int k = 0; k < step; k++) {
            x += xInc;
            y += yInc;
            titikList.add(new int[]{Math.round(x), Math.round(y)});
        }

        return titikList;
    }

    /**
     * Algoritma Bresenham (Midpoint Line Algorithm)
     * Sesuai materi slide 13 & 20, digeneralisasi agar
     * mendukung semua arah kemiringan (bukan hanya 0 < m < 1).
     */
    private List<int[]> hitungBresenham() {
        List<int[]> titikList = new ArrayList<>();

        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);

        int sx = (x1 < x2) ? 1 : -1;
        int sy = (y1 < y2) ? 1 : -1;

        int x = x1;
        int y = y1;

        if (dx >= dy) {
            // Kemiringan landai, iterasi berdasarkan sumbu x (seperti materi slide 20)
            int p = 2 * dy - dx;
            int duaDy = 2 * dy;
            int duaDyDx = 2 * (dy - dx);

            titikList.add(new int[]{x, y});
            for (int k = 0; k < dx; k++) {
                x += sx;
                if (p < 0) {
                    p += duaDy;
                } else {
                    y += sy;
                    p += duaDyDx;
                }
                titikList.add(new int[]{x, y});
            }
        } else {
            // Kemiringan curam, iterasi berdasarkan sumbu y (generalisasi)
            int p = 2 * dx - dy;
            int duaDx = 2 * dx;
            int duaDxDy = 2 * (dx - dy);

            titikList.add(new int[]{x, y});
            for (int k = 0; k < dy; k++) {
                y += sy;
                if (p < 0) {
                    p += duaDx;
                } else {
                    x += sx;
                    p += duaDxDy;
                }
                titikList.add(new int[]{x, y});
            }
        }

        return titikList;
    }
}