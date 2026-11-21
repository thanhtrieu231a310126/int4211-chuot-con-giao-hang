# Tài liệu API – Nhóm Chuột Con (Đề 6)

Backend hệ thống điều phối giao hàng chặng cuối nội thành.

- **Địa chỉ gốc (BASE_URL):** `https://<chưa deploy>.onrender.com` — chạy ở máy thì là `http://localhost:3000`
  - Máy ảo Android gọi máy tính thì dùng `http://10.0.2.2:3000`
- Dữ liệu gửi và nhận đều là JSON (`Content-Type: application/json`).
- Render gói Free sẽ "ngủ" sau 15 phút không ai gọi. Lần gọi đầu tiên có thể mất 30–50 giây, app nên đặt timeout ít nhất 60 giây.

## 1. Gửi token

Mọi API trừ `/health` và `/auth/dang-nhap` đều cần token lấy từ API đăng nhập, gửi trong header:

```
Authorization: Bearer <token>
```

Token hết hạn sau **7 ngày**. Khi nhận mã 401, app xóa token đã lưu và quay về màn hình đăng nhập.

## 2. Định dạng lỗi

Mọi lỗi đều trả về một object có trường `loi`, một số lỗi kèm thêm thông tin:

```json
{ "loi": "Shipper đã đủ số đơn tối đa", "dangCam": 2, "toiDa": 2 }
```

| Mã | Ý nghĩa |
|---|---|
| 200 | Thành công |
| 400 | Dữ liệu gửi lên thiếu hoặc sai định dạng |
| 401 | Thiếu token, token sai, token hết hạn, hoặc sai email/mật khẩu |
| 403 | Đúng token nhưng sai vai trò (ví dụ shipper gọi API của điều phối) |
| 404 | Không tìm thấy đơn, shipper hoặc đường dẫn |
| 409 | Vi phạm nghiệp vụ (shipper đủ đơn, đơn không ở trạng thái chờ gán...) |
| 500 | Lỗi máy chủ |

## 3. Mã trạng thái dùng chung

**Vai trò (`vai_tro`):** `DIEU_PHOI`, `SHIPPER`

**Trạng thái đơn (`trang_thai` của `don_giao`):**

| Mã | Hiển thị |
|---|---|
| `CHUA_GAN` | Chưa gán |
| `DA_GAN` | Đã gán |
| `DA_LAY_HANG` | Đã lấy hàng |
| `DANG_GIAO` | Đang giao |
| `THANH_CONG` | Giao thành công |
| `THAT_BAI` | Giao thất bại |
| `CHO_GIAO_LAI` | Chờ giao lại |
| `DA_HOAN_TRA` | Đã hoàn trả |
| `DA_HUY` | Đã hủy |

Shipper được tính là **đang cầm** đơn khi đơn ở `DA_GAN`, `DA_LAY_HANG` hoặc `DANG_GIAO`.

**Trạng thái shipper:** `RANH`, `BAN`, `NGHI`

## 4. Tài khoản thử nghiệm

Mật khẩu chung: `123456`

| Email | Vai trò | Ghi chú |
|---|---|---|
| `dieuphoi@chuotcon.vn` | DIEU_PHOI | Dùng cho app Admin |
| `shipper1@chuotcon.vn` | SHIPPER | Shipper #1, giới hạn 2 đơn |
| `shipper2@chuotcon.vn` | SHIPPER | Shipper #2, giới hạn 5 đơn |

---

## 5. Danh sách API

| Phương thức | Đường dẫn | Vai trò | Dùng cho |
|---|---|---|---|
| GET | `/health` | Không cần | Kiểm tra server |
| POST | `/auth/dang-nhap` | Không cần | Cả hai app |
| GET | `/don-giao` | DIEU_PHOI | App Admin – bàn điều phối |
| PATCH | `/don-giao/:id/gan-shipper` | DIEU_PHOI | App Admin – gán đơn |
| GET | `/shipper` | DIEU_PHOI | App Admin – chọn shipper khi gán |
| GET | `/shipper/toi/don` | SHIPPER | App Flutter – đơn được gán |

### GET /health

Kiểm tra server còn chạy và kết nối được cơ sở dữ liệu.

Phản hồi 200:

```json
{ "trangThai": "ok", "csdl": "ok", "thoiGian": "2026-11-21T08:00:00.000Z" }
```

`csdl` là `"loi"` nếu server chạy nhưng không kết nối được cơ sở dữ liệu.

### POST /auth/dang-nhap

Body:

```json
{ "email": "dieuphoi@chuotcon.vn", "matKhau": "123456" }
```

Phản hồi 200:

```json
{
  "token": "eyJhbGciOiJIUzI1NiIs...",
  "nguoiDung": { "id": 1, "hoTen": "Điều phối A", "vaiTro": "DIEU_PHOI" }
}
```

