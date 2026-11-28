package vn.edu.vhu.ltdd.appadmin;

public class DonHang {
    private int id;             // Mã ID số trong Database (dùng để gửi API gán đơn)
    private String maDon;       // Mã vận đơn hiển thị
    private String nguoiNhan;   // Tên khách nhận hàng
    private String soDienThoai; // Số điện thoại khách
    private String diaChi;      // Địa chỉ giao hàng
    private String phuongXa;    // Phường/Xã
    private double trongLuong;  // Cân nặng theo kg
    private int tienCod;        // Tiền thu hộ COD

    public DonHang(int id, String maDon, String nguoiNhan, String soDienThoai,
                   String diaChi, String phuongXa, double trongLuong, int tienCod) {
        this.id = id;
        this.maDon = maDon;
        this.nguoiNhan = nguoiNhan;
        this.soDienThoai = soDienThoai;
        this.diaChi = diaChi;
        this.phuongXa = phuongXa;
        this.trongLuong = trongLuong;
        this.tienCod = tienCod;
    }

    // Các hàm lấy thông tin (Getter)
    public int getId() { return id; }
    public String getMaDon() { return maDon; }
    public String getNguoiNhan() { return nguoiNhan; }
    public String getSoDienThoai() { return soDienThoai; }
    public String getDiaChi() { return diaChi; }
    public String getPhuongXa() { return phuongXa; }
    public double getTrongLuong() { return trongLuong; }
    public int getTienCod() { return tienCod; }

    public static String dinhDangTien(int gia) {
        String s = String.valueOf(gia);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            if (i > 0 && (s.length() - i) % 3 == 0) sb.append('.');
            sb.append(s.charAt(i));
        }
        return sb + " đ";
    }
}