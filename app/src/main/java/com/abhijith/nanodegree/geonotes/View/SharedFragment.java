package com.abhijith.nanodegree.geonotes.View;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.abhijith.nanodegree.geonotes.Model.Notes;
import com.abhijith.nanodegree.geonotes.Model.NotesAdapter;
import com.abhijith.nanodegree.geonotes.R;
import com.abhijith.nanodegree.geonotes.Utils.Constants;
import com.abhijith.nanodegree.geonotes.Utils.GeoNotesUtils;
import com.abhijith.nanodegree.geonotes.Utils.GooglePlayServicesHelper;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.shreyaspatil.MaterialDialog.BottomSheetMaterialDialog;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.text.DecimalFormat;

/*
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
 */

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class SharedFragment extends Fragment {

    private static final String TAG = SharedFragment.class.getSimpleName();

    @BindView(R.id.rv_notes)
    RecyclerView rvNotes;

    @BindView(R.id.textViewEmpty)
    TextView mTextViewEmpty;

    @BindView(R.id.imageViewEmpty)
    ImageView mImageViewEmpty;

    @BindView(R.id.progressBarLoading)
    ProgressBar mProgressBarLoading;

    private FirebaseFirestore firestoreDB = FirebaseFirestore.getInstance();
    private CollectionReference notesRef = firestoreDB.collection("notes");
    private NotesAdapter adapter;
    private String userID;
    private Gson gson;
/////////////////////////////////////////// Agregado por Isaac, extraido de FootprintFragment.java
    private CollectionReference notesRefFil = firestoreDB.collection("delivered_notes"); //aqui se guardarán las notas que cumplan las condiciones de rango
    private Location currentLocation;
    private List<String> s_documents = new ArrayList<>(); //Agregado por Isaac
    private LatLng actualLocation; //agregado por Isaac

    private boolean mLocationPermissionsGranted = false;

    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATIONS_PERMISSIONS_REQUEST = 5445;
//////////////////////////////////////////////////////
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gson = new Gson();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!mLocationPermissionsGranted) {
            if (GooglePlayServicesHelper.isGooglePlayServicesAvailable(this.getContext())) {
                getLocationPermissions();
            }
        }
        getDeviceLocation();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_notes_list, container, false);

        ButterKnife.bind(this, rootView);

        setUpRecyclerView();

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();

    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    //agregado por Isaac
    private void getLocationPermissions() {
        Log.d(TAG, "getLocationPermissions: getting location permissions");
        String[] permissions = {FINE_LOCATION, COARSE_LOCATION};

        if (ActivityCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                (getActivity().getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(getActivity(), permissions, LOCATIONS_PERMISSIONS_REQUEST);

        } else {
            mLocationPermissionsGranted = true;
            //initMap(); No se necesita incializar mapa en esta vista
        }
    }

    private void setUpRecyclerView() {
        mProgressBarLoading.setVisibility(View.VISIBLE);
        userID = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        notesRefFil.whereEqualTo("addressee",userID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task1) {
                        if(task1.isSuccessful()){
                            for(QueryDocumentSnapshot document1: task1.getResult()){
                                Notes noteA = document1.toObject(Notes.class);
                                s_documents.add(noteA.getTitle());
                                Log.i(TAG,"Added to s_documents: "+document1.getId());
                            }
                        }else{
                            Log.d(TAG,"No se han encontrado notas compartidas");
                        }
                    }
                });

        notesRef.whereEqualTo("addressee", userID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Notes noteR = document.toObject(Notes.class);
                                LatLng noteLoc = new LatLng(
                                        Double.parseDouble(noteR.getLatitude()),
                                        Double.parseDouble(noteR.getLongitute()));

                                Double noteDistance = CalculationByDistance(actualLocation,noteLoc); //distancia en km
                                Log.i(TAG,"LO QUE BUSCO: "+noteR.getRange()+","+noteR.getTitle());
                                if(noteR.getRange() == ""){
                                    String docId = document.getId();
                                    String docTitle = noteR.getTitle();
                                    if(!s_documents.contains(docTitle)){
                                        Log.i(TAG,"Adding note to notesRefFil\n"+document.getData());
                                        notesRefFil.add(document.getData());
                                    }
                                }
                                else {
                                    if (noteDistance <= Double.parseDouble(noteR.getRange()) || noteR.getRange() == "0") {
                                        String docId = document.getId();
                                        String docTitle = noteR.getTitle();
                                        if (!s_documents.contains(docTitle)) {
                                            Log.i(TAG, "Adding note to notesRefFil\n" + docId);
                                            notesRefFil.add(document.getData());
                                        }
                                    }
                                }
                                //Log.d(TAG,"FILTERS: "+ notesRefFil.toString());
                                //Log.d(TAG+" FILTERS", document.getId() + " => " + document.getData());
                            }
                        } else {
                            Log.d(TAG+" FILTERS", "Error getting documents: ", task.getException());
                        }
                    }
                });
        Query query = notesRefFil.whereEqualTo("addressee", userID).orderBy("date", Query.Direction.DESCENDING);
        String aList = query.get().toString();

        FirestoreRecyclerOptions<Notes> options = new FirestoreRecyclerOptions.Builder<Notes>()
                .setQuery(query, Notes.class)
                .build();

        Log.i("QueryLoad",options.toString());
        mProgressBarLoading.setVisibility(View.INVISIBLE);
        adapter = new NotesAdapter(options);
        Log.i("AdapterSet",adapter.toString());
        rvNotes.setHasFixedSize(true);
        rvNotes.setLayoutManager(new LinearLayoutManager(getContext()));
        rvNotes.setAdapter(adapter);
        Log.i("rvNotesSet",rvNotes.toString());

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                adapter.deleteItem(viewHolder.getAdapterPosition());
            }
        }).attachToRecyclerView(rvNotes);

        adapter.setOnItemClickListener((documentSnapshot, position) -> {
            Notes note = documentSnapshot.toObject(Notes.class);
            String id = documentSnapshot.getId();
            String path = documentSnapshot.getReference().getPath();

            if (note != null) {
                GeoNotesUtils.showMarkerInfo((AppCompatActivity) getContext(), note.getTitle(), note.getSnippet());
            }
        });

        adapter.setOnLongPressListener((documentSnapshot, position) -> {
            Notes note = documentSnapshot.toObject(Notes.class);
            if (note != null) {
                new BottomSheetMaterialDialog.Builder((AppCompatActivity) getContext())
                        .setTitle(getString(R.string.favorite_note))
                        .setMessage(getString(R.string.add_to_widget))
                        .setCancelable(true)
                        .setPositiveButton("Add", android.R.drawable.ic_input_add, (dialogInterface, which) -> {
                            addItemToWidget(note);
                            dialogInterface.dismiss();
                        })
                        .build()
                        .show();
            }
        });
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
                        actualLocation = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                        //no se necesita mover camara en esta vista, Isaac
                        //moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()),"My Location");
                    } else {
                        Log.d(TAG, "onComplete: current location is null");
                        Toast.makeText(getActivity(), getString(R.string.current_location_not_found), Toast.LENGTH_SHORT).show();
                        actualLocation = new LatLng(Constants.DEFAULT_LATITUDE, Constants.DEFAULT_LONGITUDE);
                        //no se necesita mover camara en esta vista, Isaac
                        //moveCamera(new LatLng(Constants.DEFAULT_LATITUDE, Constants.DEFAULT_LONGITUDE), getString(R.string.my_location));
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e(TAG, "getDeviceLocation: SecurityException");
        }
    }

    //Método para calcular la distancia entre dos puntos
    public double CalculationByDistance(LatLng StartP, LatLng EndP) {
        Log.i("CalcDistance", "Points: "+StartP.toString()+","+EndP.toString());
        int Radius = 6371;// radius of earth in Km
        double lat1 = StartP.latitude;
        double lat2 = EndP.latitude;
        double lon1 = StartP.longitude;
        double lon2 = EndP.longitude;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2)
                * Math.sin(dLon / 2);
        double c = 2 * Math.asin(Math.sqrt(a));
        double valueResult = Radius * c;
        double km = valueResult / 1;
        DecimalFormat newFormat = new DecimalFormat("####");
        int kmInDec = Integer.valueOf(newFormat.format(km));
        double meter = valueResult % 1000;
        int meterInDec = Integer.valueOf(newFormat.format(meter));
        Log.i("Radius Value", "" + valueResult + "   KM  " + kmInDec
                + " Meter   " + meterInDec);

        return Radius * c;
    }

    private void addItemToWidget(Notes note) {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(Constants.SHARED_PREFERENCES, Context.MODE_PRIVATE);
        sharedPreferences.edit().putString(Constants.WIDGET_NOTES_SELECTED, gson.toJson(note)).apply();
        Toast.makeText(getActivity(), "Added " + note.getTitle() + " to Widget.", Toast.LENGTH_SHORT).show();
    }
}
