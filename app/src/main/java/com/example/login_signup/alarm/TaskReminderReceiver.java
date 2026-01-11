package com.example.login_signup.alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.login_signup.home.HomeActivity;
import com.example.login_signup.NotificationHelper;
import com.example.login_signup.classes.FirebaseRepo;
import com.example.login_signup.classes.Task;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

// BroadcastReceiver nhận tín hiệu từ AlarmManager để xử lý nhắc nhở công việc khi đến giờ hẹn
public class TaskReminderReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // Kiểm tra nếu thiết bị vừa khởi động xong (BOOT_COMPLETED)
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            rescheduleAlarms(context); // Lên lịch lại tất cả báo thức vì chúng bị xóa khi tắt máy
        } else {
            handleAlarm(context, intent); // Xử lý báo thức khi đến giờ hẹn
        }
    }


    // Xử lý hành động khi báo thức được kích hoạt
    private void handleAlarm(Context context, Intent sourceIntent) {
        boolean isAdvance = sourceIntent.getBooleanExtra("isAdvance", false);

        // Nếu là thông báo nhắc trước: Hiển thị thông báo trên thanh trạng thái
        if (isAdvance) {
            String taskId = sourceIntent.getStringExtra("taskId");
            String title = sourceIntent.getStringExtra("title");
            String dueTimeString = sourceIntent.getStringExtra("due_time_string");
            String taskInfo = "Upcoming: " + title + "\nAt: " + dueTimeString;

            if (taskId != null) {
                // Hiển thị thông báo trên thanh trạng thái
                NotificationHelper.showAdvanceNotification(context, "Upcoming work", taskInfo, taskId.hashCode(), taskId);
            }
        }
        // Nếu là báo thức chính thức: Mở màn hình báo thức (AlarmActivity)
        else {
            // Tạo một đối tượng intent mới để mở màn hình báo thức
            Intent alarmIntent = new Intent(context, AlarmActivity.class);

            // Đặt flags để activity chạy độc lập và làm mới stack
            alarmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            alarmIntent.putExtras(sourceIntent.getExtras());

            // Bắt đầu activity báo thức
            context.startActivity(alarmIntent);
        }
    }

    // Tải lại các công việc từ Firebase và thiết lập lại báo thức sau khi khởi động máy
    private void rescheduleAlarms(Context context) {
        FirebaseRepo fbRepo = new FirebaseRepo();

        // Gọi phương thức lấy danh sách các công việc từ Firebase
        fbRepo.loadReminders(new FirebaseRepo.OnTasksLoadedListener() {
            @Override
            public void onTasksLoaded(List<Task> tasks) {
                for (Task t : tasks) {
                    // Chỉ lên lịch cho các công việc có ngày tháng trong tương lai
                    if (t.getTaskDate() != null && t.getTaskDate().getTime() > System.currentTimeMillis()) {
                        scheduleAlarmsForTask(context, t);
                    }
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e("TaskReminderReceiver", "Error rescheduling alarm", e);
            }
        });
    }

    // Tính toán và thiết lập các loại báo thức cho một công việc cụ thể
    private void scheduleAlarmsForTask(Context context, Task task) {
        long dueTime = task.getTaskDate().getTime(); // Lấy thời gian hẹn công việc

        // Lên lịch cho báo thức chính
        scheduleNotification(context, task, dueTime, false);

        // Lên lịch cho báo thức nhắc trước 24 giờ
        long twentyFourHoursInMillis = 24 * 60 * 60 * 1000;
        long advanceTime = dueTime - twentyFourHoursInMillis;

        // Chỉ lên lịch nếu thời gian nhắc trước vẫn còn ở tương lai
        if (advanceTime > System.currentTimeMillis()) {
            scheduleNotification(context, task, advanceTime, true);
        }
    }

    // Sử dụng AlarmManager để đăng ký báo thức với hệ thống Android
    private void scheduleNotification(Context context, Task task, long time, boolean isAdvance) {
        // Tạo ID duy nhất cho báo thức
        String taskId = isAdvance ? task.getId() + "_advance" : task.getId();

        // Tạo Intent để gửi đến TaskReminderReceiver khi báo thức được kích hoạt
        Intent intent = new Intent(context, TaskReminderReceiver.class);
        intent.putExtra("taskId", taskId);
        intent.putExtra("title", task.getTitle());
        intent.putExtra("note", task.getNote());
        intent.putExtra("category", task.getCategory());
        intent.putExtra("isAdvance", isAdvance);

        // Cấu hình nhạc chuông và rung
        if (task.getRingtone() != null) {
            intent.putExtra("ringtone", task.getRingtone());
        }
        if (task.getVibration() != null) {
            intent.putExtra("vibration", task.getVibration());
        }

        // Nếu là báo thức trước, định dạng ngày giờ để hiển thị trong nội dung notification
        if (isAdvance) {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm, dd/MM", Locale.getDefault());
            intent.putExtra("due_time_string", sdf.format(task.getTaskDate()));
        }

        int requestCode = taskId.hashCode(); // Tạo mã yêu cầu duy nhất để xác định báo thức
        // Tạo PendingIntent để gửi Intent đến TaskReminderReceiver
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Sử dụng AlarmManager để đăng ký báo thức
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            try {
                // Thiết lập để khi nhấn vào báo thức sẽ mở HomeActivity
                Intent showTaskIntent = new Intent(context, HomeActivity.class);
                PendingIntent showTaskPendingIntent = PendingIntent.getActivity(context, requestCode, showTaskIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                
                // Sử dụng setAlarmClock để đảm bảo báo thức hoạt động chính xác ngay cả khi máy ở chế độ ngủ
                AlarmManager.AlarmClockInfo alarmClockInfo = new AlarmManager.AlarmClockInfo(time, showTaskPendingIntent);
                alarmManager.setAlarmClock(alarmClockInfo, pendingIntent);
            } catch (SecurityException se) {
                Log.e("TaskReminderReceiver", "No permission to set alarms", se);
            }
        }
    }
}
