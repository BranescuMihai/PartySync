package com.branes.partysync.activities.main;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.branes.partysync.R;
import com.branes.partysync.activities.peers.PeerActivity;
import com.branes.partysync.custom_ui_elements.CircularTextView;
import com.branes.partysync.helper.Constants;
import com.branes.partysync.helper.SecurityHelper;

import java.util.ArrayList;

/**
 * Copyright (c) 2017 Mihai Branescu
 */
public class MainActivity extends AppCompatActivity implements MainContract.View {

    private TextView changeSyncStateButton;
    private EditText insertPassword;
    private EditText insertUsername;
    private CircularTextView numberOfPeers;

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
    public String getUsername() {
        return insertUsername.getText().toString();
    }

    public void syncButtonClicked(View view) throws Exception {
        if (isSyncStarted()) {

            String password = insertPassword.getText().toString();

            if (password.length() < 5) {
                Toast.makeText(MainActivity.this, getString(R.string.password_error), Toast.LENGTH_SHORT).show();
            } else {
                if (insertUsername.getText().length() < 4) {
                    Toast.makeText(MainActivity.this, getString(R.string.username_error), Toast.LENGTH_SHORT).show();
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
        insertUsername.setText("");
        insertUsername.setEnabled(true);
    }

    private void disableFields() {
        insertPassword.setEnabled(false);
        insertUsername.setEnabled(false);
    }

    public void galleryButtonClicked(View view) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        Uri uri = Uri.parse(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getPath()
                + "/partySync/");
        intent.setDataAndType(uri, "image/*");
        startActivity(Intent.createChooser(intent, "Open folder"));
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {

            case Constants.REQUIRED_PERMISSIONS:
                if (grantResults.length > 0) {
                    for (int grant : grantResults) {
                        if (grant != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(MainActivity.this, getString(R.string.no_permissions_granted), Toast.LENGTH_SHORT).show();
                            checkPermissions();
                            break;
                        }
                    }
                    startServices();
                } else {
                    Toast.makeText(MainActivity.this, getString(R.string.no_permissions_granted), Toast.LENGTH_SHORT).show();
                    checkPermissions();
                }
                break;

            default:
                break;
        }
    }

    public void peersButtonClicked(View view) {

        if (!presenter.arePeersConnected()) {
            Toast.makeText(MainActivity.this, getString(R.string.no_peers_connected), Toast.LENGTH_SHORT).show();
            return;
        }

        Intent startPeerActivity = new Intent(MainActivity.this, PeerActivity.class);
        startActivity(startPeerActivity);
    }

    private void initViews() {
        changeSyncStateButton = (TextView) findViewById(R.id.change_sync_state_button);
        insertPassword = (EditText) findViewById(R.id.insert_password);
        insertUsername = (EditText) findViewById(R.id.insert_username);
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
