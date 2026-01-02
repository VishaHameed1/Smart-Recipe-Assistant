package com.hanson.android.recipe;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.hanson.android.recipe.Helper.ImageHelper;

import java.io.IOException;
import java.util.ArrayList;

import static android.app.Activity.RESULT_OK;

public class AddRecipeFragment extends Fragment {

    private ImageHelper imageHelper = new ImageHelper();
    private InputMethodManager imm;

    // Updated to match your new XML IDs and Material Components
    private TextInputEditText recipeName;
    private TextView author;
    private Spinner country;
    private TextInputEditText description;
    private FloatingActionButton cameraFab;
    private ImageView mPhotoImageView;
    private MaterialButton btnNext;

    private static final int PICK_FROM_CAMERA = 0;
    private static final int PICK_FROM_ALBUM = 1;
    private static final int PERMISSION_REQUEST_CODE = 1052;

    public AddRecipeFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_add_recipe, container, false);

        // 1. Setup Author from SharedPreferences
        author = view.findViewById(R.id.txt_Add_Author);
        SharedPreferences pref = requireContext().getSharedPreferences("Login", Context.MODE_PRIVATE);
        String userID = pref.getString("userID", "Chef");
        author.setText("👨‍🍳 by " + userID);

        // 2. Initialize Views
        recipeName = view.findViewById(R.id.txt_Add_NewName);
        description = view.findViewById(R.id.txt_Add_Description);
        country = view.findViewById(R.id.spinner_Add_Country);
        cameraFab = view.findViewById(R.id.btn_Add_Camera);
        mPhotoImageView = view.findViewById(R.id.imgv_Add_Image);
        btnNext = view.findViewById(R.id.btn_next_step);

        // 3. Keyboard Management
        imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        RelativeLayout root = view.findViewById(R.id.root_Add_Recipe);
        root.setOnClickListener(v -> hideKeyboard());

        // 4. Click Listeners
        cameraFab.setOnClickListener(v -> showImageSourceDialog());

        // Navigation to next page in ViewPager
        btnNext.setOnClickListener(v -> {
            if (getActivity() instanceof AddRecipeActivity) {
                ((AddRecipeActivity) getActivity()).moveToNextStep(1);
            }
        });

        checkPermissions();
        return view;
    }

    private void hideKeyboard() {
        if (imm != null && getView() != null) {
            imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
        }
    }

    // --- Methods for AddRecipeActivity to fetch data ---
    public String getRecipeName() {
        return recipeName.getText().toString().trim();
    }

    public String getCategory() {
        return country.getSelectedItem().toString();
    }

    public String getDescription() {
        return description.getText().toString().trim();
    }

    public Bitmap getRecipeImage() {
        if (mPhotoImageView.getDrawable() != null) {
            return ((BitmapDrawable) mPhotoImageView.getDrawable()).getBitmap();
        }
        return null;
    }

    // --- Image Handling Logic ---
    private void showImageSourceDialog() {
        String[] options = {"Take Photo", "Choose from Gallery", "Cancel"};
        new AlertDialog.Builder(requireContext())
                .setTitle("Select Image Source")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) doTakePhotoAction();
                    else if (which == 1) doTakeAlbumAction();
                })
                .show();
    }

    private void doTakePhotoAction() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, PICK_FROM_CAMERA);
    }

    private void doTakeAlbumAction() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_FROM_ALBUM);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            try {
                Bitmap bitmap;
                if (requestCode == PICK_FROM_CAMERA) {
                    bitmap = (Bitmap) data.getExtras().get("data");
                } else {
                    bitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), data.getData());
                }

                if (bitmap != null) {
                    // Placeholder visibility control logic can be added here
                    mPhotoImageView.setImageBitmap(imageHelper.resizeImage(bitmap));
                }
            } catch (Exception e) {
                Toast.makeText(getContext(), "Failed to load image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void checkPermissions() {
        String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA};
        ArrayList<String> listPermissionsNeeded = new ArrayList<>();
        for (String p : permissions) {
            if (ContextCompat.checkSelfPermission(requireContext(), p) != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p);
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(requireActivity(), listPermissionsNeeded.toArray(new String[0]), PERMISSION_REQUEST_CODE);
        }
    }
}