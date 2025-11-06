package com.example.restapicalling.config;

import com.example.restapicalling.entity.Product;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.*;

public interface ProductApiService {
    // Get all products
    @GET("products")
    Call<List<Product>> getAllProducts();

    // Get product by ID
    @GET("products/{id}")
    Call<Product> getProductById(@Path("id") Long id);

    // Get product by SKU
    @GET("products/sku/{sku}")
    Call<Product> getProductBySku(@Path("sku") String sku);

    // Create new product
    @POST("products")
    Call<Product> createProduct(@Body Product product);

    // Update product
    @PUT("products/{id}")
    Call<Product> updateProduct(@Path("id") Long id, @Body Product product);

    // Delete product
    @DELETE("products/{id}")
    Call<Void> deleteProduct(@Path("id") Long id);

    // Get products by category
    @GET("products/category/{category}")
    Call<List<Product>> getProductsByCategory(@Path("category") String category);

    // Search products by name
    @GET("products/search")
    Call<List<Product>> searchProductsByName(@Query("name") String name);

    // Get products by max price
    @GET("products/price/less-than")
    Call<List<Product>> getProductsByMaxPrice(@Query("maxPrice") Double maxPrice);

    // Get products in stock
    @GET("products/in-stock")
    Call<List<Product>> getProductsInStock();
}
