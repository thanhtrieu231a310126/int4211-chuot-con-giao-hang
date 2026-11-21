package vn.edu.vhu.ltdd.appadmin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/**
 * LoginActivity: Màn hình Đăng nhập & Phân quyền
 */
public class LoginActivity extends AppCompatActivity {

    private TextInputLayout tilUsername, tilPassword;
    private TextInputEditText etUsername, etPassword;
    private MaterialButton btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 1. Ánh xạ các View từ file giao diện XML
        tilUsername = findViewById(R.id.tilUsername);
        tilPassword = findViewById(R.id.tilPassword);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);

        // 2. Bắt sự kiện khi bấm nút Đăng nhập
        btnLogin.setOnClickListener(v -> xuLyDangNhap());
    }
    private void xuLyDangNhap() {
        String username = etUsername.getText() != null ? etUsername.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

        // Xóa thông báo lỗi cũ
        tilUsername.setError(null);
        tilPassword.setError(null);

        // Kiểm tra để trống (Yêu cầu kỹ thuật: kiểm tra dữ liệu và hiện lỗi đúng vị trí)
        if (username.isEmpty()) {
            tilUsername.setError("Vui lòng nhập tên đăng nhập");
            etUsername.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            tilPassword.setError("Vui lòng nhập mật khẩu");
            etPassword.requestFocus();
            return;
        }

        // DỮ LIỆU MẪU XÁC THỰC & PHÂN QUYỀN:
        if ("admin".equalsIgnoreCase(username) && "123456".equals(password)) {
            // Đúng tài khoản Điều phối viên -> Chuyển sang Bàn điều phối (MainActivity)
            Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish(); // Đóng màn hình đăng nhập, không cho bấm Back quay lại

        } else if ("shipper1".equalsIgnoreCase(username) && "123456".equals(password)) {
            // Tài khoản Shipper -> Chặn truy cập theo yêu cầu phân quyền hệ thống
            Toast.makeText(this, "Tài khoản của bạn là Shipper! Vui lòng đăng nhập trên App Shipper.", Toast.LENGTH_LONG).show();

        } else {
            // Sai thông tin đăng nhập
            tilPassword.setError("Tên đăng nhập hoặc mật khẩu không chính xác");
        }
    }
}