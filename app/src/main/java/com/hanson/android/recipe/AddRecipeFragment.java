package com.hanson.android.recipe;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

// CORRECTED ANDROIDX IMPORTS
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.hanson.android.recipe.Helper.ImageHelper;

import java.io.IOException;
import java.util.ArrayList;

import static android.app.Activity.RESULT_OK;

public class AddRecipeFragment extends Fragment {

    private ImageHelper imageHelper = new ImageHelper();
    private InputMethodManager imm;
    private TextView recipeName;
    private TextView author;
    private Spinner country;
    private EditText description;
    private ImageButton camera;

    private static final int PICK_FROM_CAMERA = 0;
    private static final int PICK_FROM_ALBUM = 1;
    private static final int PERMISSION_REQUEST_CODE = 1052;

    private ImageView mPhotoImageView;

    public AddRecipeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_add_recipe, container, false);

        // setUserID logic
        SharedPreferences pref = requireContext().getSharedPreferences("Login", Context.MODE_PRIVATE);
        String userID = pref.getString("userID", "");
        author = view.findViewById(R.id.txt_Add_Author);
        author.setText(userID);

        // Keyboard management
        imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        description = view.findViewById(R.id.txt_Add_Description);
        recipeName = view.findViewById(R.id.txt_Add_NewName);

        RelativeLayout root = view.findViewById(R.id.root_Add_Recipe);
        root.setOnClickListener(v -> {
            if (imm != null) {
                imm.hideSoftInputFromWindow(description.getWindowToken(), 0);
                imm.hideSoftInputFromWindow(recipeName.getWindowToken(), 0);
            }
        });

        camera = view.findViewById(R.id.btn_Add_Camera);
        mPhotoImageView = view.findViewById(R.id.imgv_Add_Image);

        checkPermissions();

        camera.setOnClickListener(v -> showImageSourceDialog());

        return view;
    }

    private void showImageSourceDialog() {
        String[] options = {"Take Photo", "Choose from Gallery", "Cancel"};
        new AlertDialog.Builder(requireContext())
                .setTitle("Select Image Source")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) doTakePhotoAction();
                    else if (which == 1) doTakeAlbumAction();
                    else dialog.dismiss();
                })
                .show();
    }

    private void doTakePhotoAction() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Note: For modern Android (7.0+), you'd typically need a FileProvider
        // to handle actual file URIs, but for small thumbnails, data.getExtras() works.
        startActivityForResult(intent, PICK_FROM_CAMERA);
    }

    private void doTakeAlbumAction() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_FROM_ALBUM);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == PICK_FROM_CAMERA) {
                Bundle extras = data.getExtras();
                if (extras != null) {
                    Bitmap photo = (Bitmap) extras.get("data");
                    if (photo != null) {
                        mPhotoImageView.setImageBitmap(imageHelper.resizeImage(photo));
                    }
                }
            } else if (requestCode == PICK_FROM_ALBUM) {
                Uri imageUri = data.getData();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), imageUri);
                    mPhotoImageView.setImageBitmap(imageHelper.resizeImage(bitmap));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void checkPermissions() {
        String[] permissions = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
        };

        // Note: WRITE_EXTERNAL_STORAGE is often not needed for simple gallery picking on newer Android
        ArrayList<String> listPermissionsNeeded = new ArrayList<>();
        for (String p : permissions) {
            if (ContextCompat.checkSelfPermission(requireContext(), p) != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p);
            }
        }

        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(requireActivity(),
                    listPermissionsNeeded.toArray(new String[0]), PERMISSION_REQUEST_CODE);
        }
    }
}