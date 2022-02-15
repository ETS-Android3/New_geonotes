
package com.abhijith.nanodegree.geonotes.View;

import android.app.AlertDialog;

import android.content.DialogInterface;
import android.os.Bundle;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.abhijith.nanodegree.geonotes.Model.Friends;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.CollectionReference;
import androidx.fragment.app.Fragment;

import com.abhijith.nanodegree.geonotes.R;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import butterknife.ButterKnife;

public class FriendsFragment extends Fragment {
        private FirebaseFirestore db = FirebaseFirestore.getInstance();
        private CollectionReference friendsRef = db.collection("friends");
        private FirebaseAuth mAuth = FirebaseAuth.getInstance();
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            // Inflate the layout for this fragment
            View rootView = inflater.inflate(R.layout.fragment_friend, container, false);
            Button boton_amigo = rootView.findViewById(R.id.button_friend);
            EditText correo_amigo = rootView.findViewById(R.id.et_correo);
            String email = mAuth.getCurrentUser().getEmail();
            boton_amigo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String comparar = "Correo Amigo";
                    if(correo_amigo.getText().toString().isEmpty()){
                        Toast.makeText(getActivity(),"El correo esta vacio",Toast.LENGTH_SHORT).show();
                    }else{
                        String ad = correo_amigo.getText().toString().trim();
                        Friends friends = new Friends(ad, email);
                        db.collection("friends")
                                .add(friends);
                    }
                    /*new AlertDialog.Builder(view.getContext())
                            .setTitle("Closing application")
                            .setMessage("Are you sure you want to exit?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            }).setNegativeButton("No", null).show();*/
                }
            });
            return rootView;
        }

        public void guardarDatos(){

        }
        private void mostrarDialogo(){

        /*AlertDialog.Builder dialog = new AlertDialog.Builder(this.getContext());
        LayoutInflater inflador = this.getLayoutInflater();
        View dialogView = inflador.inflate(R.layout.friends_dialog, null);
        dialog.setView(dialogView);
        dialog.setCancelable(true);
        dialog.setTitle(getString(R.string.dialog_add_friend_title));

        EditText correo = dialogView.findViewById(R.id.et_friend);
        String email = mAuth.getCurrentUser().getEmail();*/
/*
        dialog.setPositiveButton(getString(R.string.btn_submit), (alert, which) -> {

            String friend_email = correo.getText().toString().trim(); //title

            if (TextUtils.isEmpty(friend_email)) {
                Toast.makeText(this.getActivity(), getString(R.string.error_empty_friend), Toast.LENGTH_SHORT).show();
            } else {
                Friends friends = new Friends(friend_email, email);
            }
        });*/

            new AlertDialog.Builder(this.getContext())
                    .setTitle("Esto es un titulo")
                    .setMessage("Esto es un mensaje")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Log.d("Mensaje","Acepto");
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Log.d("mensaje","Se cancelo la accion");
                        }
                    })
                    .show();
        }
    }
