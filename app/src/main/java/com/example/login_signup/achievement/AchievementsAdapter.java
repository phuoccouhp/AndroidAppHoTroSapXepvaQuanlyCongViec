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

public class AchievementsAdapter extends RecyclerView.Adapter<AchievementsAdapter.ViewHolder> {
    private List<Achievement> list;

    public AchievementsAdapter(List<Achievement> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_achievement, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Achievement item = list.get(position);

        holder.imgIcon.setImageResource(item.iconResId);

        if (item.isUnlocked()) {

            holder.imgIcon.setColorFilter(item.color, PorterDuff.Mode.SRC_IN);
            holder.ivStatus.setImageResource(R.drawable.baseline_check_circle_24);
            holder.ivStatus.setColorFilter(Color.parseColor("#4CAF50"));
            holder.tvTitle.setTextColor(Color.BLACK);
        } else {

            holder.imgIcon.setColorFilter(Color.parseColor("#BDBDBD"), PorterDuff.Mode.SRC_IN);
            holder.ivStatus.setImageResource(R.drawable.baseline_lock_24);
            holder.ivStatus.setColorFilter(Color.parseColor("#BDBDBD"));
            holder.tvTitle.setTextColor(Color.GRAY);
        }

        holder.tvTitle.setText(item.title);
        holder.tvDesc.setText(item.description);

        holder.pb.setMax(item.maxProgress);
        holder.pb.setProgress(item.currentProgress);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgIcon;
        TextView tvTitle, tvDesc;
        ProgressBar pb;
        ImageView ivStatus;

        public ViewHolder(View v) {
            super(v);
            imgIcon = v.findViewById(R.id.imgIcon);
            tvTitle = v.findViewById(R.id.tvTitle);
            tvDesc = v.findViewById(R.id.tvDesc);
            pb = v.findViewById(R.id.pbProgress);
            ivStatus = v.findViewById(R.id.ivStatus);
        }
    }
}