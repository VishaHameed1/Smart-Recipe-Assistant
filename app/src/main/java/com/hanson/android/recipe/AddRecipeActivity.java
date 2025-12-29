package com.hanson.android.recipe;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.hanson.android.recipe.Helper.DBHelper;
import com.hanson.android.recipe.Helper.ImageHelper;

import java.util.ArrayList;
import java.util.Date;

public class AddRecipeActivity extends AppCompatActivity {

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;

    private Button btn_addNewRecipe;
    private EditText newName;
    private TextView newAuthor;
    private Spinner newCounty;
    private ImageView newMainImg;
    private EditText newDescription;
    private ListView newIngredientList;
    private EditText newHowto;

    private ImageView navi1, navi2, navi3;

    ImageHelper imageHelper = new ImageHelper();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_recipe);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }

        navi1 = findViewById(R.id.imgv_Add_navi1);
        navi2 = findViewById(R.id.imgv_Add_navi2);
        navi3 = findViewById(R.id.imgv_Add_navi3);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mSectionsPagerAdapter.add(new AddRecipeFragment());
        mSectionsPagerAdapter.add(new AddIngredientFragment());
        mSectionsPagerAdapter.add(new AddHowtoFragment());

        mViewPager = findViewById(R.id.container);
        mViewPager.setOffscreenPageLimit(3);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            @Override
            public void onPageSelected(int position) {
                updateNavigationDots(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {}
        });

        btn_addNewRecipe = findViewById(R.id.btn_Add_recipeAdd);
        btn_addNewRecipe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performSave(v);
            }
        });
    }

    private void updateNavigationDots(int position) {
        navi1.setImageDrawable(ContextCompat.getDrawable(this, position == 0 ? R.drawable.greenbutton : R.drawable.graybackground));
        navi2.setImageDrawable(ContextCompat.getDrawable(this, position == 1 ? R.drawable.greenbutton : R.drawable.graybackground));
        navi3.setImageDrawable(ContextCompat.getDrawable(this, position == 2 ? R.drawable.greenbutton : R.drawable.graybackground));
    }

    private void performSave(View v) {
        // Initialize views
        newMainImg = findViewById(R.id.imgv_Add_Image);
        newName = findViewById(R.id.txt_Add_NewName);
        newAuthor = findViewById(R.id.txt_Add_Author);
        newCounty = findViewById(R.id.spinner_Add_Country);
        newDescription = findViewById(R.id.txt_Add_Description);
        newIngredientList = findViewById(R.id.ListView_Add_Ingredient);
        newHowto = findViewById(R.id.txt_ADD_Howto);

        byte[] makeMainImg;
        byte[] makeThumbnail;
        String makeRecipeName;
        String makeDescription;
        String makeAuthor;
        String makeCategory;
        String makeHowto;
        Date today = new Date();
        ArrayList<String> makeIndeList = new ArrayList<>();

        // Image Validation
        if (newMainImg != null && newMainImg.getDrawable() != null) {
            BitmapDrawable d = (BitmapDrawable) newMainImg.getDrawable();
            Bitmap bitmap = d.getBitmap();
            Bitmap thBitmap = imageHelper.getThubmail(bitmap);
            makeMainImg = imageHelper.getByteArrayFromBitmap(bitmap);
            makeThumbnail = imageHelper.getByteArrayFromBitmap(thBitmap);
        } else {
            Toast.makeText(v.getContext(), "Please, pick your picture!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Name Validation
        if (newName != null && newName.getText() != null && !newName.getText().toString().isEmpty()) {
            makeRecipeName = newName.getText().toString();
        } else {
            Toast.makeText(v.getContext(), "Please, input new recipe name!", Toast.LENGTH_SHORT).show();
            return;
        }

        makeAuthor = (newAuthor != null) ? newAuthor.getText().toString() : "Unknown";

        // Category Validation
        if (newCounty != null && newCounty.getSelectedItem() != null) {
            makeCategory = newCounty.getSelectedItem().toString();
        } else {
            Toast.makeText(v.getContext(), "Please, select the Country!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Description Validation
        if (newDescription != null && newDescription.getText() != null && !newDescription.getText().toString().isEmpty()) {
            makeDescription = newDescription.getText().toString();
        } else {
            Toast.makeText(v.getContext(), "Please, input new description!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Ingredients Validation
        if (newIngredientList != null && newIngredientList.getCount() > 0) {
            for (int i = 0; i < newIngredientList.getCount(); i++) {
                makeIndeList.add(newIngredientList.getItemAtPosition(i).toString());
            }
        } else {
            Toast.makeText(v.getContext(), "Please, input your ingredients!", Toast.LENGTH_SHORT).show();
            return;
        }

        // HowTo Validation
        if (newHowto != null && newHowto.getText() != null && !newHowto.getText().toString().isEmpty()) {
            makeHowto = newHowto.getText().toString();
        } else {
            Toast.makeText(v.getContext(), "Please, input how to cook this recipe!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Database Insertion
        DBHelper dbHelper = new DBHelper(v.getContext(), "Recipes.db", null, 1);
        dbHelper.recipes_Insert(makeCategory, makeRecipeName, makeAuthor, today.toString(),
                makeHowto, makeDescription, makeThumbnail, makeMainImg, 0);

        int makeRecipeid = dbHelper.recipes_GetIdByName(makeRecipeName);
        if (makeRecipeid != -1) {
            for (int i = 0; i < makeIndeList.size(); i++) {
                dbHelper.ingredients_Insert(makeRecipeid, makeIndeList.get(i));
            }
            Toast.makeText(v.getContext(), "Completed to add your recipe!!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(v.getContext(), MainActivity.class);
            startActivity(intent);
            finish(); // Close this activity
        } else {
            Toast.makeText(v.getContext(), "Upload Failed ", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static class SectionsPagerAdapter extends FragmentPagerAdapter {
        private final ArrayList<Fragment> _fragments = new ArrayList<>();

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        public void add(Fragment fragment) {
            _fragments.add(fragment);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return _fragments.get(position);
        }

        @Override
        public int getCount() {
            return _fragments.size();
        }
    }
}