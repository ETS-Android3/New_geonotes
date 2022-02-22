package com.abhijith.nanodegree.geonotes.View;

import android.Manifest;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.abhijith.nanodegree.geonotes.Model.Notes;
import com.abhijith.nanodegree.geonotes.Model.NotesPrivate;
import com.abhijith.nanodegree.geonotes.R;
import com.abhijith.nanodegree.geonotes.Utils.Constants;
import com.abhijith.nanodegree.geonotes.Utils.GeoNotesUtils;
import com.abhijith.nanodegree.geonotes.Utils.GooglePlayServicesHelper;
import com.abhijith.nanodegree.geonotes.Utils.MarkerClusterRenderer;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.maps.android.clustering.ClusterManager;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.abhijith.nanodegree.geonotes.Utils.Constants.DEFAULT_ZOOM;
import static com.abhijith.nanodegree.geonotes.Utils.GeoNotesUtils.showMarkerInfo;

public class FootprintFragment extends Fragment implements OnMapReadyCallback {

    private static final String TAG = FootprintFragment.class.getSimpleName();

    @BindView(R.id.tv_search)
    TextView mSearchText;

    @BindView(R.id.ic_gps)
    ImageView mGps;

    private String finalLocation;
    private GoogleMap mMap;
    private Location currentLocation;

    private boolean mLocationPermissionsGranted = false;

    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATIONS_PERMISSIONS_REQUEST = 5445;

    private AutocompleteSupportFragment autocompleteFragment;

    private Geocoder geocoder;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference notesRef = db.collection("notes");
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    private List<Notes> allNotesFromDb = new ArrayList<>();

    public FootprintFragment() {
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!mLocationPermissionsGranted) {
            if (GooglePlayServicesHelper.isGooglePlayServicesAvailable(this.getContext())) {
                getLocationPermissions();
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_footprint, container, false);

        ButterKnife.bind(this, rootView);

        return rootView;
    }

