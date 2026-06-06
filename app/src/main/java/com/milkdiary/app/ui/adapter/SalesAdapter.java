package com.milkdiary.app.ui.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.milkdiary.app.databinding.ItemSaleBinding;
import com.milkdiary.app.model.Sale;

import java.util.ArrayList;
import java.util.List;

public class SalesAdapter extends RecyclerView.Adapter<SalesAdapter.ViewHolder> {

    public interface SaleListener {
        void onEditSale(Sale sale);
        void onDeleteSale(Sale sale);
    }

    private List<Sale> sales = new ArrayList<>();
    private final SaleListener listener;

    public SalesAdapter(SaleListener listener) {
        this.listener = listener;
    }

    public void setSales(List<Sale> sales) {
        this.sales = sales != null ? sales : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemSaleBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int i) {
        Sale s = sales.get(i);
        String milkEmoji = s.getMilkType().equals(Sale.MILK_COW) ? "\uD83D\uDC04" : "\uD83D\uDC03";
        h.binding.tvHeader.setText(milkEmoji + "  " + s.getCustomerName() + "  (" + s.getCustomerType() + ")");
        h.binding.tvDetail.setText(
                s.getDate() + "  " + s.getSessionType()
                + "  \u2022  " + String.format("%.2f", s.getQuantity()) + " L"
                + "  @  \u20B9" + String.format("%.2f", s.getRate()));
        h.binding.tvAmount.setText("\u20B9" + String.format("%.2f", s.getAmount()));
        h.binding.tvPaidStatus.setText(s.isPaid() ? "Paid" : "Credit");
        h.binding.tvPaidStatus.setTextColor(
                h.itemView.getContext().getColor(
                        s.isPaid() ? android.R.color.holo_green_dark : android.R.color.holo_red_dark));
        h.binding.getRoot().setOnClickListener(v -> listener.onEditSale(s));
        h.binding.getRoot().setOnLongClickListener(v -> {
            listener.onDeleteSale(s);
            return true;
        });
    }

    @Override
    public int getItemCount() { return sales.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemSaleBinding binding;
        ViewHolder(ItemSaleBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
