package com.example.grafikakomputer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Kelas Garis
 * Berisi method penggambaran garis menggunakan algoritma DDA dan Bresenham.
 * Masukan: dua titik koordinat (x1, y1) dan (x2, y2)
 * Keluaran: daftar titik (pixel) yang membentuk garis tersebut.
 */
public class Garis {

    // Titik ujung garis
    private final int x1, y1, x2, y2;

    // Jenis algoritma yang dipakai
    public enum Algoritma {
        DDA,
        BRESENHAM
    }

    private final Algoritma algoritma;

    // Cache daftar titik agar tidak dihitung ulang setiap render frame di onDraw
    private final List<int[]> titikList;

    public Garis(int x1, int y1, int x2, int y2, Algoritma algoritma) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        this.algoritma = algoritma;

        // Hitung titik garis sekali saja saat objek dibuat untuk performa optimal
        if (algoritma == Algoritma.DDA) {
            this.titikList = Collections.unmodifiableList(hitungDDA());
        } else {
            this.titikList = Collections.unmodifiableList(hitungBresenham());
        }
    }

    public Algoritma getAlgoritma() {
        return algoritma;
    }

    /**
     * Menghasilkan daftar titik pixel garis sesuai algoritma yang dipilih (menggunakan cache).
     */
    public List<int[]> generateTitik() {
        return titikList;
    }

    /**
     * Algoritma DDA (Digital Differential Analyzer)
     * Langkah-langkah:
     * 1. Tentukan dua titik yang akan dihubungkan dalam pembentukan garis (x1,y1) dan (x2,y2).
     * 2. Titik awal (x1, y1) dan titik akhir (x2, y2).
     * 3. Hitung dx = x2 - x1 dan dy = y2 - y1.
     * 4. Tentukan step:
     *    - Bila |dx| > |dy| maka step = |dx|
     *    - Bila tidak, maka step = |dy|
     * 5. Hitung penambahan koordinat pixel:
     *    x_inc = dx / step
     *    y_inc = dy / step
     * 6. Koordinat selanjutnya (x + x_inc, y + y_inc).
     * 7. Plot pixel pada layar dengan nilai koordinat dibulatkan (Math.round),
     *    ulangi sampai mencapai titik akhir.
     */
    private List<int[]> hitungDDA() {
        List<int[]> list = new ArrayList<>();

        // Langkah 3: Hitung dx dan dy
        float dx = x2 - x1;
        float dy = y2 - y1;

        // Langkah 4: Tentukan step
        int step;
        if (Math.abs(dx) > Math.abs(dy)) {
            step = Math.abs((int) dx);
        } else {
            step = Math.abs((int) dy);
        }

        // Penanganan jika titik awal dan akhir sama persis
        if (step == 0) {
            list.add(new int[]{x1, y1});
            return list;
        }

        // Langkah 5: Hitung penambahan per step (x_inc dan y_inc)
        float xInc = dx / step;
        float yInc = dy / step;

        float x = x1;
        float y = y1;

        // Langkah 7: Plot pixel titik awal (dibulatkan)
        list.add(new int[]{Math.round(x), Math.round(y)});

        // Langkah 6 & 7: Ulangi penambahan koordinat dan plot pixel sampai titik akhir
        for (int k = 0; k < step; k++) {
            x += xInc;
            y += yInc;
            list.add(new int[]{Math.round(x), Math.round(y)});
        }

        return list;
    }

    /**
     * Algoritma Bresenham (Midpoint Line Algorithm)
     * Langkah-langkah:
     * 1. Tentukan dua titik yang akan dihubungkan (x1, y1) dan (x2, y2).
     * 2. Titik awal (x1, y1) dan titik akhir (x2, y2).
     * 3. Hitung dx, dy, 2dy, dan 2dy - 2dx.
     * 4. Hitung parameter keputusan awal: p0 = 2dy - dx.
     * 5. Untuk setiap xk sepanjang jalur garis (dimulai k = 0):
     *    - Bila pk < 0 maka titik selanjutnya (xk + 1, yk) dan pk+1 = pk + 2dy
     *    - Bila tidak, titik selanjutnya (xk + 1, yk + 1) dan pk+1 = pk + 2dy - 2dx
     * 6. Ulangi sampai mencapai titik akhir.
     * <p>
     * Catatan: Algoritma digeneralisasi untuk mendukung 8 oktan (kemiringan landai |m| <= 1
     * maupun curam |m| > 1, serta arah maju dan mundur).
     */
    private List<int[]> hitungBresenham() {
        List<int[]> list = new ArrayList<>();

        // Langkah 3: Hitung dx dan dy (nilai mutlak untuk generalisasi semua arah)
        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);

        // Arah inkremen (maju +1 atau mundur -1)
        int sx = (x1 < x2) ? 1 : -1;
        int sy = (y1 < y2) ? 1 : -1;

        int x = x1;
        int y = y1;

        if (dx >= dy) {
            // Kasus |m| <= 1 (Kemiringan landai, iterasi pada sumbu x sesuai langkah materi)
            // Langkah 3 & 4: Hitung 2dy, 2dy - 2dx, dan parameter awal p0 = 2dy - dx
            int p = 2 * dy - dx;
            int duaDy = 2 * dy;
            int duaDyDx = 2 * (dy - dx);

            list.add(new int[]{x, y});

            // Langkah 5 & 6: Iterasi sepanjang dx
            for (int k = 0; k < dx; k++) {
                x += sx;
                if (p < 0) {
                    p += duaDy;
                } else {
                    y += sy;
                    p += duaDyDx;
                }
                list.add(new int[]{x, y});
            }
        } else {
            // Kasus |m| > 1 (Kemiringan curam, iterasi pada sumbu y)
            int p = 2 * dx - dy;
            int duaDx = 2 * dx;
            int duaDxDy = 2 * (dx - dy);

            list.add(new int[]{x, y});

            for (int k = 0; k < dy; k++) {
                y += sy;
                if (p < 0) {
                    p += duaDx;
                } else {
                    x += sx;
                    p += duaDxDy;
                }
                list.add(new int[]{x, y});
            }
        }

        return list;
    }
}