package com.abhijith.nanodegree.geonotes.View;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.abhijith.nanodegree.geonotes.Model.Friends;
import com.abhijith.nanodegree.geonotes.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    @BindView(R.id.nav_view)
    BottomNavigationView navView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        pushFragment(new FootprintFragment());
        navView.setOnNavigationItemSelectedListener(this);
    }

    /**
     * Method to push any fragment into given id.
     *
     * @param fragment An instance of Fragment to show into the given id.
     */
    protected boolean pushFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.rootLayout, fragment)
                    .addToBackStack(null)
                    .commit();
            return true;
        }
        return false;

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment fragment = null;

        switch (item.getItemId()) {
            case R.id.navigation_home:
                fragment = new FootprintFragment();
                break;

            case R.id.navigation_notes_list:
                fragment = new NotesListFragment();
                break;

                //agregado
            case R.id.navigation_shared:
                fragment = new SharedFragment();
                break;

            case R.id.navigation_friends:
                fragment = new FriendsFragment();
                break;
            case R.id.navigation_profile:
                fragment = new ProfileFragment();
                break;


        }

        return pushFragment(fragment);
    }
}
