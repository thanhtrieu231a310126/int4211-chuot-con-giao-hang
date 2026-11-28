package vn.edu.vhu.ltdd.appadmin;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TheoDoiDonActivity extends AppCompatActivity {

    private static final String TAG = "ChuotCon_Admin";

    private final List<ApiService.DonHangApi> danhSachGoc = new ArrayList<>();
    private final List<ApiService.DonHangApi> danhSachHienThi = new ArrayList<>();

    private DonTheoDoiAdapter adapter;
    private RecyclerView rvTheoDoiDon;
    private TextView tvTongSoDon, tvDanhSachRong;
    private ProgressBar progressBarTheoDoi;
    private ChipGroup chipGroupTrangThai;
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_theo_doi_don);

        // Ánh xạ các View
        rvTheoDoiDon = findViewById(R.id.rvTheoDoiDon);
        tvTongSoDon = findViewById(R.id.tvTongSoDon);
        tvDanhSachRong = findViewById(R.id.tvDanhSachRong);
        progressBarTheoDoi = findViewById(R.id.progressBarTheoDoi);
        chipGroupTrangThai = findViewById(R.id.chipGroupTrangThai);

        // Nút quay lại Bàn điều phối
        findViewById(R.id.btnQuayLai).setOnClickListener(v -> finish());

        // Lấy token từ SharedPreferences
        token = getSharedPreferences("ChuotConPref", MODE_PRIVATE).getString("TOKEN", null);

        // Cấu hình RecyclerView thứ 2
        rvTheoDoiDon.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DonTheoDoiAdapter(danhSachHienThi, donHang -> {
            hienThiChiTietDon(donHang);
        });
        rvTheoDoiDon.setAdapter(adapter);

        // Bắt sự kiện bấm nút lọc trạng thái và tự động cuộn tab vào giữa
        android.widget.HorizontalScrollView hsvTrangThai = findViewById(R.id.hsvTrangThai);
        chipGroupTrangThai.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                View chipChon = findViewById(checkedIds.get(0));
                if (chipChon != null && hsvTrangThai != null) {
                    hsvTrangThai.post(() -> {
                        int scrollX = chipChon.getLeft() - (hsvTrangThai.getWidth() - chipChon.getWidth()) / 2;
                        hsvTrangThai.smoothScrollTo(Math.max(0, scrollX), 0);
                    });
                }
            }
            locDonTheoTrangThai();
        });

        // Tải toàn bộ danh sách đơn thật từ máy chủ Render
        goiApiLayTatCaDon();
    }

    private void goiApiLayTatCaDon() {
        progressBarTheoDoi.setVisibility(View.VISIBLE);
        rvTheoDoiDon.setVisibility(View.GONE);
        tvDanhSachRong.setVisibility(View.GONE);
        tvTongSoDon.setText("Đang tải dữ liệu...");
        Log.d(TAG, "Bắt đầu gọi API lấy toàn bộ đơn hàng giám sát");

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        // Tham số trangThai = null nghĩa là lấy TOÀN BỘ đơn hàng không phân biệt trạng thái
        apiService.layDanhSachDon("Bearer " + token, null).enqueue(new Callback<List<ApiService.DonHangApi>>() {
            @Override
            public void onResponse(Call<List<ApiService.DonHangApi>> call, Response<List<ApiService.DonHangApi>> response) {
                progressBarTheoDoi.setVisibility(View.GONE);
                rvTheoDoiDon.setVisibility(View.VISIBLE);

                if (response.isSuccessful() && response.body() != null) {
                    danhSachGoc.clear();
                    danhSachGoc.addAll(response.body());
                    locDonTheoTrangThai();
                    Log.d(TAG, "Tải thành công " + danhSachGoc.size() + " đơn giám sát từ Render");
                } else {
                    Log.e(TAG, "Lỗi từ máy chủ: " + response.code());
                    Toast.makeText(TheoDoiDonActivity.this, "Lỗi lấy dữ liệu (Mã: " + response.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<ApiService.DonHangApi>> call, Throwable t) {
                progressBarTheoDoi.setVisibility(View.GONE);
                Log.e(TAG, "Lỗi mạng: " + t.getMessage(), t);
                Toast.makeText(TheoDoiDonActivity.this, "Không thể kết nối máy chủ Render!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Hàm lọc đơn theo trạng thái
    private void locDonTheoTrangThai() {
        int idNutChon = chipGroupTrangThai.getCheckedChipId();
        danhSachHienThi.clear();

        for (ApiService.DonHangApi don : danhSachGoc) {
            String tt = don.trang_thai != null ? don.trang_thai : "CHUA_GAN";

            if (idNutChon == R.id.chipChuaGan) {
                if ("CHUA_GAN".equals(tt)) danhSachHienThi.add(don);
            } else if (idNutChon == R.id.chipDaGan) {
                if ("DA_GAN".equals(tt)) danhSachHienThi.add(don);
            } else if (idNutChon == R.id.chipDangGiao) {
                if ("DANG_GIAO".equals(tt)) danhSachHienThi.add(don);
            } else if (idNutChon == R.id.chipThanhCong) {
                if ("THANH_CONG".equals(tt)) danhSachHienThi.add(don);
            } else if (idNutChon == R.id.chipThatBai) {
                if ("THAT_BAI".equals(tt)) danhSachHienThi.add(don);
            } else {
                // Mặc định: Nút "Tất cả"
                danhSachHienThi.add(don);
            }
        }

        adapter.notifyDataSetChanged();
        tvTongSoDon.setText("Hiển thị: " + danhSachHienThi.size() + "/" + danhSachGoc.size() + " đơn");

        if (danhSachHienThi.isEmpty()) {
            tvDanhSachRong.setVisibility(View.VISIBLE);
            rvTheoDoiDon.setVisibility(View.GONE);
        } else {
            tvDanhSachRong.setVisibility(View.GONE);
            rvTheoDoiDon.setVisibility(View.VISIBLE);
        }
    }

    // Hiển thị chi tiết đơn khi chạm vào ô đơn hàng
    private void hienThiChiTietDon(ApiService.DonHangApi don) {
        String shipper = don.ten_shipper != null && !don.ten_shipper.isEmpty() ? don.ten_shipper : "Chưa có";
        String thongTin = "Mã đơn: #" + don.ma_van_don + "\n"
                + "Khách nhận: " + don.nguoi_nhan + " (" + (don.sdt_nhan != null ? don.sdt_nhan : "") + ")\n"
                + "Địa chỉ: " + don.dia_chi + ", " + don.phuong_xa + "\n"
                + "Trọng lượng: " + don.trong_luong + " kg\n"
                + "Tiền COD: " + don.tien_cod + " đ\n"
                + "Trạng thái: " + don.trang_thai + "\n"
                + "Shipper phụ trách: " + shipper;

        new AlertDialog.Builder(this)
                .setTitle("Chi tiết đơn hàng")
                .setMessage(thongTin)
                .setPositiveButton("Đóng", null)
                .show();
    }
}