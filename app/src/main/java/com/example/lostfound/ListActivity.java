package com.example.lostfound;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.all_items_activity);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        DatabaseHelper dbHelper = new DatabaseHelper(this);
        List<ItemsPreview> itemList = dbHelper.getAllItems();

        if (itemList == null || itemList.isEmpty()) {
            Toast.makeText(this, "No items found", Toast.LENGTH_SHORT).show();
            return; // Exit if no items to display
        }

        ItemsAdapter adapter = new ItemsAdapter(itemList);
        adapter.setOnItemClickListener(position -> {
            ItemsPreview clickedItem = itemList.get(position);
            Intent intent = new Intent(ListActivity.this, DetailActivity.class);
            intent.putExtra("item_id", clickedItem.getId());
            startActivity(intent);
        });

        recyclerView.setAdapter(adapter);
    }
}
