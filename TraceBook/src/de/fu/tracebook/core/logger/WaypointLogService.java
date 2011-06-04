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

import org.mapsforge.android.maps.GeoPoint;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import de.fu.tracebook.core.data.IDataNode;
import de.fu.tracebook.core.data.IDataPointsList;
import de.fu.tracebook.core.data.IDataStorage;
import de.fu.tracebook.core.data.StorageFactory;
import de.fu.tracebook.util.GpsMessage;
import de.fu.tracebook.util.LogIt;
import de.fu.tracebook.util.WayFilter;

/**
 * This background service logs GPS data and stores it in the
 * {@link IDataStorage} object.
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
            oneShot = doOneShot;

            if (currentWay() == null) // start a new way
                storage.getTrack().setCurrentWay(storage.getTrack().newWay());

            if (oneShot) // in one_shot mode, add a new point
                currentNodes.add(currentWay().newNode(lastCoordinate));

            return (int) currentWay().getId();
        }

        public int createPOI(boolean onWay) {
            IDataNode tmpnode = null;

            if (onWay && currentWay() != null) {
                tmpnode = currentWay().newNode(lastCoordinate);
            } else {
                tmpnode = storage.getTrack().newNode(lastCoordinate);
            }

            currentNodes.add(tmpnode);

            return (int) tmpnode.getId();
        }

        public synchronized int endWay() {
            if (oneShot) // add the last point if in one_shot mode
                beginWay(oneShot);

            IDataPointsList tmp = currentWay();

            storage.getTrack().setCurrentWay(null);

            if (tmp != null)
                /* do not store non-ways */
                if (tmp.getNodes().size() < 2)
                    storage.getTrack().deleteWay((int) tmp.getId());
                else {
                    if (!oneShot) {
                        WayFilter.smoothenPoints(tmp.getNodes(), 3, 3);
                        WayFilter.filterPoints(tmp.getNodes(), 2);
                    }
                    getSender().sendEndWay((int) tmp.getId());
                    return (int) tmp.getId();
                }
            return -1;
        }

        public double getLastLatitude() {
            return lastCoordinate.getLatitude();
        }

        public double getLongitudeLatitude() {
            return lastCoordinate.getLongitude();
        }

        public boolean hasFix() {
            return lastCoordinate != null;
        }

        public boolean isAreaLogging() {
            if (storage.getTrack() != null) {
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
            if (storage.getTrack() != null) {
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
            storage.setTrack(storage.newTrack());
        }

        public int stopTrack() {
            stopGPS();

            if (storage.getTrack() != null) {

                long start = System.currentTimeMillis();
                storage.serialize();
                long stop = System.currentTimeMillis() - start;
                LogIt.d("#### Stop saving. Time: " + stop);
                storage.unloadAllTracks();
                return 1;
            }

            return -1;
        }
    };

    private boolean gps_on = false;

    private LocationListener locListener = this;

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
     * otherwise it contains references to the {@link IDataNode}s waiting for a
     * GPS fix.
     */
    Queue<IDataNode> currentNodes = new LinkedList<IDataNode>();

    /**
     * The last received coordinate.
     */
    GeoPoint lastCoordinate;

    /**
     * One shot mode - no continuous tracking, points are only added to the way
     * on request.
     */
    boolean oneShot = false;

    /**
     * Reference to the {@link IDataStorage} singleton.
     */
    IDataStorage storage = StorageFactory.getStorage();

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
        sender = new GpsMessage(this);
    }

    @Override
    public void onDestroy() {
        stopGPS();
        super.onDestroy();
    }

    /** GPS related Methods. **/

    public synchronized void onLocationChanged(Location loc) {
        sender.sendCurrentPosition(loc, oneShot);

        if (loc != null) {
            lastCoordinate = new GeoPoint(loc.getLatitude(), loc.getLongitude());

            if (!currentNodes.isEmpty()) { // one_shot or POI mode
                for (IDataNode node : currentNodes) {
                    node.setLocation(new GeoPoint(loc.getLatitude(), loc
                            .getLongitude())); // update node with proper GPS
                                               // fix

                    if (currentWay() != null) {
                        LogIt.d("new one-shot way point");
                        sender.sendWayUpdate((int) currentWay().getId(),
                                (int) node.getId()); // one_shot
                        // update
                    } else
                        sender.sendWayUpdate(-1, (int) node.getId()); // after
                                                                      // end way
                    // in
                    // one_shot
                    // mode, we
                    // send an
                    // update
                    // for the last
                    // waypoint
                }
                currentNodes.clear(); // no node waiting for GPS position any
                                      // more
            } else if (currentWay() != null && !oneShot) { // Continuous mode
                IDataNode nn = currentWay().newNode(
                        new GeoPoint(loc.getLatitude(), loc.getLongitude())); // POI
                                                                              // in
                                                                              // track
                                                                              // was
                                                                              // already
                // added before
                sender.sendWayUpdate((int) currentWay().getId(),
                        (int) nn.getId()); // call for an update of the way
            }
        }
    }

    public void onProviderDisabled(String arg0) {
        // do nothing
    }

    public void onProviderEnabled(String provider) {
        // do nothing
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
    }

    public void onStatusChanged(String provider, int status, Bundle extras) {
        // do nothing
    }

    /**
     * Convenience function to get the current way out of the
     * {@link IDataStorage} object.
     * 
     * @return the current {@link IDataPointsList} way
     */
    IDataPointsList currentWay() {
        if (storage == null || storage.getTrack() == null)
            return null;
        return storage.getTrack().getCurrentWay();
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
                            deltaTime, deltaDistance, locListener);
        gps_on = true;
    }

    /**
     * Disables GPS updates from the {@link LocationManager}.
     */
    void stopGPS() {
        if (gps_on)
            ((LocationManager) getSystemService(Context.LOCATION_SERVICE))
                    .removeUpdates(locListener);
        gps_on = false;
    }
}
