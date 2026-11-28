package vn.edu.vhu.ltdd.appadmin;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PATCH;
import retrofit2.http.Path;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiService {

    // 1. Đăng nhập
    @POST("auth/dang-nhap")
    Call<DangNhapResponse> dangNhap(@Body DangNhapRequest request);

    // 2. Lấy danh sách đơn hàng
    @GET("don-giao")
    Call<List<DonHangApi>> layDanhSachDon(
            @Header("Authorization") String token,
            @Query("trangThai") String trangThai
    );

    // 3. Lấy danh sách Shipper đang hoạt động
    @GET("shipper")
    Call<List<ShipperApi>> layDanhSachShipper(
            @Header("Authorization") String token
    );

    // 4. Gán đơn hàng cho Shipper
    @PATCH("don-giao/{id}/gan-shipper")
    Call<Object> ganShipperChoDon(
            @Header("Authorization") String token,
            @Path("id") int donHangId,
            @Body GanShipperRequest request
    );

    class DangNhapRequest {
        public String email;
        public String matKhau;

        public DangNhapRequest(String email, String matKhau) {
            this.email = email;
            this.matKhau = matKhau;
        }
    }

    class DangNhapResponse {
        public String token;
        public NguoiDung nguoiDung;

        public static class NguoiDung {
            public int id;
            public String hoTen;
            public String vaiTro;
        }
    }

    class DonHangApi {
        public int id;
        public String ma_van_don;
        public String nguoi_nhan;
        public String sdt_nhan;
        public String dia_chi;
        public String phuong_xa;
        public String trong_luong;
        public String tien_cod;
        public String trang_thai;
        public String ten_shipper;
    }

    // Khuôn dữ liệu Shipper do máy chủ Render trả về
    class ShipperApi {
        public int id;
        public String ho_ten;
        public String sdt;
        public String bien_so;
        public int so_don_toi_da;
        public int so_don_dang_cam;
        public String trang_thai;
    }

    // Khuôn dữ liệu gửi lên khi gán đơn cho Shipper
    class GanShipperRequest {
        public int shipperId;

        public GanShipperRequest(int shipperId) {
            this.shipperId = shipperId;
        }
    }
}