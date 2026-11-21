// Mô tả một đơn hàng: mỗi đơn có những thông tin gì
class DonHang {
  final String maDon;
  final String tenNguoiNhan;
  final String diaChi;
  final String trangThai; // DA_GAN, DA_LAY_HANG, DANG_GIAO, THANH_CONG, THAT_BAI
  final int tienCod;      // tiền thu hộ, đơn vị đồng
  final double trongLuong; // kg

  const DonHang({
    required this.maDon,
    required this.tenNguoiNhan,
    required this.diaChi,
    required this.trangThai,
    required this.tienCod,
    this.trongLuong = 0,
  });

  // Đổi 1 đơn dạng JSON (backend gửi về) thành object DonHang
  // TODO: khi Bình gửi mẫu JSON thật thì sửa tên các trường cho khớp
  factory DonHang.fromJson(Map<String, dynamic> j) {
    return DonHang(
      maDon: (j['ma_don'] ?? j['id'] ?? '').toString(),
      tenNguoiNhan: (j['ten_nguoi_nhan'] ?? '').toString(),
      diaChi: (j['dia_chi_giao'] ?? j['dia_chi'] ?? '').toString(),
      trangThai: (j['trang_thai'] ?? '').toString(),
      tienCod: docSo(j['tien_cod']).round(),
      trongLuong: docSo(j['trong_luong']),
    );
  }

  // Backend có thể trả số dạng 150000 hoặc chuỗi "150000" / "150000.00"
  // Hàm này đổi tất cả về số, lỗi thì trả 0 chứ không làm app văng
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

// Dữ liệu giả để làm giao diện trước, sau này thay bằng dữ liệu từ API
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