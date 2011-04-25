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

package de.fu.tracebook.core.logger;

import java.util.LinkedList;
import java.util.Queue;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import de.fu.tracebook.core.data.DataNode;
import de.fu.tracebook.core.data.DataPointsList;
import de.fu.tracebook.core.data.DataStorage;
import de.fu.tracebook.util.GpsMessage;
import de.fu.tracebook.util.LogIt;
import de.fu.tracebook.util.WayFilter;

/**
 * This background service logs GPS data and stores it in the
 * {@link DataStorage} object.
 */
public class WaypointLogService extends Service implements LocationListener {
    private static final String LOG_TAG = "LOGSERVICE";

    /**
     * The IAdderService is defined through IDL.
     */
    private final ILoggerService.Stub binder = new ILoggerService.Stub() {

        public int beginArea(boolean doOneShot) {
            int ret = beginWay(doOneShot);
            currentWay().setArea(true);
            return ret;
        }

        public int beginWay(boolean doOneShot) {
            one_shot = doOneShot;

            if (currentWay() == null) // start a new way
                storage.getCurrentTrack().setCurrentWay(
                        storage.getCurrentTrack().newWay());

            if (one_shot) // in one_shot mode, add a new point
                current_nodes.add(currentWay().newNode());

            return currentWay().getId();
        }

        public int createPOI(boolean onWay) {
            DataNode tmpnode = null;
            if (onWay && currentWay() != null)
                tmpnode = currentWay().newNode();
            else
                tmpnode = storage.getCurrentTrack().newNode();
            current_nodes.add(tmpnode);

            return tmpnode.getId();
        }

        public synchronized int endWay() {
            if (one_shot) // add the last point if in one_shot mode
                beginWay(one_shot);

            DataPointsList tmp = currentWay();

            storage.getCurrentTrack().setCurrentWay(null);

            if (tmp != null)
                /* do not store non-ways */
                if (tmp.getNodes().size() < 2)
                    storage.getCurrentTrack().deleteWay(tmp.getId());
                else {
                    if (!one_shot) {
                        WayFilter.smoothenPoints(tmp.getNodes(), 3, 3);
                        WayFilter.filterPoints(tmp.getNodes(), 2);
                    }
                    getSender().sendEndWay(tmp.getId());
                    return tmp.getId();
                }
            return -1;
        }

        public boolean isAreaLogging() {
            if (storage.getCurrentTrack() != null) {
                if (currentWay() != null) {
                    return currentWay().isArea();
                }
            }
            return false;
        }

        public boolean isLogging() {
            return gpsEnabled();
        }

        public boolean isWayLogging() {
            if (storage.getCurrentTrack() != null) {
                if (currentWay() != null) {
                    return !currentWay().isArea();
                }
            }
            return false;
        }

        public void pauseLogging() {
            stopGPS();
        }

        public void resumeLogging() {
            startGPS();
        }

        public void startTrack() {
            restartGPS();
            storage.setCurrentTrack(storage.newTrack());
        }

        public int stopTrack() {
            stopGPS();

            if (storage.getCurrentTrack() != null) {
                storage.getCurrentTrack().serialize();
                storage.unloadAllTracks();
                return 1;
            }

            return -1;
        }
    };

    private boolean gps_on = false;

    private LocationListener ll = this;

    private GpsMessage sender = null;

    /**
     * Parameters for GPS update intervals.
     */
    protected int deltaDistance = 0;

    /**
     * Time between two GPS fixes.
     */
    protected int deltaTime = 0;

    /**
     * Current nodes, empty if no node with missing GPS location is present,
     * otherwise it contains references to the {@link DataNode}s waiting for a
     * GPS fix.
     */
    Queue<DataNode> current_nodes = new LinkedList<DataNode>();

    /**
     * One shot mode - no continuous tracking, points are only added to the way
     * on request.
     */
    boolean one_shot = false;

    /**
     * Reference to the {@link DataStorage} singleton.
     */
    DataStorage storage = DataStorage.getInstance();

    /**
     * Returns the status of the GPS logging.
     * 
     * @return true if GPS is on.
     */
    public boolean gpsEnabled() {
        return gps_on;
    }

    @Override
    public IBinder onBind(Intent intent) {
        startGPS();
        return binder;
    }

    @Override
    public synchronized void onCreate() {
        super.onCreate();
        LogIt.d(LOG_TAG, "onCreate");
        sender = new GpsMessage(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogIt.d(LOG_TAG, "onDestroy");
        stopGPS();
    }

    /** GPS related Methods. **/

    public synchronized void onLocationChanged(Location loc) {
        sender.sendCurrentPosition(loc, one_shot);

        if (!current_nodes.isEmpty()) { // one_shot or POI mode
            for (DataNode node : current_nodes) {
                node.setLocation(loc); // update node with proper GPS fix

                if (currentWay() != null) {
                    LogIt.d("Logger", "new one-shot way point");
                    sender.sendWayUpdate(currentWay().getId(), node.getId()); // one_shot
                    // update
                } else
                    sender.sendWayUpdate(-1, node.getId()); // after end way in
                                                            // one_shot mode, we
                                                            // send an update
                                                            // for the last
                                                            // waypoint
            }
            current_nodes.clear(); // no node waiting for GPS position any more
        } else if (currentWay() != null && !one_shot) { // Continuous mode
            DataNode nn = currentWay().newNode(loc); // POI in track was already
                                                     // added before
            sender.sendWayUpdate(currentWay().getId(), nn.getId()); // call for
                                                                    // an update
                                                                    // of
            // the way
        }
    }

    public void onProviderDisabled(String provider) {
        LogIt.d(LOG_TAG, "GPS Provider Disabled: " + provider);
    }

    public void onProviderEnabled(String provider) {
        LogIt.d(LOG_TAG, "GPS Provider Enabled: " + provider);
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        LogIt.d(LOG_TAG, "onStart");
    }

    public void onStatusChanged(String provider, int status, Bundle extra) {
        LogIt.d(LOG_TAG, "GPS Status Changed: " + provider);
    }

    /**
     * Convenience function to get the current way out of the
     * {@link DataStorage} object.
     * 
     * @return the current {@link DataPointsList} way
     */
    DataPointsList currentWay() {
        if (storage == null || storage.getCurrentTrack() == null)
            return null;
        return storage.getCurrentTrack().getCurrentWay();
    }

    /**
     * Returns the Intent sender helper.
     * 
     * @return a reference to the {@link GpsMessage} helper class
     */
    GpsMessage getSender() {
        return sender;
    }

    /**
     * Tries to stop first and then to start receiving GPS updates. This
     * effectively reloads the settings for the {@link LocationManager}
     */
    void restartGPS() {
        stopGPS();
        startGPS();
    }

    /**
     * Enables GPS updates from the {@link LocationManager}.
     */
    void startGPS() {
        if (!gps_on)
            ((LocationManager) getSystemService(Context.LOCATION_SERVICE))
                    .requestLocationUpdates(LocationManager.GPS_PROVIDER,
                            deltaTime, deltaDistance, ll);
        gps_on = true;
    }

    /**
     * Disables GPS updates from the {@link LocationManager}.
     */
    void stopGPS() {
        if (gps_on)
            ((LocationManager) getSystemService(Context.LOCATION_SERVICE))
                    .removeUpdates(ll);
        gps_on = false;
    }
}
