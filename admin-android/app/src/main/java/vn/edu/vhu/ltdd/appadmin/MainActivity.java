package vn.edu.vhu.ltdd.appadmin;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MainActivity extends AppCompatActivity {

    private final List<DonHang> danhSachGoc = new ArrayList<>();
    private final List<DonHang> danhSachHienThi = new ArrayList<>();

    private DonHangAdapter adapter;
    private RecyclerView rvDonHang;
    private TextView tvSoLuongDon, tvDanhSachRong;
    private EditText edtLocPhuong;
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rvDonHang = findViewById(R.id.rvDonHang);
        tvSoLuongDon = findViewById(R.id.tvSoLuongDon);
        tvDanhSachRong = findViewById(R.id.tvDanhSachRong);
        edtLocPhuong = findViewById(R.id.edtLocPhuong);

        // Lấy token thật truyền từ màn hình Đăng nhập sang
        token = getIntent().getStringExtra("TOKEN");

        rvDonHang.setLayoutManager(new LinearLayoutManager(this));

        adapter = new DonHangAdapter(danhSachHienThi, donHang -> {
            Toast.makeText(this, "Đã chọn đơn: #" + donHang.getMaDon() + " (" + donHang.getPhuongXa() + ")", Toast.LENGTH_SHORT).show();
        });
        rvDonHang.setAdapter(adapter);

        // 1. Kiểm tra: Nếu có token thật thì gọi API thật, nếu không có thì dùng dữ liệu mẫu
        if (token != null && !token.isEmpty()) {
            goiApiLayDanhSachDonThat();
        } else {
            taoDuLieuMau();
            capNhatSoLuong();
        }

        // 2. Chức năng lọc đơn theo Phường/Xã
        edtLocPhuong.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                locDonTheoPhuong(s.toString().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void goiApiLayDanhSachDonThat() {
        tvSoLuongDon.setText("Đang tải dữ liệu từ máy chủ Render...");

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.layDanhSachDon("Bearer " + token, "CHUA_GAN").enqueue(new Callback<List<ApiService.DonHangApi>>() {
            @Override
            public void onResponse(Call<List<ApiService.DonHangApi>> call, Response<List<ApiService.DonHangApi>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    danhSachGoc.clear();

                    // Đọc từng đơn hàng từ database Supabase trên Render gửi về
                    for (ApiService.DonHangApi apiItem : response.body()) {
                        double kg = 0.0;
                        int cod = 0;
                        try {
                            if (apiItem.trong_luong != null) kg = Double.parseDouble(apiItem.trong_luong);
                            if (apiItem.tien_cod != null) cod = (int) Double.parseDouble(apiItem.tien_cod);
                        } catch (Exception ignored) {}

                        danhSachGoc.add(new DonHang(
                                apiItem.ma_van_don,
                                apiItem.nguoi_nhan,
                                apiItem.sdt_nhan,
                                apiItem.dia_chi,
                                apiItem.phuong_xa,
                                kg,
                                cod
                        ));
                    }

                    danhSachHienThi.clear();
                    danhSachHienThi.addAll(danhSachGoc);
                    adapter.notifyDataSetChanged();
                    capNhatSoLuong();

                    Toast.makeText(MainActivity.this, "Đã tải thành công " + danhSachGoc.size() + " đơn từ server!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Lỗi lấy dữ liệu (Mã: " + response.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<ApiService.DonHangApi>> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Lỗi kết nối máy chủ: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void taoDuLieuMau() {
        danhSachGoc.add(new DonHang("DH001", "Nguyễn Văn An", "0901112233", "123 Lê Lợi", "Phường Bến Nghé", 1.2, 85000));
        danhSachGoc.add(new DonHang("DH002", "Trần Thị Bình", "0912223344", "45 Hai Bà Trưng", "Phường Đa Kao", 0.5, 0));
        danhSachHienThi.addAll(danhSachGoc);
    }

    private void locDonTheoPhuong(String tuKhoa) {
        danhSachHienThi.clear();

        if (tuKhoa.isEmpty()) {
            danhSachHienThi.addAll(danhSachGoc);
        } else {
            for (DonHang don : danhSachGoc) {
                if (don.getPhuongXa().toLowerCase().contains(tuKhoa.toLowerCase())) {
                    danhSachHienThi.add(don);
                }
            }
        }

        adapter.notifyDataSetChanged();
        capNhatSoLuong();

        if (danhSachHienThi.isEmpty()) {
            tvDanhSachRong.setVisibility(View.VISIBLE);
            rvDonHang.setVisibility(View.GONE);
        } else {
            tvDanhSachRong.setVisibility(View.GONE);
            rvDonHang.setVisibility(View.VISIBLE);
        }
    }

    private void capNhatSoLuong() {
        tvSoLuongDon.setText("Đơn chưa gán: " + danhSachHienThi.size());
    }
}