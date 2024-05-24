package com.example.lostfound;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG = "MapActivity";
    private GoogleMap mMap;
    private List<ItemsPreview> items;
    private GeocodingService geocodingService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_activity);

        // Initialize Retrofit for Geocoding API
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://maps.googleapis.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        geocodingService = retrofit.create(GeocodingService.class);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        } else {
            Log.e(TAG, "MapFragment is null!");
        }

        items = getLostFoundItems();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        if (items != null) {
            for (ItemsPreview item : items) {
                if (item.getLatitude() == 0 && item.getLongitude() == 0) {
                    geocodeAddress(item);
                } else {
                    addMarker(item);
                }
            }
        } else {
            Log.e(TAG, "Items list is null!");
        }

        // Move the camera to a default location
        LatLng defaultLocation = new LatLng(-34, 151);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 10f));

        // Set a custom info window adapter
        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter());

        // Handle info window clicks
        mMap.setOnInfoWindowClickListener(marker -> {
            ItemsPreview item = (ItemsPreview) marker.getTag();
            if (item != null) {
                Intent intent = new Intent(MapActivity.this, DetailActivity.class);
                intent.putExtra("item_id", item.getId());
                startActivity(intent);
            } else {
                Log.e(TAG, "Marker tag is null!");
            }
        });
    }

    private void geocodeAddress(ItemsPreview item) {
        Call<GeocodingResponse> call = geocodingService.getGeocodingResult(item.getLocation(), getString(R.string.google_maps_key));
        call.enqueue(new Callback<GeocodingResponse>() {
            @Override
            public void onResponse(@NonNull Call<GeocodingResponse> call, @NonNull Response<GeocodingResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<GeocodingResponse.Result> results = response.body().results;
                    if (!results.isEmpty()) {
                        GeocodingResponse.Result result = results.get(0);
                        item.setLatitude(result.geometry.location.lat);
                        item.setLongitude(result.geometry.location.lng);
                        addMarker(item);
                    } else {
                        Log.e(TAG, "Geocoding results are empty!");
                    }
                } else {
                    Log.e(TAG, "Geocoding response unsuccessful!");
                }
            }

            @Override
            public void onFailure(@NonNull Call<GeocodingResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "Geocoding request failed!", t);
            }
        });
    }

    private void addMarker(ItemsPreview item) {
        LatLng position = new LatLng(item.getLatitude(), item.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions().position(position)
                .title(item.getName())
                .snippet(item.getAdvertType());
        Marker marker = mMap.addMarker(markerOptions);
        if (marker != null) {
            marker.setTag(item);
        } else {
            Log.e(TAG, "Marker is null for item: " + item.getName());
        }
    }

    private List<ItemsPreview> getLostFoundItems() {
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        return dbHelper.getAllItems();
    }

    class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {
        @Override
        public View getInfoWindow(@NonNull Marker marker) {
            return null;
        }

        @Override
        public View getInfoContents(@NonNull Marker marker) {
            View view = getLayoutInflater().inflate(R.layout.custom_info_window, null);
            TextView type = view.findViewById(R.id.type);
            TextView snippet = view.findViewById(R.id.snippet);
            TextView name = view.findViewById(R.id.name);

            type.setText(marker.getTitle());
            snippet.setText(marker.getSnippet());
            ItemsPreview item = (ItemsPreview) marker.getTag();
            if (item != null) {
                name.setText(item.getName());
            } else {
                Log.e(TAG, "Item is null for marker: " + marker.getTitle());
            }

            return view;
        }
    }
}
