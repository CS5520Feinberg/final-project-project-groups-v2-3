package edu.northeastern.stutrade;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "username";
    private static final String ARG_PARAM2 = "email";

    private String username;
    private String email;
    private ImageView iv_profile_photo, iv_camera_icon, iv_delete_icon;
    private TextView tv_username, tv_email, tv_bio, tv_location, tv_university;
    private EditText et_username, et_bio, et_location, et_university;
    private Button btn_edit, btn_save, btn_cancel, btn_logout;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private Bitmap originalProfilePhotoBitmap;
    private static String userId;
    DatabaseReference profileRef;
    private FirebaseAuth firebaseAuth;

    private static final String KEY_USERNAME = "username";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_BIO = "bio";
    private static final String KEY_LOCATION = "location";
    private static final String KEY_UNIVERSITY= "university";
    private static final String KEY_PROFILE_PHOTO = "profile_photo";
    private StorageReference profileStorageRef;
    Bitmap profilePhotoBitmap;

    public ProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param username username.
     * @param email email id.
     * @return A new instance of fragment ProfileFragment.
     */
    public static ProfileFragment newInstance(String username, String email) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, username);
        args.putString(ARG_PARAM2, email);
        fragment.setArguments(args);

        userId = email.substring(0, email.indexOf("@")); // Extract user ID from the email

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            username = getArguments().getString(ARG_PARAM1);
            email = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save the data to the outState bundle
        outState.putString(KEY_USERNAME, et_username.getText().toString());
        outState.putString(KEY_EMAIL, tv_email.getText().toString());
        outState.putString(KEY_BIO, et_bio.getText().toString());
        outState.putString(KEY_LOCATION, et_location.getText().toString());
        //outState.putParcelable(KEY_PROFILE_PHOTO, originalProfilePhotoBitmap);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            // Restore the data from the restored instance state
            et_username.setText(savedInstanceState.getString(KEY_USERNAME));
            et_bio.setText(savedInstanceState.getString(KEY_BIO));
            et_location.setText(savedInstanceState.getString(KEY_LOCATION));
            et_university.setText(savedInstanceState.getString(KEY_UNIVERSITY));
            tv_username.setText(savedInstanceState.getString(KEY_USERNAME));
            tv_email.setText(savedInstanceState.getString(KEY_EMAIL));
            tv_bio.setText(savedInstanceState.getString(KEY_BIO));
            tv_location.setText(savedInstanceState.getString(KEY_LOCATION));
            tv_university.setText(savedInstanceState.getString(KEY_UNIVERSITY));

            originalProfilePhotoBitmap = savedInstanceState.getParcelable(KEY_PROFILE_PHOTO);
            iv_profile_photo.setImageBitmap(originalProfilePhotoBitmap);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialize views
        iv_profile_photo = view.findViewById(R.id.iv_profile_photo);
        iv_camera_icon = view.findViewById(R.id.iv_camera_icon);
        iv_delete_icon = view.findViewById(R.id.iv_delete_icon);

        tv_username = view.findViewById(R.id.tv_username);
        tv_username.setText(username);
        tv_email = view.findViewById(R.id.tv_email);
        tv_email.setText(email);

        tv_bio = view.findViewById(R.id.tv_bio);
        tv_location = view.findViewById(R.id.tv_location);
        tv_university = view.findViewById(R.id.tv_university);

        et_username = view.findViewById(R.id.et_username);
        et_username.setText(username);

        et_bio = view.findViewById(R.id.et_bio);
        et_location = view.findViewById(R.id.et_location);
        et_university = view.findViewById(R.id.et_university);

        btn_edit = view.findViewById(R.id.btn_edit);
        btn_edit.setOnClickListener(v -> showEditViews());

        btn_logout = view.findViewById(R.id.btn_logout);
        btn_logout.setOnClickListener(v -> logout());
        firebaseAuth = FirebaseAuth.getInstance();

        btn_save = view.findViewById(R.id.btn_save);
        btn_save.setOnClickListener(v -> saveChanges());

        btn_cancel = view.findViewById(R.id.btn_cancel);
        btn_cancel.setOnClickListener(v -> showExitConfirmationDialog());

        if (savedInstanceState == null) {
            // Get a reference to the user's profile data in the database
            profileRef = FirebaseDatabase.getInstance().getReference("profiles").child(userId);
            profileRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (isAdded() && snapshot.exists()) {
                        String storedUsername = snapshot.child("username").getValue(String.class);
                        String storedBio = snapshot.child("bio").getValue(String.class);
                        String storedLocation = snapshot.child("location").getValue(String.class);
                        String storedUniversity = snapshot.child("university").getValue(String.class);
                        String imageUrl = snapshot.child("profile_photo").getValue(String.class);

                        // Update the UI on the main thread
                        requireActivity().runOnUiThread(() -> {
                            // Populate the views with the data from the database
                            tv_username.setText(storedUsername);
                            et_username.setText(storedUsername);
                            tv_bio.setText(storedBio);
                            et_bio.setText(storedBio);
                            tv_location.setText(storedLocation);
                            et_location.setText(storedLocation);
                            tv_university.setText(storedUniversity);
                            et_university.setText(storedUniversity);
                            new LoadImageTask().execute(imageUrl);
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Error occurred, handle the error
                    Toast.makeText(getContext(), "Database Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (savedInstanceState != null) {
            // Retrieve the data from the saved instance state if available
            et_username.setText(savedInstanceState.getString(KEY_USERNAME));
            et_bio.setText(savedInstanceState.getString(KEY_BIO));
            et_location.setText(savedInstanceState.getString(KEY_LOCATION));
            et_university.setText(savedInstanceState.getString(KEY_UNIVERSITY));
            tv_username.setText(savedInstanceState.getString(KEY_USERNAME));
            tv_email.setText(savedInstanceState.getString(KEY_EMAIL));
            tv_bio.setText(savedInstanceState.getString(KEY_BIO));
            tv_location.setText(savedInstanceState.getString(KEY_LOCATION));
            tv_university.setText(savedInstanceState.getString(KEY_UNIVERSITY));
            originalProfilePhotoBitmap = savedInstanceState.getParcelable(KEY_PROFILE_PHOTO);
            iv_profile_photo.setImageBitmap(originalProfilePhotoBitmap);
        }

        return view;
    }

    private void saveChanges() {
        // Save the edited data
        String editedUsername = et_username.getText().toString();
        String editedBio = et_bio.getText().toString();
        String editedLocation = et_location.getText().toString();
        String editedUniversity = et_university.getText().toString();

        UserSessionManager sessionManager;
        if(!editedUsername.contentEquals(tv_username.getText())) {
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
            userRef.child("name").setValue(editedUsername);

            sessionManager = new UserSessionManager(getContext());
            sessionManager.saveUserDetails(editedUsername, email);
            sessionManager.setLoggedIn(true);

            ((MainActivity) getContext()).updateUsernameTextView(editedUsername);
        }

        // Update the read-only views with the edited data
        tv_username.setText(editedUsername);
        tv_bio.setText(editedBio);
        tv_location.setText(editedLocation);
        tv_university.setText(editedUniversity);

        profileRef = FirebaseDatabase.getInstance().getReference("profiles").child(userId);
        profileRef.child("username").setValue(editedUsername);
        profileRef.child("bio").setValue(editedBio);
        profileRef.child("location").setValue(editedLocation);
        profileRef.child("university").setValue(editedUniversity);

        profileStorageRef = FirebaseStorage.getInstance().getReference("profile_image/" + userId);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        if (profilePhotoBitmap != null) {
            profilePhotoBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] profileImage = baos.toByteArray();
            UploadTask uploadImage = profileStorageRef.putBytes(profileImage);

            uploadImage.addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    profileStorageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageUrl = uri.toString();
                        // Save the image URL to Firebase Realtime Database
                        profileRef.child("profile_photo").setValue(imageUrl);
                    });
                } else {
                    Toast.makeText(getContext(), "Save not successful", Toast.LENGTH_SHORT).show();
                }
            });
        }

        showReadOnlyViews();
    }

    private void cancelChanges() {
        // Revert the edited views with the read-only data
        et_username.setText(tv_username.getText());
        et_bio.setText(tv_bio.getText());
        et_location.setText(tv_location.getText());
        et_university.setText(tv_university.getText());
        iv_profile_photo.setImageBitmap(originalProfilePhotoBitmap);

        showReadOnlyViews();
    }

    private void showReadOnlyViews() {
        //show the text views and hide the edit views
        tv_username.setVisibility(View.VISIBLE);
        tv_email.setVisibility(View.VISIBLE);
        tv_bio.setVisibility(View.VISIBLE);
        tv_location.setVisibility(View.VISIBLE);
        tv_university.setVisibility(View.VISIBLE);
        btn_edit.setVisibility(View.VISIBLE);
        btn_logout.setVisibility(View.VISIBLE);

        et_username.setVisibility(View.GONE);
        et_bio.setVisibility(View.GONE);
        et_location.setVisibility(View.GONE);
        et_university.setVisibility(View.GONE);
        btn_save.setVisibility(View.GONE);
        btn_cancel.setVisibility(View.GONE);
        iv_camera_icon.setVisibility(View.GONE);
        iv_delete_icon.setVisibility(View.GONE);
    }

    private void showEditViews() {
        originalProfilePhotoBitmap = ((BitmapDrawable) iv_profile_photo.getDrawable()).getBitmap();

        //show the edit views and hide the text views
        et_username.setVisibility(View.VISIBLE);
        et_bio.setVisibility(View.VISIBLE);
        et_location.setVisibility(View.VISIBLE);
        et_university.setVisibility(View.VISIBLE);
        btn_save.setVisibility(View.VISIBLE);
        btn_cancel.setVisibility(View.VISIBLE);
        iv_camera_icon.setVisibility(View.VISIBLE);
        iv_camera_icon.setOnClickListener(v -> dispatchTakePictureIntent());
        iv_delete_icon.setVisibility(View.VISIBLE);
        iv_delete_icon.setOnClickListener(v -> setDefaultProfilePicture());

        tv_username.setVisibility(View.GONE);
        tv_bio.setVisibility(View.GONE);
        tv_location.setVisibility(View.GONE);
        tv_university.setVisibility(View.GONE);
        btn_edit.setVisibility(View.GONE);
        btn_logout.setVisibility(View.GONE);
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            Bundle extras = data.getExtras();
            profilePhotoBitmap = (Bitmap) extras.get("data");
            iv_profile_photo.setImageBitmap(profilePhotoBitmap);
        }
    }

    private void setDefaultProfilePicture() {
        iv_profile_photo.setImageResource(R.drawable.ic_profile);
        profilePhotoBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_profile);
    }

    public boolean onBackPressed() {
        if (btn_edit.getVisibility() == View.GONE && btn_save.getVisibility() == View.VISIBLE) {
            showExitConfirmationDialog();
            return true; // Consume the back button press event
        }
        return false; // Let the activity handle the back button press
    }

    private void showExitConfirmationDialog() {
        new AlertDialog.Builder(getContext())
                .setTitle("Discard Changes")
                .setMessage("Are you sure you want to discard changes?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // If the user clicks "Yes", discard the changes
                    cancelChanges();
                })
                .setNegativeButton("No", (dialog, which) -> {
                    // If the user clicks "No", dismiss the dialog and continue
                    dialog.dismiss();
                })
                .show();
    }

    private void logout() {
        // Sign out the user from Firebase Authentication
        firebaseAuth.signOut();

        // Redirect the user to the login page
        redirectToLoginScreen();
    }

    private void redirectToLoginScreen() {
        Intent intent = new Intent(getContext(), LoginActivity.class);
        startActivity(intent);

        UserSessionManager sessionManager = new UserSessionManager(getContext());
        sessionManager.clearSession();

        // Finish the current activity to prevent the user from going back to the profile screen after logging out
        AppCompatActivity activity = (AppCompatActivity) requireActivity();
        activity.finish();

        Toast.makeText(getContext(), "Logged out successfully!", Toast.LENGTH_SHORT).show();
    }

    @SuppressLint("StaticFieldLeak")
    private class LoadImageTask extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... urls) {
            String imageUrl = urls[0];
            try {
                URL url = new URL(imageUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                return BitmapFactory.decodeStream(input);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            if (result != null) {
                iv_profile_photo.setImageBitmap(result);
            }
        }
    }
}