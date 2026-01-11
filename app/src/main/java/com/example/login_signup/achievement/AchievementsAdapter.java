package com.example.login_signup.achievement;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.login_signup.R;

import java.util.List;

// Adapter cho RecyclerView để quản lý danh sách các thành tựu
public class AchievementsAdapter extends RecyclerView.Adapter<AchievementsAdapter.ViewHolder> {
    private List<Achievement> list;

    public AchievementsAdapter(List<Achievement> list) {
        this.list = list;
    }

    // Tạo ViewHolder cho mỗi item trong RecyclerView
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_achievement, parent, false);
        return new ViewHolder(v);
    }

    // Gắn dữ liệu vào mỗi item trong RecyclerView
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Lấy dữ liệu của thành tựu tại vị trí hiện tại
        Achievement item = list.get(position);

        // Gắn icon cho thành tựu
        holder.imgIcon.setImageResource(item.iconResId);

        // Hiển thị tên và mô tả của thành tựu
        holder.tvTitle.setText(item.title);
        holder.tvDesc.setText(item.description);

        // Cập nhật tiến độ hoàn thành trên thanh ProgressBar
        holder.pbProgress.setMax(item.maxProgress);
        holder.pbProgress.setProgress(item.currentProgress);

        // Xây dựng giao diện cho mỗi thành tựu đã hoàn thành hay chưa hoàn thành
        if (item.isUnlocked()) {
            holder.imgIcon.setColorFilter(item.color, PorterDuff.Mode.SRC_IN);
            holder.imgStatus.setImageResource(R.drawable.baseline_check_circle_24);
            holder.imgStatus.setColorFilter(Color.parseColor("#4CAF50")); // Xanh lá
            holder.tvTitle.setTextColor(Color.BLACK);
        } else {
            holder.imgIcon.setColorFilter(Color.parseColor("#BDBDBD"), PorterDuff.Mode.SRC_IN);
            holder.imgStatus.setImageResource(R.drawable.baseline_lock_24);
            holder.imgStatus.setColorFilter(Color.parseColor("#BDBDBD")); // Xám
            holder.tvTitle.setTextColor(Color.GRAY);
        }
    }

    // Trả về số lượng item trong danh sách
    @Override
    public int getItemCount() {
        return list.size();
    }

    // ViewHolder giữ các tham chiếu đến các thành phần giao diện của mỗi item
    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgIcon, imgStatus;
        TextView tvTitle, tvDesc;
        ProgressBar pbProgress;

        public ViewHolder(View v) {
            // Kế thừa ViewHolder của lớp cha
            super(v);

            // Ánh xạ các thành phần trong giao diện
            tvTitle = v.findViewById(R.id.tvTitle);
            tvDesc = v.findViewById(R.id.tvDesc);
            imgIcon = v.findViewById(R.id.imgIcon);
            imgStatus = v.findViewById(R.id.imgStatus);
            pbProgress = v.findViewById(R.id.pbProgress);
        }
    }
}