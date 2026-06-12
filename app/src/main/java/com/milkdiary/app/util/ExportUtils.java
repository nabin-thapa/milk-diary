package com.milkdiary.app.util;

import android.content.Context;
import android.os.Environment;

import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.milkdiary.app.model.MilkRecord;
import com.milkdiary.app.model.Payment;

import java.io.File;
import java.io.FileOutputStream;
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

    public static File exportRecordsPdf(Context context, List<MilkRecord> records, String title) throws Exception {
        File dir = getExportDir(context);
        File file = new File(dir, "Milk_Report_" + DateUtils.todayDb() + ".pdf");
        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(file));
        document.open();

        Font headFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
        Paragraph p = new Paragraph(title, headFont);
        p.setAlignment(Element.ALIGN_CENTER);
        p.setSpacingAfter(20);
        document.add(p);

        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.addCell("Date");
        table.addCell("Cow (L)");
        table.addCell("Buf (L)");
        table.addCell("Rate");
        table.addCell("Total");

        double totalAmt = 0;
        for (MilkRecord r : records) {
            table.addCell(r.getDate());
            table.addCell(String.valueOf(r.getCowLiters()));
            table.addCell(String.valueOf(r.getBuffaloLiters()));
            table.addCell(String.valueOf(r.getCowRate() > 0 ? r.getCowRate() : r.getBuffaloRate()));
            table.addCell(FormatUtils.money(r.getTotal()));
            totalAmt += r.getTotal();
        }
        document.add(table);

        Paragraph footer = new Paragraph("\nGrand Total: " + FormatUtils.money(totalAmt), 
                new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD));
        footer.setAlignment(Element.ALIGN_RIGHT);
        document.add(footer);

        document.close();
        return file;
    }

    public static File exportPaymentsPdf(Context context, List<Payment> payments, String title) throws Exception {
        File dir = getExportDir(context);
        File file = new File(dir, "Payment_Report_" + DateUtils.todayDb() + ".pdf");
        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(file));
        document.open();

        Font headFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
        Paragraph p = new Paragraph(title, headFont);
        p.setAlignment(Element.ALIGN_CENTER);
        p.setSpacingAfter(20);
        document.add(p);

        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(100);
        table.addCell("Date");
        table.addCell("Note");
        table.addCell("Amount");

        double totalAmt = 0;
        for (Payment pay : payments) {
            table.addCell(pay.getDate());
            table.addCell(pay.getNote() != null ? pay.getNote() : "-");
            table.addCell(FormatUtils.money(pay.getAmount()));
            totalAmt += pay.getAmount();
        }
        document.add(table);

        Paragraph footer = new Paragraph("\nTotal Paid: " + FormatUtils.money(totalAmt), 
                new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD));
        footer.setAlignment(Element.ALIGN_RIGHT);
        document.add(footer);

        document.close();
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
