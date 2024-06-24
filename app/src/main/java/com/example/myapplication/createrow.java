package com.example.myapplication;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;import java.util.Objects;

public class createrow extends AppCompatActivity {

    EditText editText;
    EditText editText2;
    FloatingActionButton button;
    FirebaseAuth mAuth;
    FirebaseUser firebaseUser;
    FirebaseFirestore firestore;
    Toolbar toolbar;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_createrow);

        editText = findViewById(R.id.editTextText1);
        editText2 = findViewById(R.id.editTextText2);
        button = findViewById(R.id.floatingActionButton);
        toolbar = findViewById(R.id.toolbar2);

        mAuth = FirebaseAuth.getInstance();
        firebaseUser = mAuth.getCurrentUser();
        firestore = FirebaseFirestore.getInstance();

        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        button.setOnClickListener(view -> {
            String text1 = editText.getText().toString().trim(); // Trim whitespace
            String text2 = editText2.getText().toString().trim();

            if (text1.isEmpty() || text2.isEmpty()) {
                Toast.makeText(createrow.this, "Please enter both fields", Toast.LENGTH_SHORT).show();
            } else if (text1.length() < 3) { // Example minimum length validation
                Toast.makeText(createrow.this, "Title should be at least 3 characters long", Toast.LENGTH_SHORT).show();
            } else {
                DocumentReference documentReference = firestore.collection("Notes")
                        .document(firebaseUser.getUid())
                        .collection("MyNotes")
                        .document();

                Map<String, Object> note = new HashMap<>();
                note.put("Title", text1);
                note.put("content", text2);


                documentReference.set(note)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(createrow.this, "Note saved", Toast.LENGTH_SHORT).show();
                            finish(); // Close createrow activity
                        }).addOnFailureListener(e -> {
                            Toast.makeText(createrow.this, "Failed to save note: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            Log.e("createrow", "Error saving note", e); // Log the error
                        });
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}