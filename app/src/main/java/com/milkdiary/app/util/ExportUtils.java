package com.milkdiary.app.util;

import android.content.Context;
import android.os.Environment;

import com.milkdiary.app.model.MilkRecord;
import com.milkdiary.app.model.Payment;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class ExportUtils {

    public static File exportRecordsCsv(Context context, List<MilkRecord> records) throws IOException {
        File dir = getExportDir(context);
        File file = new File(dir, "milk_records_" + DateUtils.todayDb() + ".csv");
        FileWriter writer = new FileWriter(file);
        writer.write("Date,Cow Liters,Cow Rate,Cow Amount,Buffalo Liters,Buffalo Rate,Buffalo Amount,Total,Note\n");
        for (MilkRecord r : records) {
            writer.write(String.format("%s,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%s\n",
                    r.getDate(), r.getCowLiters(), r.getCowRate(), r.getCowAmount(),
                    r.getBuffaloLiters(), r.getBuffaloRate(), r.getBuffaloAmount(),
                    r.getTotal(), r.getNote() != null ? r.getNote().replace(",", ";") : ""));
        }
        writer.flush();
        writer.close();
        return file;
    }

    public static File exportPaymentsCsv(Context context, List<Payment> payments) throws IOException {
        File dir = getExportDir(context);
        File file = new File(dir, "payments_" + DateUtils.todayDb() + ".csv");
        FileWriter writer = new FileWriter(file);
        writer.write("Date,Amount,Note\n");
        for (Payment p : payments) {
            writer.write(String.format("%s,%.2f,%s\n",
                    p.getDate(), p.getAmount(),
                    p.getNote() != null ? p.getNote().replace(",", ";") : ""));
        }
        writer.flush();
        writer.close();
        return file;
    }

    private static File getExportDir(Context context) {
        File dir;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            dir = new File(context.getExternalFilesDir(null), "MilkDiary/exports");
        } else {
            dir = new File(Environment.getExternalStorageDirectory(), "MilkDiary/exports");
        }
        if (!dir.exists()) dir.mkdirs();
        return dir;
    }
}
