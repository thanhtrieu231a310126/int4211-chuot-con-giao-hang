import 'package:flutter/material.dart';

import '../models/don_hang.dart';

class ChiTietDonScreen extends StatelessWidget {
  final DonHang don;
  final String tenTrangThai;

  const ChiTietDonScreen({
    super.key,
    required this.don,
    required this.tenTrangThai,
  });

  String _hienThi(String giaTri) =>
      giaTri.trim().isEmpty ? 'Chưa có thông tin' : giaTri;

  String _dinhDangTien(int tien) => tien.toString().replaceAllMapped(
    RegExp(r'(\d)(?=(\d{3})+(?!\d))'),
    (m) => '${m[1]}.',
  );

  String _hanGiao() {
    final han = don.hanGiao;
    if (han == null) return 'Chưa có thông tin';
    // Hien thi gio Viet Nam, khong phu thuoc mui gio may dang chay.
    final gioVN = han.toUtc().add(const Duration(hours: 7));
    String haiSo(int so) => so.toString().padLeft(2, '0');
    return '${haiSo(gioVN.day)}/${haiSo(gioVN.month)}/${gioVN.year} '
        '${haiSo(gioVN.hour)}:${haiSo(gioVN.minute)} (giờ Việt Nam)';
  }

  Widget _thongTin(String nhan, String giaTri) => Padding(
    padding: const EdgeInsets.symmetric(vertical: 10),
    child: Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(nhan, style: const TextStyle(color: Colors.black54)),
        const SizedBox(height: 4),
        SelectableText(giaTri, style: const TextStyle(fontSize: 16)),
      ],
    ),
  );

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Chi tiết đơn')),
      body: ListView(
        padding: const EdgeInsets.all(20),
        children: [
          Text(
            _hienThi(don.maDon),
            style: Theme.of(context).textTheme.headlineSmall,
          ),
          const SizedBox(height: 8),
          Text(tenTrangThai, style: Theme.of(context).textTheme.titleMedium),
          const Divider(height: 28),
          _thongTin('Người nhận', _hienThi(don.tenNguoiNhan)),
          _thongTin('Số điện thoại', _hienThi(don.sdtNhan)),
          _thongTin('Địa chỉ giao hàng', _hienThi(don.diaChi)),
          _thongTin('Tiền thu hộ (COD)', '${_dinhDangTien(don.tienCod)} đ'),
          _thongTin('Trọng lượng', '${don.trongLuong} kg'),
          _thongTin('Hạn giao', _hanGiao()),
        ],
      ),
    );
  }
}