App lưu `token` để gửi kèm các API sau, dùng `vaiTro` để mở đúng màn hình.

| Lỗi | Khi nào |
|---|---|
| 400 `Thiếu email hoặc mật khẩu` | Thiếu một trong hai trường |
| 401 `Email hoặc mật khẩu không đúng` | Sai email hoặc sai mật khẩu (cùng một thông báo) |

### GET /don-giao

Danh sách đơn cho bàn điều phối. Tối đa 500 đơn, sắp theo hạn giao gần nhất.

Tham số lọc (đều không bắt buộc, có thể kết hợp):

| Tham số | Ví dụ | Ý nghĩa |
|---|---|---|
| `trangThai` | `CHUA_GAN` | Lọc theo trạng thái đơn |
| `phuongXa` | `Phường Bến Thành` | Lọc theo phường/xã (nhớ mã hóa URL) |
| `shipper` | `1` | Lọc theo mã shipper |

Ví dụ: `GET /don-giao?trangThai=CHUA_GAN&phuongXa=Ph%C6%B0%E1%BB%9Dng%20B%E1%BA%BFn%20Th%C3%A0nh`

Phản hồi 200 (mảng):

```json
[
  {
    "id": 1,
    "ma_van_don": "CC0001",
    "nguoi_nhan": "Nguyễn Văn An",
    "sdt_nhan": "0911000001",
    "dia_chi": "12 Lê Lợi",
    "phuong_xa": "Phường Bến Thành",
    "trong_luong": "1.50",
    "tien_cod": "150000",
    "shipper_id": null,
    "trang_thai": "CHUA_GAN",
    "han_giao": "2026-11-22T08:00:00.000Z",
    "ten_shipper": null
  }
]
```

Lưu ý: `trong_luong` và `tien_cod` trả về dạng **chuỗi** (kiểu NUMERIC của PostgreSQL), app tự đổi sang số.

### PATCH /don-giao/:id/gan-shipper

Gán đơn `:id` cho một shipper. Máy chủ kiểm tra trong một transaction:

1. Shipper tồn tại và không ở trạng thái `NGHI`.
2. Đơn đang ở `CHUA_GAN` hoặc `CHO_GIAO_LAI`.
3. Số đơn shipper đang cầm nhỏ hơn `so_don_toi_da`.

Đạt cả ba thì đơn chuyển sang `DA_GAN` và ghi một dòng vào `lich_su_trang_thai`.

Body:

```json
{ "shipperId": 1 }
```

Phản hồi 200: đơn sau khi gán (cùng các trường như `GET /don-giao`, không có `ten_shipper`).

| Lỗi | Khi nào |
|---|---|
| 400 `Mã đơn hoặc mã shipper không hợp lệ` | `:id` hoặc `shipperId` không phải số |
| 404 `Không tìm thấy đơn` / `Không tìm thấy shipper` | Mã không tồn tại |
| 409 `Shipper đang nghỉ, không thể nhận đơn` | Shipper ở trạng thái `NGHI` |
| 409 `Đơn không ở trạng thái chờ gán` | Kèm `trangThai` hiện tại của đơn |
| 409 `Shipper đã đủ số đơn tối đa` | Kèm `dangCam` và `toiDa` |

Ví dụ lỗi 409 để app hiển thị cho điều phối:

```json
{ "loi": "Shipper đã đủ số đơn tối đa", "dangCam": 2, "toiDa": 2 }
```

### GET /shipper

Danh sách shipper kèm số đơn đang cầm, để điều phối chọn người khi gán đơn.

Phản hồi 200 (mảng):

```json
[
  {
    "id": 1,
    "ho_ten": "Shipper Một",
    "sdt": "0900000002",
    "bien_so": "59A1-11111",
    "khu_vuc_phu_trach": 1,
    "trang_thai": "RANH",
    "so_don_toi_da": 2,
    "so_don_dang_cam": 0
  }
]
```

### GET /shipper/toi/don

Các đơn shipper đang đăng nhập đang cầm (`DA_GAN`, `DA_LAY_HANG`, `DANG_GIAO`), sắp theo hạn giao gần nhất. Máy chủ lấy mã shipper từ token, app không cần và không được gửi mã shipper.

Phản hồi 200: mảng đơn, cùng các trường như `GET /don-giao` (không có `ten_shipper`).

---

## 6. Sắp có

| Phương thức | Đường dẫn | Ghi chú |
|---|---|---|
| PATCH | `/don-giao/:id/trang-thai` | Shipper cập nhật trạng thái, kèm ảnh bằng chứng và lý do thất bại |
| POST | `/don-giao` | Điều phối tạo đơn |
| PATCH | `/shipper/toi/trang-thai` | Shipper bật/tắt nhận đơn |
| GET | `/doi-soat`, POST `/doi-soat/:id/xac-nhan` | Đối soát COD |
| GET | `/bao-cao/hieu-suat` | Báo cáo |
