package com.hanson.android.recipe;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

public class AddHowtoFragment extends Fragment {

    // XML IDs ke mutabiq Variables
    private EditText txt_ADD_Howto;
    private TextView txt_step_counter;
    private Button btn_format_steps, btn_clear_steps, btn_save_steps;
    private ConstraintLayout root_Add_howto;
    private InputMethodManager imm;

    public AddHowtoFragment() {
        // Empty constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Layout inflate karna
        View view = inflater.inflate(R.layout.fragment_add_howto, container, false);

        // 1. IDs Initializing (Exactly as per your XML)
        txt_ADD_Howto = view.findViewById(R.id.txt_ADD_Howto);
        txt_step_counter = view.findViewById(R.id.txt_step_counter);
        btn_format_steps = view.findViewById(R.id.btn_format_steps);
        btn_clear_steps = view.findViewById(R.id.btn_clear_steps);
        btn_save_steps = view.findViewById(R.id.btn_save_steps);
        root_Add_howto = view.findViewById(R.id.root_Add_howto);

        // Keyboard hide karne ke liye service
        if (getActivity() != null) {
            imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        }

        // 2. Hide Keyboard Logic
        root_Add_howto.setOnClickListener(v -> {
            if (imm != null && txt_ADD_Howto != null) {
                imm.hideSoftInputFromWindow(txt_ADD_Howto.getWindowToken(), 0);
            }
        });

        // 3. Real-time Step Counter
        txt_ADD_Howto.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String text = s.toString().trim();
                if (text.isEmpty()) {
                    txt_step_counter.setText("0");
                } else {
                    String[] lines = text.split("\n+");
                    txt_step_counter.setText(String.valueOf(lines.length));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // 4. Buttons Click Listeners
        btn_format_steps.setOnClickListener(v -> formatMySteps());
        btn_clear_steps.setOnClickListener(v -> txt_ADD_Howto.setText(""));
        btn_save_steps.setOnClickListener(v -> {
            if (txt_ADD_Howto.getText().toString().trim().isEmpty()) {
                Toast.makeText(getContext(), "Pehle steps likhein!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Steps Saved!", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    // AddHowtoFragment ke andar ye method zaroor rakhen
    public String getHowToText() {
        return txt_ADD_Howto != null ? txt_ADD_Howto.getText().toString().trim() : "";
    }

    private void formatMySteps() {
        String input = txt_ADD_Howto.getText().toString().trim();
        if (input.isEmpty()) return;

        String[] lines = input.split("\n+");
        StringBuilder formattedText = new StringBuilder();

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            // Purane numbers remove karna
            line = line.replaceFirst("^[0-9]️⃣\\s*", "");
            line = line.replaceFirst("^[0-9]+\\.\\s*", "");

            formattedText.append(i + 1).append("️⃣ ").append(line);
            if (i < lines.length - 1) formattedText.append("\n");
        }

        txt_ADD_Howto.setText(formattedText.toString());
        txt_ADD_Howto.setSelection(txt_ADD_Howto.getText().length());
    }
}