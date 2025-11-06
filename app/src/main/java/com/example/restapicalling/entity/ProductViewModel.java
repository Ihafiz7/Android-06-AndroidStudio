package com.example.restapicalling.entity;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.restapicalling.repo.ProductRepository;

import java.util.List;

public class ProductViewModel extends ViewModel {
    private ProductRepository repository;
    private MutableLiveData<List<Product>> products = new MutableLiveData<>();
    private MutableLiveData<String> error = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>();

    public ProductViewModel() {
        repository = new ProductRepository();
    }

    public LiveData<List<Product>> getProducts() {
        return products;
    }

    public LiveData<String> getError() {
        return error;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public void loadAllProducts() {
        isLoading.setValue(true);
        repository.getAllProducts(new ProductRepository.ProductListCallback() {
            @Override
            public void onSuccess(List<Product> productList) {
                products.setValue(productList);
                error.setValue("");
                isLoading.setValue(false);
            }

            @Override
            public void onError(String errorMessage) {
                error.setValue(errorMessage);
                isLoading.setValue(false);
            }
        });
    }

    public void createProduct(Product product) {
        isLoading.setValue(true);
        repository.createProduct(product, new ProductRepository.ProductCallback() {
            @Override
            public void onSuccess(Product createdProduct) {
                // Refresh the product list after creation
                loadAllProducts();
                String successMsg = "Product created: " + createdProduct.getName();
                error.setValue(successMsg);
            }

            @Override
            public void onError(String errorMessage) {
                error.setValue(errorMessage);
                isLoading.setValue(false);
            }
        });
    }

    public void updateProduct(Long id, Product product) {
        isLoading.setValue(true);
        repository.updateProduct(id, product, new ProductRepository.ProductCallback() {
            @Override
            public void onSuccess(Product updatedProduct) {
                loadAllProducts();
            }

            @Override
            public void onError(String errorMessage) {
                error.setValue(errorMessage);
                isLoading.setValue(false);
            }
        });
    }

    // Add this method to your ProductViewModel class
    public void deleteProduct(Long id) {
        isLoading.setValue(true);
        repository.deleteProduct(id, new ProductRepository.BooleanCallback() {
            @Override
            public void onSuccess(Boolean success) {
                // Refresh the product list after deletion
                loadAllProducts();
            }

            @Override
            public void onError(String errorMessage) {
                error.setValue(errorMessage);
                isLoading.setValue(false);
            }
        });
    }

    public void searchProducts(String name) {
        isLoading.setValue(true);
        repository.searchProductsByName(name, new ProductRepository.ProductListCallback() {
            @Override
            public void onSuccess(List<Product> productList) {
                products.setValue(productList);
                error.setValue("");
                isLoading.setValue(false);
            }

            @Override
            public void onError(String errorMessage) {
                error.setValue(errorMessage);
                isLoading.setValue(false);
            }
        });
    }
}