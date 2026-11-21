import 'package:flutter/material.dart';
import 'ds_don_screen.dart';
import '../services/api_service.dart';

class DangNhapScreen extends StatefulWidget {
  const DangNhapScreen({super.key});

  @override
  State<DangNhapScreen> createState() => _DangNhapScreenState();
}

class _DangNhapScreenState extends State<DangNhapScreen> {
  final _formKey = GlobalKey<FormState>();       // dùng để kiểm tra form
  final _sdtController = TextEditingController(); // giữ chữ trong ô số điện thoại
  final _matKhauController = TextEditingController();
  bool _anMatKhau = true;
  bool _dangTai = false; // true khi đang chờ backend trả lời

  @override
  void dispose() {
    _sdtController.dispose();
    _matKhauController.dispose();
    super.dispose();
  }

  Future<void> _dangNhap() async {
    // Nếu ô nào còn trống thì dừng, các dòng báo lỗi đỏ sẽ hiện ra
    if (!_formKey.currentState!.validate()) return;

    setState(() => _dangTai = true);
    try {
      // Gọi API POST /auth/dang-nhap (hoặc dữ liệu giả nếu chưa có domain)
      await ApiService.dangNhap(_sdtController.text.trim(), _matKhauController.text);
      if (!mounted) return;
      Navigator.pushReplacement(
        context,
        MaterialPageRoute(builder: (_) => const DsDonScreen()),
      );
    } catch (e) {
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Đăng nhập thất bại: $e')),
      );
    } finally {
      if (mounted) setState(() => _dangTai = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: SafeArea(
        child: Padding(
          padding: const EdgeInsets.all(24),
          child: Form(
            key: _formKey,
            child: Column(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                const Icon(Icons.delivery_dining, size: 80, color: Colors.orange),
                const SizedBox(height: 8),
                const Text('Chuột Con - Shipper',
                    style: TextStyle(fontSize: 24, fontWeight: FontWeight.bold)),
                const SizedBox(height: 32),
                TextFormField(
                  controller: _sdtController,
                  keyboardType: TextInputType.phone,
                  decoration: const InputDecoration(
                    labelText: 'Số điện thoại',
                    prefixIcon: Icon(Icons.phone),
                    border: OutlineInputBorder(),
                  ),
                  validator: (v) =>
                  (v == null || v.isEmpty) ? 'Vui lòng nhập số điện thoại' : null,
                ),
                const SizedBox(height: 16),
                TextFormField(
                  controller: _matKhauController,
                  obscureText: _anMatKhau,
                  decoration: InputDecoration(
                    labelText: 'Mật khẩu',
                    prefixIcon: const Icon(Icons.lock),
                    border: const OutlineInputBorder(),
                    suffixIcon: IconButton(
                      icon: Icon(_anMatKhau ? Icons.visibility : Icons.visibility_off),
                      onPressed: () => setState(() => _anMatKhau = !_anMatKhau),
                    ),
                  ),
                  validator: (v) =>
                  (v == null || v.isEmpty) ? 'Vui lòng nhập mật khẩu' : null,
                ),
                const SizedBox(height: 24),
                SizedBox(
                  width: double.infinity,
                  height: 48,
                  child: ElevatedButton(
                    onPressed: _dangTai ? null : _dangNhap, // khóa nút khi đang tải
                    child: _dangTai
                        ? const CircularProgressIndicator()
                        : const Text('ĐĂNG NHẬP'),
                  ),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}