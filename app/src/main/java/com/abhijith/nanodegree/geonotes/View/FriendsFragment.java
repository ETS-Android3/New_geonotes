
package com.abhijith.nanodegree.geonotes.View;

import android.app.AlertDialog;

import android.content.DialogInterface;
import android.os.Bundle;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.EditText;

import android.widget.Toast;

import com.abhijith.nanodegree.geonotes.Model.Friends;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.abhijith.nanodegree.geonotes.R;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class FriendsFragment extends Fragment {
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        final FirebaseAuth mAuth = FirebaseAuth.getInstance();
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
                    String comparar = correo_amigo.getText().toString().trim();
                    if(correo_amigo.getText().toString().isEmpty()){
                        Toast.makeText(getActivity(),"El correo esta vacio",Toast.LENGTH_SHORT).show();
                    }else{
                        db.collection("friends")
                                .whereEqualTo("addressee", comparar)
                                .get()
                                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                    @Override
                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                                        if (queryDocumentSnapshots.isEmpty()){
                                            String ad = correo_amigo.getText().toString().trim();
                                            Friends friends = new Friends(ad, email);
                                            db.collection("friends")
                                                    .add(friends);
                                            Toast.makeText(getActivity(),"Se agrego exitosamente tu amigo",Toast.LENGTH_SHORT).show();
                                        }else{
                                            Toast.makeText(getActivity(),"Ya es tu amigo",Toast.LENGTH_SHORT).show();
                                        }
                                    }

                                }).addOnFailureListener(new OnFailureListener() {
                                 @Override
                                public void onFailure(@NonNull Exception e) {
                                     Toast.makeText(getActivity(),"Error al agregar",Toast.LENGTH_SHORT).show();
                                }
                                });
                    }
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
