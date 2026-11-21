const express = require('express');
const { truyVan } = require('../db');
const { xacThuc, choPhep } = require('../middleware/auth');
const { boc } = require('../loi');

const router = express.Router();

// GET /shipper   (điều phối xem danh sách shipper, số đơn đang cầm và khu vực trước khi gán)
router.get('/', xacThuc, choPhep('DIEU_PHOI'), boc(async (req, res) => {
  const r = await truyVan(
    `SELECT s.id, n.ho_ten, n.sdt, s.bien_so, s.khu_vuc_phu_trach,
            s.trang_thai, s.so_don_toi_da,
            COUNT(d.id)::int AS so_don_dang_cam
       FROM shipper s
       JOIN nguoi_dung n ON n.id = s.nguoi_dung_id
       LEFT JOIN don_giao d ON d.shipper_id = s.id
                           AND d.trang_thai IN ('DA_GAN', 'DA_LAY_HANG', 'DANG_GIAO')
      GROUP BY s.id, n.ho_ten, n.sdt
      ORDER BY s.id`);
  res.json(r.rows);
}));

// GET /shipper/toi/don   (đơn của shipper đang đăng nhập, dùng cho app Flutter)
router.get('/toi/don', xacThuc, choPhep('SHIPPER'), boc(async (req, res) => {
  // Mã shipper lấy từ token, không nhận từ app, để shipper không xem được đơn của người khác
  const r = await truyVan(
    `SELECT d.*
       FROM don_giao d
       JOIN shipper s ON s.id = d.shipper_id
      WHERE s.nguoi_dung_id = $1
        AND d.trang_thai IN ('DA_GAN', 'DA_LAY_HANG', 'DANG_GIAO')
      ORDER BY d.han_giao NULLS LAST, d.id`,
    [req.nguoiDung.id]);
  res.json(r.rows);
}));

module.exports = router;
