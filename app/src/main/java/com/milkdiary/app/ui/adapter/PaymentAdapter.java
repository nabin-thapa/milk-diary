package com.milkdiary.app.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.milkdiary.app.R;
import com.milkdiary.app.model.Payment;
import com.milkdiary.app.util.DateUtils;
import com.milkdiary.app.util.FormatUtils;

import java.util.ArrayList;
import java.util.List;

public class PaymentAdapter extends RecyclerView.Adapter<PaymentAdapter.ViewHolder> {

    public interface PaymentListener {
        void onDeletePayment(Payment payment);
    }

    private List<Payment> payments = new ArrayList<>();
    private final PaymentListener listener;

    public PaymentAdapter(PaymentListener listener) {
        this.listener = listener;
    }

    public void setPayments(List<Payment> payments) {
        this.payments = payments;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_payment, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Payment payment = payments.get(position);
        holder.tvDate.setText(DateUtils.dbToDisplay(payment.getDate()));
        holder.tvAmount.setText(FormatUtils.money(payment.getAmount()));

        if (payment.getNote() != null && !payment.getNote().isEmpty()) {
            holder.tvNote.setVisibility(View.VISIBLE);
            holder.tvNote.setText(payment.getNote());
        } else {
            holder.tvNote.setVisibility(View.GONE);
        }

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDeletePayment(payment);
        });
    }

    @Override
    public int getItemCount() {
        return payments.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvAmount, tvNote;
        ImageButton btnDelete;

        ViewHolder(View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvPayDate);
            tvAmount = itemView.findViewById(R.id.tvPayAmount);
            tvNote = itemView.findViewById(R.id.tvPayNote);
            btnDelete = itemView.findViewById(R.id.btnDeletePayment);
        }
    }
}
