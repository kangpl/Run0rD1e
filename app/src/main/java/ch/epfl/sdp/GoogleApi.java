package ch.epfl.sdp;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Pair;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Math.toDegrees;

public class GoogleApi implements MapApi {
    private static double listenTime = 1000; // milliseconds
    private static double listenDistance = 5; // meters

    private Location currentLocation = new Location("init");
    private LocationManager locationManager;
    private Criteria criteria;
    private String bestProvider;
    private LocationListener locationListener;
    private GoogleMap mMap;
    private Activity activity;
    private Pair<Marker, Circle> myCircle;
    private Map<Enemy, Pair<Marker, Circle>> enemiesCircles;
    private List<Enemy> tempList;

    public GoogleApi(LocationManager locationManager, Activity activity) {
        Enemy tempEnemy = new Enemy(6, 45, 10000);
        tempList = new ArrayList();
        tempList.add(tempEnemy);

        this.locationManager = locationManager;
        this.activity = activity;

        enemiesCircles = new HashMap<>();

        // setup bestProvider
        criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        bestProvider = locationManager.getBestProvider(criteria, true);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location latestLocation) {
                // Called when a new location is found by the network location provider.
                updatePosition();
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {}

            @Override
            public void onProviderEnabled(String provider) {}

            @Override
            public void onProviderDisabled(String provider) {}
        };
    }

    @Override
    public Location getCurrentLocation() {
        return currentLocation;
    }

    @Override
    public void updatePosition() {
        displayEnemies(tempList);
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 101);
            return;
        }

        for (String locManager : locationManager.getAllProviders()) {
            locationManager.requestLocationUpdates(locManager, (long) listenTime, (float) listenDistance, locationListener);
        }

        bestProvider = locationManager.getBestProvider(criteria, true);
        currentLocation = locationManager.getLastKnownLocation(bestProvider);
        if (currentLocation == null) {
            return;
        }

        LatLng myPos = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        if (myCircle != null) {
            myCircle.first.remove();
            myCircle.second.remove();
        }
        myCircle = new Pair<>(mMap.addMarker(new MarkerOptions().position(myPos).title("My position")),
                mMap.addCircle(new CircleOptions().center(myPos).strokeColor(Color.BLUE).fillColor(Color.argb(128, 30,144,255)).radius(1000)));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(myPos));
    }

    @Override
    public void displayEnemies(List<Enemy> enemies) {
        if (enemies == null) {
            return;
        }
        for (Enemy enemy: enemies) {
            if (enemiesCircles.containsKey(enemy)) {
                enemiesCircles.get(enemy).first.remove();
                enemiesCircles.get(enemy).second.remove();
            }
            LatLng enemyPosition = new LatLng(toDegrees(enemy.getLocation().latitude()), toDegrees(enemy.getLocation().longitude()));
            Bitmap smallMarker = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(activity.getResources(), R.drawable.robot), 100, 100, false);
            enemiesCircles.put(enemy, new Pair<>(mMap.addMarker(new MarkerOptions()
                    .position(enemyPosition)
                    .title("Enemy")
                    .icon(BitmapDescriptorFactory.fromBitmap(smallMarker))),
                    mMap.addCircle(new CircleOptions().center(enemyPosition).radius(enemy.getAoeRadius())
                            .fillColor(Color.argb(128, 255, 51, 51)).strokeColor(Color.RED))));
        }

    }

    public void setMap(GoogleMap googleMap) {
        mMap = googleMap;
    }
}
