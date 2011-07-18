/*======================================================================
 *
 * This file is part of TraceBook.
 *
 * TraceBook is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * TraceBook is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with TraceBook. If not, see <http://www.gnu.org/licenses/>.
 *
 =====================================================================*/

package de.fu.tracebook.gui.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.mapsforge.android.maps.GeoPoint;
import org.mapsforge.android.maps.MapActivity;
import org.mapsforge.android.maps.MapController;
import org.mapsforge.android.maps.MapView;
import org.mapsforge.android.maps.MapViewMode;
import org.mapsforge.android.maps.OverlayItem;
import org.mapsforge.android.maps.OverlayWay;
import org.mapsforge.android.maps.Projection;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import de.fu.tracebook.R;
import de.fu.tracebook.core.bugs.Bug;
import de.fu.tracebook.core.bugs.BugManager;
import de.fu.tracebook.core.data.IDataNode;
import de.fu.tracebook.core.data.IDataPointsList;
import de.fu.tracebook.core.data.IDataTrack;
import de.fu.tracebook.core.data.StorageFactory;
import de.fu.tracebook.core.logger.ServiceConnector;
import de.fu.tracebook.core.overlays.BugOverlay;
import de.fu.tracebook.core.overlays.DataNodeArrayItemizedOverlay;
import de.fu.tracebook.core.overlays.DataPointsListArrayRouteOverlay;
import de.fu.tracebook.util.GpsMessage;
import de.fu.tracebook.util.Helper;
import de.fu.tracebook.util.LineSimplification;
import de.fu.tracebook.util.LogIt;

/**
 * This class implements a MapsForge map activity to draw ways and nodes as
 * overlays on it. It also contains a class to handle Messages form the
 * LoggerService and offers means to modify points on the map.
 */
public class MapsForgeActivity extends MapActivity {
    /**
     * This class receives Broadcast Messages from the WaypointLogService in
     * order to update the current location and overlays if position or overlay
     * data has changed.
     */
    private class GPSReceiver extends BroadcastReceiver {

        /**
         * Used to recognise new way.
         */
        private long oldWayId = -1;

        /**
         * Centres the map to the current position if true. This will be set to
         * false once the map is centred, it's used to initially centre the map
         * when no GPS fix is available yet.
         */
        boolean centerMap = true;

        /**
         * OverlayItem for the current position.
         */
        OverlayItem currentPosOI = null;

        public GPSReceiver() {
            // Do nothing
        }

