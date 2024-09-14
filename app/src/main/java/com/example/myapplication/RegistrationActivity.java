package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


public class RegistrationActivity extends AppCompatActivity {

    EditText editTextName, editTextEmail;
    RadioGroup radioGroupGender;
    Spinner spinnerCountry;
    Button buttonSubmit, buttonHome;
    DatabaseHelper myDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        // Initialize views
        editTextName = findViewById(R.id.editTextName);
        editTextEmail = findViewById(R.id.editTextEmail);
        radioGroupGender = findViewById(R.id.radioGroupGender);
        spinnerCountry = findViewById(R.id.spinnerCountry);
        buttonSubmit = findViewById(R.id.buttonSubmit);
        buttonHome = findViewById(R.id.buttonHome);

        // Initialize the database helper
        myDb = new DatabaseHelper(this);

        // Set up the spinner for country selection
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.countries_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCountry.setAdapter(adapter);

        // Submit button logic
        buttonSubmit.setOnClickListener(v -> {
            submitData();  // Handle form submission
        });

        // Home button logic
        buttonHome.setOnClickListener(v -> {
            // Navigate back to MainActivity
            startActivity(new Intent(RegistrationActivity.this, MainActivity.class));
        });
    }

    private void submitData() {
        // Get input values
        String name = editTextName.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim().toLowerCase();  // Convert email to lowercase

        // Validate name
        if (name.isEmpty()) {
            Toast.makeText(this, "Name input empty", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate email
        if (email.isEmpty()) {
            Toast.makeText(this, "Email input empty", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if email already exists in the database
        if (myDb.isEmailExists(email)) {
            Toast.makeText(this, "Email already exists", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get selected gender
        int selectedGenderId = radioGroupGender.getCheckedRadioButtonId();
        RadioButton radioButtonGender = findViewById(selectedGenderId);
        String gender = (radioButtonGender != null) ? radioButtonGender.getText().toString() : "Not specified";

        // Get selected country from spinner
        String country = spinnerCountry.getSelectedItem().toString();

        // Insert data into the database
        boolean isInserted = myDb.insertData(name, email, gender, country);

        // Display feedback to the user
        if (isInserted) {
            Toast.makeText(this, "Data saved successfully", Toast.LENGTH_SHORT).show();
            clearForm();  // Optional: clear form after successful submission
        } else {
            Toast.makeText(this, "Data not saved", Toast.LENGTH_SHORT).show();
        }
    }

    // Optional: Clears the form after successful submission
    private void clearForm() {
        editTextName.setText("");
        editTextEmail.setText("");
        radioGroupGender.clearCheck();
        spinnerCountry.setSelection(0);  // Set spinner to the first item
    }
}
