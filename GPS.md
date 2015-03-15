# Introduction #

The answer to the question "Where am I?" can be given by the [Location Manager](http://developer.android.com/reference/android/location/LocationManager.html). It provides access to different location providers, such as GPS and network positioning.

# Setting up the listener for location updates #

As of now (Mar 01st), acquiring the current GPS location with

```
// Acquire a reference to the system Location Manager
LocationManager lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

// Define a listener that responds to location updates
LocationListener locationListener = new LocationListener() {
    public void onLocationChanged(Location location) {}

    public void onStatusChanged(String provider, int status, Bundle extras) {}

    public void onProviderEnabled(String provider) {}

    public void onProviderDisabled(String provider) {}
};

// Register the listener with the Location Manager to receive location updates
lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
```

works using the LocationListener#onLocationChanged method.

```
public void onLocationChanged(Location location) {
    TextView t = (TextView) findViewById(R.id.textView1);
    t.setText(location.toString());
}
```

This code snippet for example updates textView1 whenever the current location has changed.