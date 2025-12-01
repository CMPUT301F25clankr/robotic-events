package com.example.robotic_events_test;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * VIEW: Allows individuals to register to use the app. Users can choose to be Participants/Entrants
 * or Event Organizers. Users input a username, login email, and a password.
 */
public class SignupActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private EditText nameInput, emailInput, passwordInput, confirmPasswordInput;
    private RadioButton organizerRadio;
    private Button signupButton, backToLoginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        nameInput = findViewById(R.id.nameInput);
        emailInput = findViewById(R.id.signupEmailInput);
        passwordInput = findViewById(R.id.signupPasswordInput);
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput);
        organizerRadio = findViewById(R.id.organizerRadio);

        signupButton = findViewById(R.id.signupConfirmButton);
        backToLoginButton = findViewById(R.id.backToLoginButton);

        signupButton.setOnClickListener(v -> registerUser());
        backToLoginButton.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    // Handles all user registration and creates new users in the DB based on input field values.
    private void registerUser() {
        String name = nameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String confirm = confirmPasswordInput.getText().toString().trim();
        boolean isOrganizer = organizerRadio.isChecked();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirm)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {
                    String uid = result.getUser().getUid();

                    User user = new User(uid, name, email);
                    user.setOrganizer(isOrganizer);

                    db.collection("users").document(uid).set(user)
                            .addOnSuccessListener(unused -> {
                                // Save to SharedPreferences
                                SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
                                prefs.edit().putBoolean("isOrganizer", isOrganizer).apply();

                                Toast.makeText(this, "Account created!", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(this, MainActivity.class));
                                finish();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Firestore Error: " + e.getMessage(), Toast.LENGTH_LONG).show()
                            );

                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Signup Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }
}
