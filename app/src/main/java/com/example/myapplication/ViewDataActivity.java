package com.example.myapplication;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class ViewDataActivity extends AppCompatActivity {

    ListView listViewStudents;
    Button buttonToggle, buttonPermanentDelete;
    DatabaseHelper myDb;
    ArrayList<String> studentList;
    ArrayList<Integer> studentIds;
    boolean showingDeleted = false; // To track which data is being shown

    @SuppressLint({"MissingInflatedId", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_data);

        listViewStudents = findViewById(R.id.listViewStudents);
        buttonToggle = findViewById(R.id.buttonToggle);
        buttonPermanentDelete = findViewById(R.id.buttonPermanentDelete);
        myDb = new DatabaseHelper(this);
        studentList = new ArrayList<>();
        studentIds = new ArrayList<>();

        loadStudentData();

        // Toggle between showing active and deleted users
        buttonToggle.setOnClickListener(v -> {
            showingDeleted = !showingDeleted;
            if (showingDeleted) {
                buttonToggle.setText("Show Active Users");
                buttonPermanentDelete.setVisibility(View.VISIBLE); // Show permanently delete button when viewing deleted users
                loadDeletedStudentData();
            } else {
                buttonToggle.setText("Show Deleted Users");
                buttonPermanentDelete.setVisibility(View.GONE); // Hide permanently delete button for active users
                loadStudentData();
            }
        });

        // Permanently delete button functionality
        buttonPermanentDelete.setOnClickListener(v -> showPermanentDeleteConfirmationDialog());
    }

    // Load active student data
    private void loadStudentData() {
        Cursor res = myDb.getAllData();
        studentList.clear();  // Clear the list to avoid old data showing up
        studentIds.clear();

        if (res.getCount() == 0) {
            Toast.makeText(this, "No active users found", Toast.LENGTH_SHORT).show();
            updateListView();
            return;
        }

        while (res.moveToNext()) {
            studentList.add(res.getString(1)); // Name at index 1
            studentIds.add(res.getInt(0)); // ID at index 0
        }

        updateListView();  // Update the ListView with the new data
    }

    // Load deleted student data
    private void loadDeletedStudentData() {
        try {
            Cursor res = myDb.getDeletedData();
            studentList.clear();  // Clear the list to avoid old data showing up
            studentIds.clear();

            if (res == null || res.getCount() == 0) {
                Toast.makeText(this, "No deleted users found", Toast.LENGTH_SHORT).show();
                updateListView();
                return;
            }

            while (res.moveToNext()) {
                studentList.add(res.getString(1)); // Name at index 1
                studentIds.add(res.getInt(0)); // ID at index 0
            }

            updateListView();
        } catch (Exception e) {
            Log.e("ViewDataActivity", "Error loading deleted students", e);
            Toast.makeText(this, "Error loading deleted students", Toast.LENGTH_LONG).show();
        }
    }

    // Update the ListView with data
    private void updateListView() {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.list_item, R.id.textViewName, studentList) {
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                if (convertView == null) {
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item, parent, false);
                }

                TextView textViewName = convertView.findViewById(R.id.textViewName);
                Button buttonDelete = convertView.findViewById(R.id.buttonDelete);
                Button buttonRestore = convertView.findViewById(R.id.buttonRestore);

                textViewName.setText(studentList.get(position));
                textViewName.setOnClickListener(v -> showStudentDetails(studentIds.get(position)));

                // Handle the visibility of Delete and Restore buttons
                if (showingDeleted) {
                    buttonDelete.setVisibility(View.GONE); // Hide delete button for deleted students
                    buttonRestore.setVisibility(View.VISIBLE); // Show restore button for deleted students
                    buttonRestore.setOnClickListener(v -> restoreStudent(studentIds.get(position), position));
                } else {
                    buttonDelete.setVisibility(View.VISIBLE); // Show delete button for active students
                    buttonRestore.setVisibility(View.GONE); // Hide restore button for active students
                    buttonDelete.setOnClickListener(v -> showDeleteConfirmationDialog(studentIds.get(position), position));
                }

                return convertView;
            }
        };

        listViewStudents.setAdapter(adapter);
    }


    // Restore a deleted student
    private void restoreStudent(int studentId, int position) {
        boolean isRestored = myDb.restoreData(studentId);
        if (isRestored) {
            studentList.remove(position);
            studentIds.remove(position);
            ((ArrayAdapter<?>) listViewStudents.getAdapter()).notifyDataSetChanged();
            Toast.makeText(ViewDataActivity.this, "Student restored", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(ViewDataActivity.this, "Restoration failed", Toast.LENGTH_SHORT).show();
        }
    }

    // Show student details
    private void showStudentDetails(int studentId) {
        Cursor cursor = myDb.getStudentData(studentId);
        if (cursor != null && cursor.moveToFirst()) {
            String name = cursor.getString(1);
            String email = cursor.getString(2);
            String gender = cursor.getString(3);
            String country = cursor.getString(4);
            cursor.close();

            // Show the student's details in an AlertDialog
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Student Details");
            builder.setMessage("Name: " + name + "\nEmail: " + email + "\nGender: " + gender + "\nCountry: " + country);
            builder.setPositiveButton("OK", null);
            builder.show();
        } else {
            Toast.makeText(this, "Student details not found", Toast.LENGTH_SHORT).show();
        }
    }

    // Show confirmation dialog for deletion
    private void showDeleteConfirmationDialog(int studentId, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Student")
                .setMessage("Are you sure you want to delete this student?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    boolean isDeleted = myDb.deleteData(studentId);  // Call the deleteData method
                    if (isDeleted) {
                        studentList.remove(position);
                        studentIds.remove(position);
                        ((ArrayAdapter<?>) listViewStudents.getAdapter()).notifyDataSetChanged();
                        Toast.makeText(ViewDataActivity.this, "Data deleted", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ViewDataActivity.this, "Deletion failed", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    // Show confirmation dialog for permanent deletion
    private void showPermanentDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Permanently Delete Students")
                .setMessage("Are you sure you want to permanently delete all the deleted students?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    boolean isPermanentlyDeleted = myDb.permanentlyDeleteData();
                    if (isPermanentlyDeleted) {
                        studentList.clear();
                        studentIds.clear();
                        ((ArrayAdapter<?>) listViewStudents.getAdapter()).notifyDataSetChanged();
                        Toast.makeText(ViewDataActivity.this, "All deleted students have been permanently removed", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ViewDataActivity.this, "Permanent deletion failed", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }
}
