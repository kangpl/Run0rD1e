package ch.epfl.sdp.map;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.Map;

import ch.epfl.sdp.R;
import ch.epfl.sdp.entity.PlayerManager;
import ch.epfl.sdp.geometry.GeoPoint;
import ch.epfl.sdp.geometry.PointConverter;

public class GoogleMapApi implements MapApi {
    private static double listenTime = 1000; // milliseconds
    private static double listenDistance = 5; // meters
    private Location currentLocation;
    private LocationManager locationManager;
    private Criteria criteria;
    private String bestProvider;
    private LocationListener locationListener;
    private GoogleMap mMap;
    private Activity activity;
    private Map<Displayable, MapDrawing> entityCircles;

    private boolean hasMovedToPlayerPositionOnStart = false;

    public GoogleMapApi() {
        entityCircles = new HashMap<>();
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

    public void initializeApi(LocationManager locationManager, Activity activity) {
        this.locationManager = locationManager;
        this.activity = activity;

        criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        bestProvider = locationManager.getBestProvider(criteria, true);
    }

    @Override
    public Activity getActivity() {
        return activity;
    }

    @Override
    public GeoPoint getCurrentLocation() {
        if (currentLocation != null)
            return new GeoPoint(currentLocation.getLongitude(), currentLocation.getLatitude());
        else
            return null;
    }

    @Override
    public void updatePosition() {
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
        PlayerManager.getCurrentUser().setLocation(getCurrentLocation());
        PlayerManager.getCurrentUser().setPosition(PointConverter.geoPointToCartesianPoint(PlayerManager.getCurrentUser().getLocation()));
        removeMarkers(PlayerManager.getCurrentUser());
        displayMarkerCircle(PlayerManager.getCurrentUser(), Color.BLUE, "My position", 10);
    }

    @Override
    public void moveCameraOnCurrentLocation() {
        if (currentLocation == null) {
            return;
        }
        navigateCameraToPlayerPosition();
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude())));
    }

    public void setMap(GoogleMap googleMap) {
        mMap = googleMap;
        hasMovedToPlayerPositionOnStart = false;
    }

    public Bitmap createSmallCircle(int color) {
        Bitmap output = Bitmap.createBitmap(25,
                25, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        Paint paint = new Paint();
        paint.setColor(color);
        canvas.drawCircle(25 / 2, 25 / 2,
                25 / 2, paint);
        return output;
    }

    @Override
    synchronized public void displayEntity(Displayable displayable) {
        activity.runOnUiThread(() -> {
            removeMarkers(displayable);
            /*
            if(displayable instanceof DisplayableWithIcon) {
                DisplayableWithIcon displayableWithIcon = (DisplayableWithIcon)displayable;
                displaySmallIcon(displayableWithIcon, displayableWithIcon.getName(), displayableWithIcon.getDrawableIconId());
            }
            */

            switch (displayable.getEntityType()) {
                // only display User when position is updated
                case ENEMY:
                    //displayMarkerCircle(displayable, Color.RED, "Enemy", 1000);
                    displaySmallIcon(displayable, "Enemy", R.drawable.enemy); break;
                case PLAYER:
                    displayMarkerCircle(displayable, Color.YELLOW, "Other player", 100); break;
                case ITEMBOX:
                    displaySmallIcon(displayable, "ItemBox", R.drawable.itembox);
                    break;
                case TRAP:
                    displaySmallIcon(displayable, "My trap", R.drawable.trap);
                    break;
                case SHELTER:
                    displayMarkerCircle(displayable, 0x7fffbf, "Shelter Area", 100);
                    break;
            }
        });
    }

    @Override
    synchronized public void unDisplayEntity(Displayable displayable) {
        activity.runOnUiThread(() -> {
            removeMarkers(displayable);
            entityCircles.remove(displayable);
        });
    }

    private void removeMarkers(Displayable displayable) {
        if (entityCircles.containsKey(displayable)) {
            if (entityCircles.get(displayable).hasMarker())
                entityCircles.get(displayable).getMarker().remove();
            if (entityCircles.get(displayable).hasCircle())
                entityCircles.get(displayable).getAoe().remove();
        }
    }

    public void displaySmallIcon(Displayable displayable, String title, int id) {
        LatLng position = new LatLng(displayable.getLocation().getLatitude(), displayable.getLocation().getLongitude());
        entityCircles.put(displayable, new MapDrawing(mMap.addMarker(new MarkerOptions()
                .position(position)
                .title(title)
                .icon(BitmapDescriptorFactory.fromResource(id)))));
    }

    private void displayMarkerCircle(Displayable displayable, int color, String title, int aoeRadius) {
        LatLng position = new LatLng(displayable.getLocation().getLatitude(), displayable.getLocation().getLongitude());
        entityCircles.put(displayable, new MapDrawing(mMap.addMarker(new MarkerOptions()
                .position(position)
                .title(title)
                .icon(BitmapDescriptorFactory.fromBitmap(createSmallCircle(color)))),
                mMap.addCircle(new CircleOptions()
                        .center(position)
                        .strokeColor(color)
                        .fillColor(color-0x80000000)
                        .radius(aoeRadius)
                        .strokeWidth(1f))));
    }

    private void navigateCameraToPlayerPosition() {
        LatLng currentLocationLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocationLatLng, 14));
    }
}
