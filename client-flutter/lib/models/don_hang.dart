class DonHang {
  final int id;
  final String maDon;
  final String tenNguoiNhan;
  final String sdtNhan;
  final String diaChi;
  final String trangThai;
  final int tienCod;
  final double trongLuong;

  const DonHang({
    this.id = 0,
    required this.maDon,
    required this.tenNguoiNhan,
    this.sdtNhan = '',
    required this.diaChi,
    required this.trangThai,
    required this.tienCod,
    this.trongLuong = 0,
  });

  factory DonHang.fromJson(Map<String, dynamic> j) {
    final diaChi = (j['dia_chi'] ?? '').toString();
    final phuongXa = (j['phuong_xa'] ?? '').toString();
    return DonHang(
      id: docSo(j['id']).toInt(),
      maDon: (j['ma_van_don'] ?? '').toString(),
      tenNguoiNhan: (j['nguoi_nhan'] ?? '').toString(),
      sdtNhan: (j['sdt_nhan'] ?? '').toString(),
      diaChi: phuongXa.isEmpty ? diaChi : '$diaChi, $phuongXa',
      trangThai: (j['trang_thai'] ?? '').toString(),
      tienCod: docSo(j['tien_cod']).round(),
      trongLuong: docSo(j['trong_luong']),
    );
  }

  // backend co the tra so dang chuoi "150000"
  static double docSo(dynamic giaTri) {
    try {
      if (giaTri == null) return 0;
      if (giaTri is num) return giaTri.toDouble();
      return double.parse(giaTri.toString().trim());
    } catch (_) {
      return 0;
    }
  }
}

// du lieu test khi chua co backend
const List<DonHang> danhSachDonGia = [
  DonHang(maDon: 'DH001', tenNguoiNhan: 'Nguyễn Văn An',
      diaChi: '12 Lê Lợi, P. Bến Thành', trangThai: 'DA_GAN', tienCod: 150000, trongLuong: 1.2),
  DonHang(maDon: 'DH002', tenNguoiNhan: 'Trần Thị Bình',
      diaChi: '45 Nguyễn Trãi, P. Chợ Quán', trangThai: 'DANG_GIAO', tienCod: 320000, trongLuong: 3.5),
  DonHang(maDon: 'DH003', tenNguoiNhan: 'Lê Minh Cường',
      diaChi: '8 Võ Văn Tần, P. Xuân Hòa', trangThai: 'THANH_CONG', tienCod: 0, trongLuong: 0.5),
  DonHang(maDon: 'DH004', tenNguoiNhan: 'Phạm Thu Dung',
      diaChi: '99 Cách Mạng Tháng 8, P. Bàn Cờ', trangThai: 'THAT_BAI', tienCod: 210000, trongLuong: 2),
];