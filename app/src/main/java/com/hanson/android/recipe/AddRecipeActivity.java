package com.hanson.android.recipe;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
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
    private ImageView navi1, navi2, navi3;
    private ImageHelper imageHelper = new ImageHelper();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_recipe);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("Add New Recipe");
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
            public void onPageScrolled(int pos, float offset, int offsetPx) {
            }
            @Override
            public void onPageSelected(int position) {
                updateNavigationDots(position);
            }
            @Override
            public void onPageScrollStateChanged(int state) {}
        });

        btn_addNewRecipe = findViewById(R.id.btn_Add_recipeAdd);
        btn_addNewRecipe.setOnClickListener(v -> performSave());
    }

    private void updateNavigationDots(int position) {
        navi1.setColorFilter(ContextCompat.getColor(this, position == 0 ? R.color.red_primary : R.color.medium_gray));
        navi2.setColorFilter(ContextCompat.getColor(this, position == 1 ? R.color.red_primary : R.color.medium_gray));
        navi3.setColorFilter(ContextCompat.getColor(this, position == 2 ? R.color.red_primary : R.color.medium_gray));
    }

    private void performSave() {
        // Fragments ke instance lein
        AddRecipeFragment step1 = (AddRecipeFragment) mSectionsPagerAdapter.getItem(0);
        AddIngredientFragment step2 = (AddIngredientFragment) mSectionsPagerAdapter.getItem(1);
        AddHowtoFragment step3 = (AddHowtoFragment) mSectionsPagerAdapter.getItem(2);

        try {
            // Data Extract karna (Ye methods niche fragments mein add kiye gaye hain)
            String name = step1.getRecipeName();
            String category = step1.getCategory();
            String desc = step1.getDescription();
            Bitmap bitmap = step1.getRecipeImage();
            ArrayList<String> ingredients = step2.getIngredientList();
            String howto = step3.getHowToText();

            // Validations
            if (name.isEmpty()) {
                Toast.makeText(this, "Enter Recipe Name!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (bitmap == null) {
                Toast.makeText(this, "Select a Picture!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (ingredients.isEmpty()) {
                Toast.makeText(this, "Add at least one Ingredient!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (howto.isEmpty()) {
                Toast.makeText(this, "Enter Cooking Steps!", Toast.LENGTH_SHORT).show();
                return;
            }

            // DB Process
            byte[] mainImg = imageHelper.getByteArrayFromBitmap(bitmap);
            byte[] thumbImg = imageHelper.getByteArrayFromBitmap(imageHelper.getThubmail(bitmap));

            DBHelper dbHelper = new DBHelper(this, "Recipes.db", null, 1);
            dbHelper.recipes_Insert(category, name, "Chef", new Date().toString(), howto, desc, thumbImg, mainImg, 0);

            int recipeId = dbHelper.recipes_GetIdByName(name);
            for (String ing : ingredients) {
                dbHelper.ingredients_Insert(recipeId, ing);
            }

            Toast.makeText(this, "Recipe Saved!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, MainActivity.class));
            finish();

        } catch (Exception e) {
            Toast.makeText(this, "Save Failed. Check all steps.", Toast.LENGTH_SHORT).show();
        }
    }

    public void moveToNextStep(int index) {
        if (mViewPager != null) {
            mViewPager.setCurrentItem(index);
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

        public void add(Fragment f) {
            _fragments.add(f);
        }

        @Override
        public Fragment getItem(int pos) {
            return _fragments.get(pos);
        }

        @Override
        public int getCount() {
            return _fragments.size();
        }
    }
}