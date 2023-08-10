package edu.northeastern.stutrade;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SellerFragment extends Fragment {

    private Button galleryButton, uploadButton;
    private List<Uri> selectedImageUris = new ArrayList<>();
    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    private List<ImageView> imageViews = new ArrayList<>();
    ProgressDialog progressDialog;
    private Button selectImageButton;
    private LinearLayout imageContainer;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_seller, container, false);
        galleryButton = view.findViewById(R.id.selectImagebtn);
        uploadButton = view.findViewById(R.id.uploadimagebtn);
        databaseReference = FirebaseDatabase.getInstance().getReference("products");
        storageReference = FirebaseStorage.getInstance().getReference();
        selectImageButton = view.findViewById(R.id.selectImagebtn);
        imageContainer = view.findViewById(R.id.imageContainer);

        galleryButton.setOnClickListener(v -> selectImages());
        uploadButton.setOnClickListener(v -> uploadProducts());

        selectImageButton.setOnClickListener(v -> selectImages());
        uploadButton.setOnClickListener(v -> uploadProducts());
        return view;
    }

    private void selectImages() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true); // Allow selecting multiple images
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 100);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == getActivity().RESULT_OK && data != null) {
            if (data.getClipData() != null) {
                int count = data.getClipData().getItemCount();
                for (int i = 0; i < count; i++) {
                    Uri imageUri = data.getClipData().getItemAt(i).getUri();
                    selectedImageUris.add(imageUri);

                    // Create a new ImageView for each selected image and add it to the container
                    ImageView imageView = new ImageView(getContext());
                    imageView.setLayoutParams(new ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT));
                    imageView.setImageURI(imageUri);

                    // Add the ImageView to the imageContainer LinearLayout
                    imageContainer.addView(imageView);
                }
            } else if (data.getData() != null) {
                Uri imageUri = data.getData();
                selectedImageUris.add(imageUri);

                // Create a new ImageView for the selected image and add it to the container
                ImageView imageView = new ImageView(getContext());
                imageView.setLayoutParams(new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
                imageView.setImageURI(imageUri);

                // Add the ImageView to the imageContainer LinearLayout
                imageContainer.addView(imageView);
            }
        }
    }


    private void uploadProducts() {
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setTitle("Uploading Files....");
        progressDialog.show();

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.CANADA);
        Date now = new Date();
        UserSessionManager sessionManager = new UserSessionManager(getContext());
        String email = sessionManager.getEmail();
        String userName = email.substring(0, email.indexOf("@"));
        for (int i = 0; i < selectedImageUris.size(); i++) {
            String fileName = formatter.format(now) + "_" + i;
            StorageReference imageStorageRef = FirebaseStorage.getInstance().getReference("images/" + userName+"/"+fileName);
            // Upload the image using the newly created storageReference
            final int finalI = i;
            imageStorageRef.putFile(selectedImageUris.get(i))
                    .addOnSuccessListener(taskSnapshot -> {
                        if (finalI == selectedImageUris.size() - 1) {
                            Toast.makeText(getContext(), "All Images Uploaded", Toast.LENGTH_SHORT).show();
                            if (progressDialog.isShowing())
                                progressDialog.dismiss();
                        }
                    }).addOnFailureListener(e -> {
                        if (progressDialog.isShowing())
                            progressDialog.dismiss();
                        Toast.makeText(getContext(), "Failed to Upload Image " + (finalI + 1), Toast.LENGTH_SHORT).show();
                    });
        }
    }
}
