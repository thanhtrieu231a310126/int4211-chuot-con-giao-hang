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

/**
 * MainActivity: Màn hình Bàn điều phối - Quản lý đơn chưa gán.
 */
public class MainActivity extends AppCompatActivity {

    // Danh sách gốc chứa tất cả đơn và danh sách hiển thị sau khi lọc
    private final List<DonHang> danhSachGoc = new ArrayList<>();
    private final List<DonHang> danhSachHienThi = new ArrayList<>();

    private DonHangAdapter adapter;
    private RecyclerView rvDonHang;
    private TextView tvSoLuongDon, tvDanhSachRong;
    private EditText edtLocPhuong;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. Ánh xạ các thành phần giao diện
        rvDonHang = findViewById(R.id.rvDonHang);
        tvSoLuongDon = findViewById(R.id.tvSoLuongDon);
        tvDanhSachRong = findViewById(R.id.tvDanhSachRong);
        edtLocPhuong = findViewById(R.id.edtLocPhuong);

        // 2. Tạo dữ liệu mẫu
        taoDuLieuMau();

        // 3. Cài đặt LayoutManager cho RecyclerView
        // LinearLayoutManager: Sắp xếp các ô đơn hàng theo chiều dọc
        rvDonHang.setLayoutManager(new LinearLayoutManager(this));

        // 4. Khởi tạo Adapter và bắt sự kiện bấm vào một đơn
        adapter = new DonHangAdapter(danhSachHienThi, donHang -> {
            // Khi bấm vào đơn, báo thông tin
            Toast.makeText(this, "Đã chọn đơn: #" + donHang.getMaDon() + " (" + donHang.getPhuongXa() + ")", Toast.LENGTH_SHORT).show();
        });
        rvDonHang.setAdapter(adapter);

        // 5. Cập nhật số lượng đơn lên tiêu đề
        capNhatSoLuong();

        // 6. Xử lý chức năng lọc đơn theo Phường/Xã khi người dùng gõ chữ (UC-03)
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

    /**
     * DỮ LIỆU MẪU (MOCK DATA):
     * Các đơn hàng nội thành TP.HCM (Quận 1, 3, 5...) ở trạng thái Chưa gán.
     * Khi trưởng nhóm cấp API thật, thay hàm này bằng hàm gọi mạng Retrofit.
     */
    private void taoDuLieuMau() {
        danhSachGoc.add(new DonHang("DH001", "Võ Vũ Sông Dài", "0901112233", "27 Trần Quốc Thảo", "Phường Xuân Hoà", 1.2, 85000));
        danhSachGoc.add(new DonHang("DH002", "Trần Thị Bình", "0912223344", "45 Hai Bà Trưng", "Phường Đa Kao", 0.5, 0));
        danhSachGoc.add(new DonHang("DH003", "Lê Hoàng Cúc", "0933334455", "78 Nguyễn Đình Chiểu", "Phường Đa Kao", 2.8, 150000));
        danhSachGoc.add(new DonHang("DH004", "Nguyễn Quốc Dũng", "0944445566", "210 Điện Biên Phủ", "Phường Võ Thị Sáu", 3.5, 220000));
        danhSachGoc.add(new DonHang("DH005", "Võ Minh Thi", "0955556677", "99 Trần Hưng Đạo", "Phường Cầu Ông Lãnh", 0.8, 60000));

        // Ban đầu copy toàn bộ danh sách gốc sang danh sách hiển thị
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

        // Báo cho Adapter biết dữ liệu đã đổi để vẽ lại danh sách
        adapter.notifyDataSetChanged();
        capNhatSoLuong();

        // Xử lý luồng ngoại lệ: Nếu không tìm thấy đơn nào thì hiện chữ thông báo rỗng
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