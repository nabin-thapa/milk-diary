package com.milkdiary.app.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.milkdiary.app.R;
import com.milkdiary.app.model.MilkRecord;
import com.milkdiary.app.util.DateUtils;
import com.milkdiary.app.util.FormatUtils;

import java.util.ArrayList;
import java.util.List;

public class RecordAdapter extends RecyclerView.Adapter<RecordAdapter.ViewHolder> {

    public interface RecordListener {
        void onEditRecord(MilkRecord record);
        void onDeleteRecord(MilkRecord record);
    }

    private List<MilkRecord> records = new ArrayList<>();
    private final RecordListener listener;

    public RecordAdapter(RecordListener listener) {
        this.listener = listener;
    }

    public void setRecords(List<MilkRecord> records) {
        this.records = records != null ? records : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_record, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MilkRecord record = records.get(position);

        // Session type
        if (record.isMorning()) {
            holder.tvSession.setText("🌅");
        } else {
            holder.tvSession.setText("🌆");
        }

        holder.tvDate.setText(DateUtils.dbToDisplay(record.getDate()));

        // Cow info
        if (record.getCowLiters() > 0) {
            holder.tvCow.setVisibility(View.VISIBLE);
            holder.tvCow.setText("🐄 "
                    + FormatUtils.liters(record.getCowLiters())
                    + " × " + FormatUtils.rate(record.getCowRate())
                    + " = " + FormatUtils.money(record.getCowAmount()));
        } else {
            holder.tvCow.setVisibility(View.GONE);
        }

        // Buffalo info
        if (record.getBuffaloLiters() > 0) {
            holder.tvBuf.setVisibility(View.VISIBLE);
            holder.tvBuf.setText("🐃 "
                    + FormatUtils.liters(record.getBuffaloLiters())
                    + " × " + FormatUtils.rate(record.getBuffaloRate())
                    + " = " + FormatUtils.money(record.getBuffaloAmount()));
        } else {
            holder.tvBuf.setVisibility(View.GONE);
        }

        holder.tvTotal.setText(FormatUtils.money(record.getTotal()));

        // Note
        String note = record.getNote();
        if (note != null && !note.trim().isEmpty()) {
            holder.tvNote.setVisibility(View.VISIBLE);
            holder.tvNote.setText(note.trim());
        } else {
            holder.tvNote.setVisibility(View.GONE);
        }

        // Edit button: disable if edit limit reached
        if (!record.canEdit()) {
            holder.btnEdit.setAlpha(0.3f);
            holder.btnEdit.setEnabled(false);
        } else {
            holder.btnEdit.setAlpha(1f);
            holder.btnEdit.setEnabled(true);
        }

        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null && record.canEdit()) {
                listener.onEditRecord(record);
            } else if (listener != null) {
                // This shouldn't reach here due to disabled button, but just in case
                android.widget.Toast.makeText(v.getContext(),
                        "This record can only be edited once.",
                        android.widget.Toast.LENGTH_SHORT).show();
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDeleteRecord(record);
        });
    }

    @Override
    public int getItemCount() {
        return records.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvSession, tvDate, tvCow, tvBuf, tvTotal, tvNote;
        ImageButton btnEdit, btnDelete;

        ViewHolder(View itemView) {
            super(itemView);
            tvSession = itemView.findViewById(R.id.tvSessionType);
            tvDate    = itemView.findViewById(R.id.tvRecordDate);
            tvCow     = itemView.findViewById(R.id.tvCowInfo);
            tvBuf     = itemView.findViewById(R.id.tvBufInfo);
            tvTotal   = itemView.findViewById(R.id.tvRecordTotal);
            tvNote    = itemView.findViewById(R.id.tvRecordNote);
            btnEdit   = itemView.findViewById(R.id.btnEditRecord);
            btnDelete = itemView.findViewById(R.id.btnDeleteRecord);
        }
    }
}
