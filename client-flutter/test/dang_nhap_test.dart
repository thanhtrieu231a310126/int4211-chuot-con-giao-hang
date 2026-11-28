import 'dart:convert';

import 'package:app_client/screens/dang_nhap_screen.dart';
import 'package:app_client/screens/ds_don_screen.dart';
import 'package:app_client/services/api_service.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:http/http.dart' as http;
import 'package:http/testing.dart';

http.Response phanHoiDangNhap(String vaiTro, {String token = 'token-test'}) =>
    http.Response(
      jsonEncode({
        'token': token,
        'nguoiDung': {'vaiTro': vaiTro},
      }),
      200,
    );

void main() {
  tearDown(ApiService.dangXuat);

  test('Shipper dang nhap va gui Bearer khi lay don', () async {
    await http.runWithClient(
      () async {
        await ApiService.dangNhap('shipper@test.vn', 'mat-khau-test');
        expect(ApiService.token, 'token-test');
        expect(await ApiService.layDonCuaToi(), isEmpty);
      },
      () => MockClient((request) async {
        if (request.method == 'POST') return phanHoiDangNhap('SHIPPER');
        expect(request.headers['Authorization'], 'Bearer token-test');
        return http.Response('[]', 200);
      }),
    );
  });

  test('Tai khoan dieu phoi bi chan va khong giu token cu', () async {
    ApiService.token = 'token-cu';
    await http.runWithClient(() async {
      await expectLater(
        ApiService.dangNhap('admin@test.vn', 'test'),
        throwsA(isA<Exception>()),
      );
      expect(ApiService.token, isNull);
    }, () => MockClient((_) async => phanHoiDangNhap('DIEU_PHOI')));
  });

  test('Khong chap nhan token rong', () async {
    await http.runWithClient(() async {
      await expectLater(
        ApiService.dangNhap('shipper@test.vn', 'test'),
        throwsA(isA<Exception>()),
      );
      expect(ApiService.token, isNull);
    }, () => MockClient((_) async => phanHoiDangNhap('SHIPPER', token: '')));
  });

  test('Sai mat khau khong bi bao thanh het phien', () async {
    await http.runWithClient(
      () async {
        await expectLater(
          ApiService.dangNhap('shipper@test.vn', 'sai'),
          throwsA(isNot(isA<PhienDangNhapHetHan>())),
        );
        expect(ApiService.token, isNull);
      },
      () =>
          MockClient((_) async => http.Response('{"loi":"Sai mat khau"}', 401)),
    );
  });

  test('Lay don gap 401 xoa token va bao het phien', () async {
    ApiService.token = 'token-het-han';
    await http.runWithClient(() async {
      await expectLater(
        ApiService.layDonCuaToi(),
        throwsA(isA<PhienDangNhapHetHan>()),
      );
      expect(ApiService.token, isNull);
    }, () => MockClient((_) async => http.Response('{}', 401)));
  });

  Widget appKiemThu() => MaterialApp(
    home: const DsDonScreen(),
    routes: {'/dang-nhap': (_) => const DangNhapScreen()},
  );

  testWidgets('401 dua ve dang nhap va xoa man danh sach khoi stack', (
    tester,
  ) async {
    ApiService.token = 'token-het-han';
    await http.runWithClient(() async {
      await tester.pumpWidget(appKiemThu());
      await tester.pumpAndSettle();
      expect(find.byType(DangNhapScreen), findsOneWidget);
      expect(find.byType(DsDonScreen), findsNothing);
      expect(ApiService.token, isNull);
      final context = tester.element(find.byType(DangNhapScreen));
      expect(Navigator.of(context).canPop(), isFalse);
    }, () => MockClient((_) async => http.Response('{}', 401)));
  });

  testWidgets('Huy dang xuat giu phien, xac nhan thi ve dang nhap', (
    tester,
  ) async {
    ApiService.token = 'token-test';
    await http.runWithClient(() async {
      await tester.pumpWidget(appKiemThu());
      await tester.pumpAndSettle();
      await tester.tap(find.byTooltip('Đăng xuất'));
      await tester.pumpAndSettle();
      await tester.tap(find.text('Hủy'));
      await tester.pumpAndSettle();
      expect(ApiService.token, 'token-test');
      await tester.tap(find.byTooltip('Đăng xuất'));
      await tester.pumpAndSettle();
      await tester.tap(find.widgetWithText(FilledButton, 'Đăng xuất'));
      await tester.pumpAndSettle();
      expect(find.byType(DangNhapScreen), findsOneWidget);
      expect(find.byType(DsDonScreen), findsNothing);
      expect(ApiService.token, isNull);
    }, () => MockClient((_) async => http.Response('[]', 200)));
  });
}
