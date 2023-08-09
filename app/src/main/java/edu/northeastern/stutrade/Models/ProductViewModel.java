package edu.northeastern.stutrade.Models;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ProductViewModel extends ViewModel {
    private MutableLiveData<Product> selectedProduct = new MutableLiveData<>();
    private MutableLiveData<Boolean> isProductSelected = new MutableLiveData<>();

    public LiveData<Product> getSelectedProduct() {
        return selectedProduct;
    }

    public void setSelectedProduct(Product product) {
        selectedProduct.setValue(product);
    }

    public LiveData<Boolean> getisProductSelected() {
        return isProductSelected;
    }

    public void setisProductSelected(Boolean value) {
        isProductSelected.setValue(value);
    }
}
