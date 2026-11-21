// Lỗi nghiệp vụ có mã HTTP, ví dụ: throw loi(409, 'Shipper đã đủ số đơn', { dangCam: 5 })
// Bộ xử lý lỗi trong index.js sẽ trả về { loi: thongBao, ...them } với đúng mã này.
function loi(maHttp, thongBao, them = {}) {
  const e = new Error(thongBao);
  e.maHttp = maHttp;
  e.them = them;
  return e;
}

// Express 4 không tự bắt lỗi trong hàm async: lỗi sẽ làm sập server.
// Bọc handler bằng boc(...) để chuyển lỗi sang bộ xử lý lỗi chung.
const boc = (handler) => (req, res, next) =>
  Promise.resolve(handler(req, res, next)).catch(next);

module.exports = { loi, boc };
