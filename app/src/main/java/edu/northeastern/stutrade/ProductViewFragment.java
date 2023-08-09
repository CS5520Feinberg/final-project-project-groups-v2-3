package edu.northeastern.stutrade;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import edu.northeastern.stutrade.Models.Product;
import edu.northeastern.stutrade.Models.ProductViewModel;

public class ProductViewFragment extends Fragment {
    private Product selectedProduct;
    ProductViewModel productViewModel;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
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

        ImageView productImageView = rootView.findViewById(R.id.productImageView);
        productImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImagePopup();
            }
        });
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        productViewModel = new ViewModelProvider(requireActivity()).get(ProductViewModel.class);
        productViewModel.setisProductSelected(true);
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

        // Here, you can dynamically add ImageViews for each image you want to display.
        // For example, if you have a list of image URLs, you can loop through the list and add an ImageView for each image:
        int[] imageResourceIds = {R.drawable.ic_buy, R.drawable.ic_camera, R.drawable.ic_chat};
        for (int resourceId : imageResourceIds) {
            ImageView imageView = new ImageView(requireContext());
            imageView.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            imageView.setPadding(16, 16, 16, 16);
            imageView.setImageResource(resourceId); // Set the image resource from the drawable folder.
            imageContainer.addView(imageView);
        }

//        for (String imageUrl : listOfImageUrls) {
//
//            ImageView imageView = new ImageView(requireContext());
//
//            imageView.setLayoutParams(new LinearLayout.LayoutParams(
//                    LinearLayout.LayoutParams.MATCH_PARENT,
//                    LinearLayout.LayoutParams.WRAP_CONTENT
//            ));
//            imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
//            imageView.setPadding(16, 16, 16, 16);
//            // Load the image into the ImageView using an image loading library like Picasso or Glide.
//            // For example, using Picasso:
//            // Picasso.get().load(imageUrl).into(imageView);
//            // Add the ImageView to the image container.
//            imageContainer.addView(imageView);
//        }

        imagePopup.show();
    }

}
