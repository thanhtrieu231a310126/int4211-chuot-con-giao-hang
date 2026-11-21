const jwt = require('jsonwebtoken');

function xacThuc(req, res, next) {
  const header = req.headers.authorization || '';
  const token = header.startsWith('Bearer ') ? header.slice(7) : null;
  if (!token) return res.status(401).json({ loi: 'Thiếu token' });
  try {
    req.nguoiDung = jwt.verify(token, process.env.JWT_SECRET);
    next();
  } catch (e) {
    return res.status(401).json({ loi: 'Token không hợp lệ hoặc đã hết hạn' });
  }
}

// Dùng: router.get('/', xacThuc, choPhep('DIEU_PHOI'), handler)
function choPhep(...vaiTroChoPhep) {
  return (req, res, next) => {
    if (!vaiTroChoPhep.includes(req.nguoiDung.vaiTro)) {
      return res.status(403).json({ loi: 'Không có quyền thực hiện' });
    }
    next();
  };
}

module.exports = { xacThuc, choPhep };
