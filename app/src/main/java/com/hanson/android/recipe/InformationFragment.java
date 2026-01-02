package com.hanson.android.recipe;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class InformationFragment extends Fragment {

    public InformationFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_information, container, false);

        // Social Media & Contact Click Listeners
        setupContactButtons(view);

        return view;
    }

    private void setupContactButtons(View view) {
        // IDs are inferred from your XML structure (Order in footer)
        // Note: You should ideally add IDs to these ImageViews in XML
        // Example: android:id="@+id/img_email"

        // Agar aapne XML mein IDs nahi di, toh aap findViewWithTag ya position use kar sakte hain
        // Behtar hai ke aap XML mein ye IDs add kar dein:

        ImageView emailBtn = view.findViewById(R.id.ic_email_btn); // Add this ID in XML
        ImageView githubBtn = view.findViewById(R.id.ic_github_btn); // Add this ID in XML

        if (emailBtn != null) {
            emailBtn.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:visha@example.com"));
                intent.putExtra(Intent.EXTRA_SUBJECT, "Recipe Master Feedback");
                startActivity(Intent.createChooser(intent, "Send Email"));
            });
        }

        if (githubBtn != null) {
            githubBtn.setOnClickListener(v -> {
                Uri webpage = Uri.parse("https://github.com/visha-hameed");
                Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
                startActivity(intent);
            });
        }
    }
}