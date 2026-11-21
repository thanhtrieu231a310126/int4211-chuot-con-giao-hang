class ApiConfig {
  static const String baseUrl = 'https://chuot-con-backend.onrender.com';

  static const String dangNhap = '/auth/dang-nhap';
  static const String donCuaToi = '/shipper/toi/don';

  // de trong baseUrl thi dung du lieu test
  static bool get dungDuLieuGia => baseUrl.isEmpty;
}
