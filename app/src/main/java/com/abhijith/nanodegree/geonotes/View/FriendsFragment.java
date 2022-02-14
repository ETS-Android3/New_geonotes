package com.abhijith.nanodegree.geonotes.View;

import android.app.AlertDialog;
import android.os.Bundle;
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
        Button buttonFriend = (Button)view.findViewById(R.id.button_friend);
        return view;
    }
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // Setup any handles to view objects here
        // EditText etFoo = (EditText) view.findViewById(R.id.etFoo);
    }
    private void dialogFriend(View view) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this.getContext());
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.custom_dialog, null);
        dialog.setView(dialogView);
        dialog.setCancelable(true);
        dialog.setIcon(R.drawable.geonotes_logo_round);
        dialog.setTitle(getString(R.string.dialog_add_note_title));
    }
}
