package vn.edu.vhu.ltdd.appadmin;

import android.content.Intent;
import android.content.SharedPreferences;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
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

    private static final String TAG = "ChuotCon_Admin";

    private final List<DonHang> danhSachGoc = new ArrayList<>();
    private final List<DonHang> danhSachHienThi = new ArrayList<>();

    private DonHangAdapter adapter;
    private RecyclerView rvDonHang;
    private TextView tvSoLuongDon, tvDanhSachRong;
    private EditText edtLocPhuong;
    private ProgressBar progressBar;
    private String token;
    private com.google.android.material.chip.ChipGroup chipGroupTrongLuong;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rvDonHang = findViewById(R.id.rvDonHang);
        tvSoLuongDon = findViewById(R.id.tvSoLuongDon);
        tvDanhSachRong = findViewById(R.id.tvDanhSachRong);
        edtLocPhuong = findViewById(R.id.edtLocPhuong);
        progressBar = findViewById(R.id.progressBar);

        // Lấy token từ SharedPreferences (nếu không có thì lấy từ Intent)
        SharedPreferences pref = getSharedPreferences("ChuotConPref", MODE_PRIVATE);
        token = pref.getString("TOKEN", null);
        if (token == null || token.isEmpty()) {
            token = getIntent().getStringExtra("TOKEN");
        }

        // Bắt sự kiện bấm nút Đăng xuất -> Mở hộp thoại xác nhận
        findViewById(R.id.btnDangXuat).setOnClickListener(v -> hienThiHopThoaiDangXuat());

        // Bắt sự kiện bấm nút Giám sát -> Mở màn hình Theo dõi đơn hàng
        findViewById(R.id.btnTheoDoiDon).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, TheoDoiDonActivity.class));
        });

        rvDonHang.setLayoutManager(new LinearLayoutManager(this));

        adapter = new DonHangAdapter(danhSachHienThi, donHang -> {
            hienThiDialogChonShipper(donHang);
        });
        rvDonHang.setAdapter(adapter);

        // 1. Kiểm tra: Nếu có token thật thì gọi API thật, nếu không có thì dùng dữ liệu mẫu
        if (token != null && !token.isEmpty()) {
            goiApiLayDanhSachDonThat();
        } else {
            taoDuLieuMau();
            capNhatSoLuong();
        }

        // Ánh xạ nhóm nút lọc trọng lượng
        chipGroupTrongLuong = findViewById(R.id.chipGroupTrongLuong);

        // Ép màu dịu mắt cho các Chip lọc
        android.content.res.ColorStateList bgList = new android.content.res.ColorStateList(
                new int[][]{ new int[]{android.R.attr.state_checked}, new int[]{} },
                new int[]{ android.graphics.Color.parseColor("#F97316"), android.graphics.Color.parseColor("#FFFDF9") }
        );
        android.content.res.ColorStateList textList = new android.content.res.ColorStateList(
                new int[][]{ new int[]{android.R.attr.state_checked}, new int[]{} },
                new int[]{ android.graphics.Color.parseColor("#FFFFFF"), android.graphics.Color.parseColor("#18181B") }
        );
        android.content.res.ColorStateList strokeList = new android.content.res.ColorStateList(
                new int[][]{ new int[]{android.R.attr.state_checked}, new int[]{} },
                new int[]{ android.graphics.Color.parseColor("#F97316"), android.graphics.Color.parseColor("#E4DFD7") }
        );

        for (int i = 0; i < chipGroupTrongLuong.getChildCount(); i++) {
            com.google.android.material.chip.Chip chip = (com.google.android.material.chip.Chip) chipGroupTrongLuong.getChildAt(i);
            chip.setChipBackgroundColor(bgList);
            chip.setTextColor(textList);
            chip.setChipStrokeColor(strokeList);
            chip.setChipStrokeWidth(2f);
        }
        chipGroupTrongLuong.setOnCheckedStateChangeListener((group, checkedIds) -> {
            locDonHang();
        });

        // Bắt sự kiện gõ tìm kiếm Phường/Xã
        edtLocPhuong.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                locDonHang();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void goiApiLayDanhSachDonThat() {
        // Trạng thái 1: ĐANG TẢI
        progressBar.setVisibility(View.VISIBLE);
        rvDonHang.setVisibility(View.GONE);
        tvDanhSachRong.setVisibility(View.GONE);
        tvSoLuongDon.setText("Đang tải dữ liệu từ máy chủ Render...");
        Log.d(TAG, "Bắt đầu gọi API lấy danh sách đơn chưa gán");

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.layDanhSachDon("Bearer " + token, "CHUA_GAN").enqueue(new Callback<List<ApiService.DonHangApi>>() {
            @Override
            public void onResponse(Call<List<ApiService.DonHangApi>> call, Response<List<ApiService.DonHangApi>> response) {
                // Trạng thái 2: THÀNH CÔNG
                progressBar.setVisibility(View.GONE);
                rvDonHang.setVisibility(View.VISIBLE);

                if (response.isSuccessful() && response.body() != null) {
                    danhSachGoc.clear();
                    for (ApiService.DonHangApi apiItem : response.body()) {
                        double kg = 0.0;
                        int cod = 0;
                        try {
                            if (apiItem.trong_luong != null) kg = Double.parseDouble(apiItem.trong_luong);
                            if (apiItem.tien_cod != null) cod = (int) Double.parseDouble(apiItem.tien_cod);
                        } catch (Exception ignored) {}

                        danhSachGoc.add(new DonHang(
                                apiItem.id,
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
                    Log.d(TAG, "Tải thành công " + danhSachGoc.size() + " đơn từ Render");
                } else {
                    // Token hết hạn (401) -> Tự động đăng xuất
                    if (response.code() == 401) {
                        Log.e(TAG, "Token hết hạn hoặc không hợp lệ (401)");
                        Toast.makeText(MainActivity.this, "Phiên đăng nhập đã hết hạn!", Toast.LENGTH_SHORT).show();
                        thucHienDangXuat();
                        return;
                    }
                    Log.e(TAG, "Lỗi từ máy chủ, mã: " + response.code());
                    Toast.makeText(MainActivity.this, "Lỗi lấy dữ liệu (Mã: " + response.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<ApiService.DonHangApi>> call, Throwable t) {
                // Trạng thái 3: LỖI MẠNG
                progressBar.setVisibility(View.GONE);
                Log.e(TAG, "Lỗi kết nối mạng: " + t.getMessage(), t);
                Toast.makeText(MainActivity.this, "Không thể kết nối máy chủ Render!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void taoDuLieuMau() {
        danhSachGoc.add(new DonHang(1, "DH001", "Nguyễn Văn An", "0901112233", "123 Lê Lợi", "Phường Bến Nghé", 1.2, 85000));
        danhSachGoc.add(new DonHang(2, "DH002", "Trần Thị Bình", "0912223344", "45 Hai Bà Trưng", "Phường Đa Kao", 0.5, 0));
        danhSachHienThi.addAll(danhSachGoc);
    }

    //Hàm lọc kết hợp cả Phường/Xã và Trọng lượng
    private void locDonHang() {
        String tuKhoa = edtLocPhuong.getText().toString().trim().toLowerCase();
        int idNutChon = chipGroupTrongLuong.getCheckedChipId();

        danhSachHienThi.clear();

        for (DonHang don : danhSachGoc) {
            // Điều kiện 1: Khớp Phường/Xã
            boolean hopLePhuong = tuKhoa.isEmpty() || don.getPhuongXa().toLowerCase().contains(tuKhoa);

            // Điều kiện 2: Khớp Khoảng trọng lượng
            boolean hopLeTrongLuong = false;
            double kg = don.getTrongLuong();

            if (idNutChon == R.id.chipNhe) {
                hopLeTrongLuong = (kg < 2.0); // Dưới 2kg
            } else if (idNutChon == R.id.chipVua) {
                hopLeTrongLuong = (kg >= 2.0 && kg <= 5.0); // Từ 2kg đến 5kg
            } else if (idNutChon == R.id.chipNang) {
                hopLeTrongLuong = (kg > 5.0); // Trên 5kg
            } else {
                hopLeTrongLuong = true; // Nút "Tất cả"
            }

            // Nếu thỏa mãn CẢ HAI điều kiện thì nhận vào danh sách hiển thị
            if (hopLePhuong && hopLeTrongLuong) {
                danhSachHienThi.add(don);
            }
        }

        adapter.notifyDataSetChanged();
        capNhatSoLuong();

        // Ẩn hiện danh sách hoặc thông báo rỗng
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
    // Hộp thoại xác nhận trước khi đăng xuất
    private void hienThiHopThoaiDangXuat() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Đăng xuất")
                .setMessage("Bạn có chắc chắn muốn đăng xuất?")
                .setPositiveButton("Đăng xuất", (dialog, which) -> thucHienDangXuat())
                .setNegativeButton("Hủy", null)
                .show();
    }

    // Xóa SharedPreferences và dọn sạch Stack chuyển về Login
    private void thucHienDangXuat() {
        getSharedPreferences("ChuotConPref", MODE_PRIVATE).edit().clear().apply();
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    // Hiển thị Dialog danh sách Shipper lấy từ API Render
    private void hienThiDialogChonShipper(DonHang donHang) {
        Toast.makeText(this, "Đang tải danh sách Shipper...", Toast.LENGTH_SHORT).show();

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.layDanhSachShipper("Bearer " + token).enqueue(new Callback<List<ApiService.ShipperApi>>() {
            @Override
            public void onResponse(Call<List<ApiService.ShipperApi>> call, Response<List<ApiService.ShipperApi>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    List<ApiService.ShipperApi> danhSachShipper = response.body();

                    // Chuẩn bị danh sách tên Shipper để hiển thị
                    String[] tenShippers = new String[danhSachShipper.size()];
                    for (int i = 0; i < danhSachShipper.size(); i++) {
                        ApiService.ShipperApi s = danhSachShipper.get(i);
                        String trangThai = s.so_don_dang_cam >= s.so_don_toi_da ? "ĐẦY ĐƠN" : "Rảnh";
                        tenShippers[i] = s.ho_ten + " (" + s.so_don_dang_cam + "/" + s.so_don_toi_da + " đơn - " + trangThai + ")";
                    }

                    // Hiện AlertDialog danh sách chọn Shipper
                    new androidx.appcompat.app.AlertDialog.Builder(MainActivity.this)
                            .setTitle("Chọn Shipper cho đơn #" + donHang.getMaDon())
                            .setItems(tenShippers, (dialog, which) -> {
                                ApiService.ShipperApi shipperDuocChon = danhSachShipper.get(which);
                                xacNhanVaGanDon(donHang, shipperDuocChon);
                            })
                            .setNegativeButton("Đóng", null)
                            .show();
                } else {
                    Toast.makeText(MainActivity.this, "Không thể lấy danh sách Shipper!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<ApiService.ShipperApi>> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Lỗi mạng khi tải Shipper: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Hộp thoại xác nhận trước khi gán đơn
    private void xacNhanVaGanDon(DonHang donHang, ApiService.ShipperApi shipper) {
        // Kiểm tra nếu Shipper đã đầy đơn (Kiểm soát tải trọng theo Đề tài 6)
        if (shipper.so_don_dang_cam >= shipper.so_don_toi_da) {
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Cảnh báo quá tải!")
                    .setMessage(shipper.ho_ten + " đã nhận đủ " + shipper.so_don_toi_da + "/" + shipper.so_don_toi_da + " đơn tối đa. Vui lòng chọn Shipper khác!")
                    .setPositiveButton("Đã hiểu", null)
                    .show();
            return;
        }

        // Hộp thoại xác nhận trước khi phân công
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Xác nhận gán đơn")
                .setMessage("Bạn có chắc chắn muốn gán đơn #" + donHang.getMaDon() + " cho " + shipper.ho_ten + "?")
                .setPositiveButton("Gán đơn", (dialog, which) -> {
                    thucHienGoiApiGanDon(donHang.getId(), shipper.id, shipper.ho_ten);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    // Gọi API PATCH lên máy chủ Render để gán đơn thật
    private void thucHienGoiApiGanDon(int donHangId, int shipperId, String tenShipper) {
        Toast.makeText(this, "Đang gửi lệnh gán đơn...", Toast.LENGTH_SHORT).show();

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.ganShipperChoDon("Bearer " + token, donHangId, new ApiService.GanShipperRequest(shipperId))
                .enqueue(new Callback<Object>() {
                    @Override
                    public void onResponse(Call<Object> call, Response<Object> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "Gán đơn thành công cho " + tenShipper + "!", Toast.LENGTH_LONG).show();
                            // Tải lại danh sách đơn để đơn vừa gán biến mất khỏi Bàn điều phối
                            goiApiLayDanhSachDonThat();
                        } else {
                            Toast.makeText(MainActivity.this, "Gán thất bại (Mã lỗi: " + response.code() + ")", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Object> call, Throwable t) {
                        Toast.makeText(MainActivity.this, "Lỗi kết nối khi gán đơn: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}