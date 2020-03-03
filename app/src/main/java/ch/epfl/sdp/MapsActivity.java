package ch.epfl.sdp;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private Location currentLocation;
    private LocationManager locationManager;
    private Criteria criteria;
    private String bestProvider;
    private LocationListener locationListener;
    private Marker marker;

    public static double netlistentime = 0 * 60 * 1000; // minutes * 60 sec/min * 1000 for milliseconds
    public static double netlistendistance = 0 * 1609.344; // miles * conversion to meters
    public static double gpslistentime = 30 * 60 * 1000; // minutes * 60 sec/min * 1000 for milliseconds
    public static double gpslistendistance = 0 * 1609.344; // miles * conversion to meters


    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        Button button = (Button) findViewById(R.id.update_loc);

        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                updatePosition();
            }
        });

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // setup bestProvider
        criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        bestProvider = locationManager.getBestProvider(criteria, true);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location latestlocation) {
                // Called when a new location is found by the network location provider.
                currentLocation.set(latestlocation);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(MapsActivity.this);
    }

    private void updatePosition() {
        if (marker != null) {
            marker.remove();
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 101);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                return;
        }

        boolean isNetEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if(isNetEnabled) {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, (long) netlistentime, (float) netlistendistance, locationListener);
        }
        if(isGpsEnabled) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, (long) gpslistentime, (float) gpslistendistance, locationListener);
        }

        bestProvider = locationManager.getBestProvider(criteria, true);
        currentLocation = locationManager.getLastKnownLocation(bestProvider);
        LatLng myPos = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        marker = mMap.addMarker(new MarkerOptions().position(myPos).title("My position"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(myPos));
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        updatePosition();
    }
}