    private void getLocationPermissions() {
        Log.d(TAG, "getLocationPermissions: getting location permissions");
        String[] permissions = {FINE_LOCATION, COARSE_LOCATION};

        if (ActivityCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                (getActivity().getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(getActivity(), permissions, LOCATIONS_PERMISSIONS_REQUEST);

        } else {
            mLocationPermissionsGranted = true;
            initMap();
        }
    }

    private void initMap() {
        Log.d(TAG, "initMap: initializing map fragment");
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.frg_footprint);

        if (!Places.isInitialized()) {
            Places.initialize(getActivity().getApplicationContext(), getString(R.string.API_KEY));
        }

        // Initialize the AutocompleteSupportFragment.
        autocompleteFragment = (AutocompleteSupportFragment)
                getChildFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        // Specify the types of place data to return.
        if (autocompleteFragment != null) {
            autocompleteFragment.setHint("Search Location");
            autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));
        }

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady: Map is ready");
        this.mMap = googleMap;

        if (mLocationPermissionsGranted) {
            Log.d(TAG, "onMapReady: Getting current location of the device and moving the camera");

            getDeviceLocation();

            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false); // to get rid of the default location button

            init();
        }
    }

    private void init() {
        Log.d(TAG, "init: initializing");
        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                Log.i(TAG, "Place: " + place.getName() + ", " + place.getId());
                mSearchText.setText(place.getName());
                geoLocateByPlaceName(place.getLatLng());
            }

            @Override
            public void onError(@NonNull Status status) {
                Log.i(TAG, "An error occurred: " + status);
            }

        });

        mGps.setOnClickListener(view -> {
            Log.d(TAG, "onClick: clicked gps icon");
            getDeviceLocation();
        });

        getAllNotes();

        mMap.setOnMapClickListener(this::displayDecisionDialog);
        mMap.setOnMarkerClickListener(marker -> {
            displayMarkerWithNotes(marker);
            return true;
        });
    }

    private void geoLocateByPlaceName(LatLng latLng) {
        //mMap.animateCamera(CameraUpdateFactory.newCameraPosition(getCameraPositionWithBearing(latLng)));
        displayNoteDialog(latLng);
    }

    // Getting the base location of the user
    private void getDeviceLocation() {
        Log.d(TAG, "getDeviceLocation: getting device's current location");
        FusedLocationProviderClient mfusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this.getActivity());
        try {
            if (mLocationPermissionsGranted) {
                Task location = mfusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        Log.d(TAG, "onComplete: found location!");
                        currentLocation = (Location) task.getResult();
                        moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()),
                                "My Location");
                    } else {
                        Log.d(TAG, "onComplete: current location is null");
                        Toast.makeText(getActivity(), getString(R.string.current_location_not_found), Toast.LENGTH_SHORT).show();
                        moveCamera(new LatLng(Constants.DEFAULT_LATITUDE, Constants.DEFAULT_LONGITUDE), getString(R.string.my_location));
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e(TAG, "getDeviceLocation: SecurityException");
        }
    }

    private void getAllNotes() {
        notesRef.get()
                .addOnSuccessListener(documentSnapshots -> {
                    if (documentSnapshots.isEmpty()) {
                        Log.d(TAG, "onSuccess: LIST EMPTY");
                        return;
                    } else {
                        List<Notes> types = documentSnapshots.toObjects(Notes.class);
                        allNotesFromDb.addAll(types);
                        showClusterMapView(allNotesFromDb, mMap);
                    }

                })
                .addOnFailureListener(e -> Log.d(TAG, "Error receiving all documents to show on map"));
    }
    private void displayDecisionDialog(LatLng latlng){
        AlertDialog.Builder decision_dialog = new AlertDialog.Builder(this.getContext());
        LayoutInflater inflater = this.getLayoutInflater();
        View decision_dialog_view = inflater.inflate(R.layout.decision_dialog, null);
        decision_dialog.setView(decision_dialog_view);
        decision_dialog.setCancelable(true);
        decision_dialog.setTitle("Escoja una opcion");

        Button boton_privada = decision_dialog_view.findViewById(R.id.button_privada);
        Button boton_publica = decision_dialog_view.findViewById(R.id.button_publica);
        boton_publica.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayNoteDialog(latlng);
            }
        });
        boton_privada.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayNotePrivateDialog(latlng);
            }
        });
        decision_dialog.setNeutralButton(getString(R.string.btn_cancel), (alert, which) -> alert.dismiss());
        decision_dialog.show();

    }
    private void displayNotePrivateDialog(LatLng latLng){
        AlertDialog.Builder dialog = new AlertDialog.Builder(this.getContext());
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.notes_private_dialog, null);
        dialog.setView(dialogView);
        dialog.setCancelable(true);
        dialog.setIcon(R.drawable.geonotes_logo_round);
        dialog.setTitle(getString(R.string.dialog_add_note_title));

        EditText title = dialogView.findViewById(R.id.et_title);
        EditText description = dialogView.findViewById(R.id.et_description);
        EditText range = dialogView.findViewById(R.id.et_range);
        EditText expiration = dialogView.findViewById(R.id.et_expiration);
        EditText hour = dialogView.findViewById(R.id.et_Hour);
        TextView date = dialogView.findViewById(R.id.tv_date);
        TextView formLocation = dialogView.findViewById(R.id.tv_location);
        String currentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.UK).format(Calendar.getInstance().getTime());
        date.setText(currentDate);

        String location = getLocationDetails(latLng);
        formLocation.setText(location);
        String email = mAuth.getCurrentUser().getEmail();

        dialog.setPositiveButton(getString(R.string.btn_submit), (alert, which) -> {
            String heading = title.getText().toString().trim(); //title
            String desc = description.getText().toString().trim(); //description
            String ran = range.getText().toString().trim(); //Rango de recepción
            String ex = expiration.getText().toString().trim(); //Fecha de expiracion
            String ho = hour.getText().toString().trim(); //Hora

            if (TextUtils.isEmpty(heading)) {
                Toast.makeText(this.getActivity(), getString(R.string.error_empty_title), Toast.LENGTH_SHORT).show();
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Error");
                builder.setMessage("Campo faltante");

                builder.setPositiveButton("Aceptar", null);
                AlertDialog dialog_2 = builder.create();
                dialog_2.show();
            }else{
                NotesPrivate notesPrivate = new NotesPrivate(heading, desc, ran, ex,ho, email, currentDate, location, String.valueOf(latLng.latitude), String.valueOf(latLng.longitude));
                db.collection("notes")
                        .add(notesPrivate)
                        .addOnSuccessListener(documentReference -> {
                            Log.d(TAG, "DocumentSnapshot written with ID: " + documentReference.getId());
                            Toast.makeText(getContext(), getString(R.string.note_added), Toast.LENGTH_SHORT).show();
                            alert.dismiss();
                            moveCamera(new LatLng(latLng.latitude, latLng.longitude), title.toString());
                        })
                        .addOnFailureListener(e -> Log.w(TAG, "Error adding document", e));
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Hecho");
                builder.setMessage("Nota creada con exito");

                builder.setPositiveButton("Aceptar", null);
                AlertDialog dialog_2 = builder.create();
                dialog_2.show();
            }
        });
        dialog.show();
    }
    private void displayNoteDialog(LatLng latLng) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this.getContext());
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.custom_dialog, null);
        dialog.setView(dialogView);
        dialog.setCancelable(true);
        dialog.setIcon(R.drawable.geonotes_logo_round);
        dialog.setTitle(getString(R.string.dialog_add_note_title));

        EditText title = dialogView.findViewById(R.id.et_title);
        EditText description = dialogView.findViewById(R.id.et_description);
        EditText addressee = dialogView.findViewById(R.id.et_addressee);
        EditText range = dialogView.findViewById(R.id.et_range);
        EditText expiration = dialogView.findViewById(R.id.et_expiration);
        EditText hour = dialogView.findViewById(R.id.et_Hour);
        TextView date = dialogView.findViewById(R.id.tv_date);
        TextView formLocation = dialogView.findViewById(R.id.tv_location);

        String currentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.UK).format(Calendar.getInstance().getTime());
        date.setText(currentDate);

        String location = getLocationDetails(latLng);
        formLocation.setText(location);
        String email = mAuth.getCurrentUser().getEmail();
        dialog.setPositiveButton(getString(R.string.btn_submit), (alert, which) -> {

            String heading = title.getText().toString().trim(); //title
            String desc = description.getText().toString().trim(); //description
            String ad = addressee.getText().toString().trim(); //Direccion de amigo
            String ran = range.getText().toString().trim(); //Rango de recepción
            String ex = expiration.getText().toString().trim(); //Fecha de expiracion
            String ho = hour.getText().toString().trim(); //Hora

            if (TextUtils.isEmpty(heading)) {
                Toast.makeText(this.getActivity(), getString(R.string.error_empty_title), Toast.LENGTH_SHORT).show();
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Error");
                builder.setMessage("Nota no creada, campo faltante");

                builder.setPositiveButton("Aceptar", null);
                AlertDialog dialog_2 = builder.create();
                dialog_2.show();
            } else {
                db.collection("friends")
                        .whereEqualTo("addressee", ad)
                        .whereEqualTo("email", email)
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                if(queryDocumentSnapshots.isEmpty()){
                                    Toast.makeText(getContext(),"El destino no es amigo tuyo", Toast.LENGTH_SHORT).show();
                                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                    builder.setTitle("Error");
                                    builder.setMessage("Destino no es tu amigo");

                                    builder.setPositiveButton("Aceptar", null);
                                    AlertDialog dialog_2 = builder.create();
                                    dialog_2.show();
                                }else{
                                    Notes notes = new Notes(heading, desc, ad, ran, ex,ho, email, currentDate, location, String.valueOf(latLng.latitude), String.valueOf(latLng.longitude));
                                    db.collection("notes")
                                            .add(notes)
                                            .addOnSuccessListener(documentReference -> {
                                                Log.d(TAG, "DocumentSnapshot written with ID: " + documentReference.getId());
                                                Toast.makeText(getContext(), getString(R.string.note_added), Toast.LENGTH_SHORT).show();
                                                alert.dismiss();
                                                moveCamera(new LatLng(latLng.latitude, latLng.longitude), title.toString());
                                            })
                                            .addOnFailureListener(e -> Log.w(TAG, "Error adding document", e));
                                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                    builder.setTitle("Hecho");
                                    builder.setMessage("Nota creada con exito");

                                    builder.setPositiveButton("Aceptar", null);
                                    AlertDialog dialog_2 = builder.create();
                                    dialog_2.show();
                                }
                            }
                        });
            }
        });

        dialog.setNeutralButton(getString(R.string.btn_cancel), (alert, which) -> alert.dismiss());

        dialog.show();
    }

    private void displayMarkerWithNotes(Marker marker) {
        Toast.makeText(this.getContext(), marker.getTitle(), Toast.LENGTH_SHORT).show();
    }

    private String getLocationDetails(LatLng latLng) {
        geocoder = new Geocoder(this.getActivity());

        List<Address> addresses = null;

        try {
            addresses = geocoder.getFromLocation(
                    latLng.latitude,
                    latLng.longitude,
                    1);
        } catch (IOException ioException) {
            // Catch network or other I/O problems.
            finalLocation = getString(R.string.service_not_available);
            Log.e(TAG, finalLocation, ioException);
        } catch (IllegalArgumentException illegalArgumentException) {
            // Catch invalid latitude or longitude values.
            finalLocation = getString(R.string.invalid_lat_long_used);
            Log.e(TAG, finalLocation + ". " +
                    "Latitude = " + latLng.latitude +
                    ", Longitude = " +
                    latLng.longitude, illegalArgumentException);
        }

        // Handle case where no address was found.
        if (addresses == null || addresses.size() == 0) {
            if (finalLocation.isEmpty()) {
                finalLocation = getString(R.string.no_address_found);
                Log.e(TAG, finalLocation);
            }
        } else {
            Address address = addresses.get(0);
            Log.i(TAG, getString(R.string.address_found));
            finalLocation = address.getAddressLine(0);
        }
        return finalLocation;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionResult: called");
        mLocationPermissionsGranted = false;
        if (requestCode == LOCATIONS_PERMISSIONS_REQUEST) {
            if (grantResults.length > 0) {
                for (int grantResult : grantResults) {
                    if (grantResult != PackageManager.PERMISSION_GRANTED) {
                        Log.d(TAG, "onRequestPermissionsResult: permission failed");
                        return;
                    }
                }
                Log.d(TAG, "onRequestPermissionsResult: permission granted");
                mLocationPermissionsGranted = true;
                // initialize our map
                initMap();
            }
        }
    }

    private void moveCamera(LatLng latLng, String title) {
        Log.d(TAG, "moveCamera: moving the camera to: lat: " + latLng.latitude + ", lng: " + latLng.longitude);

        if (!title.equals("My Location")) {
            MarkerOptions options = new MarkerOptions()
                    .position(latLng)
                    .title(title)
                    .icon(GeoNotesUtils.bitmapDescriptorFromVector(getContext(), R.drawable.ic_marker_cluster));
            mMap.addMarker(options);
        }

        //Zoom in and animate the camera.
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(getCameraPositionWithBearing(latLng)));
    }

    @NonNull
    private CameraPosition getCameraPositionWithBearing(LatLng latLng) {
        return new CameraPosition.Builder().target(latLng).zoom(DEFAULT_ZOOM).build();
    }

    private void showClusterMapView(List<Notes> noteList, GoogleMap mMap) {

        // moveCamera(new LatLng(Constants.DEFAULT_LATITUDE, Constants.DEFAULT_LONGITUDE), Constants.DEFAULT_TITLE);

        ClusterManager<Notes> mClusterManager = new ClusterManager<>(getContext(), mMap);
        mClusterManager.setRenderer(new MarkerClusterRenderer(getContext(), mMap, mClusterManager, mAuth.getCurrentUser().getEmail()));
        mMap.setOnCameraIdleListener(mClusterManager);
        mMap.setOnMarkerClickListener(mClusterManager);
        mMap.setOnInfoWindowClickListener(marker -> {
            String title = "More Details of " + "\"" + marker.getTitle() + "\"";
            String description = marker.getSnippet();
            showMarkerInfo((AppCompatActivity) getContext(), title, description);

        });
        addNoteItemsWhichHasLatLng(mClusterManager, noteList);
        mClusterManager.cluster();
    }

    private void addNoteItemsWhichHasLatLng(ClusterManager<Notes> mClusterManager, List<Notes> noteList) {
        for (Notes note : noteList) {
            if (note.getLocation() != null) {
                mClusterManager.addItem(note);
            }
        }
    }
}
