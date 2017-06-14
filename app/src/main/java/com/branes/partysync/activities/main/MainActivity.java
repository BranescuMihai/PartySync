package com.branes.partysync.activities.main;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.branes.partysync.R;
import com.branes.partysync.activities.peers.PeerActivity;
import com.branes.partysync.custom_ui_elements.CircularTextView;
import com.branes.partysync.helper.Constants;
import com.branes.partysync.helper.SecurityHelper;
import com.branes.partysync.helper.Utilities;
import com.branes.partysync.network_communication.WifiStateChangedBroadcastReceiver;
import com.facebook.Profile;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, MainContract.View {

    private TextView changeSyncStateButton;
    private EditText insertPassword;
    private EditText insertGroupName;
    private CircularTextView numberOfPeers;
    private WifiStateChangedBroadcastReceiver wifiStateChangedBroadcastReceiver;

    private MainContract.Presenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();

        presenter = new MainPresenter(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        if (isSyncStarted()) {
            outState.putBoolean(Constants.SYNC_STATUS, true);
        } else {
            outState.putBoolean(Constants.SYNC_STATUS, false);
        }
        super.onSaveInstanceState(outState, outPersistentState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState.getBoolean(Constants.SYNC_STATUS)) {
            checkPermissions();
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_peers) {
            if (!presenter.arePeersConnected()) {
                Toast.makeText(this, getString(R.string.no_peers_connected), Toast.LENGTH_SHORT).show();
            } else {
                Intent startPeerActivity = new Intent(this, PeerActivity.class);
                startActivity(startPeerActivity);
            }
        } else if (id == R.id.nav_gallery) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            Uri uri = Uri.parse(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getPath()
                    + "/partySync/");
            intent.setDataAndType(uri, "image/*");
            startActivity(Intent.createChooser(intent, "Open folder"));
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (wifiStateChangedBroadcastReceiver != null) {
            unregisterReceiver(wifiStateChangedBroadcastReceiver);
        }
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public void setNumberOfPeers(final String peerNumber) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                numberOfPeers.setText(peerNumber);
            }
        });
    }

    @Override
    public String getGroupName() {
        return insertGroupName.getText().toString();
    }

    public void syncButtonClicked(View view) throws Exception {
        if (isSyncStarted()) {

            String password = insertPassword.getText().toString();

            if (password.length() < 5) {
                Toast.makeText(this, getString(R.string.password_error), Toast.LENGTH_SHORT).show();
            } else {
                if (insertGroupName.getText().length() < 4) {
                    Toast.makeText(this, getString(R.string.group_name_error), Toast.LENGTH_SHORT).show();
                } else {
                    SecurityHelper.initialize(password);
                    disableFields();
                    checkPermissions();
                }
            }
        } else {
            enableFields();
            stopServices();
        }
    }

    private void enableFields() {
        insertPassword.setText("");
        insertPassword.setEnabled(true);
        insertGroupName.setText("");
        insertGroupName.setEnabled(true);
    }

    private void disableFields() {
        insertPassword.setEnabled(false);
        insertGroupName.setEnabled(false);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {

            case Constants.REQUIRED_PERMISSIONS:
                if (grantResults.length > 0) {
                    for (int grant : grantResults) {
                        if (grant != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(this, getString(R.string.no_permissions_granted), Toast.LENGTH_SHORT).show();
                            checkPermissions();
                            break;
                        }
                    }
                    startServices();
                } else {
                    Toast.makeText(this, getString(R.string.no_permissions_granted), Toast.LENGTH_SHORT).show();
                    checkPermissions();
                }
                break;

            default:
                break;
        }
    }

    private void initViews() {

        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ImageView menuButton = (ImageView) findViewById(R.id.menu_button);
        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (drawer.isDrawerOpen(GravityCompat.START)) {
                    drawer.closeDrawer(GravityCompat.START);
                } else {
                    drawer.openDrawer(GravityCompat.START);
                }
            }
        });

        LinearLayout navHeader = (LinearLayout) navigationView.getHeaderView(0);
        TextView navHeaderTitle = (TextView) navHeader.findViewById(R.id.nav_header_title);
        ImageView navHeaderPicture = (ImageView) navHeader.findViewById(R.id.nav_header_picture);

        Profile profile = Profile.getCurrentProfile();
        Uri pictureFromFb = null;

        if(profile != null) {
            pictureFromFb = profile.getProfilePictureUri(160, 160);
            if (pictureFromFb != null) {
                Utilities.loadRoundedImage(this, pictureFromFb, navHeaderPicture);
            } else {
                navHeaderPicture.setImageDrawable(ContextCompat.getDrawable(this, R.mipmap.group_photo_backup));
            }
            if (profile.getName() != null) {
                navHeaderTitle.setText(profile.getName());
            }
        }

        changeSyncStateButton = (TextView) findViewById(R.id.change_sync_state_button);
        insertPassword = (EditText) findViewById(R.id.insert_password);
        insertGroupName = (EditText) findViewById(R.id.insert_group_name);
        numberOfPeers = (CircularTextView) findViewById(R.id.number_of_peers);
        numberOfPeers.setText("0");
        numberOfPeers.setStrokeWidth(1);
        numberOfPeers.setStrokeColor("#ffffff");
        numberOfPeers.setSolidColor("#FFB300");
    }

    private void checkPermissions() {
        int permissionCheckExternalStorage = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        int permissionCheckWifi = ContextCompat.checkSelfPermission(this, Manifest.permission.CHANGE_WIFI_STATE);
        int permissionReadPhoneState = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);

        if (permissionCheckExternalStorage == PackageManager.PERMISSION_GRANTED
                && permissionCheckWifi == PackageManager.PERMISSION_GRANTED
                && permissionReadPhoneState == PackageManager.PERMISSION_GRANTED) {
            startServices();
            return;
        }

        ArrayList<String> permissions = new ArrayList<>();

        if (permissionCheckExternalStorage != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }

        if (permissionCheckWifi != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.CHANGE_WIFI_STATE);
        }

        if (permissionReadPhoneState != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.READ_PHONE_STATE);
        }

        String[] permissionsArray = permissions.toArray(new String[permissions.size()]);

        ActivityCompat.requestPermissions(this, permissionsArray, Constants.REQUIRED_PERMISSIONS);
    }

    private void startServices() {
        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        wifiManager.setWifiEnabled(true);

        presenter.startServices();

        final IntentFilter filters = new IntentFilter();
        filters.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        filters.addAction("android.net.wifi.STATE_CHANGE");

        wifiStateChangedBroadcastReceiver = new WifiStateChangedBroadcastReceiver(this);
        registerReceiver(wifiStateChangedBroadcastReceiver, filters);

        changeSyncStateButton.setText(getResources().getString(R.string.stop_sync));
    }

    private void stopServices() {
        presenter.stopServices();
        changeSyncStateButton.setText(getResources().getString(R.string.start_sync));
    }

    private boolean isSyncStarted() {
        return changeSyncStateButton.getText().toString().equals(getResources().getString(R.string.start_sync));
    }
}
