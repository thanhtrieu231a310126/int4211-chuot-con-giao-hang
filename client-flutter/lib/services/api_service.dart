import 'dart:async';
import 'dart:convert';
import 'package:http/http.dart' as http;
import '../config/api_config.dart';
import '../models/don_hang.dart';

// Mọi lời gọi backend đều đi qua class này
class ApiService {
  // "Vé" backend phát sau khi đăng nhập, mọi request lấy dữ liệu đều phải gửi kèm
  static String? token;

  // Render bản miễn phí tự tắt khi lâu không có request, bật lại rất lâu
  // nên request nào cũng chờ tối đa 60 giây
  static const _thoiGianCho = Duration(seconds: 60);

  // Header dùng chung: có token thì gắn "Authorization: Bearer <token>"
  static Map<String, String> _header() => {
        'Content-Type': 'application/json',
        if (token != null) 'Authorization': 'Bearer $token',
      };

  // ---------- POST /auth/dang-nhap ----------
  static Future<void> dangNhap(String sdt, String matKhau) async {
    if (ApiConfig.dungDuLieuGia) {
      token = 'token-gia';
      return;
    }
    final res = await _goi(() => http.post(
          Uri.parse(ApiConfig.baseUrl + ApiConfig.dangNhap),
          headers: {'Content-Type': 'application/json'}, // đăng nhập chưa có token
          body: jsonEncode({'so_dien_thoai': sdt, 'mat_khau': matKhau}),
        ));
    final data = _docJson(res);
    token = data['token'] ?? data['access_token'];
    if (token == null) throw Exception('Backend không trả về token');
  }

  // ---------- GET /shipper/toi/don ----------
  static Future<List<DonHang>> layDonCuaToi() async {
    if (ApiConfig.dungDuLieuGia) {
      await Future.delayed(const Duration(milliseconds: 500));
      return danhSachDonGia;
    }
    final res = await _goi(() => http.get(
          Uri.parse(ApiConfig.baseUrl + ApiConfig.donCuaToi),
          headers: _header(), // có token
        ));
    try {
      final data = _docJson(res);
      // Backend có thể trả thẳng mảng [...] hoặc {"data": [...]}
      final List ds = data is List ? data : (data['data'] ?? data['don'] ?? []);
      return ds.map((e) => DonHang.fromJson(Map<String, dynamic>.from(e))).toList();
    } catch (e) {
      throw Exception('Dữ liệu đơn từ backend không đúng định dạng');
    }
  }

  // Gọi request có timeout 60s + đổi lỗi kỹ thuật thành câu dễ hiểu
  static Future<http.Response> _goi(Future<http.Response> Function() yeuCau) async {
    final http.Response res;
    try {
      res = await yeuCau().timeout(_thoiGianCho);
    } on TimeoutException {
      throw Exception('Máy chủ đang khởi động hoặc quá chậm, đợi 1 lát rồi thử lại');
    } catch (_) {
      throw Exception('Không kết nối được máy chủ, kiểm tra mạng');
    }
    if (res.statusCode == 401) {
      token = null;
      throw Exception('Sai thông tin đăng nhập hoặc phiên đã hết hạn');
    }
    if (res.statusCode < 200 || res.statusCode >= 300) {
      throw Exception(_docLoi(res));
    }
    return res;
  }

  static dynamic _docJson(http.Response res) => jsonDecode(utf8.decode(res.bodyBytes));

  static String _docLoi(http.Response res) {
    try {
      final d = _docJson(res);
      return (d['message'] ?? d['error'] ?? 'Lỗi ${res.statusCode}').toString();
    } catch (_) {
      return 'Lỗi ${res.statusCode}';
    }
  }
}
