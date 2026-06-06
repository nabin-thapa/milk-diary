package com.milkdiary.app.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.milkdiary.app.databinding.ItemSupplierBinding;
import com.milkdiary.app.model.Supplier;

import java.util.ArrayList;
import java.util.List;

public class SupplierAdapter extends RecyclerView.Adapter<SupplierAdapter.ViewHolder> {

    public interface SupplierListener {
        void onEditSupplier(Supplier supplier);
        void onDeleteSupplier(Supplier supplier);
    }

    private List<Supplier> suppliers = new ArrayList<>();
    private final SupplierListener listener;

    public SupplierAdapter(SupplierListener listener) {
        this.listener = listener;
    }

    public void setSuppliers(List<Supplier> suppliers) {
        this.suppliers = suppliers != null ? suppliers : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemSupplierBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int i) {
        Supplier s = suppliers.get(i);
        h.binding.tvName.setText(s.getName());
        h.binding.tvDetail.setText(formatDetail(s));
        h.binding.tvRate.setText(formatRate(s));
        h.binding.getRoot().setOnClickListener(v -> listener.onEditSupplier(s));
        h.binding.getRoot().setOnLongClickListener(v -> {
            listener.onDeleteSupplier(s);
            return true;
        });
        h.binding.ivActive.setVisibility(s.isActive() ? View.VISIBLE : View.GONE);
    }

    @Override
    public int getItemCount() { return suppliers.size(); }

    private String formatDetail(Supplier s) {
        StringBuilder sb = new StringBuilder();
        if (s.getVillage() != null && !s.getVillage().isEmpty())
            sb.append(s.getVillage());
        if (s.getPhone() != null && !s.getPhone().isEmpty()) {
            if (sb.length() > 0) sb.append("  •  ");
            sb.append(s.getPhone());
        }
        return sb.toString();
    }

    private String formatRate(Supplier s) {
        StringBuilder sb = new StringBuilder();
        if (s.getDefaultCowRate() > 0)
            sb.append("🐄 ₹").append(String.format("%.2f", s.getDefaultCowRate()));
        if (s.getDefaultBuffaloRate() > 0) {
            if (sb.length() > 0) sb.append("  ");
            sb.append("🐃 ₹").append(String.format("%.2f", s.getDefaultBuffaloRate()));
        }
        if (sb.length() == 0) sb.append("No rates set");
        return sb.toString();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemSupplierBinding binding;
        ViewHolder(ItemSupplierBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
