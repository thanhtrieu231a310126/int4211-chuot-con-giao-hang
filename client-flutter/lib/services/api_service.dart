import 'dart:async';
import 'dart:convert';

import 'package:http/http.dart' as http;

import '../config/api_config.dart';
import '../models/don_hang.dart';

class PhienDangNhapHetHan implements Exception {
  @override
  String toString() =>
      'Phiên đăng nhập đã hết hạn. Bạn vui lòng đăng nhập lại.';
}

class ApiService {
  static String? token;

  static void dangXuat() => token = null;

  // render free khoi dong lai cham nen de timeout 60s
  static const _thoiGianCho = Duration(seconds: 60);

  static Map<String, String> _header() => {
    'Content-Type': 'application/json',
    if (token != null) 'Authorization': 'Bearer $token',
  };

  // POST /auth/dang-nhap
  static Future<void> dangNhap(String email, String matKhau) async {
    dangXuat();
    final res = await _goi(
      () => http.post(
        Uri.parse(ApiConfig.baseUrl + ApiConfig.dangNhap),
        headers: {'Content-Type': 'application/json'},
        body: jsonEncode({'email': email, 'matKhau': matKhau}),
      ),
      canDangNhap: false,
    );
    final data = _docJson(res);
    final tokenMoi = data['token'];
    if (tokenMoi is! String || tokenMoi.trim().isEmpty) {
      throw Exception('Máy chủ chưa xác nhận đăng nhập. Bạn vui lòng thử lại.');
    }
    if (data['nguoiDung'] is! Map || data['nguoiDung']['vaiTro'] != 'SHIPPER') {
      throw Exception(
        'Ứng dụng này dành cho tài khoản Shipper. Bạn vui lòng dùng đúng tài khoản.',
      );
    }
    token = tokenMoi;
  }

  // GET /shipper/toi/don
  static Future<List<DonHang>> layDonCuaToi() async {
    if (token == null) throw PhienDangNhapHetHan();
    final res = await _goi(
      () => http.get(
        Uri.parse(ApiConfig.baseUrl + ApiConfig.donCuaToi),
        headers: _header(),
      ),
    );
    try {
      final data = _docJson(res);
      final List ds = data;
      return ds
          .map((e) => DonHang.fromJson(Map<String, dynamic>.from(e)))
          .toList();
    } catch (e) {
      throw Exception('Dữ liệu đơn từ backend không đúng định dạng');
    }
  }

  static Future<http.Response> _goi(
    Future<http.Response> Function() yeuCau, {
    bool canDangNhap = true,
  }) async {
    final http.Response res;
    try {
      res = await yeuCau().timeout(_thoiGianCho);
    } on TimeoutException {
      throw Exception(
        'Máy chủ đang khởi động hoặc quá chậm, đợi 1 lát rồi thử lại',
      );
    } catch (_) {
      throw Exception('Không kết nối được máy chủ, kiểm tra mạng');
    }
    if (res.statusCode == 401 && canDangNhap) {
      dangXuat();
      throw PhienDangNhapHetHan();
    }
    if (res.statusCode < 200 || res.statusCode >= 300) {
      throw Exception(_docLoi(res));
    }
    return res;
  }

  static dynamic _docJson(http.Response res) =>
      jsonDecode(utf8.decode(res.bodyBytes));

  static String _docLoi(http.Response res) {
    try {
      final d = _docJson(res);
      return (d['loi'] ?? d['message'] ?? d['error'] ?? 'Lỗi ${res.statusCode}')
          .toString();
    } catch (_) {
      return 'Lỗi ${res.statusCode}';
    }
  }
}
