package vn.edu.vhu.ltdd.appadmin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * LoginActivity: Màn hình Đăng nhập gọi API thật lên máy chủ Render.
 */
public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "ChuotCon_Admin";
    private TextInputLayout tilUsername, tilPassword;
    private TextInputEditText etUsername, etPassword;
    private MaterialButton btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Auto vào thẳng Bàn điều phối nếu đã có Token lưu sẵn
        String savedToken = getSharedPreferences("ChuotConPref", MODE_PRIVATE).getString("TOKEN", null);
        if (savedToken != null && !savedToken.isEmpty()) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        tilUsername = findViewById(R.id.tilUsername);
        tilPassword = findViewById(R.id.tilPassword);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(v -> xuLyDangNhap());
        // Nhập mật khẩu xong bấm Enter -> Tự Đăng Nhập
        etPassword.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE ||
                    (event != null && event.getKeyCode() == android.view.KeyEvent.KEYCODE_ENTER && event.getAction() == android.view.KeyEvent.ACTION_DOWN)) {
                xuLyDangNhap();
                return true;
            }
            return false;
        });
    }

    private void xuLyDangNhap() {
        String email = etUsername.getText() != null ? etUsername.getText().toString().trim() : "";
        String matKhau = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

        tilUsername.setError(null);
        tilPassword.setError(null);

        // 1. Kiểm tra để trống
        if (email.isEmpty()) {
            tilUsername.setError("Vui lòng nhập Email");
            etUsername.requestFocus();
            return;
        }

        if (matKhau.isEmpty()) {
            tilPassword.setError("Vui lòng nhập mật khẩu");
            etPassword.requestFocus();
            return;
        }

        // Khóa nút và đổi chữ sang trạng thái Loading chống bấm liên tục
        btnLogin.setEnabled(false);
        btnLogin.setText("Đang đăng nhập...");

        // 3. GỌI API THẬT LÊN RENDER
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        ApiService.DangNhapRequest request = new ApiService.DangNhapRequest(email, matKhau);

        apiService.dangNhap(request).enqueue(new Callback<ApiService.DangNhapResponse>() {
            @Override
            public void onResponse(Call<ApiService.DangNhapResponse> call, Response<ApiService.DangNhapResponse> response) {
                btnLogin.setEnabled(true);
                btnLogin.setText("ĐĂNG NHẬP");

                if (response.isSuccessful() && response.body() != null) {
                    ApiService.DangNhapResponse data = response.body();

                    // Kiểm tra phân quyền: đúng DIEU_PHOI mới cho vào app Admin
                    if ("DIEU_PHOI".equals(data.nguoiDung.vaiTro)) {
                        Toast.makeText(LoginActivity.this, "Đăng nhập thành công: " + data.nguoiDung.hoTen, Toast.LENGTH_SHORT).show();

                        // Lưu Token và Tên người dùng vào bộ nhớ máy
                        getSharedPreferences("ChuotConPref", MODE_PRIVATE)
                                .edit()
                                .putString("TOKEN", data.token)
                                .putString("HO_TEN", data.nguoiDung.hoTen)
                                .apply();

                        // Chuyển sang Bàn điều phối
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        // Tài khoản Shipper -> Chặn quyền
                        Toast.makeText(LoginActivity.this, "Tài khoản của bạn là Shipper! Vui lòng dùng App Shipper.", Toast.LENGTH_LONG).show();
                    }
                } else {
                    // Sai mật khẩu hoặc tài khoản không tồn tại (mã 401)
                    tilPassword.setError("Email hoặc mật khẩu không chính xác");
                    Toast.makeText(LoginActivity.this, "Đăng nhập thất bại (Mã: " + response.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiService.DangNhapResponse> call, Throwable t) {
                btnLogin.setEnabled(true);
                btnLogin.setText("ĐĂNG NHẬP");
                Toast.makeText(LoginActivity.this, "Lỗi kết nối mạng: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}