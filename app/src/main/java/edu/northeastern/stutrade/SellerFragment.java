package edu.northeastern.stutrade;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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

    private EditText productNameEditText, descriptionEditText, priceEditText;
    private Button captureButton, galleryButton, uploadButton;
    private ImageView selectedImageView;

    private List<Uri> selectedImageUris = new ArrayList<>();
    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    Uri imageUri;
    ProgressDialog progressDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_seller, container, false);
//        productNameEditText = view.findViewById(R.id.editProductName);
//        descriptionEditText = view.findViewById(R.id.editDescription);
//        priceEditText = view.findViewById(R.id.editPrice);
        selectedImageView = view.findViewById(R.id.firebaseimage);
        galleryButton = view.findViewById(R.id.selectImagebtn);
        uploadButton = view.findViewById(R.id.uploadimagebtn);
        databaseReference = FirebaseDatabase.getInstance().getReference("products");
        storageReference = FirebaseStorage.getInstance().getReference();
        galleryButton.setOnClickListener(v -> selectImage());
        uploadButton.setOnClickListener(v -> uploadProduct());

        return view;
    }

    private void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 100);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && data != null && data.getData() != null) {
            imageUri = data.getData();
            selectedImageView.setImageURI(imageUri);
        }
    }


    private void uploadProduct() {
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setTitle("Uploading File....");
        progressDialog.show();


        SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.CANADA);
        Date now = new Date();
        String fileName = formatter.format(now);
        storageReference = FirebaseStorage.getInstance().getReference("images/" + fileName);

        storageReference.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    selectedImageView.setImageURI(null);
                    Toast.makeText(getContext(), "Successfully Uploaded", Toast.LENGTH_SHORT).show();
                    if (progressDialog.isShowing())
                        progressDialog.dismiss();

                }).addOnFailureListener(e -> {
                    if (progressDialog.isShowing())
                        progressDialog.dismiss();
                    Toast.makeText(getContext(), "Failed to Upload", Toast.LENGTH_SHORT).show();
                });
    }

}
