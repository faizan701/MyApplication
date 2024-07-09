package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class createrow extends AppCompatActivity {

    EditText editTextTitle;
    EditText editTextContent;
    FloatingActionButton buttonSave;
    FirebaseAuth mAuth;
    FirebaseUser firebaseUser;
    FirebaseFirestore firestore;
    Toolbar toolbar;
    TextView textView5;
    private String documentId; // To store the document ID for editing

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_createrow);

        mAuth = FirebaseAuth.getInstance();
        firebaseUser = mAuth.getCurrentUser();

        if (firebaseUser == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        firestore = FirebaseFirestore.getInstance();

        editTextTitle = findViewById(R.id.editTextText1);
        editTextContent = findViewById(R.id.editTextText2);
        buttonSave = findViewById(R.id.floatingActionButton);
        toolbar = findViewById(R.id.toolbar2);
        textView5 = findViewById(R.id.textView5);

        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Check if intent has data (for editing)
        Intent intent = getIntent();
        if (intent.hasExtra("documentId")) {
            documentId = intent.getStringExtra("documentId");
            loadNoteData(documentId); // Load existing note data
        }

        // Add TextWatcher to editTextTitle to update sum
        editTextTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not used in this case
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Not used in this case
            }

            @Override
            public void afterTextChanged(Editable s) {
                int sum = calculateSumOfNumbers(s.toString());
                if (getNumberOfNumbers(s.toString()) >= 2) {
                    textView5.setText("Sum : " + sum);
                    textView5.setVisibility(TextView.VISIBLE);
                } else {
                    textView5.setVisibility(TextView.GONE);
                }
            }
        });

        buttonSave.setOnClickListener(view -> {
            String title = editTextTitle.getText().toString().trim();
            String content = editTextContent.getText().toString().trim();

            if (title.isEmpty() || content.isEmpty()) {
                Toast.makeText(createrow.this, "Please enter both fields", Toast.LENGTH_SHORT).show();
            } else if (title.length() < 3) {
                Toast.makeText(createrow.this, "Title should be at least 3 characters long", Toast.LENGTH_SHORT).show();
            } else {
                if (documentId != null) {
                    // Update existing note
                    updateNote(documentId, title, content);
                } else {
                    // Create new note
                    createNewNote(title, content);
                }
            }
        });
    }

    private void loadNoteData(String documentId) {
        firestore.collection("Notes")
                .document(firebaseUser.getUid())
                .collection("MyNotes")
                .document(documentId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        FireBaseModel note = documentSnapshot.toObject(FireBaseModel.class);
                        if (note != null) {
                            editTextTitle.setText(note.getTitle());
                            editTextContent.setText(note.getContent());
                            int sum = calculateSumOfNumbers(note.getTitle());
                            textView5.setText("Sum of numbers: " + sum);
                        }
                    } else {
                        Toast.makeText(createrow.this, "Note not found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(createrow.this, "Error loading note: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("createrow", "Error loading note", e);
                    finish();
                });
    }

    private void updateNote(String documentId, String title, String content) {
        DocumentReference documentReference = firestore.collection("Notes")
                .document(firebaseUser.getUid())
                .collection("MyNotes")
                .document(documentId);

        Map<String, Object> note = new HashMap<>();
        note.put("Title", title);
        note.put("Content", content);

        documentReference.update(note)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(createrow.this, "Note updated", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(createrow.this, "Failed to update note: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("createrow", "Error updating note", e);
                });
    }

    private void createNewNote(String title, String content) {
        DocumentReference documentReference = firestore.collection("Notes")
                .document(firebaseUser.getUid())
                .collection("MyNotes")
                .document();

        Map<String, Object> note = new HashMap<>();
        note.put("Title", title);
        note.put("Content", content);

        documentReference.set(note)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(createrow.this, "Note saved", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(createrow.this, "Failed to save note: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("createrow", "Error saving note", e);
                });
    }

    private int calculateSumOfNumbers(String text) {
        int sum = 0;
        String[] words = text.split("\\s+"); // Split by whitespace
        for (String word : words) {
            try {
                int number = Integer.parseInt(word);
                sum += number;
            } catch (NumberFormatException ignored) {
                // Ignore non-number words
            }
        }
        return sum;
    }

    private int getNumberOfNumbers(String text) {
        int count = 0;
        String[] words = text.split("\\s+");
        for (String word : words) {
            try {
                Integer.parseInt(word);
                count++;
            } catch (NumberFormatException ignored) {
                // Ignore non-number words
            }
        }
        return count;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // Handle the back button press
            String title = editTextTitle.getText().toString().trim();
            String content = editTextContent.getText().toString().trim();

            if (title.isEmpty() && content.isEmpty()) {
                // Both title and content are empty, just finish the activity
                finish();
            } else {
                if (title.isEmpty()) {
                    Toast.makeText(createrow.this, "Please enter Title", Toast.LENGTH_SHORT).show();
                } else if (title.length() < 3) {
                    Toast.makeText(createrow.this, "Title should be at least 3 characters long", Toast.LENGTH_SHORT).show();
                } else {
                    if (documentId != null) {
                        // Update existing note
                        updateNote(documentId, title, content);
                    } else {
                        // Create new note
                        createNewNote(title, content);
                    }
                }
            }
            return true;
        } else if (item.getItemId() == R.id.action_logout) {
            // Handle the logout action
            mAuth.signOut();
            Intent intent = new Intent(this, LandingPage.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
