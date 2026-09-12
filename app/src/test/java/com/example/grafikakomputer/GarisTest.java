package com.example.grafikakomputer;

import org.junit.Test;
import java.util.List;

import static org.junit.Assert.*;

public class GarisTest {

    @Test
    public void testDda_GarisHorizontal() {
        Garis garis = new Garis(0, 0, 4, 0, Garis.Algoritma.DDA);
        List<int[]> titik = garis.generateTitik();

        assertEquals(5, titik.size());
        assertArrayEquals(new int[]{0, 0}, titik.get(0));
        assertArrayEquals(new int[]{1, 0}, titik.get(1));
        assertArrayEquals(new int[]{2, 0}, titik.get(2));
        assertArrayEquals(new int[]{3, 0}, titik.get(3));
        assertArrayEquals(new int[]{4, 0}, titik.get(4));
    }

    @Test
    public void testDda_GarisVertikal() {
        Garis garis = new Garis(2, 1, 2, 4, Garis.Algoritma.DDA);
        List<int[]> titik = garis.generateTitik();

        assertEquals(4, titik.size());
        assertArrayEquals(new int[]{2, 1}, titik.get(0));
        assertArrayEquals(new int[]{2, 4}, titik.get(titik.size() - 1));
    }

    @Test
    public void testDda_TitikTunggal() {
        Garis garis = new Garis(3, 3, 3, 3, Garis.Algoritma.DDA);
        List<int[]> titik = garis.generateTitik();

        assertEquals(1, titik.size());
        assertArrayEquals(new int[]{3, 3}, titik.get(0));
    }

    @Test
    public void testBresenham_GarisLandai() {
        // Kemiringan landai (dx > dy)
        Garis garis = new Garis(1, 1, 8, 4, Garis.Algoritma.BRESENHAM);
        List<int[]> titik = garis.generateTitik();

        // Jumlah titik harus dx + 1 = 8 - 1 + 1 = 8
        assertEquals(8, titik.size());
        assertArrayEquals(new int[]{1, 1}, titik.get(0));
        assertArrayEquals(new int[]{8, 4}, titik.get(titik.size() - 1));
    }

    @Test
    public void testBresenham_GarisCuram() {
        // Kemiringan curam (dy > dx)
        Garis garis = new Garis(2, 1, 5, 9, Garis.Algoritma.BRESENHAM);
        List<int[]> titik = garis.generateTitik();

        // Jumlah titik harus dy + 1 = 9 - 1 + 1 = 9
        assertEquals(9, titik.size());
        assertArrayEquals(new int[]{2, 1}, titik.get(0));
        assertArrayEquals(new int[]{5, 9}, titik.get(titik.size() - 1));
    }

    @Test
    public void testBresenham_ArahMundurNegatif() {
        // Arah mundur dari (5, 5) ke (1, 2)
        Garis garis = new Garis(5, 5, 1, 2, Garis.Algoritma.BRESENHAM);
        List<int[]> titik = garis.generateTitik();

        assertEquals(5, titik.size());
        assertArrayEquals(new int[]{5, 5}, titik.get(0));
        assertArrayEquals(new int[]{1, 2}, titik.get(titik.size() - 1));
    }

    @Test
    public void testBresenham_TitikTunggal() {
        Garis garis = new Garis(0, 0, 0, 0, Garis.Algoritma.BRESENHAM);
        List<int[]> titik = garis.generateTitik();

        assertEquals(1, titik.size());
        assertArrayEquals(new int[]{0, 0}, titik.get(0));
    }

    @Test
    public void testGaris_CachingTitikKonsisten() {
        Garis garis = new Garis(0, 0, 3, 3, Garis.Algoritma.DDA);
        List<int[]> pertama = garis.generateTitik();
        List<int[]> kedua = garis.generateTitik();

        // Memastikan referensi cache yang sama dikembalikan
        assertSame(pertama, kedua);
    }
}
