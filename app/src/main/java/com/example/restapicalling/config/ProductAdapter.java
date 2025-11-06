package com.example.restapicalling.config;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.restapicalling.R;
import com.example.restapicalling.entity.Product;

import java.util.ArrayList;
import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private List<Product> products = new ArrayList<>();
    private OnItemClickListener itemClickListener;
    private OnItemLongClickListener itemLongClickListener;

    public interface OnItemClickListener {
        void onItemClick(Product product);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(Product product);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.itemClickListener = listener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.itemLongClickListener = listener;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = products.get(position);
        holder.bind(product);

        holder.itemView.setOnClickListener(v -> {
            if (itemClickListener != null) {
                itemClickListener.onItemClick(product);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (itemLongClickListener != null) {
                itemLongClickListener.onItemLongClick(product);
                return true;
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        private TextView textProductName, textProductDescription, textProductPrice,
                textProductQuantity, textProductCategory, textManufacturingDate, textProductSku;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            textProductName = itemView.findViewById(R.id.textProductName);
            textProductDescription = itemView.findViewById(R.id.textProductDescription);
            textProductPrice = itemView.findViewById(R.id.textProductPrice);
            textProductQuantity = itemView.findViewById(R.id.textProductQuantity);
            textProductCategory = itemView.findViewById(R.id.textProductCategory);
            textManufacturingDate = itemView.findViewById(R.id.textManufacturingDate);
            textProductSku = itemView.findViewById(R.id.textProductSku);
        }

        public void bind(Product product) {
            textProductName.setText(product.getName());
            textProductDescription.setText(product.getDescription());
            textProductPrice.setText(String.format("$%.2f", product.getPrice()));

            // Set quantity with stock status
            String quantityText = product.getQuantity() > 0 ?
                    product.getQuantity() + " in stock" : "Out of stock";
            textProductQuantity.setText(quantityText);
            textProductQuantity.setTextColor(itemView.getContext().getResources().getColor(
                    product.getQuantity() > 0 ? R.color.green_500 : R.color.red_500));

            textProductCategory.setText(product.getCategory());

            if (product.getManufacturingDate() != null) {
                textManufacturingDate.setText(product.getManufacturingDate());
            }

            if (product.getSku() != null) {
                textProductSku.setText(product.getSku());
            }
        }
    }
}