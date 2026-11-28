class DonHang {
  final int id;
  final String maDon;
  final String tenNguoiNhan;
  final String sdtNhan;
  final String diaChi;
  final String trangThai;
  final int tienCod;
  final double trongLuong;
  final DateTime? hanGiao;

  const DonHang({
    this.id = 0,
    required this.maDon,
    required this.tenNguoiNhan,
    this.sdtNhan = '',
    required this.diaChi,
    required this.trangThai,
    required this.tienCod,
    this.trongLuong = 0,
    this.hanGiao,
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
      hanGiao: DateTime.tryParse((j['han_giao'] ?? '').toString()),
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
