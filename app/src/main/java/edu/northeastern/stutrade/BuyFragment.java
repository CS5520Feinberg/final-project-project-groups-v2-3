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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import edu.northeastern.stutrade.Models.Product;

public class BuyFragment extends Fragment {
    private RecyclerView productsRecyclerView;
    private ProductAdapter productAdapter;
    private String[] sortingOptions = {
            "Price Increasing",
            "Price Decreasing",
            "Date Ascending",
            "Date Descending"
    };
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_buy, container, false);
        productsRecyclerView = rootView.findViewById(R.id.productRecyclerView);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), R.layout.dropdown_item, sortingOptions);
        AutoCompleteTextView sortDropdown = rootView.findViewById(R.id.sortDropdown);
        sortDropdown.setAdapter(adapter);

        // Handle the selected sorting option
        sortDropdown.setOnItemClickListener((parent, view, position, id) -> {
            String selectedOption = sortingOptions[position];
            handleSorting(selectedOption);

        });
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference productsRef = database.getReference("products");

        // Fetch data from Firebase
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
                productsRecyclerView.setAdapter(productAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle database errors if any
            }
        });

        return rootView;
    }

    private void handleSorting(String selectedOption) {
        List<Product> sortedList = new ArrayList<>(productAdapter.getProductList());

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

        // Update the RecyclerView with the sorted list
        productAdapter.setProductList(sortedList);
        productAdapter.notifyDataSetChanged();
    }


}
