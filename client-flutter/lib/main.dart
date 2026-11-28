import 'package:flutter/material.dart';

import 'screens/dang_nhap_screen.dart';

void main() {
  runApp(const AppClient());
}

class AppClient extends StatelessWidget {
  const AppClient({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Chuột Con Shipper',
      debugShowCheckedModeBanner: false,
      theme: ThemeData(colorSchemeSeed: Colors.orange, useMaterial3: true),
      home: const DangNhapScreen(),
      routes: {'/dang-nhap': (_) => const DangNhapScreen()},
    );
  }
}
