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
import java.util.Timer;
import java.util.TimerTask;

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
    /**
     * Parameters for GPS update intervals.
     */
    private static final int DELTA_DISTANCE = 0;

    /**
     * Time between two GPS fixes.
     */
    private static final int DELTA_TIME = 0;

    /**
     * The IAdderService is defined through IDL.
     */
    private final ILoggerService.Stub binder = new ILoggerService.Stub() {

        public long beginArea() {
            long ret = beginWay();
            currentWay().setArea(true);
            return ret;
        }

        public long beginWay() {

            if (currentWay() == null) // start a new way
                getStorage().getTrack().setCurrentWay(
                        getStorage().getTrack().newWay());
            // TODO kill old way

            return currentWay().getId();
        }

        public long createPOI(boolean onWay) {
            IDataNode tmpnode = null;

            if (onWay && currentWay() != null) {
                tmpnode = currentWay().newNode(lastCoordinate);
            } else {
                tmpnode = getStorage().getTrack().newNode(lastCoordinate);
            }

            if (lastCoordinate == null) {
                currentNodes.add(tmpnode);
            }

            return tmpnode.getId();
        }

        public synchronized long endWay() {

            IDataPointsList tmp = currentWay();

            getStorage().getTrack().setCurrentWay(null);

            if (tmp != null) {
                /* do not store non-ways */
                if (tmp.getNodes().size() < 2)
                    getStorage().getTrack().deleteWay(tmp.getId());
                else {
                    WayFilter.smoothenPoints(tmp.getNodes(), 3, 3); // TODO
                    WayFilter.filterPoints(tmp.getNodes(), 2); // TODO
                    // TODO
                    getSender().sendEndWay(tmp.getId());
                    return tmp.getId();
                }
            }
            return -1;
        }

        public double getLatitude() {
            if (lastCoordinate == null) {
                return 0;
            }
            return lastCoordinate.getLatitude();
        }

        public double getLongitude() {
            if (lastCoordinate == null) {
                return 0;
            }
            return lastCoordinate.getLongitude();
        }

        public boolean hasFix() {
            return lastCoordinate != null;
        }

        public boolean isAreaLogging() {
            if (getStorage().getTrack() != null) {
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
            if (getStorage().getTrack() != null) {
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
            getStorage().setTrack(getStorage().newTrack());
        }

        public int stopTrack() {
            stopGPS();

            if (getStorage().getTrack() != null) {

                // TODO Thread it
                long start = System.currentTimeMillis();
                getStorage().serialize();
                long stop = System.currentTimeMillis() - start;
                LogIt.d("Saving Track (in WaypointLogService.stopTrack). Time: "
                        + stop);
                getStorage().unloadAllTracks();
                return 1;
            }

            return -1;
        }
    };

    private boolean gps_on = false;

    private LocationListener locListener = this;

    private GpsMessage sender = null;

    private Timer timer = new Timer(); // TODO for debug

    /**
     * Current nodes, empty if no node with missing GPS location is present,
     * otherwise it contains references to the {@link IDataNode}s waiting for a
     * GPS fix.
     */
    Queue<IDataNode> currentNodes = new LinkedList<IDataNode>();

    /**
     * The last received coordinate.
     */
    GeoPoint lastCoordinate = null;

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

        // TODO for debug
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                MockLocationProvider.getInstance(WaypointLogService.this)
                        .newLoc();
            }
        }, 0, 1000);
    }

    @Override
    public void onDestroy() {
        stopGPS();
        timer.cancel(); // TODO for debug
        super.onDestroy();
    }

    /** GPS related Methods. **/
    public synchronized void onLocationChanged(Location loc) {
        if (loc == null) {
            LogIt.e("loc in onLocationChanged is null");
            return;
        }

        sender.sendCurrentPosition(loc);

        lastCoordinate = new GeoPoint(loc.getLatitude(), loc.getLongitude());

        if (!currentNodes.isEmpty()) { // one_shot or POI mode
            for (IDataNode node : currentNodes) {
                // update position of this node
                node.setLocation(new GeoPoint(loc.getLatitude(), loc
                        .getLongitude()));
            }
            currentNodes.clear();
        }
        if (currentWay() != null) {
            IDataNode nn = currentWay().newNode(lastCoordinate);
            sender.sendWayUpdate(currentWay().getId(), nn.getId());
        }
    }

    public void onProviderDisabled(String arg0) {
        // do nothing
    }

    public void onProviderEnabled(String provider) {
        // do nothing
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
        if (getStorage().getTrack() == null)
            return null;
        return getStorage().getTrack().getCurrentWay();
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
     * Shortcut for StorageFactory.getStorage().
     * 
     * @return Instance of IDataStorage.
     */
    IDataStorage getStorage() {
        return StorageFactory.getStorage();
    }

    /**
     * Tries to stop first and then to start receiving GPS updates. This
     * effectively reloads the settings for the {@link LocationManager}.
     */
    void restartGPS() {
        stopGPS();
        startGPS();
    }

    /**
     * Enables GPS updates from the {@link LocationManager}.
     */
    void startGPS() {
        LogIt.w("startGPS()");
        if (!gps_on) {
            LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, DELTA_TIME,
                    DELTA_DISTANCE, locListener);
        }
        gps_on = true;
    }

    /**
     * Disables GPS updates from the {@link LocationManager}.
     */
    void stopGPS() {
        LogIt.w("stopGPS()");
        if (gps_on) {
            ((LocationManager) getSystemService(Context.LOCATION_SERVICE))
                    .removeUpdates(locListener);
        }
        gps_on = false;
    }
}
