Introduction
============

The answer to the question "Where am I?" can be given by the [Location
Manager](http://developer.android.com/reference/android/location/LocationManager.html).
It provides access to different location providers, such as GPS and
network positioning.

Setting up the listener for location updates
============================================

As of now (Mar 01st), acquiring the current GPS location with

works using the !LocationListener\#onLocationChanged method.

This code snippet for example updates textView1 whenever the current
location has changed.
