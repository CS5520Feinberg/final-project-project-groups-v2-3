package edu.northeastern.stutrade;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import edu.northeastern.stutrade.Models.Product;

public class ProductViewFragment extends Fragment {
    private Product selectedProduct;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_product_view, container, false);

        Bundle args = getArguments();
        if (args != null && args.containsKey("selected_product")) {
            selectedProduct = (Product) args.getSerializable("selected_product");
        }

        if (selectedProduct != null) {
            ImageView productImageView = rootView.findViewById(R.id.productImageView);
            TextView productDescriptionTextView = rootView.findViewById(R.id.productDescriptionTextView);
            TextView datePostedTextView = rootView.findViewById(R.id.datePostedTextView);
            TextView sellerNameTextView = rootView.findViewById(R.id.sellerNameTextView);
            TextView productPriceTextView = rootView.findViewById(R.id.productPriceTextView);
            Button chatButton = rootView.findViewById(R.id.chatButton);


            productDescriptionTextView.setText(selectedProduct.getProductDescription());
            datePostedTextView.setText(selectedProduct.getDatePosted());
            sellerNameTextView.setText(selectedProduct.getSellerName());
            productPriceTextView.setText(String.valueOf(selectedProduct.getProductPrice()));

            // chatButton.setOnClickListener(view -> {});

        }

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.setFocusableInTouchMode(true);
        view.requestFocus();
        view.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == android.view.KeyEvent.ACTION_UP && keyCode == android.view.KeyEvent.KEYCODE_BACK) {
                requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, new BuyFragment()).commit();
                return true;
            }
            return false;
        });
    }
}
