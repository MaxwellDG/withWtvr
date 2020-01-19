package com.example.myapplication.the_near_me;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.myapplication.homePage;
import com.example.myapplication.profile_database.Database;
import com.example.myapplication.the_profile.ProfilePage;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.mancj.materialsearchbar.adapter.SuggestionsAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "TAG";
    private GoogleMap mMap;

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    public static final int LOCATION_PERMISSIONS_REQUEST_CODE = 9001;
    private static final float STANDARD_ZOOM = 15f;

    private FusedLocationProviderClient fusedLocationProviderClient;
    private OkHttpClient okHttpClient;

    private boolean isLocationEnabled = false;
    private String username;

    private ImageView giveMeIdeasButton;
    private LatLng currentLocation;
    private LatLng selectedLocation;
    private List<AutocompletePrediction> predictionList;
    private PlacesClient placesClient;
    private Database database;
    private MaterialSearchBar materialSearchBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        okHttpClient = new OkHttpClient();

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        materialSearchBar = findViewById(R.id.searchBar);
        materialSearchBar.setHint("Input location: ");
        materialSearchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
            @Override
            public void onSearchStateChanged(boolean enabled) {
                String s = enabled ? "enabled" : "disabled";
                Toast.makeText(MapsActivity.this, "Search " + s, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSearchConfirmed(CharSequence text) {
                startSearch(text.toString(), true, null, true);
            }

            @Override
            public void onButtonClicked(int buttonCode) {
                switch (buttonCode){
                    case MaterialSearchBar.BUTTON_BACK:
                        materialSearchBar.disableSearch();
                        break;
                    case MaterialSearchBar.BUTTON_NAVIGATION:
                        Toast.makeText(MapsActivity.this, "NAV BAR", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });

        materialSearchBar.addTextChangeListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                makeAutoSuggestions(isLocationEnabled, s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        materialSearchBar.setSuggestionsClickListener(new SuggestionsAdapter.OnItemViewClickListener() {
            @Override
            public void OnItemClickListener(int position, View v) {
                String inputText = materialSearchBar.getLastSuggestions().get(position).toString();
                materialSearchBar.setText(inputText);

                try {
                    geoLocate(inputText);
                } catch (IOException e) {
                    Toast.makeText(MapsActivity.this, "Error finding exact location.", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        materialSearchBar.clearSuggestions();
                    }
                }, 500);
            }
            @Override
            public void OnItemDeleteListener(int position, View v) {
                materialSearchBar.getLastSuggestions().remove(position);
            }
        });

        Places.initialize(getApplicationContext(), "AIzaSyBkmf2k5A4GgoZ9qc8eSyinEX5yWPTCOww");
        placesClient = Places.createClient(this);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        this.database = Database.getWithWtvrDatabase(getApplicationContext());
        username = getIntent().getStringExtra("USERNAME");

        if (checkPlayServices()) {
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            if (mapFragment != null) {
                mapFragment.getMapAsync(this);
            }
            enableMyLocation();
        }

        ImageView returnToLocationButton = findViewById(R.id.returnToMyLocationButton);
        returnToLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateLocationUI();
            }
        });

        giveMeIdeasButton = findViewById(R.id.giveMeIdeasButton);
        giveMeIdeasButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LatLng someCoords;
                if (selectedLocation != null){
                    someCoords = selectedLocation;
                } else {
                    someCoords = currentLocation;
                }
                moveTheCamera(new LatLng(someCoords.latitude, someCoords.longitude), STANDARD_ZOOM);
                PopupMenu popupMenu = new PopupMenu(MapsActivity.this, giveMeIdeasButton);
                getMenuInflater().inflate(R.menu.types_of_destinations, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.destinationRestaurant:
                                createSuggestMarkers("restaurant", someCoords);
                                break;
                            case R.id.destinationPark:
                                createSuggestMarkers("park", someCoords);
                                break;
                            case R.id.destinationClub:
                                createSuggestMarkers("night_club", someCoords);
                                break;
                            case R.id.destinationBar:
                                createSuggestMarkers("bar", someCoords);
                                break;
                            case R.id.destinationMuseum:
                                createSuggestMarkers("museum", someCoords);
                                break;
                            case R.id.destinationArt:
                                createSuggestMarkers("art_gallery", someCoords);
                        }
                        return true;
                    }
                });
                popupMenu.show();
            }
        });
    }

    private void makeAutoSuggestions(boolean isTheirLocationEnabled, String theText) {
        AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();
        FindAutocompletePredictionsRequest request;

        if (isTheirLocationEnabled) {
            RectangularBounds bounds = RectangularBounds.newInstance
                    (new LatLng(currentLocation.latitude - 1, currentLocation.longitude - 1),
                            new LatLng(currentLocation.latitude + 1, currentLocation.longitude + 1));
            request = FindAutocompletePredictionsRequest.builder()
                    .setLocationBias(bounds)
                    .setSessionToken(token)
                    .setQuery(theText)
                    .build();
        } else {
            request = FindAutocompletePredictionsRequest.builder()
                    .setSessionToken(token)
                    .setQuery(theText)
                    .build();
        }
        Task<FindAutocompletePredictionsResponse> reeeSponse = placesClient.findAutocompletePredictions(request);

        reeeSponse.addOnCompleteListener(new OnCompleteListener<FindAutocompletePredictionsResponse>() {
            @Override
            public void onComplete(@NonNull Task<FindAutocompletePredictionsResponse> task) {
                if (task.isSuccessful()) {
                    FindAutocompletePredictionsResponse predictionsResponse = task.getResult();
                    if (predictionsResponse != null) {
                        predictionList = predictionsResponse.getAutocompletePredictions();
                        List<String> suggestionsList = new ArrayList<>();
                        for (int i = 0; i < predictionList.size(); i++) {
                            AutocompletePrediction prediction = predictionList.get(i);
                            suggestionsList.add(prediction.getFullText(null).toString());
                        }
                        materialSearchBar.updateLastSuggestions(suggestionsList);
                        if (!materialSearchBar.isSuggestionsVisible()){
                            materialSearchBar.showSuggestionsList();
                        }
                    }
                }
            }
        });
    }

    private void geoLocate(String stringInput) throws IOException {
        Geocoder geocoder = new Geocoder(MapsActivity.this);
        List<Address> desiredLocation = new ArrayList<>();
        desiredLocation = geocoder.getFromLocationName(stringInput, 1);

        if (desiredLocation.size() > 0){
            Address theDesiredLocation = desiredLocation.get(0);
            LatLng coordsTheDesiredLocation = new LatLng(theDesiredLocation.getLatitude(), theDesiredLocation.getLongitude());
            selectedLocation = coordsTheDesiredLocation;
            moveTheCamera(coordsTheDesiredLocation, STANDARD_ZOOM);
            mMap.addMarker(new MarkerOptions().position(coordsTheDesiredLocation).title(theDesiredLocation.getAddressLine(0)));
        }
    }

    private void createSuggestMarkers(String fieldType, LatLng theirCoordinates){
        if (theirCoordinates != null) {
            StringBuilder URL = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
            URL.append("location=" + theirCoordinates.latitude + "," + theirCoordinates.longitude)
                    .append("&radius=1000")
                    .append("&type=" + fieldType)
                    .append("&key=AIzaSyBkmf2k5A4GgoZ9qc8eSyinEX5yWPTCOww");

            Request request = new Request.Builder().url(URL.toString()).build();
            Call call = okHttpClient.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        String jsonData = response.body().string();
                        if (response.isSuccessful()) {
                            try {
                                NearbyPlace[] nearbyPlaces = allTheNearbyPlaces(jsonData);
                                addSuggestMarkers(nearbyPlaces);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    } catch (NullPointerException e) {
                       e.printStackTrace();
                    }
                }
            });
        }
    }

    private NearbyPlace[] allTheNearbyPlaces(String jsonData) throws JSONException {
        JSONObject jsonObject = new JSONObject(jsonData);
        JSONArray jsonResults = jsonObject.getJSONArray("results");
        NearbyPlace[] nearbyPlacesList = new NearbyPlace[jsonResults.length()];

        for (int i = 0; i < jsonResults.length(); i++){
            NearbyPlace aPlace = new NearbyPlace();
            JSONObject aLocation = jsonResults.getJSONObject(i);

            aPlace.setName(aLocation.getString("name"));
            aPlace.setLat(aLocation.getJSONObject("geometry").getJSONObject("location").getString("lat"));
            aPlace.setLng(aLocation.getJSONObject("geometry").getJSONObject("location").getString("lng"));

            nearbyPlacesList[i] = aPlace;
        }
        return nearbyPlacesList;
    }

    public void moveTheCamera(LatLng latLng, Float zoom){
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }

    public void addSuggestMarkers(NearbyPlace[] nearbyPlaces){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mMap.clear();
                    for (NearbyPlace nearbyPlace : nearbyPlaces) {
                        LatLng itsCoordinates = new LatLng(Double.parseDouble(nearbyPlace.getLat()), Double.parseDouble(nearbyPlace.getLng()));
                        mMap.addMarker(new MarkerOptions().position(itsCoordinates).title(nearbyPlace.getName()));
                    }
                }
            });
        }

    public void updateLocationUI(){
        if (mMap == null) {
            return;
        }
        try {
            final Task userLocation = fusedLocationProviderClient.getLastLocation();
            userLocation.addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (userLocation.isSuccessful()){
                        mMap.clear();
                        Location lastKnownLocation = (Location) userLocation.getResult();
                        if(lastKnownLocation != null) {
                            currentLocation = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                            moveTheCamera(currentLocation, STANDARD_ZOOM);
                            mMap.setMyLocationEnabled(isLocationEnabled);
                        } else {
                            isLocationEnabled = false;
                        }
                    } else {
                        isLocationEnabled = false;
                    }
                }
            });
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
        } catch (SecurityException e){
            e.getMessage();
        }
    }

    private void updateDatabaseGPS(boolean isTrue){
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                database.getDAO_UserInfo().updateGPS(isTrue, username);
            }
        };
        new Thread(runnable).start();
    }

    // Methods: Initialization //

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (isLocationEnabled){
            mMap.getUiSettings().setMapToolbarEnabled(false);
            updateLocationUI();
        } else {
            LatLng sydney = new LatLng(-34, 151);
            mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
            mMap.getUiSettings().setMapToolbarEnabled(false);
        }
    }

    private boolean checkPlayServices() {
        int resultCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GoogleApiAvailability.getInstance().isUserResolvableError(resultCode)) {
                GoogleApiAvailability.getInstance().getErrorDialog(this, resultCode,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } return false;
        }
        // init();
        return true;
    }

    private void enableMyLocation(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission
                (this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            updateDatabaseGPS(true);
            isLocationEnabled = true;
            updateLocationUI();
        } else {
            String[] permissionsList = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
            ActivityCompat.requestPermissions(this, permissionsList, LOCATION_PERMISSIONS_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case LOCATION_PERMISSIONS_REQUEST_CODE:
                if (grantResults.length > 0){
                    for(int i : grantResults) {
                        try {
                            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                                isLocationEnabled = false;
                                updateDatabaseGPS(false);
                                break;
                            }
                            isLocationEnabled = true;
                            updateDatabaseGPS(true);
                            updateLocationUI();
                        } catch (RuntimeException e){
                            e.printStackTrace();
                        }
                    }
                }
        }
    }

    // Methods: App Navigation //

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.map_main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.clear_menu:
                mMap.clear();
                break;
            case R.id.gobackMenu:
                Intent intent = new Intent(this, homePage.class);
                intent.putExtra("USERNAME", username);
                finish();
                startActivity(intent);
                break;
            case R.id.map_profile_menu:
                Intent intent2 = new Intent(this, ProfilePage.class);
                intent2.putExtra("USERNAME", username);
                finish();
                startActivity(intent2);
                break;
        }
        return super.onOptionsItemSelected(item);
    }


}
