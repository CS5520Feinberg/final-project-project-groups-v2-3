package edu.northeastern.stutrade;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
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

public class BuyFragment extends Fragment implements ProductAdapter.OnProductClickListener{
    private RecyclerView productsRecyclerView;
    private ProductAdapter productAdapter;
    private String[] sortingOptions = {
            "Price Increasing",
            "Price Decreasing",
            "Date Ascending",
            "Date Descending"
    };

    private String selectedSortingOption="";
    private List<Product> productList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_buy, container, false);
        productsRecyclerView = rootView.findViewById(R.id.productRecyclerView);
        if (savedInstanceState != null && savedInstanceState.containsKey("product_list")) {
            List<Product> savedProductList = (ArrayList<Product>) savedInstanceState.getSerializable("product_list");
            if (savedProductList != null) {
                productList = savedProductList;
            }
            String sortValue = savedInstanceState.getString("sorting_option");
            if( !sortValue.equals("")){
                setDefaultSortingOption(sortValue,rootView);
            }
        }else{
            sortDropdown(rootView);
            productsRecyclerView();
        }
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
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
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
}
