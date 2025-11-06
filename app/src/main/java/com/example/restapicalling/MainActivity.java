package com.example.restapicalling;

import static android.content.ContentValues.TAG;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.restapicalling.config.ApiClient;
import com.example.restapicalling.config.ProductAdapter;
import com.example.restapicalling.config.ProductApiService;
import com.example.restapicalling.entity.Product;
import com.example.restapicalling.entity.ProductViewModel;
import com.example.restapicalling.repo.ProductRepository;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private ProductViewModel viewModel;
    private TextView textStatus, textProductCount;
    private ProgressBar progressBar;
    private Button btnLoadProducts, btnAddProduct, btnClearAll;
    private RecyclerView recyclerViewProducts;
    private View emptyState;
    private ProductAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupViewModel();
        setupRecyclerView();
        setupClickListeners();
        observeData();
    }

    private void initViews() {
        textStatus = findViewById(R.id.textStatus);
        textProductCount = findViewById(R.id.textProductCount);
        progressBar = findViewById(R.id.progressBar);
        btnLoadProducts = findViewById(R.id.btnLoadProducts);
        btnAddProduct = findViewById(R.id.btnAddProduct);
        btnClearAll = findViewById(R.id.btnClearAll);
        recyclerViewProducts = findViewById(R.id.recyclerViewProducts);
        emptyState = findViewById(R.id.emptyState);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(ProductViewModel.class);
    }

    private void setupRecyclerView() {
        adapter = new ProductAdapter();
        adapter.setOnItemClickListener(this::showProductDetails);
        adapter.setOnItemLongClickListener(this::showQuickActions);

        recyclerViewProducts.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewProducts.setAdapter(adapter);
    }

    private void setupClickListeners() {
        btnLoadProducts.setOnClickListener(v -> loadProducts());
        btnAddProduct.setOnClickListener(v -> showAddProductDialog());
        btnClearAll.setOnClickListener(v -> showClearAllConfirmation());
    }

    private void observeData() {
        viewModel.getProducts().observe(this, products -> {
            if (products != null) {
                adapter.setProducts(products);
                updateUI(products);
            }
        });

        viewModel.getError().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                if (error.startsWith("Product created:")) {
                    showSuccessMessage(error);
                    loadProducts(); // Refresh list
                } else {
                    showErrorMessage(error);
                }
            }
        });

        viewModel.getIsLoading().observe(this, isLoading -> {
            if (isLoading != null) {
                progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
                btnLoadProducts.setEnabled(!isLoading);
                btnAddProduct.setEnabled(!isLoading);
                btnClearAll.setEnabled(!isLoading);

                if (isLoading) {
                    textStatus.setText("Processing...");
                }
            }
        });
    }

    private void updateUI(List<Product> products) {
        int productCount = products.size();
        textProductCount.setText("Total Products: " + productCount);

        // Show/hide empty state
        if (productCount == 0) {
            recyclerViewProducts.setVisibility(View.GONE);
            emptyState.setVisibility(View.VISIBLE);
            btnClearAll.setVisibility(View.GONE);
        } else {
            recyclerViewProducts.setVisibility(View.VISIBLE);
            emptyState.setVisibility(View.GONE);
            btnClearAll.setVisibility(View.VISIBLE);
        }

        textStatus.setText("Ready to manage products");
    }

    private void loadProducts() {
        textStatus.setText("Loading products...");
        viewModel.loadAllProducts();
    }

    private void showAddProductDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add New Product");

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_product, null);
        builder.setView(dialogView);

        // Get input fields
        TextInputEditText editName = dialogView.findViewById(R.id.editProductName);
        TextInputEditText editDescription = dialogView.findViewById(R.id.editProductDescription);
        TextInputEditText editPrice = dialogView.findViewById(R.id.editProductPrice);
        TextInputEditText editQuantity = dialogView.findViewById(R.id.editProductQuantity);
        TextInputEditText editCategory = dialogView.findViewById(R.id.editProductCategory);
        TextInputEditText editManufacturingDate = dialogView.findViewById(R.id.editManufacturingDate);
        TextInputEditText editSku = dialogView.findViewById(R.id.editProductSku);

        // Set default values
        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        editManufacturingDate.setText(currentDate);
        editSku.setText("SKU-" + System.currentTimeMillis());

        builder.setPositiveButton("Save", (dialog, which) -> {
            if (validateInput(editName, editDescription, editPrice, editQuantity, editCategory, editManufacturingDate)) {
                createProductFromInput(editName, editDescription, editPrice, editQuantity,
                        editCategory, editManufacturingDate, editSku);
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private boolean validateInput(TextInputEditText... fields) {
        boolean isValid = true;

        if (fields[0].getText().toString().trim().isEmpty()) {
            fields[0].setError("Product name is required");
            isValid = false;
        }

        if (fields[1].getText().toString().trim().isEmpty()) {
            fields[1].setError("Description is required");
            isValid = false;
        }

        if (fields[2].getText().toString().trim().isEmpty()) {
            fields[2].setError("Price is required");
            isValid = false;
        } else {
            try {
                Double.parseDouble(fields[2].getText().toString());
            } catch (NumberFormatException e) {
                fields[2].setError("Invalid price format");
                isValid = false;
            }
        }

        if (fields[3].getText().toString().trim().isEmpty()) {
            fields[3].setError("Quantity is required");
            isValid = false;
        } else {
            try {
                Integer.parseInt(fields[3].getText().toString());
            } catch (NumberFormatException e) {
                fields[3].setError("Invalid quantity format");
                isValid = false;
            }
        }

        if (fields[4].getText().toString().trim().isEmpty()) {
            fields[4].setError("Category is required");
            isValid = false;
        }

        if (fields[5].getText().toString().trim().isEmpty()) {
            fields[5].setError("Manufacturing date is required");
            isValid = false;
        }

        return isValid;
    }

    private void createProductFromInput(TextInputEditText name, TextInputEditText description, TextInputEditText price,
                                        TextInputEditText quantity, TextInputEditText category, TextInputEditText manufacturingDate,
                                        TextInputEditText sku) {
        try {
            Product product = new Product(
                    name.getText().toString().trim(),
                    description.getText().toString().trim(),
                    Double.parseDouble(price.getText().toString()),
                    Integer.parseInt(quantity.getText().toString()),
                    category.getText().toString().trim(),
                    manufacturingDate.getText().toString().trim(),
                    sku.getText().toString().trim()
            );

            viewModel.createProduct(product);
            Toast.makeText(this, "Creating product...", Toast.LENGTH_SHORT).show();

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Error: Invalid number format", Toast.LENGTH_LONG).show();
        }
    }

    private void showProductDetails(Product product) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Product Details");

        StringBuilder details = new StringBuilder();
        details.append("ID: ").append(product.getId()).append("\n\n");
        details.append("Name: ").append(product.getName()).append("\n\n");
        details.append("Description: ").append(product.getDescription()).append("\n\n");
        details.append("Price: $").append(product.getPrice()).append("\n\n");
        details.append("Quantity: ").append(product.getQuantity()).append("\n\n");
        details.append("Category: ").append(product.getCategory()).append("\n\n");
        details.append("Manufacturing Date: ").append(product.getManufacturingDate()).append("\n\n");
        details.append("SKU: ").append(product.getSku()).append("\n");

        builder.setMessage(details.toString());
        builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
        builder.setNegativeButton("Delete", (dialog, which) -> showDeleteConfirmation(product));

        builder.show();
    }

    private void showQuickActions(Product product) {
        String[] actions = {"Edit", "Delete", "Details"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Quick Actions");
        builder.setItems(actions, (dialog, which) -> {
            switch (which) {
                case 0:
                    // Edit functionality can be added here
                    Toast.makeText(this, "Edit feature coming soon", Toast.LENGTH_SHORT).show();
                    break;
                case 1:
                    showDeleteConfirmation(product);
                    break;
                case 2:
                    showProductDetails(product);
                    break;
            }
        });
        builder.show();
    }

    private void showDeleteConfirmation(Product product) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Product")
                .setMessage("Are you sure you want to delete \"" + product.getName() + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> deleteProduct(product))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showClearAllConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Clear All Products")
                .setMessage("Are you sure you want to delete all products? This action cannot be undone.")
                .setPositiveButton("Clear All", (dialog, which) -> {
                    // Implement clear all functionality
                    Toast.makeText(this, "Clear all functionality to be implemented", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteProduct(Product product) {
        if (product.getId() != null) {
            viewModel.deleteProduct(product.getId());
            Toast.makeText(this, "Deleting product...", Toast.LENGTH_SHORT).show();
        }
    }

    private void showSuccessMessage(String message) {
        textStatus.setText(message);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void showErrorMessage(String message) {
        textStatus.setText("Error: " + message);
        Toast.makeText(this, "Error: " + message, Toast.LENGTH_LONG).show();
    }

}