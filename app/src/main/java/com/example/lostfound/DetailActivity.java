package com.example.lostfound;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

public class DetailActivity extends AppCompatActivity {
    private TextView idTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail_activity);

        DatabaseHelper dbHelper = new DatabaseHelper(this);

        // Assuming TextView IDs from XML are correctly referenced
        idTextView = findViewById(R.id.idTextView);
        TextView postType = findViewById(R.id.postType);
        TextView name = findViewById(R.id.name);
        TextView phone = findViewById(R.id.phone);
        TextView description = findViewById(R.id.description);
        TextView date = findViewById(R.id.date);
        TextView location = findViewById(R.id.location);

        Intent intent = getIntent();
        int itemId = intent.getIntExtra("item_id", -1);

        if (itemId == -1) {
            Toast.makeText(this, "Invalid item ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        idTextView.setText(String.valueOf(itemId));

        // Retrieve the item details using the itemId
        LostFoundItems item = dbHelper.getItemById(itemId);
        if (item == null) {
            Toast.makeText(this, "Item not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Set the text views with item details
        postType.setText(item.getAdvertType());
        name.setText(item.getName());
        phone.setText(item.getPhone());
        description.setText(item.getDescription());
        date.setText(item.getDate());
        location.setText(item.getLocation());

        Button deleteButton = findViewById(R.id.deleteButton);

        // Set the click event for the remove button
        deleteButton.setOnClickListener(v -> {
            // Get the item ID from the hidden TextView and delete the item from the database
            int idToDelete = Integer.parseInt(idTextView.getText().toString());
            dbHelper.deleteItem(idToDelete);

            Toast.makeText(this, "Item deleted", Toast.LENGTH_SHORT).show();

            // After deletion, go back to the main activity
            Intent backToMain = new Intent(this, MainActivity.class);
            startActivity(backToMain); // Start the MainActivity
            finish(); // Close the current activity
        });
    }
}