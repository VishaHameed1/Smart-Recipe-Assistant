package com.hanson.android.recipe;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawer;
    private ActionBarDrawerToggle toggle;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        // Drawer Toggle Setup
        toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                updateNavigationMenu(); // Drawer khulne par menu update karein
            }
        };

        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);

        // Default Fragment Load karein (Home)
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment(), "Home");
            navigationView.setCheckedItem(R.id.nav_home);
        }
    }

    // Ek common method fragments load karne ke liye
    private void loadFragment(Fragment fragment, String title) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.root_layout, fragment)
                .commit();
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }
    }

    private void updateNavigationMenu() {
        Menu navMenu = navigationView.getMenu();

        SharedPreferences pref = getSharedPreferences("UserLogin", MODE_PRIVATE);
        boolean isLoggedIn = pref.getBoolean("isLoggedIn", false);
        String userID = pref.getString("userID", "");

        View headerView = navigationView.getHeaderView(0);
        TextView txtMenuUser = headerView.findViewById(R.id.txt_Menu_UserId);

        // Common items jo hamesha dikhenge
        navMenu.findItem(R.id.nav_home).setVisible(true);
        navMenu.findItem(R.id.nav_category).setVisible(true);
        navMenu.findItem(R.id.nav_search).setVisible(true);
        navMenu.findItem(R.id.nav_info).setVisible(true);

        // Login status ke hisab se items hide/show karein
        navMenu.findItem(R.id.nav_admin_login).setVisible(!isLoggedIn);
        navMenu.findItem(R.id.nav_user_login).setVisible(!isLoggedIn);
        navMenu.findItem(R.id.nav_logout).setVisible(isLoggedIn);

        // Admin vs User logic
        if (isLoggedIn) {
            boolean isAdmin = userID.equalsIgnoreCase("admin");
            navMenu.findItem(R.id.nav_add).setVisible(isAdmin);
            navMenu.findItem(R.id.nav_saved_ai).setVisible(!isAdmin);

            if (txtMenuUser != null) {
                txtMenuUser.setText(isAdmin ? "Admin: Visha" : "User: " + userID);
            }
        } else {
            navMenu.findItem(R.id.nav_add).setVisible(false);
            navMenu.findItem(R.id.nav_saved_ai).setVisible(false);
            if (txtMenuUser != null) txtMenuUser.setText("Guest User");
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        // Fragment transitions
        if (id == R.id.nav_home) {
            loadFragment(new HomeFragment(), "Recipe Home");
        } else if (id == R.id.nav_category) {
            loadFragment(new CategoryFragment(), "Categories");
        } else if (id == R.id.nav_search) {
            loadFragment(new SearchFragment(), "Search Recipes");
        } else if (id == R.id.nav_info) {
            loadFragment(new InformationFragment(), "About App");
        }

        // Activity transitions (Intents)
        else if (id == R.id.nav_admin_login) {
            startActivity(new Intent(this, LoginActivity.class));
        } else if (id == R.id.nav_user_login) {
            startActivity(new Intent(this, com.hanson.android.recipe.UserLoginActivity.class));
        } else if (id == R.id.nav_saved_ai) {
            startActivity(new Intent(this, SavedAiRecipesActivity.class));
        } else if (id == R.id.nav_add) {
            startActivity(new Intent(this, AddRecipeActivity.class));
        } else if (id == R.id.nav_logout) {
            performLogout();
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void performLogout() {
        getSharedPreferences("UserLogin", MODE_PRIVATE).edit().clear().apply();
        Toast.makeText(this, "Logged Out Successfully!", Toast.LENGTH_SHORT).show();
        updateNavigationMenu();
        loadFragment(new HomeFragment(), "Recipe Home");
        navigationView.setCheckedItem(R.id.nav_home);
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            // Agar home ke ilawa kisi aur fragment par hain toh home par wapis jayein
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.root_layout);
            if (!(currentFragment instanceof HomeFragment)) {
                loadFragment(new HomeFragment(), "Recipe Home");
                navigationView.setCheckedItem(R.id.nav_home);
            } else {
                super.onBackPressed();
            }
        }
    }
}