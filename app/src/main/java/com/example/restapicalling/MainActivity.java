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
    private TextView textStatus;
    private ProgressBar progressBar;
    private Button btnLoadProducts, btnAddProduct;
    private LinearLayout mainLayout, productsLayout;
    private ScrollView mainScrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createProgrammaticUI();
        setupViewModel();
        setupClickListeners();
        observeData();
    }

    private void createProgrammaticUI() {
        // Main ScrollView
        mainScrollView = new ScrollView(this);

        // Main layout
        mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setPadding(50, 50, 50, 50);
        mainLayout.setBackgroundColor(Color.WHITE);

        // Title
        TextView title = new TextView(this);
        title.setText("Product Manager");
        title.setTextSize(24);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setTextColor(Color.BLACK);
        title.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        titleParams.setMargins(0, 0, 0, 30);
        title.setLayoutParams(titleParams);
        mainLayout.addView(title);

        // Progress Bar
        progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyle);
        progressBar.setVisibility(View.GONE);
        LinearLayout.LayoutParams progressParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        progressParams.setMargins(0, 0, 0, 20);
        progressBar.setLayoutParams(progressParams);
        mainLayout.addView(progressBar);

        // Status Text
        textStatus = new TextView(this);
        textStatus.setText("Click below to load products");
        textStatus.setTextSize(16);
        textStatus.setGravity(Gravity.CENTER);
        textStatus.setTextColor(Color.DKGRAY);
        LinearLayout.LayoutParams statusParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        statusParams.setMargins(0, 0, 0, 20);
        textStatus.setLayoutParams(statusParams);
        mainLayout.addView(textStatus);

        // Button Layout
        LinearLayout buttonLayout = new LinearLayout(this);
        buttonLayout.setOrientation(LinearLayout.HORIZONTAL);
        buttonLayout.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams buttonLayoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        buttonLayoutParams.setMargins(0, 0, 0, 30);
        buttonLayout.setLayoutParams(buttonLayoutParams);

        // Load Products Button
        btnLoadProducts = new Button(this);
        btnLoadProducts.setText("Load Products");
        btnLoadProducts.setBackgroundColor(Color.parseColor("#6200EE"));
        btnLoadProducts.setTextColor(Color.WHITE);
        LinearLayout.LayoutParams loadParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        loadParams.setMargins(0, 0, 20, 0);
        btnLoadProducts.setLayoutParams(loadParams);
        buttonLayout.addView(btnLoadProducts);

        // Add Product Button
        btnAddProduct = new Button(this);
        btnAddProduct.setText("Add Product");
        btnAddProduct.setBackgroundColor(Color.parseColor("#03DAC5"));
        btnAddProduct.setTextColor(Color.BLACK);
        LinearLayout.LayoutParams addParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        btnAddProduct.setLayoutParams(addParams);
        buttonLayout.addView(btnAddProduct);

        mainLayout.addView(buttonLayout);

        // Products Layout (where products will be displayed)
        productsLayout = new LinearLayout(this);
        productsLayout.setOrientation(LinearLayout.VERTICAL);
        productsLayout.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        mainLayout.addView(productsLayout);

        mainScrollView.addView(mainLayout);
        setContentView(mainScrollView);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(ProductViewModel.class);
    }

    private void setupClickListeners() {
        btnLoadProducts.setOnClickListener(v -> {
            loadProducts();
        });

        btnAddProduct.setOnClickListener(v -> {
            showAddProductDialog();
        });
    }

    private void observeData() {
        viewModel.getProducts().observe(this, products -> {
            if (products != null) {
                displayProducts(products);
                String message = "Loaded " + products.size() + " products";
                textStatus.setText(message);
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getError().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                if (error.startsWith("Product created:")) {
                    textStatus.setText(error);
                    Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
                    // Refresh the product list after creating a product
                    loadProducts();
                } else {
                    textStatus.setText("Error: " + error);
                    Toast.makeText(this, "Error: " + error, Toast.LENGTH_LONG).show();
                }
            }
        });

        viewModel.getIsLoading().observe(this, isLoading -> {
            if (isLoading != null) {
                progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
                btnLoadProducts.setEnabled(!isLoading);
                btnAddProduct.setEnabled(!isLoading);
            }
        });
    }

    private void loadProducts() {
        textStatus.setText("Loading products...");
        viewModel.loadAllProducts();
    }

    private void displayProducts(List<Product> products) {
        // Clear existing products
        productsLayout.removeAllViews();

        if (products.isEmpty()) {
            TextView noProductsText = new TextView(this);
            noProductsText.setText("No products found. Add some products first!");
            noProductsText.setTextSize(16);
            noProductsText.setGravity(Gravity.CENTER);
            noProductsText.setTextColor(Color.GRAY);
            noProductsText.setPadding(0, 50, 0, 50);
            productsLayout.addView(noProductsText);
            return;
        }

        // Add products title
        TextView productsTitle = new TextView(this);
        productsTitle.setText("Products List:");
        productsTitle.setTextSize(18);
        productsTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        productsTitle.setTextColor(Color.BLACK);
        productsTitle.setPadding(0, 0, 0, 20);
        productsLayout.addView(productsTitle);

        // Display each product
        for (Product product : products) {
            addProductToLayout(product);
        }
    }

    private void addProductToLayout(Product product) {
        // Create product card
        LinearLayout productCard = new LinearLayout(this);
        productCard.setOrientation(LinearLayout.VERTICAL);
        productCard.setBackgroundColor(Color.parseColor("#F5F5F5"));
        productCard.setPadding(20, 20, 20, 20);

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, 15);
        productCard.setLayoutParams(cardParams);

        // Product Name
        TextView nameText = new TextView(this);
        nameText.setText("Name: " + product.getName());
        nameText.setTextSize(16);
        nameText.setTypeface(null, android.graphics.Typeface.BOLD);
        nameText.setTextColor(Color.BLACK);
        productCard.addView(nameText);

        // Product Description
        if (product.getDescription() != null && !product.getDescription().isEmpty()) {
            TextView descText = new TextView(this);
            descText.setText("Desc: " + product.getDescription());
            descText.setTextSize(14);
            descText.setTextColor(Color.DKGRAY);
            descText.setPadding(0, 5, 0, 0);
            productCard.addView(descText);
        }

        // Price and Quantity in a horizontal layout
        LinearLayout priceQtyLayout = new LinearLayout(this);
        priceQtyLayout.setOrientation(LinearLayout.HORIZONTAL);
        priceQtyLayout.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        // Price
        TextView priceText = new TextView(this);
        priceText.setText("Price: $" + product.getPrice());
        priceText.setTextSize(14);
        priceText.setTextColor(Color.GREEN);
        priceText.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1
        ));
        priceQtyLayout.addView(priceText);

        // Quantity
        TextView qtyText = new TextView(this);
        qtyText.setText("Qty: " + product.getQuantity());
        qtyText.setTextSize(14);
        qtyText.setTextColor(Color.BLUE);
        qtyText.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1
        ));
        priceQtyLayout.addView(qtyText);

        productCard.addView(priceQtyLayout);

        // Category and Date
        LinearLayout categoryDateLayout = new LinearLayout(this);
        categoryDateLayout.setOrientation(LinearLayout.HORIZONTAL);
        categoryDateLayout.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        // Category
        TextView categoryText = new TextView(this);
        categoryText.setText("Category: " + product.getCategory());
        categoryText.setTextSize(12);
        categoryText.setTextColor(Color.MAGENTA);
        categoryText.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1
        ));
        categoryDateLayout.addView(categoryText);

        // Manufacturing Date
        if (product.getManufacturingDate() != null) {
            TextView dateText = new TextView(this);
            dateText.setText("Made: " + product.getManufacturingDate());
            dateText.setTextSize(12);
            dateText.setTextColor(Color.GRAY);
            dateText.setLayoutParams(new LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    1
            ));
            categoryDateLayout.addView(dateText);
        }

        productCard.addView(categoryDateLayout);

        // Add click listener to product card
        productCard.setOnClickListener(v -> {
            showProductDetails(product);
        });

        productsLayout.addView(productCard);
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
        builder.setNegativeButton("Delete", (dialog, which) -> {
            deleteProduct(product);
        });

        builder.show();
    }

    private void deleteProduct(Product product) {
        if (product.getId() != null) {
            viewModel.deleteProduct(product.getId());
            Toast.makeText(this, "Deleting product...", Toast.LENGTH_SHORT).show();
        }
    }

    // ... [Keep the existing showAddProductDialog, validateInput, createProductFromInput methods] ...
    // Add the rest of your existing methods here (showAddProductDialog, validateInput, createProductFromInput)

    private void showAddProductDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add New Product");

        // Create scroll view for dialog
        ScrollView scrollView = new ScrollView(this);
        LinearLayout dialogLayout = new LinearLayout(this);
        dialogLayout.setOrientation(LinearLayout.VERTICAL);
        dialogLayout.setPadding(50, 50, 50, 50);

        // Product Name
        TextView nameLabel = new TextView(this);
        nameLabel.setText("Product Name:");
        nameLabel.setTextSize(16);
        dialogLayout.addView(nameLabel);

        EditText editName = new EditText(this);
        editName.setHint("Enter product name");
        editName.setPadding(0, 10, 0, 30);
        dialogLayout.addView(editName);

        // Description
        TextView descLabel = new TextView(this);
        descLabel.setText("Description:");
        descLabel.setTextSize(16);
        dialogLayout.addView(descLabel);

        EditText editDescription = new EditText(this);
        editDescription.setHint("Enter description");
        editDescription.setPadding(0, 10, 0, 30);
        dialogLayout.addView(editDescription);

        // Price
        TextView priceLabel = new TextView(this);
        priceLabel.setText("Price:");
        priceLabel.setTextSize(16);
        dialogLayout.addView(priceLabel);

        EditText editPrice = new EditText(this);
        editPrice.setHint("Enter price");
        editPrice.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        editPrice.setPadding(0, 10, 0, 30);
        dialogLayout.addView(editPrice);

        // Quantity
        TextView qtyLabel = new TextView(this);
        qtyLabel.setText("Quantity:");
        qtyLabel.setTextSize(16);
        dialogLayout.addView(qtyLabel);

        EditText editQuantity = new EditText(this);
        editQuantity.setHint("Enter quantity");
        editQuantity.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        editQuantity.setPadding(0, 10, 0, 30);
        dialogLayout.addView(editQuantity);

        // Category
        TextView categoryLabel = new TextView(this);
        categoryLabel.setText("Category:");
        categoryLabel.setTextSize(16);
        dialogLayout.addView(categoryLabel);

        EditText editCategory = new EditText(this);
        editCategory.setHint("Enter category");
        editCategory.setPadding(0, 10, 0, 30);
        dialogLayout.addView(editCategory);

        // Manufacturing Date
        TextView dateLabel = new TextView(this);
        dateLabel.setText("Manufacturing Date:");
        dateLabel.setTextSize(16);
        dialogLayout.addView(dateLabel);

        EditText editManufacturingDate = new EditText(this);
        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        editManufacturingDate.setText(currentDate);
        editManufacturingDate.setHint("YYYY-MM-DD");
        editManufacturingDate.setPadding(0, 10, 0, 30);
        dialogLayout.addView(editManufacturingDate);

        // SKU
        TextView skuLabel = new TextView(this);
        skuLabel.setText("SKU:");
        skuLabel.setTextSize(16);
        dialogLayout.addView(skuLabel);

        EditText editSku = new EditText(this);
        editSku.setText("SKU-" + System.currentTimeMillis());
        editSku.setHint("Enter SKU");
        editSku.setPadding(0, 10, 0, 40);
        dialogLayout.addView(editSku);

        scrollView.addView(dialogLayout);
        builder.setView(scrollView);

        builder.setPositiveButton("Save", (dialog, which) -> {
            if (validateInput(editName, editDescription, editPrice, editQuantity,
                    editCategory, editManufacturingDate)) {
                createProductFromInput(editName, editDescription, editPrice, editQuantity,
                        editCategory, editManufacturingDate, editSku);
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> {
            dialog.dismiss();
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private boolean validateInput(EditText... fields) {
        boolean isValid = true;

        // Check each field
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

    private void createProductFromInput(EditText name, EditText description, EditText price,
                                        EditText quantity, EditText category, EditText manufacturingDate,
                                        EditText sku) {
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

}