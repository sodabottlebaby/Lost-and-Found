package com.example.lostfound;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AddActivity extends AppCompatActivity {

    private static final String TAG = "AddActivity";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    private static final int AUTOCOMPLETE_REQUEST_CODE = 1;

    private RadioGroup radioGroup;
    private EditText nameEditText, phoneEditText, descriptionEditText, locationEditText;
    private DatePicker datePicker;
    private double latitude, longitude;
    private FusedLocationProviderClient fusedLocationClient;
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_activity);

        initializeUI();

        // Initialize the Places SDK
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getString(R.string.google_maps_key));
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        executorService = Executors.newSingleThreadExecutor();

        findViewById(R.id.getCurrentLocationButton).setOnClickListener(v -> {
            if (checkLocationPermission()) {
                getCurrentLocation();
            } else {
                requestLocationPermission();
            }
        });

        locationEditText.setOnClickListener(v -> startAutocompleteActivity());
    }

    private void initializeUI() {
        radioGroup = findViewById(R.id.radioGroup);
        nameEditText = findViewById(R.id.nameEditText);
        phoneEditText = findViewById(R.id.phoneEditText);
        descriptionEditText = findViewById(R.id.descriptionEditText);
        datePicker = findViewById(R.id.datePicker);
        locationEditText = findViewById(R.id.locationEditText);

        findViewById(R.id.saveButton).setOnClickListener(view -> saveData());
    }

    // Initialize Retrofit
    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("https://maps.googleapis.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build();
    GeocodingService geocodingService = retrofit.create(GeocodingService.class);

    @SuppressLint("MissingPermission")
    private void getCurrentLocation() {
        Log.d(TAG, "Getting current location...");
        fusedLocationClient.getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                        Log.d(TAG, "Current location: " + latitude + ", " + longitude);
                        useGoogleGeocodingAPI(latitude, longitude);
                    } else {
                        Log.d(TAG, "Location is null");
                        Toast.makeText(this, "Unable to get current location", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to get location: " + e.getMessage());
                    Toast.makeText(this, "Failed to get current location", Toast.LENGTH_SHORT).show();
                });
    }

    private void useGoogleGeocodingAPI(double latitude, double longitude) {
        String latlng = latitude + "," + longitude;
        Log.d(TAG, "Calling Geocoding API for latlng: " + latlng);
        Call<GeocodingResponse> call = geocodingService.getGeocodingResult(latlng, getString(R.string.google_maps_key));
        call.enqueue(new Callback<GeocodingResponse>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onResponse(@NonNull Call<GeocodingResponse> call, @NonNull Response<GeocodingResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<GeocodingResponse.Result> results = response.body().results;
                    Log.d(TAG, "Geocoding results: " + results.toString());
                    if (!results.isEmpty()) {
                        String address = results.get(0).formattedAddress;
                        Log.d(TAG, "First address in results: " + address);
                        for (GeocodingResponse.Result result : results) {
                            Log.d(TAG, "Address: " + result.formattedAddress + ", Location: " + result.geometry.location.lat + ", " + result.geometry.location.lng);
                        }
                        if (address != null) {
                            runOnUiThread(() -> updateLocationTextBox(address));
                        } else {
                            Log.d(TAG, "Formatted address is null");
                            runOnUiThread(() -> {
                                Toast.makeText(AddActivity.this, "Unable to get address for the location", Toast.LENGTH_SHORT).show();
                                locationEditText.setText("Unable to get address for the location");
                            });
                        }
                    } else {
                        Log.d(TAG, "Geocoding results are empty");
                        runOnUiThread(() -> {
                            Toast.makeText(AddActivity.this, "Unable to get address for the location", Toast.LENGTH_SHORT).show();
                            locationEditText.setText("Unable to get address for the location");
                        });
                    }
                } else {
                    try {
                        String errorBody = response.errorBody().string();
                        Log.d(TAG, "Failed to get address: " + errorBody);
                        // Log the raw JSON response for further analysis
                        Log.d(TAG, "Raw JSON response: " + response.raw().toString());
                        runOnUiThread(() -> {
                            Toast.makeText(AddActivity.this, "Failed to get address", Toast.LENGTH_SHORT).show();
                            locationEditText.setText("Failed to get address");
                        });
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing error body", e);
                    }
                }
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onFailure(@NonNull Call<GeocodingResponse> call, @NonNull Throwable t) {
                Log.d(TAG, "Geocoding API failure: " + t.getMessage(), t);
                runOnUiThread(() -> {
                    Toast.makeText(AddActivity.this, "Error getting address: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    locationEditText.setText("Error getting address");
                });
            }
        });
    }

    private void updateLocationTextBox(String address) {
        Log.d(TAG, "Updating location text box with address: " + address);
        runOnUiThread(() -> locationEditText.setText(address));
    }

    private void startAutocompleteActivity() {
        // Set the fields to specify which types of place data to return.
        List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG);

        // Start the autocomplete intent.
        Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                .build(this);
        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(Objects.requireNonNull(data));
                Log.d(TAG, "Place: " + place.getName() + ", " + place.getAddress() + ", " + place.getLatLng());
                locationEditText.setText(place.getAddress()); // Use getAddress instead of getName
                LatLng latLng = place.getLatLng();
                if (latLng != null) {
                    latitude = latLng.latitude;
                    longitude = latLng.longitude;
                }
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                com.google.android.gms.common.api.Status status = Autocomplete.getStatusFromIntent(Objects.requireNonNull(data));
                Log.i(TAG, Objects.requireNonNull(status.getStatusMessage()));
            } else if (resultCode == RESULT_CANCELED) {
                Log.i(TAG, "Autocomplete canceled by user.");
                Toast.makeText(this, "Autocomplete canceled.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveData() {
        if (radioGroup.getCheckedRadioButtonId() == -1 || nameEditText.getText().toString().isEmpty() || phoneEditText.getText().toString().isEmpty() || locationEditText.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        String advertType = ((RadioButton) findViewById(radioGroup.getCheckedRadioButtonId())).getText().toString();
        String name = nameEditText.getText().toString();
        String phone = phoneEditText.getText().toString();
        String description = descriptionEditText.getText().toString();
        String date = datePicker.getDayOfMonth() + "/" + (datePicker.getMonth() + 1) + "/" + datePicker.getYear();
        String location = locationEditText.getText().toString();

        new Thread(() -> {
            DatabaseHelper dbHelper = new DatabaseHelper(AddActivity.this);
            long newTaskId = dbHelper.addAdvert(advertType, name, phone, description, date, location, latitude, longitude);
            runOnUiThread(() -> {
                if (newTaskId != -1) {
                    Toast.makeText(this, "Task added with ID: " + newTaskId, Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(this, "Error adding task", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    private boolean checkLocationPermission() {
        boolean granted = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        Log.d(TAG, "Location permission granted: " + granted);
        return granted;
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}
