package com.hanson.android.recipe;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.hanson.android.recipe.Model.UserLoginActivity;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawer;
    private BottomNavigationView bottomNav;
    private FloatingActionButton fabAdd;
    private SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Session Check (Agar login nahi hai toh Login Screen bhej dain)
        pref = getSharedPreferences("UserLogin", MODE_PRIVATE);
        boolean isLoggedIn = pref.getBoolean("isLoggedIn", false);
        if (!isLoggedIn) {
            startActivity(new Intent(this, UserLoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        // 2. Setup Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // 3. Setup Navigation Drawer
        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        // 4. Role-Based Access (Admin vs User)
        fabAdd = findViewById(R.id.fab_add);
        String role = pref.getString("userID", "user"); // Default 'user'
        if (role.equals("admin")) {
            fabAdd.setVisibility(View.VISIBLE);
            fabAdd.setOnClickListener(v -> {
                // Add Recipe Activity open karein (Aapki Activity ka naam yahan likhein)
                // startActivity(new Intent(MainActivity.this, AddRecipeActivity.class));
                Toast.makeText(this, "Admin: Add Recipe Opened", Toast.LENGTH_SHORT).show();
            });
        } else {
            fabAdd.setVisibility(View.GONE);
        }

        // 5. Setup Bottom Navigation
        bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) loadFragment(new HomeFragment());
            else if (id == R.id.nav_categories) loadFragment(new CategoryFragment());
            else if (id == R.id.nav_ai_assistant) loadFragment(new SearchFragment());
            return true;
        });

        // 6. Search Logic
        EditText etSearch = findViewById(R.id.et_search);
        if (etSearch != null) {
            etSearch.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    String query = etSearch.getText().toString();
                    performSearch(query);
                    return true;
                }
                return false;
            });
        }

        // Default Fragment Load
        if (savedInstanceState == null) {
            bottomNav.setSelectedItemId(R.id.nav_home);
        }
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    private void performSearch(String query) {
        // Search logic yahan likhein ya SearchFragment ko query bhej dain
        Toast.makeText(this, "Searching for: " + query, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_logout) {
            logoutUser();
        } else if (id == R.id.nav_profile) {
            // Profile logic
            Toast.makeText(this, "Profile Clicked", Toast.LENGTH_SHORT).show();
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void logoutUser() {
        SharedPreferences.Editor editor = pref.edit();
        editor.clear();
        editor.apply();

        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(MainActivity.this, UserLoginActivity.class));
        finish();
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}