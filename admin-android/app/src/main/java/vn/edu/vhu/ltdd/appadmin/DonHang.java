package vn.edu.vhu.ltdd.appadmin;

public class DonHang {
    private String maDon;       // Ví dụ: "DH001"
    private String nguoiNhan;   // Tên khách nhận hàng
    private String soDienThoai; // Số điện thoại khách
    private String diaChi;      // Địa chỉ giao hàng
    private String phuongXa;    // Phường/Xã (để lọc đơn theo yêu cầu)
    private double trongLuong;  // Cân nặng theo kg (để lọc đơn theo yêu cầu)
    private int tienCod;        // Tiền thu hộ COD

    public DonHang(String maDon, String nguoiNhan, String soDienThoai,
                   String diaChi, String phuongXa, double trongLuong, int tienCod) {
        this.maDon = maDon;
        this.nguoiNhan = nguoiNhan;
        this.soDienThoai = soDienThoai;
        this.diaChi = diaChi;
        this.phuongXa = phuongXa;
        this.trongLuong = trongLuong;
        this.tienCod = tienCod;
    }
    // Hàm lấy thông tin
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