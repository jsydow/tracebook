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

package de.fu.tracebook.util;

import de.fu.tracebook.core.data.DataNode;
import de.fu.tracebook.core.data.DataStorage;
import de.fu.tracebook.gui.activity.MapsForgeActivity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;

/**
 * To send messages to the {@link MapsForgeActivity} this class offers some
 * helpful methods which fill the intent according to the action that is
 * performed.
 */
public class GpsMessage {

    /**
     * Type of the Intent that signals a way was closed.
     */
    public static final int END_WAY = 2;

    /**
     * Tag of the Intent that signals the start of editing a points location.
     */
    public static final int MOVE_POINT = 3;

    /**
     * Type of the Intent that signals the existence of invalid OverlayItems.
     */
    public static final int REMOVE_INVALIDS = 4;

    /**
     * Tag of the Intent send by this class.
     */
    public static final String TAG = "de.fu-berlin.inf.de.fu.tracebook.UPDATE";

    /**
     * Type of the Intent that signals a change of the current position.
     */
    public static final int UPDATE_GPS_POS = 0;

    /**
     * Type of the Intent that signals an update to an object in
     * {@link DataStorage}.
     */
    public static final int UPDATE_OBJECT = 1;

    private Context ctx;

    private Intent intent;

    // private boolean one_shot = false;

    /**
     * Creates a new GPS Intent sender helper class.
     * 
     * @param ctx
     *            Context from which the Intents should be sent
     */
    public GpsMessage(Context ctx) {
        this.ctx = ctx;
        intent = new Intent(TAG);
    }

    /**
     * Send the current position to the MapsView.
     * 
     * @param loc
     *            current GPS position
     * @param oneShot
     *            true if currently a way is recorded in one_shot mode
     */
    public void sendCurrentPosition(Location loc, boolean oneShot) {
        intent.putExtra("type", UPDATE_GPS_POS);
        intent.putExtra("long", loc.getLongitude());
        intent.putExtra("lat", loc.getLatitude());
        intent.putExtra("one_shot", oneShot);
        ctx.sendBroadcast(intent);
    }

    /**
     * Signals the MapActivity that invalid OverlayItems are availiable to be
     * removed.
     */
    public void sendDiscardIntent() {
        intent.putExtra("type", REMOVE_INVALIDS);
        ctx.sendBroadcast(intent);
    }

    /**
     * Signal the end of a way, so that e.g. it will be redrawn in a different
     * color.
     * 
     * @param id
     *            id of the way that was just finished
     */
    public void sendEndWay(int id) {
        intent.putExtra("type", END_WAY);
        intent.putExtra("way_id", id);
        ctx.sendBroadcast(intent);
    }

    /**
     * Start moving of a {@link DataNode}.
     * 
     * @param id
     *            the ID of the DataNode that will enter edit mode
     */
    public void sendMovePoint(int id) {
        intent.putExtra("type", MOVE_POINT);
        intent.putExtra("point_id", id);
        ctx.sendBroadcast(intent);
    }

    /**
     * Signal an update of the way, so that it can be redrawn.
     * 
     * @param id
     *            id of the way that was changed
     * @param newNode
     *            the Id of the new waypoint, -1 if the way just has to be
     *            updated without adding a new point (e.g. when removing one)
     */
    public void sendWayUpdate(int id, int newNode) {
        intent.putExtra("type", UPDATE_OBJECT);
        intent.putExtra("point_id", newNode);
        intent.putExtra("way_id", id);
        ctx.sendBroadcast(intent);
    }
}
