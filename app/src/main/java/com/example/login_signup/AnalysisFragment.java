package com.example.login_signup;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

// Import các thành phần biểu đồ
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart; // <--- Thêm mới
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData; // <--- Thêm mới
import com.github.mikephil.charting.data.PieDataSet; // <--- Thêm mới
import com.github.mikephil.charting.data.PieEntry; // <--- Thêm mới
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter; // <--- Thêm mới

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AnalysisFragment extends Fragment {

    private LineChart lineChart;
    private PieChart pieChart;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private List<Task> allTasks = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_analysis, container, false);

        lineChart = v.findViewById(R.id.lineChart);
        pieChart = v.findViewById(R.id.pieChart);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        loadDataAndDrawChart();

        return v;
    }

    private void loadDataAndDrawChart() {
        String uid = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (uid == null) {
            return;
        }

        db.collection("tasks")
                .whereEqualTo("uid", uid)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    allTasks.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Task t = new Task();
                        t.setCompleted(Boolean.TRUE.equals(doc.getBoolean("completed")));
                        t.setCategory(doc.getString("category")); // <--- Lấy thêm trường Category

                        Date date = doc.getDate("taskDate");
                        if (date != null) {
                            t.setTaskDate(date);
                            allTasks.add(t);
                        }
                    }
                    setupLineChart();
                    setupPieChart();
                })
                .addOnFailureListener(e -> {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Failed to load data", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setupPieChart() {
        if (getContext() == null || allTasks.isEmpty()) return;

        Map<String, Integer> categoryCount = new HashMap<>();

        categoryCount.put("Work", 0);
        categoryCount.put("Personal", 0);
        categoryCount.put("Health", 0);
        categoryCount.put("Shopping", 0);

        int totalTasks = 0;
        for (Task t : allTasks) {
            String cat = t.getCategory();
            if (cat == null) cat = "Other";

            categoryCount.put(cat, categoryCount.getOrDefault(cat, 0) + 1);
            totalTasks++;
        }

        if (totalTasks == 0) return;

        ArrayList<PieEntry> entries = new ArrayList<>();
        ArrayList<Integer> colors = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : categoryCount.entrySet()) {
            if (entry.getValue() > 0) {
                entries.add(new PieEntry(entry.getValue(), entry.getKey()));

                switch (entry.getKey()) {
                    case "Work":
                        colors.add(Color.parseColor("#FF9800")); // Cam
                        break;
                    case "Personal":
                        colors.add(Color.parseColor("#2196F3")); // Xanh dương
                        break;
                    case "Health":
                        colors.add(Color.parseColor("#E91E63")); // Hồng
                        break;
                    case "Shopping":
                        colors.add(Color.parseColor("#4CAF50")); // Xanh lá
                        break;
                    default:
                        colors.add(Color.parseColor("#9E9E9E")); // Xám cho loại khác
                        break;
                }
            }
        }

        PieDataSet dataSet = new PieDataSet(entries, "Categories");
        dataSet.setColors(colors);
        dataSet.setSliceSpace(2f);
        dataSet.setSelectionShift(5f);

        dataSet.setValueFormatter(new PercentFormatter(pieChart));
        dataSet.setValueTextSize(14f);
        dataSet.setValueTextColor(Color.WHITE);

        PieData data = new PieData(dataSet);
        pieChart.setData(data);

        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);

        pieChart.setDrawHoleEnabled(false);
        pieChart.getLegend().setEnabled(false);
        pieChart.setCenterText("");
        pieChart.setDrawEntryLabels(false);
        pieChart.invalidate();
    }

    private void setupLineChart() {
        if (getContext() == null) return;

        ArrayList<String> xLabels = new ArrayList<>();
        ArrayList<Entry> entriesFinished = new ArrayList<>();
        ArrayList<Entry> entriesUnfinished = new ArrayList<>();

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -6);

        SimpleDateFormat sdfCompare = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat sdfLabel = new SimpleDateFormat("dd/MM", Locale.getDefault());

        for (int i = 0; i < 7; i++) {
            Date currentDate = calendar.getTime();
            String keyDate = sdfCompare.format(currentDate);
            xLabels.add(sdfLabel.format(currentDate));

            int countFinished = 0;
            int countUnfinished = 0;

            for (Task t : allTasks) {
                if (t.getTaskDate() != null) {
                    String taskDateStr = sdfCompare.format(t.getTaskDate());
                    if (taskDateStr.equals(keyDate)) {
                        if (t.isCompleted()) countFinished++;
                        else countUnfinished++;
                    }
                }
            }
            entriesFinished.add(new Entry(i, countFinished));
            entriesUnfinished.add(new Entry(i, countUnfinished));
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        LineDataSet setFinished = new LineDataSet(entriesFinished, "Finished");
        setFinished.setColor(Color.parseColor("#2196F3"));
        setFinished.setCircleColor(Color.parseColor("#2196F3"));
        setFinished.setLineWidth(2f);
        setFinished.setCircleRadius(4f);
        setFinished.setDrawValues(false);
        setFinished.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        setFinished.setDrawFilled(true);
        setFinished.setFillColor(Color.parseColor("#2196F3"));
        setFinished.setFillAlpha(30);

        LineDataSet setUnfinished = new LineDataSet(entriesUnfinished, "Unfinished");
        setUnfinished.setColor(Color.parseColor("#FF9800"));
        setUnfinished.setCircleColor(Color.parseColor("#FF9800"));
        setUnfinished.setLineWidth(2f);
        setUnfinished.setCircleRadius(4f);
        setUnfinished.setDrawValues(false);
        setUnfinished.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        setUnfinished.setDrawFilled(true);
        setUnfinished.setFillColor(Color.parseColor("#FF9800"));
        setUnfinished.setFillAlpha(30);

        LineData data = new LineData(setFinished, setUnfinished);
        lineChart.setData(data);

        lineChart.getDescription().setEnabled(false);
        lineChart.getLegend().setEnabled(false);
        lineChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(xLabels));
        lineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        lineChart.getXAxis().setGranularity(1f);
        lineChart.getXAxis().setDrawGridLines(false);
        lineChart.getAxisLeft().setGranularity(1f);
        lineChart.getAxisLeft().setAxisMinimum(0f);
        lineChart.getAxisRight().setEnabled(false);
        lineChart.invalidate();
    }
}