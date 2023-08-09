package edu.northeastern.stutrade;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ProgressBar;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import edu.northeastern.stutrade.Models.Product;
import edu.northeastern.stutrade.Models.ProductViewModel;

public class BuyFragment extends Fragment implements ProductAdapter.OnProductClickListener{
    private RecyclerView productsRecyclerView;
    private ProgressBar loader;
    private ProductAdapter productAdapter;
    private ProductViewModel productViewModel;
    private String[] sortingOptions = {
            "Price Increasing",
            "Price Decreasing",
            "Date Ascending",
            "Date Descending"
    };

    private String selectedSortingOption="";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_buy, container, false);
        productsRecyclerView = rootView.findViewById(R.id.productRecyclerView);
        loader = rootView.findViewById(R.id.loader);
        if (savedInstanceState != null && savedInstanceState.containsKey("product_list")) {
            List<Product> savedProductList = (ArrayList<Product>) savedInstanceState.getSerializable("product_list");
            if (savedProductList != null) {
                productAdapter.setProductList(savedProductList);
            }
            String sortValue = savedInstanceState.getString("sorting_option");
            if( !sortValue.equals("")){
                setDefaultSortingOption(sortValue,rootView);
            }
        }else{
            productsRecyclerView();
            sortDropdown(rootView);
        }

        productViewModel = new ViewModelProvider(requireActivity()).get(ProductViewModel.class);

        productViewModel.getisProductSelected().observe(getViewLifecycleOwner(), isProductSelected -> {
            if (isProductSelected) {
                Product product = productViewModel.getSelectedProduct().getValue();
                onProductClick(product);
            }
        });

        return rootView;
    }

    private void sortDropdown(View rootView){
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), R.layout.dropdown_item, sortingOptions);
        AutoCompleteTextView sortDropdown = rootView.findViewById(R.id.sortDropdown);
        sortDropdown.setAdapter(adapter);

        // Handle the selected sorting option
        sortDropdown.setOnItemClickListener((parent, view, position, id) -> {
            String selectedOption = sortingOptions[position];
            handleSorting(selectedOption);
        });
    }

    private void productsRecyclerView(){
        loader.setVisibility(View.VISIBLE);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference productsRef = database.getReference("products");

        productsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Product> productList = new ArrayList<>();
                for (DataSnapshot productSnapshot : dataSnapshot.getChildren()) {
                    Product product = productSnapshot.getValue(Product.class);
                    productList.add(product);
                }

                // Create and set up the RecyclerView with the fetched data
                productsRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(),2));
                productAdapter = new ProductAdapter(productList);
                productAdapter.setOnProductClickListener((ProductAdapter.OnProductClickListener) BuyFragment.this);
                productsRecyclerView.setAdapter(productAdapter);

                loader.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                loader.setVisibility(View.GONE);
            }
        });
    }

    private void handleSorting(String selectedOption) {
        List<Product> sortedList = new ArrayList<>(productAdapter.getProductList());
        selectedSortingOption = selectedOption;
        switch (selectedOption) {
            case "Price Increasing":
                Collections.sort(sortedList, Comparator.comparing(Product::getPriceAsDouble));
                break;
            case "Price Decreasing":
                Collections.sort(sortedList, (product1, product2) -> product2.getPriceAsDouble().compareTo(product1.getPriceAsDouble()));
                break;
            case "Date Ascending":
                Collections.sort(sortedList, (product1, product2) -> {
                    Date date1 = product1.getDatePostedAsDate();
                    Date date2 = product2.getDatePostedAsDate();
                    return date1 != null && date2 != null ? date1.compareTo(date2) : 0;
                });
                break;
            case "Date Descending":
            default:
                Collections.sort(sortedList, (product1, product2) -> {
                    Date date1 = product1.getDatePostedAsDate();
                    Date date2 = product2.getDatePostedAsDate();
                    return date1 != null && date2 != null ? date2.compareTo(date1) : 0;
                });
                break;
        }

        productAdapter.setProductList(sortedList);
        productAdapter.notifyDataSetChanged();
    }

    @Override
    public void onProductClick(Product product) {
        // Create a new ProductViewFragment and pass the selected product details
        productViewModel.setSelectedProduct(product);

        ProductViewFragment productViewFragment = new ProductViewFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("selected_product", product);
        productViewFragment.setArguments(bundle);

        // Replace the current fragment with the new ProductViewFragment
        FragmentTransaction fragmentTransaction = requireActivity().getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, productViewFragment);
        fragmentTransaction.commit();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("product_list", (ArrayList<Product>) productAdapter.getProductList());
        outState.putSerializable("sorting_option", selectedSortingOption);
    }

    private void setDefaultSortingOption(String defaultOption, View rootView) {
        AutoCompleteTextView sortDropdown = rootView.findViewById(R.id.sortDropdown);
        int position = Arrays.asList(sortingOptions).indexOf(defaultOption);
        if (position >= 0) {
            sortDropdown.setText(sortingOptions[position], false);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        productViewModel.setisProductSelected(false);
    }
}
