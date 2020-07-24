package com.fizzyapple12.safetimer;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class TimeTable {
    public Context context;

    public Date start = null;

    public List<Date> laps = new LinkedList<Date>();

    public Date end = null;

    public TimeTable(Context context, boolean overwrite) {
        this.context = context;
        start = new Date();
        if (overwrite) save(".TIMER~RECOV");
    }

    public void lap() {
        laps.add(new Date());
        save(".TIMER~RECOV");
    }

    public void stop() {
        end = new Date();
        save(".TIMER~RECOV");
    }

    public void resume() {
        end = null;
        save(".TIMER~RECOV");
    }

    public void save(String fileName) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput(fileName, Context.MODE_PRIVATE));
            outputStreamWriter.write(toString());
            outputStreamWriter.close();
        } catch (IOException e) {
            Log.e("Exception", "Failed to save TimeTable: " + e.toString());
        }
    }

    public void saveExternal() {
        String fileName = "safetimer_" + start.getTime() + "_timetable.txt";
        File dir = new File("/storage/emulated/0/", "SafeTimer");

        if (!dir.exists()) dir.mkdirs();

        File file = new File("/storage/emulated/0/SafeTimer/", fileName);

        try (FileWriter fileWriter = new FileWriter(file)) {
            fileWriter.write(toString());
            fileWriter.flush();
            fileWriter.close();
            Toast.makeText(context, "Saved Time Table as \"" + fileName + "\"", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Log.e("Exception", "Failed to save TimeTable: " + e.toString());
            Toast.makeText(context, "Could not save Time Table", Toast.LENGTH_SHORT).show();
        }
    }

    public boolean load(String fileName) {
        try {
            InputStream inputStream = context.openFileInput(fileName);

            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ((receiveString = bufferedReader.readLine()) != null) stringBuilder.append("\n").append(receiveString);

                inputStream.close();

                this.fromString(stringBuilder.toString());
            }
        } catch (FileNotFoundException e) {
            Log.e("Exception", "Failed to find TimeTable: " + e.toString());
            return false;
        } catch (IOException e) {
            Log.e("Exception", "Failed to load TimeTable: " + e.toString());
            return false;
        }
        return true;
    }

    public String toString() {
        final String[] timeTable = {""};

        if (start != null) timeTable[0] += "s:" + start.getTime() + System.getProperty("line.separator");
        laps.forEach((Date lap) -> {
            timeTable[0] += "l:" + lap.getTime() + System.getProperty("line.separator");
        });
        if (end != null) timeTable[0] += "e:" + end.getTime() + System.getProperty("line.separator");

        return timeTable[0];
    }

    public void fromString(String timeTable) {
        String[] elements = timeTable.split(System.getProperty("line.separator"));

        for (String element : elements) {
            String[] parts = element.split(":");

            if (parts.length != 2) continue;

            Date parsed = new Date();
            parsed.setTime(Long.parseLong(parts[1]));

            switch (parts[0]) {
                case "s":
                    start = parsed;
                    break;
                case "l":
                    laps.add(parsed);
                    break;
                case "e":
                    end = parsed;
                    break;
            }
        }
    }
}
