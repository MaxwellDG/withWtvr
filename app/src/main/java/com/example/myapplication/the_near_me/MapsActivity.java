package com.example.myapplication.the_near_me;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.example.myapplication.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Response;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG = "TAG";
    private GoogleMap mMap;

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    public static final int LOCATION_PERMISSIONS_REQUEST_CODE = 9001;
    private static final float STANDARD_ZOOM = 15f;

    private FusedLocationProviderClient fusedLocationProviderClient;

    // might later on just get this from the databse GPS field //
    private boolean isLocationEnabled;
    private ArrayList<Marker> allMarkers;

    private ImageView returnToLocationButton;
    private ImageView giveMeIdeasButton;
    private EditText destinationInput;
    private LatLng currentLocation;
    private List<AutocompletePrediction> predictionList;
    private PlacesClient placesClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);


        Places.initialize(getApplicationContext(), "@string/google_maps_key");
        placesClient = Places.createClient(this);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        List<Place.Field> fields = new ArrayList<>();
        fields.add(Place.Field.NAME);
        FindCurrentPlaceRequest currentPlaceRequest =
                FindCurrentPlaceRequest.newInstance(fields);

        Task<FindCurrentPlaceResponse> placeResponse = placesClient.findCurrentPlace(currentPlaceRequest);
        placeResponse.addOnCompleteListener(new OnCompleteListener<FindCurrentPlaceResponse>() {
            @Override
            public void onComplete(@NonNull Task<FindCurrentPlaceResponse> task) {
                if (task.isSuccessful()){
                    FindCurrentPlaceResponse theResponse = task.getResult();
                    for (PlaceLikelihood placeLikelihood : theResponse.getPlaceLikelihoods()) {
                        Log.i(TAG, String.format("Place '%s' has likelihood: %f",
                                placeLikelihood.getPlace().getName(),
                                placeLikelihood.getLikelihood()));
                    }
                } else {
                    Log.d(TAG, "onComplete: Task not successful.");
                }
            }
        });

        if (checkPlayServices()) {
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            if (mapFragment != null) {
                mapFragment.getMapAsync(this);
            }
            enableMyLocation();
        }

        destinationInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                makeAutoSuggestions(isLocationEnabled);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        returnToLocationButton = findViewById(R.id.returnToMyLocationButton);
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
                Log.d(TAG, "onClick: registered clack.");
                PopupMenu popupMenu = new PopupMenu(MapsActivity.this, giveMeIdeasButton);
                getMenuInflater().inflate(R.menu.types_of_destinations, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.destinationRestaurant:
                                break;
                            case R.id.destinationPark:
                                break;
                            case R.id.destinationClub:
                                break;
                            case R.id.destinationBar:
                                break;
                            case R.id.destinationMuseum:
                                break;
                        }
                        return true;
                    }
                });
                popupMenu.show();
            }
        });

    }


    public void testing(){

    }



    public void init() {
        destinationInput = findViewById(R.id.destinationInput);
        destinationInput.setSingleLine();
        destinationInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                Log.d(TAG, "onEditorAction: Are we even listening?!");
                if (event != null && event.getAction() != KeyEvent.ACTION_DOWN) {
                    Log.d(TAG, "onEditorAction: baby1");
                    return false;
                } else if (actionId == EditorInfo.IME_ACTION_SEARCH
                        || event == null
                        || event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                        try {
                            Log.d(TAG, "onEditorAction: baby2");
                            geoLocate(destinationInput.getText().toString());
                        } catch (IOException e) {
                            Log.d(TAG, "onEditorAction: baby confused");
                            e.printStackTrace();
                        }
                    }
                Log.d(TAG, "onEditorAction: baby3");
                    return false;
            }
        });
    }

    private void makeAutoSuggestions(boolean isTheirLocationEnabled) {
        AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();
        FindAutocompletePredictionsRequest request;

        if (isTheirLocationEnabled) {
            RectangularBounds bounds = RectangularBounds.newInstance
                    (new LatLng(currentLocation.latitude - 1, currentLocation.longitude - 1),
                            new LatLng(currentLocation.latitude + 1, currentLocation.longitude + 1));
            request = FindAutocompletePredictionsRequest.builder()
                    .setLocationBias(bounds)
                    .setSessionToken(token)
                    .setQuery(destinationInput.getText().toString())
                    .build();
        } else {
            request = FindAutocompletePredictionsRequest.builder()
                    .setSessionToken(token)
                    .setQuery(destinationInput.getText().toString())
                    .build();
        }
        placesClient.findAutocompletePredictions(request).addOnCompleteListener(new OnCompleteListener<FindAutocompletePredictionsResponse>() {
            @Override
            public void onComplete(@NonNull Task<FindAutocompletePredictionsResponse> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "onComplete: Success1");
                    FindAutocompletePredictionsResponse predictionsResponse = task.getResult();
                    if (predictionsResponse != null) {
                        Log.d(TAG, "onComplete: Success 2");
                        predictionList = predictionsResponse.getAutocompletePredictions();
                        List<String> suggestionsList = new ArrayList<>();
                        for (int i = 0; i < predictionList.size(); i++) {
                            AutocompletePrediction prediction = predictionList.get(i);
                            suggestionsList.add(prediction.getFullText(null).toString());
                            Log.d(TAG, "onComplete: " + prediction.toString());
                        }
                    }
                } else {
                    Log.i("mytag", "prediction fetching task unsuccessful");
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
            moveTheCamera(coordsTheDesiredLocation,
                    STANDARD_ZOOM);
            mMap.addMarker(new MarkerOptions().position(coordsTheDesiredLocation).title(theDesiredLocation.getFeatureName()));

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
        init();
        return true;
    }

    private void enableMyLocation(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission
                (this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            Log.d(TAG, "enableMyLocation: Permissions successful.");
            isLocationEnabled = true;
            updateLocationUI();
        } else {
            Log.d(TAG, "enableMyLocation: Permissions unsucessful, attemping request.");
            String[] permissionsList = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
            ActivityCompat.requestPermissions(this, permissionsList, LOCATION_PERMISSIONS_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case LOCATION_PERMISSIONS_REQUEST_CODE:
                if (grantResults.length > 0){
                    Log.d(TAG, "onRequestPermissionsResult: Requesting permissions.");
                    for(int i : grantResults){
                        // TODO: add some Database GPS input stuff below (even though, yes, it makes the toggle on the profile page entirely redundant  //
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED){
                            isLocationEnabled = false;
                            break;
                        }
                        isLocationEnabled = true;
                        updateLocationUI();
                    }
                }
        }
    }

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

    public void moveTheCamera(LatLng latLng, Float zoom){
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }

    public void updateLocationUI(){
        if (mMap == null) {
            Log.d(TAG, "updateLocationUI: mMap is null");
            return;
        }
        try {
            Log.d(TAG, "updateLocationUI: called and tried.");
            final Task userLocation = fusedLocationProviderClient.getLastLocation();
            userLocation.addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (userLocation.isSuccessful()){
                        mMap.clear();
                        Location lastKnownLocation = (Location) userLocation.getResult();
                        assert lastKnownLocation != null;
                        currentLocation = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                        mMap.addMarker(new MarkerOptions().position(currentLocation).title("My Location"));
                        moveTheCamera(currentLocation, STANDARD_ZOOM);
                    }
                }
            });
            mMap.setMyLocationEnabled(isLocationEnabled);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
        } catch (SecurityException e){
            e.getMessage();
        }
    }

    /* TODO: each option will call another function (obv with paramaters pertaining to the choice) that
        will geolocate 10 of the closest options to you). Keep in mind that this will have to be tweaked
        later to geolocate the closest 5 options to a marker that you will point down somewhere
        Also remember that the whole geolocate this is annoying looking and deals with a series of
        list<location>s and will take some time to learn on youtube   .
     */
}
