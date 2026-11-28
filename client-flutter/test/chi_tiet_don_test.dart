import 'dart:convert';

import 'package:app_client/models/don_hang.dart';
import 'package:app_client/screens/chi_tiet_don_screen.dart';
import 'package:app_client/screens/ds_don_screen.dart';
import 'package:app_client/services/api_service.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:http/http.dart' as http;
import 'package:http/testing.dart';

void main() {
  final jsonDon = <String, dynamic>{
    'id': 15,
    'ma_van_don': 'TEST001',
    'nguoi_nhan': 'Người nhận kiểm thử',
    'sdt_nhan': '0900000000',
    'dia_chi': 'Địa chỉ kiểm thử',
    'phuong_xa': 'Phường kiểm thử',
    'tien_cod': '150000',
    'trong_luong': '1.50',
    'trang_thai': 'DA_GAN',
    'han_giao': '2026-11-28T20:30:00.000Z',
  };

  tearDown(ApiService.dangXuat);

  test('Doc han giao ISO, thieu hoac sai dinh dang khong gay loi', () {
    expect(
      DonHang.fromJson(jsonDon).hanGiao,
      DateTime.utc(2026, 11, 28, 20, 30),
    );
    expect(DonHang.fromJson({...jsonDon, 'han_giao': null}).hanGiao, isNull);
    expect(DonHang.fromJson({...jsonDon, 'han_giao': 'sai'}).hanGiao, isNull);
  });

  testWidgets('Bam dung don mo chi tiet va quay lai danh sach', (tester) async {
    ApiService.token = 'token-kiem-thu';
    await http.runWithClient(
      () async {
        await tester.pumpWidget(const MaterialApp(home: DsDonScreen()));
        await tester.pumpAndSettle();
        await tester.tap(find.text('TEST001 - Người nhận kiểm thử'));
        await tester.pumpAndSettle();
        expect(find.byType(ChiTietDonScreen), findsOneWidget);
        expect(find.text('0900000000'), findsOneWidget);
        expect(find.text('Địa chỉ kiểm thử, Phường kiểm thử'), findsOneWidget);
        expect(find.text('150.000 đ'), findsOneWidget);
        expect(find.text('1.5 kg'), findsOneWidget);
        await tester.scrollUntilVisible(
          find.text('29/11/2026 03:30 (giờ Việt Nam)'),
          150,
          scrollable: find
              .descendant(
                of: find.byType(ListView),
                matching: find.byType(Scrollable),
              )
              .first,
        );
        expect(find.text('29/11/2026 03:30 (giờ Việt Nam)'), findsOneWidget);
        await tester.tap(find.byType(BackButton));
        await tester.pumpAndSettle();
        expect(find.byType(ChiTietDonScreen), findsNothing);
        expect(find.text('TEST001 - Người nhận kiểm thử'), findsOneWidget);
      },
      () => MockClient(
        (_) async => http.Response(
          jsonEncode([jsonDon]),
          200,
          headers: {'content-type': 'application/json; charset=utf-8'},
        ),
      ),
    );
  });

  testWidgets('Man nho cuon duoc va thong tin trong co loi nhac', (
    tester,
  ) async {
    tester.view.physicalSize = const Size(320, 480);
    tester.view.devicePixelRatio = 1;
    addTearDown(tester.view.resetPhysicalSize);
    addTearDown(tester.view.resetDevicePixelRatio);
    await tester.pumpWidget(
      MaterialApp(
        home: ChiTietDonScreen(
          don: DonHang.fromJson({...jsonDon, 'sdt_nhan': '', 'han_giao': null}),
          tenTrangThai: 'Đã gán',
        ),
      ),
    );
    await tester.pumpAndSettle();
    expect(find.text('Đã gán'), findsOneWidget);
    await tester.scrollUntilVisible(
      find.text('Hạn giao'),
      100,
      scrollable: find
          .descendant(
            of: find.byType(ListView),
            matching: find.byType(Scrollable),
          )
          .first,
    );
    expect(find.text('Chưa có thông tin'), findsWidgets);
    expect(tester.takeException(), isNull);
  });
}
