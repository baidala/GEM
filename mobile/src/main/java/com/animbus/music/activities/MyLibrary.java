package com.animbus.music.activities;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.animbus.music.MediaController;
import com.animbus.music.R;
import com.animbus.music.ThemeManager;
import com.animbus.music.data.DataManager;
import com.animbus.music.data.SettingsManager;
import com.animbus.music.data.adapter.AlbumGridAdapter;
import com.animbus.music.data.adapter.SongListAdapter;
import com.animbus.music.data.dataModels.AlbumGridDataModel;

import java.util.List;


public class MyLibrary extends AppCompatActivity implements AlbumGridAdapter.AlbumArtGridClickListener, NavigationView.OnNavigationItemSelectedListener {
    public RecyclerView mainList;
    String AlbumName, AlbumArtist, currentScreenName;
    int AlbumArt = R.drawable.album_art;
    MediaController musicControl;
    Configuration config;
    DataManager dataManager;
    SettingsManager settings;
    Toolbar toolbar;
    DrawerLayout drawerLayout;
    NavigationView drawerContent;
    ThemeManager themeManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        settings = new SettingsManager(this);
        themeManager = new ThemeManager(this, ThemeManager.TYPE_NORMAL);

        setTheme(themeManager.getCurrentTheme());
        setContentView(R.layout.activity_main);
        findViewById(R.id.MainView).setBackgroundColor(themeManager.getCurrentBackgroundColor());

        //This sets all of the variables
        toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        musicControl = new MediaController(this);
        dataManager = new DataManager(this);
        mainList = (RecyclerView) findViewById(R.id.MyLibraryMainListLayout);
        drawerContent = (NavigationView) findViewById(R.id.navigation);

        drawerContent.setNavigationItemSelectedListener(this);

