package vn.edu.vhu.ltdd.appadmin;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class DonTheoDoiAdapter extends RecyclerView.Adapter<DonTheoDoiAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(ApiService.DonHangApi donHang);
    }

    private final List<ApiService.DonHangApi> danhSach;
    private final OnItemClickListener listener;

    public DonTheoDoiAdapter(List<ApiService.DonHangApi> danhSach, OnItemClickListener listener) {
        this.danhSach = danhSach;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_don_theo_doi, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ApiService.DonHangApi don = danhSach.get(position);

        holder.tvMaDon.setText(don.ma_van_don != null ? don.ma_van_don : "---");
        holder.tvNguoiNhan.setText(don.nguoi_nhan != null ? don.nguoi_nhan : "");
        holder.tvDiaChi.setText((don.dia_chi != null ? don.dia_chi : "") + (don.phuong_xa != null ? ", " + don.phuong_xa : ""));

        // Định dạng tiền COD và khối lượng
        int cod = 0;
        try {
            if (don.tien_cod != null) cod = (int) Double.parseDouble(don.tien_cod);
        } catch (Exception ignored) {}
        holder.tvTienCod.setText("COD: " + DonHang.dinhDangTien(cod));
        holder.tvTrongLuong.setText((don.trong_luong != null ? don.trong_luong : "0") + " kg");

        // Hiển thị tên Shipper phụ trách
        if (don.ten_shipper != null && !don.ten_shipper.isEmpty()) {
            holder.tvShipper.setText("Shipper: " + don.ten_shipper);
            holder.tvShipper.setTextColor(Color.parseColor("#2563EB")); // Xanh dương
        } else {
            holder.tvShipper.setText("Chưa có Shipper phụ trách");
            holder.tvShipper.setTextColor(Color.parseColor("#71717A")); // Xám
        }

        // Đổi màu trạng thái
        String tt = don.trang_thai != null ? don.trang_thai : "CHUA_GAN";
        if ("THANH_CONG".equals(tt)) {
            holder.tvTrangThai.setText("Thành công");
            holder.cardTrangThai.setCardBackgroundColor(Color.parseColor("#DCFCE7"));
            holder.tvTrangThai.setTextColor(Color.parseColor("#15803D"));
        } else if ("DANG_GIAO".equals(tt)) {
            holder.tvTrangThai.setText("Đang giao");
            holder.cardTrangThai.setCardBackgroundColor(Color.parseColor("#FEF3C7"));
            holder.tvTrangThai.setTextColor(Color.parseColor("#B45309"));
        } else if ("DA_GAN".equals(tt)) {
            holder.tvTrangThai.setText("Đã gán");
            holder.cardTrangThai.setCardBackgroundColor(Color.parseColor("#DBEAFE"));
            holder.tvTrangThai.setTextColor(Color.parseColor("#1D4ED8"));
        } else if ("THAT_BAI".equals(tt)) {
            holder.tvTrangThai.setText("Thất bại");
            holder.cardTrangThai.setCardBackgroundColor(Color.parseColor("#FEE2E2"));
            holder.tvTrangThai.setTextColor(Color.parseColor("#B91C1C"));
        } else {
            // Mặc định: Chưa gán
            holder.tvTrangThai.setText("Chưa gán");
            holder.cardTrangThai.setCardBackgroundColor(Color.parseColor("#FFEDD5"));
            holder.tvTrangThai.setTextColor(Color.parseColor("#C2410C"));
        }

        // Bắt sự kiện chạm vào ô đơn hàng
        holder.itemView.setOnClickListener(v -> {
            int p = holder.getBindingAdapterPosition();
            if (p != RecyclerView.NO_POSITION && listener != null) {
                listener.onItemClick(danhSach.get(p));
            }
        });
    }

    @Override
    public int getItemCount() {
        return danhSach.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMaDon, tvNguoiNhan, tvDiaChi, tvShipper, tvTienCod, tvTrongLuong, tvTrangThai;
        MaterialCardView cardTrangThai;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMaDon = itemView.findViewById(R.id.tvMaDon);
            tvNguoiNhan = itemView.findViewById(R.id.tvNguoiNhan);
            tvDiaChi = itemView.findViewById(R.id.tvDiaChi);
            tvShipper = itemView.findViewById(R.id.tvShipper);
            tvTienCod = itemView.findViewById(R.id.tvTienCod);
            tvTrongLuong = itemView.findViewById(R.id.tvTrongLuong);
            tvTrangThai = itemView.findViewById(R.id.tvTrangThai);
            cardTrangThai = itemView.findViewById(R.id.cardTrangThai);
        }
    }
}