        @Override
        public void onReceive(Context ctx, Intent intend) {
            final long wayId = intend.getExtras().getLong(
                    GpsMessage.EXTRA_WAY_ID);
            final long pointId = intend.getExtras().getLong(
                    GpsMessage.EXTRA_POINT_ID);

            // Receive current location and do something with it
            switch (intend.getIntExtra(GpsMessage.EXTRA_TYPE, -1)) {

            case GpsMessage.UPDATE_GPS_POS: // periodic position update
                final double lng = intend.getExtras().getDouble(
                        GpsMessage.EXTRA_LONGITUDE);
                final double lat = intend.getExtras().getDouble(
                        GpsMessage.EXTRA_LATITUDE);

                currentGeoPoint = new GeoPoint(lat, lng);

                // set marker for current position
                if (currentPosOI == null) {
                    currentPosOI = Helper.getOverlayItem(currentGeoPoint,
                            R.drawable.card_marker_green,
                            MapsForgeActivity.this);
                    pointsOverlay.addItem(currentPosOI);
                }

                currentPosOI.setPoint(currentGeoPoint);
                pointsOverlay.requestRedraw();

                // Centre map if activated.
                if (centerMap) {
                    centerOnCurrentPosition();
                }

                break;

            // Receive an update of a way and update the overlay accordingly
            case GpsMessage.UPDATE_OBJECT:
                LogIt.d("UPDATE_OBJECT received, way: " + wayId + " node: "
                        + pointId);

                if (wayId > 0) {
                    IDataPointsList way = Helper.currentTrack()
                            .getPointsListById(wayId);
                    if (way != null) {

                        if (oldWayId != wayId) {
                            // add new way
                            oldWayId = wayId;
                            routesOverlay.addWay(way, true);
                        } else {
                            // Refill way waypoints to overlay.
                            StorageFactory.getStorage().getOverlayManager()
                                    .updateOverlayRoute(way, null);
                            routesOverlay.requestRedraw();
                        }

                        // waypoints are only shown when toggleWaypoints is
                        // pressed.
                        if (pointId > 0) { // new waypoint (for waypoints
                                           // overlay)
                            IDataNode node = Helper.currentTrack().getNodeById(
                                    pointId);
                            if (node != null) {
                                routesOverlay.putWaypoint(node);
                            }
                        }

                    } else {
                        LogIt.d("Way can not be found in MapsForgeActivity.GPSreceiver.UPDATE_OBJECT");
                    }
                }

                break;
            case GpsMessage.MOVE_POINT:
                LogIt.d("Move Point " + pointId);

                editNode = Helper.currentTrack().getNodeById(pointId);

                break;
            case GpsMessage.END_WAY:
                LogIt.d("End way for way " + wayId + " received.");

                (new AsyncTask<Void, Void, Void>() {

                    @Override
                    protected Void doInBackground(Void... params) {

                        IDataPointsList way = Helper.currentTrack()
                                .getPointsListById(wayId);

                        if (way != null) {
                            SharedPreferences appPreferences = PreferenceManager
                                    .getDefaultSharedPreferences(MapsForgeActivity.this);

                            if (appPreferences.getBoolean(
                                    "check_GPSSimplifyWays", true)) {

                                // simplify line
                                Projection proj = mapView.getProjection();

                                // project GeoPoints to Points
                                List<IDataNode> nodes = way.getNodes();
                                List<Point> points = new ArrayList<Point>(
                                        nodes.size());
                                for (IDataNode n : nodes) {
                                    points.add(proj.toPoint(n.getCoordinates(),
                                            null, mapView.getZoomLevel()));
                                }

                                // Simplify line.
                                float pixelPerMeter = mapView.getProjection()
                                        .metersToPixels(2);
                                LineSimplification.simplify(points,
                                        pixelPerMeter);
                                int size = points.size();
                                IDataTrack track = StorageFactory.getStorage()
                                        .getTrack();

                                // Deleted way points are set to zero.
                                // Search for nulls and delete the node
                                for (int i = 0; i < size; ++i) {
                                    if (points.get(i) == null) {
                                        IDataNode node = nodes.get(i);
                                        if (!node.hasAdditionalInfo()) {
                                            track.deleteNode(node.getId());
                                        }
                                    }
                                }
                            }

                            StorageFactory.getStorage().getOverlayManager()
                                    .updateOverlayRoute(way, null);
                            OverlayWay w = StorageFactory.getStorage()
                                    .getOverlayManager().getOverlayRoute(way);
                            routesOverlay.color(way, w, false);
                            routesOverlay.requestRedraw();
                        }
                        removeInvalidItems();

                        return null;
                    }

                }).execute();
                break;
            case GpsMessage.REMOVE_INVALIDS:
                removeInvalidItems();
                break;
            default:
                LogIt.e("unhandled Message, ID="
                        + intend.getIntExtra("type", -1));
            }
        }

