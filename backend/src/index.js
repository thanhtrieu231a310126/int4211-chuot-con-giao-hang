require('dotenv').config();
const express = require('express');
const cors = require('cors');
const { truyVan } = require('./db');

const TAG = '[CHUOT_CON]';

// Thiếu biến môi trường thì dừng ngay và báo rõ, thay vì chạy rồi lỗi 502 khó đoán
for (const ten of ['DATABASE_URL', 'JWT_SECRET']) {
  if (!process.env[ten]) {
    console.error(TAG, `Thiếu biến môi trường ${ten}`);
    process.exit(1);
  }
}

const app = express();
app.use(cors());
app.use(express.json());

// Kiểm tra sức khỏe: Render và thầy mở đường dẫn này để biết server còn sống
app.get('/health', async (req, res) => {
  let csdl = 'ok';
  try {
    await truyVan('SELECT 1');
  } catch (e) {
    csdl = 'loi';
  }
  res.json({ trangThai: 'ok', csdl, thoiGian: new Date().toISOString() });
});

app.use('/auth', require('./routes/auth'));
app.use('/don-giao', require('./routes/donGiao'));
app.use('/shipper', require('./routes/shipper'));

app.use((req, res) => res.status(404).json({ loi: 'Không tìm thấy đường dẫn' }));

// Bộ xử lý lỗi chung
app.use((err, req, res, next) => {
  if (err.type === 'entity.parse.failed')
    return res.status(400).json({ loi: 'Dữ liệu JSON không hợp lệ' });
  if (err.maHttp)
    return res.status(err.maHttp).json({ loi: err.message, ...err.them });
  console.error(TAG, err);
  res.status(500).json({ loi: 'Lỗi máy chủ' });
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => console.log(TAG, `Server chạy ở cổng ${PORT}`));
