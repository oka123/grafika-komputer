package com.example.grafikakomputer;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private EditText edtX1, edtY1, edtX2, edtY2;
    private RadioGroup rgAlgoritma;
    private Button btnGambar, btnBersihkan;
    private CanvasGarisView canvasGarisView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        inisialisasiView();
        pasangAksi();
    }

    private void inisialisasiView() {
        edtX1 = findViewById(R.id.edtX1);
        edtY1 = findViewById(R.id.edtY1);
        edtX2 = findViewById(R.id.edtX2);
        edtY2 = findViewById(R.id.edtY2);
        rgAlgoritma = findViewById(R.id.rgAlgoritma);
        btnGambar = findViewById(R.id.btnGambar);
        btnBersihkan = findViewById(R.id.btnBersihkan);
        canvasGarisView = findViewById(R.id.canvasGarisView);
    }

    private void pasangAksi() {
        btnGambar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prosesGambarGaris();
            }
        });

        btnBersihkan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                canvasGarisView.bersihkanSemua();
            }
        });
    }

    /**
     * Ambil input dari EditText, validasi, lalu buat objek Garis
     * sesuai algoritma yang dipilih (DDA atau Bresenham),
     * lalu tambahkan ke canvas agar tampil bersama garis lain yang sudah ada.
     */
    private void prosesGambarGaris() {
        String sX1 = edtX1.getText().toString().trim();
        String sY1 = edtY1.getText().toString().trim();
        String sX2 = edtX2.getText().toString().trim();
        String sY2 = edtY2.getText().toString().trim();

        if (sX1.isEmpty() || sY1.isEmpty() || sX2.isEmpty() || sY2.isEmpty()) {
            Toast.makeText(this, "Isi semua koordinat x1, y1, x2, y2", Toast.LENGTH_SHORT).show();
            return;
        }

        int x1, y1, x2, y2;
        try {
            x1 = Integer.parseInt(sX1);
            y1 = Integer.parseInt(sY1);
            x2 = Integer.parseInt(sX2);
            y2 = Integer.parseInt(sY2);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Koordinat harus berupa bilangan bulat", Toast.LENGTH_SHORT).show();
            return;
        }

        int idTerpilih = rgAlgoritma.getCheckedRadioButtonId();
        if (idTerpilih == -1) {
            Toast.makeText(this, "Pilih algoritma terlebih dahulu", Toast.LENGTH_SHORT).show();
            return;
        }

        RadioButton rbTerpilih = findViewById(idTerpilih);
        Garis.Algoritma algoritma = rbTerpilih.getText().toString().equalsIgnoreCase("DDA")
                ? Garis.Algoritma.DDA
                : Garis.Algoritma.BRESENHAM;

        Garis garisBaru = new Garis(x1, y1, x2, y2, algoritma);
        canvasGarisView.tambahGaris(garisBaru);
    }
}