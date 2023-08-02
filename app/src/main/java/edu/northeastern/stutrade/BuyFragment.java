package edu.northeastern.stutrade;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.GridLayout;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import edu.northeastern.stutrade.Models.Product;
import edu.northeastern.stutrade.ProductAdapter;

public class BuyFragment extends Fragment {
    private RecyclerView productsRecyclerView;
    private ProductAdapter productAdapter;
    private Spinner sortSpinner;
    private GridLayout sortGridLayout;
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
        //sortGridLayout = rootView.findViewById(R.id.gridSortLayout);
        // Initialize Firebase Database reference

         // Set up the Adapter for the Material Design Spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), R.layout.dropdown_item, sortingOptions);
        AutoCompleteTextView sortDropdown = rootView.findViewById(R.id.sortDropdown);
        sortDropdown.setAdapter(adapter);

        // Handle the selected sorting option
        sortDropdown.setOnItemClickListener((parent, view, position, id) -> {
            String selectedOption = sortingOptions[position];

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
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Check the orientation
        int columns = newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE ? 2 : 1;

        // Set the number of columns in the GridLayout
        //sortGridLayout.setColumnCount(columns);
    }
}
