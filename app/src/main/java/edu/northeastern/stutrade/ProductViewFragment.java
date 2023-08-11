package edu.northeastern.stutrade;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Objects;

import edu.northeastern.stutrade.Models.Product;

public class ProductViewFragment extends Fragment {
    private Product selectedProduct;
    private StorageReference storageReference;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        View rootView = inflater.inflate(R.layout.fragment_product_view, container, false);
        UserSessionManager sessionManager = new UserSessionManager(requireContext());
        String email = sessionManager.getEmail();
        String userName = email.substring(0, email.indexOf("@"));
        FirebaseStorage storage = FirebaseStorage.getInstance();
        //remove username to sellerusername
        Bundle args = getArguments();
        if (args != null && args.containsKey("selected_product")) {
            selectedProduct = (Product) args.getSerializable("selected_product");
        }
        if (selectedProduct != null) {
            storageReference = storage.getReference("images/" + selectedProduct.getSellerId() + "/" + selectedProduct.getProductName());
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
            Picasso.get().load(selectedProduct.getImageUrl()).into(productImageView);
            // chatButton.setOnClickListener(view -> {});

        }

        ImageView productImageView = rootView.findViewById(R.id.productImageView);
        productImageView.setOnClickListener(v -> showImagePopup());
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
    private void showImagePopup() {
        Dialog imagePopup = new Dialog(requireContext());
        imagePopup.setContentView(R.layout.layout_popup_image);
        LinearLayout imageContainer = imagePopup.findViewById(R.id.imageContainer);

        storageReference.listAll()
                .addOnSuccessListener(listResult -> {
                    List<StorageReference> items = listResult.getItems();

                    for (StorageReference item : items) {
                        item.getDownloadUrl().addOnSuccessListener(uri -> {
                            String imageUrl = uri.toString();
                            // Load the image into the ImageView using Picasso
                            ImageView imageView = new ImageView(requireContext());
                            imageView.setLayoutParams(new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                            ));
                            Picasso.get().load(imageUrl).into(imageView);
                            imageContainer.addView(imageView);
                        });
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Unable to load images", Toast.LENGTH_SHORT).show();
                });
        imagePopup.show();
    }


}