package com.milkdiary.app.util;

import android.content.Context;
import android.os.Environment;

import com.milkdiary.app.db.DatabaseHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class BackupUtils {

    public static File backup(Context context) throws IOException {
        // Force a WAL checkpoint + close so the backup file is not corrupt
        DatabaseHelper helper = DatabaseHelper.getInstance(context);
        helper.getWritableDatabase().execSQL("PRAGMA wal_checkpoint(FULL)");

        File dbFile     = context.getDatabasePath(DatabaseHelper.DATABASE_NAME);
        File backupDir  = getBackupDir(context);
        File backupFile = new File(backupDir,
                "milk_diary_backup_" + DateUtils.todayDb() + ".db");
        copyFile(dbFile, backupFile);
        return backupFile;
    }

    /**
     * Replace the live DB with a backup file.
     * Resets the singleton so the next DB access re-opens the restored file cleanly.
     */
    public static void restore(Context context, File backupFile) throws IOException {
        // Close + reset singleton BEFORE overwriting the file
        DatabaseHelper.resetInstance();

        File dbFile = context.getDatabasePath(DatabaseHelper.DATABASE_NAME);
        if (!dbFile.getParentFile().exists()) dbFile.getParentFile().mkdirs();
        copyFile(backupFile, dbFile);

        // Also remove any leftover WAL / SHM files from the old DB
        new File(dbFile.getPath() + "-wal").delete();
        new File(dbFile.getPath() + "-shm").delete();
    }

    private static void copyFile(File src, File dst) throws IOException {
        if (dst.getParentFile() != null && !dst.getParentFile().exists()) {
            dst.getParentFile().mkdirs();
        }
        try (FileChannel in  = new FileInputStream(src).getChannel();
             FileChannel out = new FileOutputStream(dst).getChannel()) {
            out.transferFrom(in, 0, in.size());
        }
    }

    private static File getBackupDir(Context context) {
        File dir;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            dir = new File(context.getExternalFilesDir(null), "MilkDiary/backups");
        } else {
            //noinspection deprecation
            dir = new File(Environment.getExternalStorageDirectory(), "MilkDiary/backups");
        }
        if (!dir.exists()) dir.mkdirs();
        return dir;
    }

    public static File[] listBackups(Context context) {
        File dir = getBackupDir(context);
        if (!dir.exists()) return new File[0];
        File[] files = dir.listFiles((d, name) -> name.endsWith(".db"));
        return files != null ? files : new File[0];
    }
}
