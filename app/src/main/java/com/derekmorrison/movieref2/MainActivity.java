package com.derekmorrison.movieref2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {

    //private CoordinatorLayout layout;
    private DrawerLayout dLayout;
    private MainActivityFragment mMainActivityFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setup handler for uncaught exceptions.
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable e) {
                handleUncaughtException(thread, e);
            }
        });

        if (savedInstanceState == null ) {
            // make sure the data gets updated on the first time through
            Globals.getInstance().setRefreshNeeded(true);
        }

        // allow the app to rotate with the device
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);

        setContentView(R.layout.activity_main_nav);

        setNavigationDrawer();
        setToolBar();

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        mMainActivityFragment = new MainActivityFragment();
        transaction.replace(R.id.main_activity_frame, mMainActivityFragment);
        transaction.commit();

        if (findViewById(R.id.movie_detail_container) != null) {
            Globals.getInstance().setTwoPane(true);
            if (savedInstanceState == null) {
                transaction = getSupportFragmentManager().beginTransaction();
                MovieDetailActivityFragment mdaf= new MovieDetailActivityFragment();
                transaction.replace(R.id.movie_detail_container, mdaf);
                transaction.commit();
            }
        } else {
            Globals.getInstance().setTwoPane(false);
            //getSupportActionBar().setElevation(0f);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        // make sure the nav drawer is a match for the pref selection
        NavigationView navView = setNavDrawerSelection();
        //mMainActivityFragment.updateMovieList();
        //mMainActivityFragment.updateLoader();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    private void setToolBar() {
        Toolbar tb = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(tb);

        // give the 'home' indicator that 'hamburger' look
        ActionBar ab = getSupportActionBar();
        ab.setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);
        ab.setDisplayHomeAsUpEnabled(true);
    }

    private void setNavigationDrawer() {
        dLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        NavigationView navView = setNavDrawerSelection();

        // set up the listener for the nav Menu
        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {

                boolean itemSelected = true;
                String sortIt = "0";

                Globals.getInstance().setRefreshNeeded(true);

                switch (menuItem.getItemId()) {
                    case R.id.refresh:
                        // change settings to force a call to TMDB
                        sortIt = "-1";
                        Globals.getInstance().setManualRefresh(true);
                        mMainActivityFragment.updateMovieList();
                        break;

                    case R.id.highestRating:
                        sortIt = "0";
                        Globals.getInstance().setShowFavorites(false);
                        break;

                    case R.id.mostPopular:
                        sortIt = "1";
                        Globals.getInstance().setShowFavorites(false);
                        break;

                    case R.id.star:
                        //change settings to diaplay favorite movies
                        // force db update
                        sortIt = "2";
                        Globals.getInstance().setShowFavorites(true);
                        break;

                    default:
                        itemSelected = false;
                }

                // a recognized 'view type' menu item was selected, close the drawer and trigger the update
                if (itemSelected == true) {

                    // only update the 'sort order' if the user clicks on a sort order (not manual refresh)
                    if (sortIt != "-1") {
                        Globals.getInstance().setIsNewList(true);
                        Globals.getInstance().setRefreshNeeded(true);

                        // update the shared prefs behind the scene
                        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mMainActivityFragment.getActivity().getApplicationContext());
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString(getString(R.string.pref_sort_order), sortIt);
                        editor.commit();
                    }
                    dLayout.closeDrawers();
                    mMainActivityFragment.updateMovieList();
                    mMainActivityFragment.updateLoader();

                    return true;
                }
                return false;
            }
        });
    }

    private NavigationView setNavDrawerSelection() {

        NavigationView navView = (NavigationView) findViewById(R.id.navigation);

        // 0 = Highest Rating, 1 = Most Popular, 2 = Favorites
        String item_number = Utility.GetSortOrder(getBaseContext());
        int indexN = 1;

        // map the item_number from the pref settings to the nav Menu items
        switch (item_number){
            case "0": //R.id.highestRating:
                indexN = 2;
                break;
            case "1": //R.id.mostPopular:
                indexN = 1;
                break;
            case "2": // R.id.star:
                indexN = 3;
                break;
            default:
                indexN = 1;
        }

        // make the visual cue of the nav Menu item be 'checked' to match the pref setting
        navView.getMenu().getItem(indexN).setChecked(true);

        return navView;
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            // use an Intent to launch the SettingsActivity
            // Adding these EXTRAs tells the O/S to skip over the HEADER page and go straight to the GeneralPreferenceFragment
            Intent settingsIntent =  new Intent(this, SettingsActivity.class);
            settingsIntent.putExtra(PreferenceActivity.EXTRA_NO_HEADERS, true);
            settingsIntent.putExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT, SettingsActivity.GeneralPreferenceFragment.class.getName());
            startActivity(settingsIntent);
            return true;
        }

        // Android home means show the nav drawer
        if (id == android.R.id.home) {
            dLayout.openDrawer(GravityCompat.START);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // taken from:   http://stackoverflow.com/questions/19897628/need-to-handle-uncaught-exception-and-send-log-file
    // did not include the sending of a log file at this time
    public void handleUncaughtException (Thread thread, Throwable e)
    {
        e.printStackTrace(); // not all Android versions will print the stack trace automatically

        // ask the O/S to end this app
        finish();
    }
}
