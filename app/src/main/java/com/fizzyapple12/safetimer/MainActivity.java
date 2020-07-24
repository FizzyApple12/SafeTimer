package com.fizzyapple12.safetimer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    public TimeTable timeTable = null;

    public Context context;

    public Timer renderTimer = new Timer();

    public TextView currentTime;
    public ListView laps;

    public ArrayAdapter lapsAdapter;

    public ArrayList<String> lapsList = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        currentTime = findViewById(R.id.currentTime);
        laps = findViewById(R.id.laps);

        lapsAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, lapsList);
        laps.setAdapter(lapsAdapter);

        context = this;

        timeTable = new TimeTable(context, false);
        if (!timeTable.load(".TIMER~RECOV")) timeTable = null;

        final Button startButton = findViewById(R.id.start);
        startButton.setOnClickListener(v -> {
            if (timeTable == null) timeTable = new TimeTable(context, true);
            else timeTable.resume();
        });

        final Button stopButton = findViewById(R.id.stop);
        stopButton.setOnClickListener(v -> {
            if (timeTable != null && timeTable.end == null) timeTable.stop();
        });

        final Button lapButton = findViewById(R.id.lap);
        lapButton.setOnClickListener(v -> {
            if (timeTable != null && timeTable.end == null) timeTable.lap();
            System.out.println(timeTable.laps);
        });

        final Button resetButton = findViewById(R.id.reset);
        resetButton.setOnClickListener(v -> {
            context.deleteFile(".TIMER~RECOV");
            timeTable = null;
        });

        final Button saveButton = findViewById(R.id.save);
        saveButton.setOnClickListener(v -> {
            if (timeTable != null) timeTable.saveExternal();
        });

        renderTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(() -> {
                    if (timeTable == null) {
                        currentTime.setText("00:00:00.00");
                        lapsAdapter.clear();
                        return;
                    }

                    if(timeTable.laps.size() > 0) {
                        lapsAdapter.clear();
                        lapsAdapter.add("Lap 1: " + durationToString(timeTable.start, timeTable.laps.get(0)));
                        if (timeTable.laps.size() > 1) {
                            for (int i = 1; i < timeTable.laps.size(); i++) {
                                lapsAdapter.add("Lap " + (i + 1) + ": " + durationToString(timeTable.laps.get(i - 1), timeTable.laps.get(i)));
                            }
                        }
                    }

                    if (timeTable.end != null) {
                        currentTime.setText(durationToString(timeTable.start, timeTable.end));
                        return;
                    }
                    currentTime.setText(durationToString(timeTable.start, new Date()));
                });
            }
        }, 10, 10);
    }

    public String durationToString(Date begin, Date end) {
        Duration duration = Duration.between(begin.toInstant(), end.toInstant());
        return String.format("%02d:%02d:%02d.%03d", duration.toHours(), valueToMax(duration.toMinutes(), 59), valueToMax(duration.getSeconds(), 59), valueToMax(duration.toMillis(), 999));
    }

    public long valueToMax(long value, long max) {
        return value - ((int) Math.floor(value / (max + 1)) * (max + 1));
    }

    private class ArrayListAdapter extends ArrayAdapter<String> {
        HashMap<String, Integer> idMap = new HashMap<String, Integer>();

        public ArrayListAdapter(Context context, int textViewID, List<String> objects) {
            super(context, textViewID, objects);
            for (int i = 0; i < objects.size(); i++) {
                idMap.put(objects.get(i), i);
            }
        }

        @Override
        public long getItemId(int pos) {
            return idMap.get(getItem(pos));
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }
    }
}