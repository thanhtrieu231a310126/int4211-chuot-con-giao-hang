// CẤU HÌNH API: chỉ cần sửa ở file này
class ApiConfig {
  // Điền domain Render khi Bình gửi, ví dụ: 'https://chuot-con-api.onrender.com'
  // (không có dấu / ở cuối). Để trống '' thì app dùng dữ liệu giả.
  static const String baseUrl = '';

  // Đường dẫn các API (Bình đã chốt)
  static const String dangNhap = '/auth/dang-nhap';     // POST
  static const String donCuaToi = '/shipper/toi/don';   // GET

  static bool get dungDuLieuGia => baseUrl.isEmpty;
}