        //Basic Stuff
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        updateSettings(this);
        setUpNavdrawer(drawerLayout, toolbar);
    }

    public void end() {
        finish();
    }

    private void updateSettings(Context cxt) {
        //Sets Dynamic Title
        if (settings.getBooleanSetting(SettingsManager.KEY_USE_CATEGORY_NAMES_ON_MAIN_SCREEN, false)) {
            toolbar.setTitle(currentScreenName);
        } else {
            toolbar.setTitle(cxt.getResources().getString(R.string.main_title));
        }

        //Sets Window description in Multitasking menu
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (!settings.getBooleanSetting(SettingsManager.KEY_USE_LIGHT_THEME, false)) {
                Bitmap bm = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_new_light);
                setTaskDescription(new ActivityManager.TaskDescription(null, bm, cxt.getResources().getColor(R.color.primaryDark)));
                bm.recycle();
            } else {
                Bitmap bm = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_new_dark);
                setTaskDescription(new ActivityManager.TaskDescription(null, bm, cxt.getResources().getColor(R.color.primaryLight)));
                bm.recycle();
            }
        }

        //This sets up the RecyclerView to the default screen based on a setting.
        Integer setting = settings.getIntegerSetting(SettingsManager.KEY_DEFAULT_SCREEN, SettingsManager.SCREEN_ALBUMS);
        if (setting == 0) {
            Toast.makeText(this, "What?", Toast.LENGTH_LONG).show();
        } else if (setting == 1) {
            switchToAlbum();
        } else if (setting == 2) {
            switchToSongs();
        } else if (setting == 3) {
            switchToArtists();
        } else if (setting == 4) {
            switchToPlaylists();
        }
    }

    private void setUpNavdrawer(DrawerLayout drawerLayout, Toolbar toolbar) {
        final ActionBarDrawerToggle mDrawerToggle;
        mDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                invalidateOptionsMenu();
            }
        };
        drawerLayout.setDrawerListener(mDrawerToggle);
        drawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });
        /*drawerContent.setItemIconTintList(new ColorStateList(
                new int[][]{
                        {android.R.attr.state_checked}, //Checked
                        {} //default
                },
                new int[]{
                        R.attr.colorAccent, //Checked
                        android.R.attr
                }
        ));
        drawerContent.setItemTextColor(new ColorStateList(
                new int[][]{
                        {android.R.attr.state_checked} //Checked
                },
                new int[]{
                        R.attr.colorAccent //Checked
                }
        ));*/
    }


    @Override
    public void AlbumGridItemClicked(View view, int position, List<AlbumGridDataModel> data) {
        AlbumGridDataModel current = data.get(position);
        //The intent
        Intent albums = new Intent(this, albums_activity.class);
        //The Album Info
        Bundle albumsInfo = new Bundle();
        albumsInfo.putString("ALBUM_NAME", current.AlbumGridAlbumName);
        albumsInfo.putString("ALBUM_ARTIST", current.AlbumGridAlbumArtist);
        albumsInfo.putInt("ALBUM_ART", current.AlbumGridAlbumart);
        albums.putExtras(albumsInfo);
        startActivity(albums);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent intent = new Intent();
            intent.setClass(this, Settings.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem) {
        int id = menuItem.getItemId();
        switch (id) {
            case R.id.navdrawer_album_icon:
                switchToAlbum();
                break;
            case R.id.navdrawer_songs:
                switchToSongs();
                break;
            case R.id.navdrawer_playlists:
                switchToPlaylists();
                break;
            case R.id.navdrawer_artists:
                switchToArtists();
                break;
            case R.id.navdrawer_settings:
                Intent intent = new Intent();
                intent.setClass(this, Settings.class);
                startActivity(intent);
                break;
        }
        menuItem.setChecked(true);
        return true;
    }

    //This section is where you select which view to see. Only views with back arrows should be set as separate activities.
    //Add code to this section as necessary (For example:If you need to update the list of songs in 'switchToSongs' you can add updateSongList(), or if you add a extra view add it to all sections)
    public void switchToAlbum() {
        //Configures the Recyclerview
        AlbumGridAdapter adapter = new AlbumGridAdapter(this, dataManager.getAlbumGridData(settings.getBooleanSetting(SettingsManager.KEY_USE_PALETTE_IN_GRID, true)));
        adapter.setOnItemClickedListener(this);
        mainList.setAdapter(adapter);
        mainList.setItemAnimator(new DefaultItemAnimator());
        config = getResources().getConfiguration();
        if (config.orientation == Configuration.ORIENTATION_PORTRAIT) {
            mainList.setLayoutManager(new GridLayoutManager(this, 2, GridLayoutManager.VERTICAL, false));
        } else if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mainList.setLayoutManager(new GridLayoutManager(this, 3, GridLayoutManager.VERTICAL, false));
        }
        //TODO:Use resource
        currentScreenName = "Albums";
        if (settings.getBooleanSetting(SettingsManager.KEY_USE_CATEGORY_NAMES_ON_MAIN_SCREEN, false)) {
            toolbar.setTitle(currentScreenName);
        }
        //Closes the Navdrawer
        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerLayout.closeDrawers();
    }

    public void switchToSongs() {
        //Configures the Recyclerview
        SongListAdapter adapter = new SongListAdapter(this, dataManager.getSongListData());
        mainList.setAdapter(adapter);
        mainList.setItemAnimator(new DefaultItemAnimator());
        mainList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        //TODO:Use Resource
        currentScreenName = "Songs";
        if (settings.getBooleanSetting(SettingsManager.KEY_USE_CATEGORY_NAMES_ON_MAIN_SCREEN, false)) {
            toolbar.setTitle(currentScreenName);
        }
        //Closes the Navdrawer
        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerLayout.closeDrawers();
    }

    public void switchToPlaylists() {
        //Sets the current screen
        //TODO:Use Resource
        currentScreenName = "Playlists";
        if (settings.getBooleanSetting(SettingsManager.KEY_USE_CATEGORY_NAMES_ON_MAIN_SCREEN, false)) {
            toolbar.setTitle(currentScreenName);
        }
        //Closes the Navdrawer
        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerLayout.closeDrawers();
    }

    public void switchToArtists() {
        //Sets the current screen
        //TODO:Use Resource
        currentScreenName = "Artists";
        if (settings.getBooleanSetting(SettingsManager.KEY_USE_CATEGORY_NAMES_ON_MAIN_SCREEN, false)) {
            toolbar.setTitle(currentScreenName);
        }
        //Closes the Navdrawer
        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerLayout.closeDrawers();
    }

    public void switchTo(View v) {
        Intent intent = new Intent(this, NowPlayingPeek.class);
        startActivity(intent);
    }
    //End Section


    //This section is for the helper methods

    //End section


    //This section is where you can open other screens (Now Playing, Albums Details, Playlist Details, etc.)
    //You add the Bundle and Intent for the alternate activities

    //This opens the album details screen
    public void openAlbums(View v) {
        //Original Album
        AlbumName = "Better Off Ted";
        AlbumArtist = "Filbert";
        AlbumArt = R.drawable.album_art;
        //The intent
        Intent albums = new Intent(this, albums_activity.class);
        //The Album Info
        Bundle albumsInfo = new Bundle();
        albumsInfo.putString("ALBUM_NAME", AlbumName);
        albumsInfo.putString("ALBUM_ARTIST", AlbumArtist);
        albumsInfo.putInt("ALBUM_ART", AlbumArt);
        albums.putExtras(albumsInfo);
        startActivity(albums);
    }
    //End Section
}
