package com.milkdiary.app.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.milkdiary.app.databinding.ItemCustomerBinding;
import com.milkdiary.app.model.Customer;

import java.util.ArrayList;
import java.util.List;

public class CustomerAdapter extends RecyclerView.Adapter<CustomerAdapter.ViewHolder> {

    public interface CustomerListener {
        void onEditCustomer(Customer customer);
        void onDeleteCustomer(Customer customer);
    }

    private List<Customer> customers = new ArrayList<>();
    private final CustomerListener listener;

    public CustomerAdapter(CustomerListener listener) {
        this.listener = listener;
    }

    public void setCustomers(List<Customer> customers) {
        this.customers = customers != null ? customers : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemCustomerBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int i) {
        Customer c = customers.get(i);
        h.binding.tvName.setText(c.getName());
        h.binding.tvDetail.setText(formatDetail(c));
        h.binding.tvType.setText(formatType(c.getCustomerType()));
        h.binding.getRoot().setOnClickListener(v -> listener.onEditCustomer(c));
        h.binding.getRoot().setOnLongClickListener(v -> {
            listener.onDeleteCustomer(c);
            return true;
        });
    }

    @Override
    public int getItemCount() { return customers.size(); }

    private String formatDetail(Customer c) {
        StringBuilder sb = new StringBuilder();
        if (c.getPhone() != null && !c.getPhone().isEmpty())
            sb.append(c.getPhone());
        if (c.getAddress() != null && !c.getAddress().isEmpty()) {
            if (sb.length() > 0) sb.append("  •  ");
            sb.append(c.getAddress());
        }
        return sb.toString();
    }

    private String formatType(String type) {
        if (type == null) return "Other";
        switch (type) {
            case Customer.TYPE_HOTEL: return "Hotel";
            case Customer.TYPE_TEA_SHOP: return "Tea Shop";
            case Customer.TYPE_HOME: return "Home";
            default: return "Other";
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemCustomerBinding binding;
        ViewHolder(ItemCustomerBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
