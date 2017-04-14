package com.saarthicareer.saarthicareer.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.saarthicareer.saarthicareer.R;
import com.saarthicareer.saarthicareer.fragment.AboutFragment;
import com.saarthicareer.saarthicareer.fragment.FeedbackFragment;
import com.saarthicareer.saarthicareer.fragment.FragmentDrawer;
import com.saarthicareer.saarthicareer.fragment.HomeFragment;
import com.saarthicareer.saarthicareer.fragment.SettingsFragment;

public class MainActivity extends ActionBarActivity implements FragmentDrawer.FragmentDrawerListener {

    private int fragno;
    private FirebaseAuth firebaseAuth;
    private Toolbar mToolbar;
    private FragmentDrawer drawerFragment;
    private DrawerLayout drawerLayout;

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState){
        savedInstanceState.putInt("fragno",fragno);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firebaseAuth = FirebaseAuth.getInstance();
        if(firebaseAuth.getCurrentUser() == null){
            //Start login activity
            finish();
            startActivity(new Intent(this, LoginActivity.class));
            return;
        }
        setContentView(R.layout.activity_main);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        drawerFragment = (FragmentDrawer)getSupportFragmentManager().findFragmentById(R.id.fragment_navigation_drawer);
        drawerLayout=(DrawerLayout) findViewById(R.id.drawer_layout);
        drawerFragment.setUp(R.id.fragment_navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout), mToolbar);
        drawerFragment.setDrawerListener(this);

        getSupportFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {

            @Override
            public void onBackStackChanged() {
                Fragment f = getSupportFragmentManager().findFragmentById(R.id.container_body);
                if (f != null){
                    updateTitleAndDrawer(f);
                }
            }
        });

        if(savedInstanceState != null){
            int no = savedInstanceState.getInt("fragno");
            displayView(no);
        }
        else
        {
            displayView(0);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Fragment fragment = new SettingsFragment();
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.container_body, fragment , "settings");
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();

            getSupportActionBar().setTitle("Settings");
            return true;
        }

        if(id == R.id.action_logout){
            firebaseAuth.signOut();
            finish();
            startActivity(new Intent(this, LoginActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDrawerItemSelected(View view, int position) {
        displayView(position);
    }

    private void displayView(int position) {
        Fragment fragment = null;
        String title = getString(R.string.app_name);
        switch (position) {
            case 0:
                fragment = new HomeFragment();
                title = "Saarthi Career";
                break;
            case 1:
                fragment = new FeedbackFragment();
                title = "Feedback";
                break;
            case 2:
                fragment = new AboutFragment();
                title = "About";
                break;
            case 3:
                fragment = new SettingsFragment();
                title = "Settings";
                break;
            default:
                break;
        }

        if (fragment != null) {
            String backStateName =  fragment.getClass().getName();

            FragmentManager manager = getSupportFragmentManager();
            boolean fragmentPopped = manager.popBackStackImmediate (backStateName, 0);

            if (!fragmentPopped && manager.findFragmentByTag(backStateName) == null){ //fragment not in back stack, create it.
                FragmentTransaction ft = manager.beginTransaction();
                ft.replace(R.id.container_body, fragment, backStateName);
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                ft.addToBackStack(backStateName);
                ft.commit();
            }
            // set the toolbar title
            getSupportActionBar().setTitle(title);
        }
    }

    private void updateTitleAndDrawer (Fragment fragment){
        String fragClassName = fragment.getClass().getName();

        if (fragClassName.equals(HomeFragment.class.getName())){
            getSupportActionBar().setTitle(getString(R.string.app_name));
        }
        else if (fragClassName.equals(FeedbackFragment.class.getName())){
            getSupportActionBar().setTitle(getString(R.string.nav_item_feedback));
        }
        else if (fragClassName.equals(AboutFragment.class.getName())){
            getSupportActionBar().setTitle(getString(R.string.nav_item_about));
        }
        else if (fragClassName.equals(SettingsFragment.class.getName())){
            getSupportActionBar().setTitle(getString(R.string.nav_item_settings));
        }
    }

    @Override
    public void onBackPressed(){
        if (this.drawerLayout.isDrawerVisible(GravityCompat.START)) {
            this.drawerLayout.closeDrawer(GravityCompat.START);
        } else if (getSupportFragmentManager().getBackStackEntryCount() == 1) {
            finish();
        } else {
            super.onBackPressed();
        }
    }
}