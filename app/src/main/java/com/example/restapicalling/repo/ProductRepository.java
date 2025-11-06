package com.example.restapicalling.repo;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.restapicalling.config.ApiClient;
import com.example.restapicalling.config.ProductApiService;
import com.example.restapicalling.entity.Product;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductRepository {
    private static final String TAG = "ProductRepository";
    private final ProductApiService apiService;

    public ProductRepository() {
        apiService = ApiClient.getProductApiService();
    }

    public interface ProductListCallback {
        void onSuccess(List<Product> products);
        void onError(String error);
    }

    public interface ProductCallback {
        void onSuccess(Product product);
        void onError(String error);
    }

    public interface BooleanCallback {
        void onSuccess(Boolean success);
        void onError(String error);
    }

    public void getAllProducts(ProductListCallback callback) {
        apiService.getAllProducts().enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(@NonNull Call<List<Product>> call, @NonNull Response<List<Product>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to fetch products: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Product>> call, Throwable t) {
                Log.e(TAG, "Error fetching products", t);
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    public void getProductById(Long id, ProductCallback callback) {
        apiService.getProductById(id).enqueue(new Callback<Product>() {
            @Override
            public void onResponse(Call<Product> call, Response<Product> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Product not found");
                }
            }

            @Override
            public void onFailure(Call<Product> call, Throwable t) {
                Log.e(TAG, "Error fetching product", t);
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    public void createProduct(Product product, ProductCallback callback) {
        Log.d(TAG, "Creating product: " + product.getName());
        apiService.createProduct(product).enqueue(new Callback<Product>() {
            @Override
            public void onResponse(Call<Product> call, Response<Product> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Product created successfully: " + response.body().getName());
                    callback.onSuccess(response.body());
                } else {
                    String errorMsg = "Failed to create product. Code: " + response.code();
                    Log.e(TAG, errorMsg);
                    callback.onError(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<Product> call, Throwable t) {
                String errorMsg = "Network error: " + t.getMessage();
                Log.e(TAG, errorMsg, t);
                callback.onError(errorMsg);
            }
        });
    }

    public void updateProduct(Long id, Product product, ProductCallback callback) {
        apiService.updateProduct(id, product).enqueue(new Callback<Product>() {
            @Override
            public void onResponse(Call<Product> call, Response<Product> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to update product: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Product> call, Throwable t) {
                Log.e(TAG, "Error updating product", t);
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    // Add this method to your ProductRepository class
    public void deleteProduct(Long id, BooleanCallback callback) {
        Log.d(TAG, "Deleting product with id: " + id);
        apiService.deleteProduct(id).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Product deleted successfully");
                    callback.onSuccess(true);
                } else {
                    String errorMsg = "Failed to delete product. Code: " + response.code();
                    Log.e(TAG, errorMsg);
                    callback.onError(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                String errorMsg = "Network error: " + t.getMessage();
                Log.e(TAG, errorMsg, t);
                callback.onError(errorMsg);
            }
        });
    }

    public void searchProductsByName(String name, ProductListCallback callback) {
        apiService.searchProductsByName(name).enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Search failed: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Product>> call, Throwable t) {
                Log.e(TAG, "Error searching products", t);
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }
}