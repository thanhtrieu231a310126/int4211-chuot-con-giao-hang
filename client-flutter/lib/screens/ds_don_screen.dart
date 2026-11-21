import 'package:flutter/material.dart';
import '../models/don_hang.dart';
import '../services/api_service.dart';

class DsDonScreen extends StatefulWidget {
  const DsDonScreen({super.key});

  @override
  State<DsDonScreen> createState() => _DsDonScreenState();
}

class _DsDonScreenState extends State<DsDonScreen> {
  late Future<List<DonHang>> _tuongLaiDs;

  @override
  void initState() {
    super.initState();
    _tuongLaiDs = ApiService.layDonCuaToi();
  }

  Future<void> _taiLai() async {
    setState(() => _tuongLaiDs = ApiService.layDonCuaToi());
    await _tuongLaiDs;
  }

  (String, Color) _hienThiTrangThai(String trangThai) {
    switch (trangThai) {
      case 'CHUA_GAN':    return ('Chưa gán', Colors.grey);
      case 'DA_GAN':      return ('Đã gán', Colors.blue);
      case 'DA_LAY_HANG': return ('Đã lấy hàng', Colors.indigo);
      case 'DANG_GIAO':   return ('Đang giao', Colors.orange);
      case 'THANH_CONG':  return ('Thành công', Colors.green);
      case 'THAT_BAI':    return ('Thất bại', Colors.red);
      case 'CHO_GIAO_LAI': return ('Chờ giao lại', Colors.purple);
      case 'DA_HOAN_TRA': return ('Đã hoàn trả', Colors.brown);
      case 'DA_HUY':      return ('Đã hủy', Colors.black54);
      default:            return (trangThai, Colors.grey);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Đơn của tôi')),
      body: FutureBuilder<List<DonHang>>(
        future: _tuongLaiDs,
        builder: (context, snapshot) {
          if (snapshot.connectionState == ConnectionState.waiting) {
            return const Center(child: CircularProgressIndicator());
          }
          if (snapshot.hasError) {
            return Center(
              child: Column(
                mainAxisSize: MainAxisSize.min,
                children: [
                  Text(snapshot.error.toString().replaceFirst('Exception: ', ''), textAlign: TextAlign.center),
                  const SizedBox(height: 12),
                  ElevatedButton(onPressed: _taiLai, child: const Text('Thử lại')),
                ],
              ),
            );
          }
          final danhSach = snapshot.data ?? [];
          return RefreshIndicator(
            onRefresh: _taiLai,
            child: danhSach.isEmpty
                ? ListView(children: const [
                    SizedBox(height: 200),
                    Center(child: Text('Chưa có đơn nào được giao')),
                  ])
                : ListView.builder(
                    itemCount: danhSach.length,
                    itemBuilder: (context, index) {
                      final don = danhSach[index];
                      final (chu, mau) = _hienThiTrangThai(don.trangThai);
                      return Card(
                        margin: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
                        child: ListTile(
                          title: Text('${don.maDon} - ${don.tenNguoiNhan}',
                              style: const TextStyle(fontWeight: FontWeight.bold)),
                          subtitle: Text('${don.diaChi}\nCOD: ${don.tienCod} đ · ${don.trongLuong} kg'),
                          isThreeLine: true,
                          trailing: Chip(
                            label: Text(chu, style: const TextStyle(color: Colors.white)),
                            backgroundColor: mau,
                          ),
                        ),
                      );
                    },
                  ),
          );
        },
      ),
    );
  }
}
