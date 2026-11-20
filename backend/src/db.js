const { Pool } = require('pg');

const pool = new Pool({
  connectionString: process.env.DATABASE_URL,
  // Supabase yêu cầu SSL. Đặt DB_SSL=false khi chạy thử với PostgreSQL cài trên máy.
  ssl: process.env.DB_SSL === 'false' ? false : { rejectUnauthorized: false },
  max: 5, // gói Free, đừng mở quá nhiều kết nối
});

// Luôn truyền tham số dạng $1, $2 — KHÔNG nối chuỗi SQL
const truyVan = (sql, params) => pool.query(sql, params);

// Dùng cho nghiệp vụ cần transaction (gán shipper phải kiểm tra giới hạn và cập nhật cùng lúc)
async function trongGiaoDich(callback) {
  const client = await pool.connect();
  try {
    await client.query('BEGIN');
    const ketQua = await callback(client);
    await client.query('COMMIT');
    return ketQua;
  } catch (e) {
    await client.query('ROLLBACK');
    throw e;
  } finally {
    client.release();
  }
}

module.exports = { pool, truyVan, trongGiaoDich };
