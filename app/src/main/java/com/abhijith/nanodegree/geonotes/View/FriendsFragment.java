package com.abhijith.nanodegree.geonotes.View;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


import androidx.fragment.app.Fragment;

import com.abhijith.nanodegree.geonotes.R;
import com.google.android.gms.maps.model.LatLng;

public class FriendsFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_friend,container,false);
        Button button_friend = view.findViewById((R.id.button_friend));
        button_friend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mostrarDialogo();
            }
        });
        return view;
    }

    private void mostrarDialogo(){
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