        /**
         * Requests the map to be centered to the current position.
         */
        void centerOnCurrentPosition() {
            if (currentGeoPoint != null && mapController != null) {
                MapsForgeActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        mapController.setCenter(currentGeoPoint);

                    }
                });
                centerMap = false;
            } else {
                centerMap = true;
            }
        }

        /**
         * Will reinitialise the current position.
         */
        void reinitPosition() {
            currentPosOI = null;
        }

        void removeInvalidItems() {
            LogIt.d("Request to remove invalid nodes");

            Collection<OverlayItem> invalids = StorageFactory.getStorage()
                    .getOverlayManager().getAndClearInvalidOverlayItems();
            for (OverlayItem oi : invalids) {
                pointsOverlay.removeItem(oi);
            }
        }
    }

    /**
     * Checks whether there is an internet connection available.
     * 
     * @param activity
     *            An activity.
     * @return Returns true if there is an internet connection available, false
     *         otherwise.
     */
    public static boolean isOnline(Activity activity) {
        ConnectivityManager cm = (ConnectivityManager) activity
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        if (info != null)
            return info.isConnectedOrConnecting();

        return false;
    }

    private boolean useInternet = false;

    /**
     * The bug manager.
     */
    BugManager bugManager;

    /**
     * The Overlay containing all Bugs.
     */
    BugOverlay bugOverlay;

    /**
     * The last known {@link GeoPoint}.
     */
    GeoPoint currentGeoPoint = null;

    /**
     * Node currently edited.
     */
    IDataNode editNode = null;

    /**
     * Reference to a internal class that receives messages from
     * {@link GpsMessage} to redraw the Overlay when Items have changed.
     */
    GPSReceiver gpsReceiver;

    /**
     * MapController object to interact with the Map.
     */
    MapController mapController;

    /**
     * Reference to the MapsForce MapView Object.
     */
    MapView mapView;

    /**
     * Overlay containing all POIs.
     */
    DataNodeArrayItemizedOverlay pointsOverlay;

    /**
     * Overlay containing all areas and ways.
     */
    DataPointsListArrayRouteOverlay routesOverlay;

    /**
     * Executed when the Bugs-Button is pressed.
     * 
     * @param v
     *            not used/.
     */
    public void bugsBtn(View v) {
        Projection proj = mapView.getProjection();
        LogIt.w("ZoomLevel: " + mapView.getZoomLevel());
        LogIt.w("PixelPerMeter: " + proj.metersToPixels(3));

        final GeoPoint p = currentGeoPoint == null ? mapView.getMapCenter()
                : currentGeoPoint;

        final CharSequence[] items = {
                getResources().getString(R.string.alert_mapsforgeactivity_osb),
                getResources().getString(
                        R.string.alert_mapsforgeactivity_newbug),
                getResources().getString(
                        R.string.alert_mapsforgeactivity_exportbugs) };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(
                R.string.alert_mapsforgeactivity_bugs));
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                switch (item) {
                case 0:
                    bugManager.downloadBugs(MapsForgeActivity.this, p);
                    break;
                case 1:
                    dialog.cancel();
                    AlertDialog.Builder bugDlgBuilder = new AlertDialog.Builder(
                            MapsForgeActivity.this);
                    final EditText input = new EditText(MapsForgeActivity.this);
                    bugDlgBuilder.setView(input);
                    bugDlgBuilder.setTitle("Bug Description:"); // MapsForgeActivity.this.getResources().getString());
                    bugDlgBuilder.setMessage("Test");
                    bugDlgBuilder.setPositiveButton(
                            MapsForgeActivity.this.getResources().getString(
                                    R.string.alert_global_ok),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dlg, int id) {
                                    BugManager.getInstance().addBug(
                                            new Bug(input.getText().toString(),
                                                    p));
                                    fillBugs();
                                }
                            });
                    bugDlgBuilder.setNegativeButton(
                            MapsForgeActivity.this.getResources().getString(
                                    R.string.alert_global_cancel),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dlg, int id) {
                                    dlg.cancel();
                                }
                            });

                    bugDlgBuilder.show();

                    break;
                case 3:
                    bugManager.serializeBugs();
                    break;
                }
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        // When a node is edited, the user can move it by
        // moving his finger on the display
        if (editNode != null) {
            GeoPoint projection = mapView.getProjection().fromPixels(
                    (int) ev.getX(), (int) ev.getY());

            editNode.setLocation(projection);
            StorageFactory.getStorage().getOverlayManager()
                    .getOverlayItem(editNode).setPoint(projection);
            if (editNode.getDataPointsList() != null) {
                StorageFactory.getStorage().getOverlayManager()
                        .updateOverlayRoute(editNode.getDataPointsList(), null);
                routesOverlay.requestRedraw();
            }

            pointsOverlay.requestRedraw();

            if (ev.getAction() == MotionEvent.ACTION_UP) {
                editNode = null;
            }

            return true;
        } else {
            return super.dispatchTouchEvent(ev);
        }
    }

    /**
     * Executed when the Exit-button is pressed.
     * 
     * @param v
     *            Not used.
     */
    public void exitBtn(View v) {
        Helper.alertStopTracking(this);
    }

    /**
     * Fills the bug overlay.
     */
    public void fillBugs() {
        bugOverlay.addItems(bugManager.getBugs());
    }

    /**
     * Executed when the Info-button is pressed.
     * 
     * @param v
     *            Not used.
     */
    public void infoBtn(View v) {
        Intent intent = new Intent(this, TrackInfoActivity.class);
        startActivity(intent);
    }

    /**
     * Executed when the Exit-button is pressed.
     * 
     * @param v
     *            Not used.
     */
    public void listBtn(View v) {
        Intent intent = new Intent(this, ListDataActivity.class);
        startActivity(intent);
    }

    /**
     * Executed when the Exit-button is pressed.
     * 
     * @param v
     *            Not used.
     */
    public void newBtn(View v) {
        final IDataPointsList way = StorageFactory.getStorage().getTrack()
                .getCurrentWay();
        List<String> itemStrings = new LinkedList<String>();
        itemStrings.add(getResources().getString(
                R.string.alert_mapsforgeactivity_newpoi));
        if (way != null) {
            itemStrings.add(getResources().getString(
                    R.string.alert_mapsforgeactivity_stopway));
            itemStrings.add(getResources().getString(
                    R.string.alert_mapsforgeactivity_editway));
        } else {
            itemStrings.add(getResources().getString(
                    R.string.alert_mapsforgeactivity_newway));
        }
        final CharSequence[] items = new CharSequence[itemStrings.size()];
        itemStrings.toArray(items);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(
                R.string.alert_mapsforgeactivity_newobj));
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                final Intent intent = new Intent(MapsForgeActivity.this,
                        AddPointActivity.class);
                switch (item) {
                case 0:

                    long nodeId = 0;

                    try {
                        nodeId = ServiceConnector.getLoggerService().createPOI(
                                false);
                    } catch (RemoteException e) {
                        LogIt.e("no service: " + e.getMessage());
                    }

                    if (nodeId < 0) {
                        fillOverlays();
                        LogIt.popup(
                                MapsForgeActivity.this,
                                getResources()
                                        .getString(
                                                R.string.popup_mapsforgeactivity_nocoords));
                        break;
                    }

                    intent.putExtra("NodeId", nodeId);
                    startActivity(intent);

                    break;
                case 1:
                    // start or stop way
                    if (way == null) {
                        try {
                            ServiceConnector.getLoggerService().beginWay();

                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    } else {
                        try {
                            ServiceConnector.getLoggerService().endWay();
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }

                    break;
                case 2:
                    // edit way
                    if (way == null) {
                        break;
                    }

                    long wayId = way.getId();

                    intent.putExtra("WayId", wayId);
                    startActivity(intent);

                    break;
                }
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onBackPressed()
     */
    @Override
    public void onBackPressed() {
        Helper.alertStopTracking(this);
    }

    /**
     * Inflates the options menu for this activity.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.optionsmenu_mapsforgeactivity, menu);
        return true;
    }

    /**
     * Catches the selected MenuItem from the options menu and
     * 
     * <ol>
     * <li>Activate the Internet connection to get more map data.</li>
     * <li>Center the map to the current own position.</li>
     * <li>Stop tracking, show alert and go back to MainActivity.</li>
     * <li>Pause tracking and show alert</li>
     * <li>5. Export current session to item.</li>
     * </ol>
     * 
     * @param item
     *            the item
     * @return true, if successful
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
        case R.id.opt_mapsforgeActivity_activateMobileInternet:

            if (useInternet) {
                item.setTitle(getResources().getString(
                        R.string.opt_mapsforgeActivity_activateMobileInternet));
                changeMapViewToOfflineRendering();
            } else {
                item.setTitle(getResources()
                        .getString(
                                R.string.opt_mapsforgeActivity_deactivateMobileInternet));
                changeMapViewMode(getOnlineTileStyle(), null);
            }
            useInternet = !useInternet;

            return true;

        case R.id.opt_mapsforgeActivity_centerAtOwnPosition:

            gpsReceiver.centerOnCurrentPosition();
            return true;
        case R.id.opt_mapsforgeActivity_showToggleWayPoints:

            routesOverlay.toggleWaypoints();
            return true;
        case R.id.opt_mapsforgeActivity_export:
            StorageFactory.getStorage().serialize();
            LogIt.popup(
                    this,
                    getResources().getString(
                            R.string.popup_mapsforgeactivity_saved));
            return true;

        default:
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * This Method for the two (title and description) button from the status
     * bar. This method starts the dialog with all activity informations.
     * 
     * @param v
     *            not used
     */
    public void statusBarTitleBtn(View v) {
        Helper.setActivityInfoDialog(this,
                getResources().getString(R.string.tv_statusbar_mapsforgeTitle),
                getResources().getString(R.string.tv_statusbar_mapsforgeDesc));
    }

    private void checkGpsStatus() {
        LocationManager loc = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Get the app's shared preferences
        SharedPreferences appPreferences = PreferenceManager
                .getDefaultSharedPreferences(this);

        LogIt.d("check gps status");
        if (loc.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            LogIt.d("provider enabled");
        }

        if (!loc.isProviderEnabled(LocationManager.GPS_PROVIDER)
                && appPreferences.getBoolean("check_GPSbyStartTracking", true)) {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setTitle(this.getResources().getString(
                    R.string.alert_NewTrackActivity_NoGpsTitle));
            builder.setMessage(this.getResources().getString(
                    R.string.alert_NewTrackActivity_NoGpsMessage));

            builder.setPositiveButton(
                    this.getResources().getString(R.string.alert_global_yes),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                            Intent intent = new Intent(
                                    Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(intent);
                            dialog.cancel();
                        }
                    });

            builder.setNegativeButton(
                    this.getResources().getString(R.string.alert_global_no),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

            builder.show();

        }

    }

    /**
     * Gets the preferred Online Tile Style from the Preferences object.
     */
    private MapViewMode getOnlineTileStyle() {
        String pref = PreferenceManager.getDefaultSharedPreferences(this)
                .getString("lst_setOnlineTitleStyle", "OSMA");
        if (pref.equals("OSMA"))
            return MapViewMode.OSMARENDER_TILE_DOWNLOAD;
        if (pref.equals("MAPNIK"))
            return MapViewMode.MAPNIK_TILE_DOWNLOAD;
        if (pref.equals("OPEN"))
            return MapViewMode.OPENCYCLEMAP_TILE_DOWNLOAD;

        // use osmarender as fallback
        return MapViewMode.OSMARENDER_TILE_DOWNLOAD;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        LogIt.d("Creating MapActivity");

        setContentView(R.layout.activity_mapsforgeactivity);

        gpsReceiver = new GPSReceiver();

        mapView = (MapView) findViewById(R.id.ly_mapsforgeMapView);

        pointsOverlay = new DataNodeArrayItemizedOverlay(this);
        routesOverlay = new DataPointsListArrayRouteOverlay(this, pointsOverlay);
        bugOverlay = new BugOverlay(this);
        bugManager = BugManager.getInstance();

        // as this activity is destroyed when adding a POI, we get all POIs here
        fillOverlays();

        // Set status bar
        Helper.setStatusBar(this,
                getResources().getString(R.string.tv_statusbar_mapsforgeTitle),
                getResources().getString(R.string.tv_statusbar_mapsforgeDesc),
                R.id.ly_mapsforgeActivity_statusbar, false);

        checkGpsStatus();

    }

    @Override
    protected void onPause() {
        super.onPause();
        LogIt.d("Pausing MapActivity");
        unregisterReceiver(gpsReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        LogIt.d("Resuming MapActivity");

        (new AsyncTask<Void, Void, Void>() {
            @Override
            public Void doInBackground(Void... arg0) {
                // with out looper it won't work
                // Looper.prepare();

                /*
                 * We init the map in a thread to archive a better ui
                 * experience. But on one core cpu system the thread still slows
                 * the ui down. So we wait one second to give the ui some time
                 * to smoothly init its self.
                 */
                if (Runtime.getRuntime().availableProcessors() == 1) {
                    try {
                        // Give the Gui Thread some time to do its init
                        // stuff
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        LogIt.e(e.getMessage());
                    }
                }
                // mapView = new MapView(MapsForgeActivity.this);
                mapView.setClickable(true);
                mapView.setBuiltInZoomControls(true);
                mapView.setScaleBar(true);

                mapView.setMemoryCardCachePersistence(PreferenceManager
                        .getDefaultSharedPreferences(MapsForgeActivity.this)
                        .getBoolean("check_activateLocalTitleMapCache", false));

                if (mapView.getOverlays().size() == 0) {
                    mapView.getOverlays().add(routesOverlay);
                    mapView.getOverlays().add(pointsOverlay);
                    mapView.getOverlays().add(bugOverlay);
                    LogIt.d("added overlays: " + mapView.getOverlays().size());
                }

                // redraw all overlays to account for the events we've missed
                // paused
                fillOverlays();

                mapController = mapView.getController();

                changeMapViewToOfflineRendering();

                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                mapView.invalidate();
                LogIt.d("Overlays: " + mapView.getOverlays().size());
            }

        }).execute();

        try {
            if (ServiceConnector.getLoggerService().isLogging()) {
                Helper.startUserNotification(this,
                        R.drawable.ic_notification_active,
                        MapsForgeActivity.class, true);
            } else {
                Helper.startUserNotification(this,
                        R.drawable.ic_notification_pause,
                        MapsForgeActivity.class, false);
            }
        } catch (RemoteException e) {

            e.printStackTrace();
        }

        registerReceiver(gpsReceiver, new IntentFilter(GpsMessage.TAG));
    }

    /**
     * Changes the render mode of the map. Possible modes are specified in
     * {@link MapViewMode}, if file is specified and the CANVAS_RENDERER mode is
     * selected, the map will be rendered off-line. If the file does not exist,
     * it will default to fetching the tiles from the Internet
     * 
     * @param mode
     *            {@link MapViewMode} render mode
     * @param file
     *            map file for off-line rendering
     */
    void changeMapViewMode(MapViewMode mode, File file) {
        MapViewMode modeLocal = mode;

        if (mode == MapViewMode.CANVAS_RENDERER) {
            if (file == null || !file.exists()) {
                LogIt.popup(
                        this,
                        getResources().getString(
                                R.string.toast_loadingOnlineMap));
                modeLocal = getOnlineTileStyle();
            } else {
                mapView.setMapViewMode(modeLocal); // MapsForge crashes if we
                // specify a maps file when in
                // Online mode
                mapView.setMapFile(file.getAbsolutePath());
            }
        } else {
            if (!isOnline(this)) {
                LogIt.popup(
                        this,
                        getResources().getString(
                                R.string.toast_noInternetAccess));
            }
        }
        mapView.setMapViewMode(modeLocal);

        gpsReceiver.centerOnCurrentPosition();
    }

    /**
     * Switch to offline rendering using a specified map file.
     */
    void changeMapViewToOfflineRendering() {
        String mapFile = PreferenceManager.getDefaultSharedPreferences(this)
                .getString("mapsforgeMapFilePath",
                        "/mnt/sdcard/TraceBook/default.map");
        changeMapViewMode(MapViewMode.CANVAS_RENDERER, new File(mapFile));
    }

    /**
     * Fill all Overlays.
     */
    void fillOverlays() {
        LogIt.d("MapsforgeActivity.fillOverlays()");
        pointsOverlay.clear();
        pointsOverlay.addItems(StorageFactory.getStorage().getOverlayManager()
                .getOverlayItems(this));
        if (gpsReceiver != null) {
            gpsReceiver.reinitPosition();
        }

        routesOverlay.clear();
        routesOverlay.addWays(Helper.currentTrack().getWays());

        bugOverlay.clear();
        bugOverlay.addItems(bugManager.getBugs());

        LogIt.d("Overlaycount: " + mapView.getOverlays().size());
    }
}
