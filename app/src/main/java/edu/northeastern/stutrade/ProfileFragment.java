package edu.northeastern.stutrade;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private ImageView iv_profile_photo, iv_camera_icon, iv_delete_icon;
    private TextView tv_username, tv_email, tv_bio, tv_location;
    private EditText et_username, et_email, et_bio, et_location;
    private Button btn_edit, btn_save, btn_cancel;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private Bitmap originalProfilePhotoBitmap;

    private static final String KEY_USERNAME = "username";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_BIO = "bio";
    private static final String KEY_LOCATION = "location";
    private static final String KEY_PROFILE_PHOTO = "profile_photo";

    public ProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ProfileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        if (savedInstanceState != null) {
            // Retrieve the data from the saved instance state if available
            et_username.setText(savedInstanceState.getString(KEY_USERNAME));
            et_email.setText(savedInstanceState.getString(KEY_EMAIL));
            et_bio.setText(savedInstanceState.getString(KEY_BIO));
            et_location.setText(savedInstanceState.getString(KEY_LOCATION));
            originalProfilePhotoBitmap = savedInstanceState.getParcelable(KEY_PROFILE_PHOTO);
            iv_profile_photo.setImageBitmap(originalProfilePhotoBitmap);
        }

    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save the data to the outState bundle
        outState.putString(KEY_USERNAME, et_username.getText().toString());
        outState.putString(KEY_EMAIL, et_email.getText().toString());
        outState.putString(KEY_BIO, et_bio.getText().toString());
        outState.putString(KEY_LOCATION, et_location.getText().toString());
        outState.putParcelable(KEY_PROFILE_PHOTO, originalProfilePhotoBitmap);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            // Restore the data from the restored instance state
            et_username.setText(savedInstanceState.getString(KEY_USERNAME));
            et_email.setText(savedInstanceState.getString(KEY_EMAIL));
            et_bio.setText(savedInstanceState.getString(KEY_BIO));
            et_location.setText(savedInstanceState.getString(KEY_LOCATION));
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
        tv_email = view.findViewById(R.id.tv_email);
        tv_bio = view.findViewById(R.id.tv_bio);
        tv_location = view.findViewById(R.id.tv_location);

        et_username = view.findViewById(R.id.et_username);
        et_email = view.findViewById(R.id.et_email);
        et_bio = view.findViewById(R.id.et_bio);
        et_location = view.findViewById(R.id.et_location);

        btn_edit = view.findViewById(R.id.btn_edit);
        btn_edit.setOnClickListener(v -> showEditViews());

        btn_save = view.findViewById(R.id.btn_save);
        btn_save.setOnClickListener(v -> saveChanges());

        btn_cancel = view.findViewById(R.id.btn_cancel);
        btn_cancel.setOnClickListener(v -> cancelChanges());

        return view;
    }

    private void saveChanges() {
        // Save the edited data
        String editedUsername = et_username.getText().toString();
        String editedEmail = et_email.getText().toString();
        String editedBio = et_bio.getText().toString();
        String editedLocation = et_location.getText().toString();

        // Update the read-only views with the edited data
        tv_username.setText(editedUsername);
        tv_email.setText(editedEmail);
        tv_bio.setText(editedBio);
        tv_location.setText(editedLocation);

        showReadOnlyViews();
    }

    private void cancelChanges() {
        // Revert the edited views with the read-only data
        et_username.setText(tv_username.getText());
        et_email.setText(tv_email.getText());
        et_bio.setText(tv_bio.getText());
        et_location.setText(tv_location.getText());
        iv_profile_photo.setImageBitmap(originalProfilePhotoBitmap);

        showReadOnlyViews();
    }

    private void showReadOnlyViews() {
        //show the text views and hide the edit views
        tv_username.setVisibility(View.VISIBLE);
        tv_email.setVisibility(View.VISIBLE);
        tv_bio.setVisibility(View.VISIBLE);
        tv_location.setVisibility(View.VISIBLE);
        btn_edit.setVisibility(View.VISIBLE);

        et_username.setVisibility(View.GONE);
        et_email.setVisibility(View.GONE);
        et_bio.setVisibility(View.GONE);
        et_location.setVisibility(View.GONE);
        btn_save.setVisibility(View.GONE);
        btn_cancel.setVisibility(View.GONE);
        iv_camera_icon.setVisibility(View.GONE);
        iv_delete_icon.setVisibility(View.GONE);
    }

    private void showEditViews() {
        originalProfilePhotoBitmap = ((BitmapDrawable) iv_profile_photo.getDrawable()).getBitmap();

        //show the edit views and hide the text views
        et_username.setVisibility(View.VISIBLE);
        et_email.setVisibility(View.VISIBLE);
        et_bio.setVisibility(View.VISIBLE);
        et_location.setVisibility(View.VISIBLE);
        btn_save.setVisibility(View.VISIBLE);
        btn_cancel.setVisibility(View.VISIBLE);
        iv_camera_icon.setVisibility(View.VISIBLE);
        iv_camera_icon.setOnClickListener(v -> dispatchTakePictureIntent());
        iv_delete_icon.setVisibility(View.VISIBLE);
        iv_delete_icon.setOnClickListener(v -> setDefaultProfilePicture());

        tv_username.setVisibility(View.GONE);
        tv_email.setVisibility(View.GONE);
        tv_bio.setVisibility(View.GONE);
        tv_location.setVisibility(View.GONE);
        btn_edit.setVisibility(View.GONE);
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private void setDefaultProfilePicture() {
        iv_profile_photo.setImageResource(R.drawable.ic_profile);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap profilePhotoBitmap = (Bitmap) extras.get("data");
            iv_profile_photo.setImageBitmap(profilePhotoBitmap);
        }
    }
}