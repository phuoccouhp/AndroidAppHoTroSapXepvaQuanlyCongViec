package com.example.login_signup.history;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.login_signup.R;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

// Adapater hiển thị danh sách logs công việc
public class TaskHistoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0; // Hiển thị ngày tháng (Header)
    private static final int TYPE_ITEM = 1; // Hiển thị chi tiết log công việc (Item)

    private List<Object> items; // Danh sách dữ liệu hiển thị

    public TaskHistoryAdapter(List<Object> items) {
        this.items = items;
    }

    // Xác định loại giao diện tại vị trí bất kỳ dựa trên kiểu dữ liệu của phần tử
    @Override
    public int getItemViewType(int position) {
        if (items.get(position) instanceof String) {
            return TYPE_HEADER; // Nếu là String thì trả về kiểu Header
        }
        return TYPE_ITEM; // Nếu là TaskLog thì trả về kiểu Item
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            // Nạp giao diện cho tiêu đề ngày tháng
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_header_date, parent, false);
            return new HeaderViewHolder(view);
        } else {
            // Nạp giao diện cho chi tiết log công việc
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task_log, parent, false);
            return new LogViewHolder(view);
        }
    }

    // Gán dữ liệu vào các View dựa trên loại ViewHolder tương ứng
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderViewHolder) {
            // Xử lý hiển thị phần Header (Ngày tháng)
            String headerTitle = (String) items.get(position);
            ((HeaderViewHolder) holder).tvHeader.setText(headerTitle);
        } else {
            // Xử lý hiển thị phần Item (Logs)
            TaskLog log = (TaskLog) items.get(position);
            LogViewHolder logHolder = (LogViewHolder) holder;

            String actionText;
            int actionColor = Color.parseColor("#2196F3");

            // Phân loại hành động dựa trên loại logs
            if ("CREATED".equals(log.getAction())) {
                actionText = "You added a task";
                actionColor = Color.parseColor("#2196F3"); // Màu xanh dương - Thêm mới
            } else if ("COMPLETED".equals(log.getAction())) {
                actionText = "You completed a task";
                actionColor = Color.parseColor("#4CAF50"); // Màu xanh lá - Hoàn thành
            } else if ("DELETED".equals(log.getAction())) {
                actionText = "You deleted a task";
                actionColor = Color.parseColor("#F44336"); // Màu đỏ - Xóa
            } else {
                actionText = "Updated task";
            }

            // Hiển thị nội dung hành động và tên công việc
            logHolder.tvContent.setText(actionText + ": " + log.getTaskTitle());
            logHolder.tvContent.setTextColor(actionColor);

            // Hiển thị thời gian thực hiện
            SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.getDefault());
            logHolder.tvTime.setText(sdf.format(log.getTimestamp()));
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    // ViewHolder tham chiếu đến các thành phần giao diện Header
    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView tvHeader;
        HeaderViewHolder(View itemView) {
            super(itemView);
            tvHeader = (TextView) itemView;
        }
    }

    // ViewHolder tham chiếu đến các thành phần giao diện Chi tiết Logs
    static class LogViewHolder extends RecyclerView.ViewHolder {
        TextView tvContent, tvTime;
        LogViewHolder(View itemView) {
            super(itemView);
            tvContent = itemView.findViewById(R.id.tvContent);
            tvTime = itemView.findViewById(R.id.tvTime);
        }
    }
}
