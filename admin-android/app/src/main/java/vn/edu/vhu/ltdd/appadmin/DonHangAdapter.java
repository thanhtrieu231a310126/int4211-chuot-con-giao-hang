package vn.edu.vhu.ltdd.appadmin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class DonHangAdapter extends RecyclerView.Adapter<DonHangAdapter.DonHangViewHolder> {

    // 1. Interface để báo sự kiện khi người dùng bấm vào 1 dòng đơn hàng
    public interface OnDonHangClickListener {
        void onDonHangClick(DonHang donHang);
    }

    // 2. ViewHolder: Giữ sẵn các View con của một dòng để khỏi findViewById lại nhiều lần
    static class DonHangViewHolder extends RecyclerView.ViewHolder {
        final TextView tvMaDon, tvTienCod, tvNguoiNhan, tvDiaChi, tvTrongLuong;

        DonHangViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMaDon = itemView.findViewById(R.id.tvMaDon);
            tvTienCod = itemView.findViewById(R.id.tvTienCod);
            tvNguoiNhan = itemView.findViewById(R.id.tvNguoiNhan);
            tvDiaChi = itemView.findViewById(R.id.tvDiaChi);
            tvTrongLuong = itemView.findViewById(R.id.tvTrongLuong);
        }
    }

    private final List<DonHang> danhSach;
    private final OnDonHangClickListener listener;

    public DonHangAdapter(List<DonHang> danhSach, OnDonHangClickListener listener) {
        this.danhSach = danhSach;
        this.listener = listener;
    }

    // Phương thức 1: Tạo một ô mới
    @NonNull
    @Override
    public DonHangViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_don_hang, parent, false);
        return new DonHangViewHolder(v);
    }

    // Phương thức 2: Đổ dữ liệu của đơn hàng vào ô
    @Override
    public void onBindViewHolder(@NonNull DonHangViewHolder holder, int position) {
        DonHang don = danhSach.get(position);

        holder.tvMaDon.setText("Mã: #" + don.getMaDon());
        holder.tvTienCod.setText("COD: " + DonHang.dinhDangTien(don.getTienCod()));
        holder.tvNguoiNhan.setText("Người nhận: " + don.getNguoiNhan() + " (" + don.getSoDienThoai() + ")");
        holder.tvDiaChi.setText("Địa chỉ: " + don.getDiaChi() + ", " + don.getPhuongXa());
        holder.tvTrongLuong.setText("Khối lượng: " + don.getTrongLuong() + " kg");

        // Dùng getBindingAdapterPosition() để bắt đúng vị trí khi bấm
        holder.itemView.setOnClickListener(v -> {
            int p = holder.getBindingAdapterPosition();
            if (p != RecyclerView.NO_POSITION && listener != null) {
                listener.onDonHangClick(danhSach.get(p));
            }
        });
    }

    // Phương thức 3: Đếm xem có tất cả bao nhiêu đơn hàng
    @Override
    public int getItemCount() {
        return danhSach != null ? danhSach.size() : 0;
    }
}