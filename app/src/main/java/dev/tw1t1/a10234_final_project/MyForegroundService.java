package dev.tw1t1.a10234_final_project;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.gms.location.CurrentLocationRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Granularity;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MyForegroundService extends Service {
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private Location lastLocation;


    @SuppressLint("MissingPermission")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        LocationRequest locationRequest = new LocationRequest.Builder(10000)
                .setGranularity(Granularity.GRANULARITY_FINE)
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .setMinUpdateDistanceMeters(10) // Only update if moved 10 meters
                .setMinUpdateIntervalMillis(60000) // Minimum time between updates: 1 minute
                .build();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                Location location = locationResult.getLastLocation();
                if (location != null && (lastLocation == null || isSignificantChange(lastLocation, location))) {
                    sendLocationToFirebase(location);
                    lastLocation = location;
                }
            }
        };

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);

        final String CHANNELID = "Foreground Service ID";
        NotificationChannel channel = new NotificationChannel(
                CHANNELID,
                CHANNELID,
                NotificationManager.IMPORTANCE_LOW
        );

        getSystemService(NotificationManager.class).createNotificationChannel(channel);
        Notification.Builder notification = new Notification.Builder(this, CHANNELID)
                .setContentText("Service is running")
                .setContentTitle("Service enabled")
                .setSmallIcon(R.drawable.ic_launcher_background);
        startForeground(1001, notification.build());

        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private boolean isSignificantChange(Location last, Location current) {
        float distance = last.distanceTo(current);
        long timeDifference = current.getTime() - last.getTime();

        // Consider it significant if moved more than 10 meters or if more than 1 minute has passed
        return distance > 10 || timeDifference > 60000;
    }

    private void sendLocationToFirebase(Location location) {
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("locations");
        String deviceId = "device1"; // Use a unique identifier for the sending device

        LocationData locationData = new LocationData(
                location.getLatitude(),
                location.getLongitude(),
                location.getTime()
        );

        databaseRef.child(deviceId).setValue(locationData);
    }
}
