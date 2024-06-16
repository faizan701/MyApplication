package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private EditText emailInput;
    private EditText passwordInput;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Set the correct layout

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // User is already logged in, redirect to LandingPage
            Intent intent = new Intent(MainActivity.this, LandingPage.class);
            startActivity(intent);
            finish(); // Optional: Finish the current activity
        }

        emailInput = findViewById(R.id.editTextText1);
        passwordInput = findViewById(R.id.editTextTextPassword1);
        TextView forgotPasswordTextView = findViewById(R.id.textView2);
        Button loginButton = findViewById(R.id.button);
        Button signUpButton = findViewById(R.id.button2);

        forgotPasswordTextView.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, forgotpass.class);
            startActivity(intent);
        });

        loginButton.setOnClickListener(view -> {
            String email = emailInput.getText().toString();
            String password = passwordInput.getText().toString();

            if (isFieldEmpty(emailInput)) {
                emailInput.setError("Email cannot be empty");
            } else if (isFieldEmpty(passwordInput)) {
                passwordInput.setError("Password cannot be empty");
            } else {
                performLogin(email, password);
            }
        });

        signUpButton.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, sign_up.class);
            startActivity(intent);
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return WindowInsetsCompat.CONSUMED;
        });
    }

    private boolean isFieldEmpty(EditText editText) {
        return editText.getText().toString().trim().isEmpty();
    }

    private void performLogin(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null && user.isEmailVerified()) {
                            Toast.makeText(MainActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(MainActivity.this, LandingPage.class);
                            startActivity(intent);
                            finish(); // Optional: Finish the login activity
                        } else {
                            // User's email is not verified
                            Toast.makeText(MainActivity.this, "Please verify your email before logging in.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // If sign in fails, display a message to the user.
                        Toast.makeText(MainActivity.this, "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}