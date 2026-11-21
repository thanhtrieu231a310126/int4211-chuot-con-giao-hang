package vn.edu.vhu.ltdd.appadmin;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiService {

    @POST("auth/dang-nhap")
    Call<DangNhapResponse> dangNhap(@Body DangNhapRequest request);

    @GET("don-giao")
    Call<List<DonHangApi>> layDanhSachDon(
            @Header("Authorization") String token,
            @Query("trangThai") String trangThai
    );

    // ==========================================
    // CÁC KHUÔN DỮ LIỆU ĐỂ GỬI VÀ NHẬN VỚI BACKEND
    // ==========================================

    // Dữ liệu gửi lên khi đăng nhập
    class DangNhapRequest {
        public String email;
        public String matKhau;

        public DangNhapRequest(String email, String matKhau) {
            this.email = email;
            this.matKhau = matKhau;
        }
    }

    // Dữ liệu máy chủ trả về khi đăng nhập thành công
    class DangNhapResponse {
        public String token;
        public NguoiDung nguoiDung;

        public static class NguoiDung {
            public int id;
            public String hoTen;
            public String vaiTro; // "DIEU_PHOI" hoặc "SHIPPER"
        }
    }

    // Dữ liệu một đơn hàng do máy chủ trả về
    class DonHangApi {
        public int id;
        public String ma_van_don;
        public String nguoi_nhan;
        public String sdt_nhan;
        public String dia_chi;
        public String phuong_xa;
        public String trong_luong; // Render trả về dạng chuỗi "1.50"
        public String tien_cod;    // Render trả về dạng chuỗi "150000"
        public String trang_thai;
    }
}