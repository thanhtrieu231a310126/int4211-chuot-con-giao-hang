const express = require('express');
const { truyVan, trongGiaoDich } = require('../db');
const { xacThuc, choPhep } = require('../middleware/auth');
const { loi, boc } = require('../loi');

const router = express.Router();

// Các trạng thái được tính là shipper "đang cầm" đơn
const DANG_CAM = ['DA_GAN', 'DA_LAY_HANG', 'DANG_GIAO'];

// GET /don-giao?trangThai=&phuongXa=&shipper=   (bộ lọc cho bàn điều phối)
router.get('/', xacThuc, choPhep('DIEU_PHOI'), boc(async (req, res) => {
  const { trangThai, phuongXa, shipper } = req.query;
  const dieuKien = [];
  const thamSo = [];

  // Mỗi bộ lọc thêm một điều kiện, giá trị đi vào tham số $n, không nối chuỗi
  if (trangThai) {
    thamSo.push(trangThai);
    dieuKien.push(`d.trang_thai = $${thamSo.length}`);
  }
  if (phuongXa) {
    thamSo.push(phuongXa);
    dieuKien.push(`d.phuong_xa = $${thamSo.length}`);
  }
  if (shipper) {
    thamSo.push(Number(shipper));
    dieuKien.push(`d.shipper_id = $${thamSo.length}`);
  }
  const where = dieuKien.length ? 'WHERE ' + dieuKien.join(' AND ') : '';

  const r = await truyVan(
    `SELECT d.*, n.ho_ten AS ten_shipper
       FROM don_giao d
       LEFT JOIN shipper s ON s.id = d.shipper_id
       LEFT JOIN nguoi_dung n ON n.id = s.nguoi_dung_id
     ${where}
     ORDER BY d.han_giao NULLS LAST, d.id
     LIMIT 500`,
    thamSo);
  res.json(r.rows);
}));

// PATCH /don-giao/:id/gan-shipper   body: { shipperId }
// Điểm nhấn của đề: máy chủ chặn gán vượt so_don_toi_da, mọi bước nằm trong một transaction.
router.patch('/:id/gan-shipper', xacThuc, choPhep('DIEU_PHOI'), boc(async (req, res) => {
  const donId = Number(req.params.id);
  const shipperId = Number(req.body.shipperId);
  if (!Number.isInteger(donId) || !Number.isInteger(shipperId))
    return res.status(400).json({ loi: 'Mã đơn hoặc mã shipper không hợp lệ' });

  const don = await trongGiaoDich(async (client) => {
    // 1. Khóa dòng shipper (FOR UPDATE). Hai yêu cầu gán cùng lúc cho một shipper
    //    phải xếp hàng chờ nhau, nên không thể cùng đếm ra "còn chỗ" rồi vượt giới hạn.
    const s = (await client.query(
      'SELECT id, trang_thai, so_don_toi_da FROM shipper WHERE id = $1 FOR UPDATE',
      [shipperId])).rows[0];
    if (!s) throw loi(404, 'Không tìm thấy shipper');
    if (s.trang_thai === 'NGHI') throw loi(409, 'Shipper đang nghỉ, không thể nhận đơn');

    // 2. Khóa dòng đơn và kiểm tra đơn còn được gán không
    const d = (await client.query(
      'SELECT id, trang_thai FROM don_giao WHERE id = $1 FOR UPDATE',
      [donId])).rows[0];
    if (!d) throw loi(404, 'Không tìm thấy đơn');
    if (!['CHUA_GAN', 'CHO_GIAO_LAI'].includes(d.trang_thai))
      throw loi(409, 'Đơn không ở trạng thái chờ gán', { trangThai: d.trang_thai });

    // 3. Đếm số đơn shipper đang cầm và so với giới hạn
    const dangCam = Number((await client.query(
      'SELECT COUNT(*) FROM don_giao WHERE shipper_id = $1 AND trang_thai = ANY($2)',
      [shipperId, DANG_CAM])).rows[0].count);
    if (dangCam >= s.so_don_toi_da)
      throw loi(409, 'Shipper đã đủ số đơn tối đa', { dangCam, toiDa: s.so_don_toi_da });

    // 4. Gán đơn
    const capNhat = await client.query(
      `UPDATE don_giao SET shipper_id = $1, trang_thai = 'DA_GAN'
        WHERE id = $2 RETURNING *`,
      [shipperId, donId]);

    // 5. Ghi lịch sử: chỉ INSERT dòng mới, không sửa dòng cũ
    await client.query(
      `INSERT INTO lich_su_trang_thai (don_id, trang_thai, nguoi_cap_nhat, ghi_chu)
       VALUES ($1, 'DA_GAN', $2, $3)`,
      [donId, req.nguoiDung.id, `Gán cho shipper #${shipperId}`]);

    return capNhat.rows[0];
  });

  res.json(don);
}));

module.exports = router;
