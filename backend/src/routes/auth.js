const express = require('express');
const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');
const { truyVan } = require('../db');
const { boc } = require('../loi');

const router = express.Router();

// POST /auth/dang-nhap  body: { email, matKhau }
router.post('/dang-nhap', boc(async (req, res) => {
  const { email, matKhau } = req.body;
  if (!email || !matKhau) return res.status(400).json({ loi: 'Thiếu email hoặc mật khẩu' });

  const r = await truyVan('SELECT * FROM nguoi_dung WHERE email = $1', [email]);
  const u = r.rows[0];
  // Thông báo giống nhau cho cả hai trường hợp, tránh lộ email nào đã tồn tại
  if (!u || !(await bcrypt.compare(matKhau, u.mat_khau_hash)))
    return res.status(401).json({ loi: 'Email hoặc mật khẩu không đúng' });

  const token = jwt.sign({ id: u.id, vaiTro: u.vai_tro },
                         process.env.JWT_SECRET, { expiresIn: '7d' });
  res.json({ token, nguoiDung: { id: u.id, hoTen: u.ho_ten, vaiTro: u.vai_tro } });
}));

module.exports = router;
