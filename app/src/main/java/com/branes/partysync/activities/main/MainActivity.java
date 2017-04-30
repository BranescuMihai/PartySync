package com.branes.partysync.activities.main;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
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

    private boolean serviceRegistrationStatus = false;
    private boolean serviceDiscoveryStatus = false;

    private ArrayList<NetworkService> discoveredServices    ;
    private ArrayList<NetworkService> conversations;

    private Handler handler = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        changeSyncStateButton = (TextView) findViewById(R.id.change_sync_state_button);

        setHandler(new Handler());
        setNetworkServiceDiscoveryOperations(new NetworkServiceDiscoveryOperations(this));

        discoveredServices = new ArrayList<>();
        conversations = new ArrayList<>();

        presenter = new MainPresenter(this);
        ObjectObserver.getInstance().addObserver(this);

    }

    @Override
    public Context getContext() {
        return this;
    }

    public void syncButtonClicked(View view) throws Exception {
        if (changeSyncStateButton.getText().toString().equals(getResources().getString(R.string.start_sync))) {
            checkPermission();
        } else {
            presenter.stopJobScheduler();
            networkServiceDiscoveryOperations.unregisterNetworkService();
            networkServiceDiscoveryOperations.stopNetworkServiceDiscovery();
            setServiceDiscoveryStatus(false);
            setServiceRegistrationStatus(false);
            changeSyncStateButton.setText(getResources().getString(R.string.start_sync));
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
        setServiceDiscoveryStatus(true);
        setServiceRegistrationStatus(true);
        changeSyncStateButton.setText(getResources().getString(R.string.stop_sync));
    }

    public void galleryButtonClicked(View view) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        Uri uri = Uri.parse(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getPath());
        intent.setDataAndType(uri, "image/*");
        startActivity(Intent.createChooser(intent, "Open folder"));
    }

    public Handler getHandler() {
        return handler;
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    public NetworkServiceDiscoveryOperations getNetworkServiceDiscoveryOperations() {
        return networkServiceDiscoveryOperations;
    }

    public void setNetworkServiceDiscoveryOperations(NetworkServiceDiscoveryOperations networkServiceDiscoveryOperations) {
        this.networkServiceDiscoveryOperations = networkServiceDiscoveryOperations;
    }

    public boolean getServiceRegistrationStatus() {
        return serviceRegistrationStatus;
    }

    public void setServiceRegistrationStatus(boolean serviceRegistrationStatus) {
        this.serviceRegistrationStatus = serviceRegistrationStatus;
    }

    public boolean getServiceDiscoveryStatus() {
        return serviceDiscoveryStatus;
    }

    public void setServiceDiscoveryStatus(boolean serviceDiscoveryStatus) {
        this.serviceDiscoveryStatus = serviceDiscoveryStatus;
    }

    public ArrayList<NetworkService> getDiscoveredServices() {
        return discoveredServices;
    }

    public void setConversations(final ArrayList<NetworkService> conversations) {
        this.conversations = conversations;
    }

    public ArrayList<NetworkService> getConversations() {
        return conversations;
    }

    @Override
    public void update(Observable o, Object arg) {
        Log.d(TAG, arg.toString());

        for(int i = 0; i < networkServiceDiscoveryOperations.getCommunicationToServers().size(); i++) {
            networkServiceDiscoveryOperations.getCommunicationToServers().get(i).sendMessage((byte[])arg);
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

    private void checkPermission(){
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, Constants.READ_EXTERNAL_STORAGE);
        } else {
            startServices();
        }
    }
}
