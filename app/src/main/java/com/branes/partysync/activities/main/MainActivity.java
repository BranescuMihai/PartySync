package com.branes.partysync.activities.main;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.branes.partysync.R;
import com.branes.partysync.helper.Constants;
import com.branes.partysync.helper.ObjectObserver;
import com.branes.partysync.model.NetworkService;
import com.branes.partysync.network_communication.NetworkServiceDiscoveryOperations;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements MainContract.View, Observer {

    private static final String TAG = MainActivity.class.getName();

    private TextView changeSyncStateButton;
    private MainContract.Presenter presenter;
    private NetworkServiceDiscoveryOperations networkServiceDiscoveryOperations = null;

    private ArrayList<NetworkService> discoveredServices;
    private ArrayList<NetworkService> conversations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        changeSyncStateButton = (TextView) findViewById(R.id.change_sync_state_button);

        setNetworkServiceDiscoveryOperations(new NetworkServiceDiscoveryOperations(this));

        discoveredServices = new ArrayList<>();
        conversations = new ArrayList<>();

        presenter = new MainPresenter(this);
        ObjectObserver.getInstance().addObserver(this);
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
            checkPermission();
        }
    }

    @Override
    public Context getContext() {
        return this;
    }

    public void syncButtonClicked(View view) throws Exception {
        if (isSyncStarted()) {
            checkPermission();
        } else {
            stopServices();
        }
    }

    public void galleryButtonClicked(View view) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        Uri uri = Uri.parse(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getPath());
        intent.setDataAndType(uri, "image/*");
        startActivity(Intent.createChooser(intent, "Open folder"));
    }

    public void setNetworkServiceDiscoveryOperations(NetworkServiceDiscoveryOperations networkServiceDiscoveryOperations) {
        this.networkServiceDiscoveryOperations = networkServiceDiscoveryOperations;
    }

    public ArrayList<NetworkService> getDiscoveredServices() {
        return discoveredServices;
    }

    public void setDiscoveredServices(ArrayList<NetworkService> discoveredServices) {
        this.discoveredServices = discoveredServices;
    }

    public ArrayList<NetworkService> getConversations() {
        return conversations;
    }

    public void setConversations(ArrayList<NetworkService> conversations) {
        this.conversations = conversations;
    }

    @Override
    public void update(Observable o, Object arg) {
        Log.d(TAG, arg.toString());

        for (int i = 0; i < networkServiceDiscoveryOperations.getCommunicationToServers().size(); i++) {
            networkServiceDiscoveryOperations.getCommunicationToServers().get(i).sendImage((byte[]) arg);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {

            case Constants.READ_EXTERNAL_STORAGE:
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    startServices();
                }
                break;

            default:
                break;
        }
    }

    private void checkPermission() {
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, Constants.READ_EXTERNAL_STORAGE);
        } else {
            startServices();
        }
    }

    private void startServices() {
        presenter.startJobScheduler();
        try {
            networkServiceDiscoveryOperations.registerNetworkService(new Random().nextInt(100) + 2000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        networkServiceDiscoveryOperations.startNetworkServiceDiscovery();
        changeSyncStateButton.setText(getResources().getString(R.string.stop_sync));
    }

    private void stopServices() {
        presenter.stopJobScheduler();
        networkServiceDiscoveryOperations.unregisterNetworkService();
        networkServiceDiscoveryOperations.stopNetworkServiceDiscovery();
        changeSyncStateButton.setText(getResources().getString(R.string.start_sync));
    }

    private boolean isSyncStarted() {
        return changeSyncStateButton.getText().toString().equals(getResources().getString(R.string.start_sync));
    }
}
