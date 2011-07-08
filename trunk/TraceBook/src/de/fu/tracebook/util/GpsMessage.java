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

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import de.fu.tracebook.core.data.IDataNode;
import de.fu.tracebook.core.data.IDataStorage;
import de.fu.tracebook.gui.activity.MapsForgeActivity;

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
     * The string of the field in the intent extra where the current latitude is
     * stored. Only valid when type is UPDATE_GPS_POS.
     * 
     * Type is double.
     */
    public static final String EXTRA_LATITUDE = "lat";

    /**
     * The string of the field in the intent extra where the current longitude
     * is stored. Only valid when type is UPDATE_GPS_POS.
     * 
     * Type is double.
     */
    public static final String EXTRA_LONGITUDE = "long";

    /**
     * The string of the field in the intent extra where the id of the way is
     * stored. Only for UPDATE_OBJECT and MOVE_POINT.
     * 
     * Type is long.
     */
    public static final String EXTRA_POINT_ID = "nodeid";

    /**
     * The string of the field in the intent extra where the type of the
     * broadcast is stored.
     * 
     * Type is integer.
     */
    public static final String EXTRA_TYPE = "type";

    /**
     * The string of the field in the intent extra where the id of the way is
     * stored. Only for UPDATE_OBJECT and END_WAY.
     * 
     * Type is long.
     */
    public static final String EXTRA_WAY_ID = "wayid";

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
     * {@link IDataStorage}.
     */
    public static final int UPDATE_OBJECT = 1;

    private Context ctx;

    /**
     * Creates a new GPS Intent sender helper class.
     * 
     * @param ctx
     *            Context from which the Intents should be sent
     */
    public GpsMessage(Context ctx) {
        this.ctx = ctx;
    }

    /**
     * Send the current position to the MapsView.
     * 
     * @param loc
     *            current GPS position
     */
    public void sendCurrentPosition(Location loc) {
        Intent intent = new Intent(TAG);
        intent.putExtra(EXTRA_TYPE, UPDATE_GPS_POS);
        intent.putExtra(EXTRA_LONGITUDE, loc.getLongitude());
        intent.putExtra(EXTRA_LATITUDE, loc.getLatitude());
        ctx.sendBroadcast(intent);
    }

    /**
     * Signals the MapActivity that invalid OverlayItems are available to be
     * removed.
     */
    public void sendDiscardIntent() {
        Intent intent = new Intent(TAG);
        intent.putExtra(EXTRA_TYPE, REMOVE_INVALIDS);
        ctx.sendBroadcast(intent);
    }

    /**
     * Signal the end of a way, so that e.g. it will be redrawn in a different
     * color.
     * 
     * @param id
     *            id of the way that was just finished
     */
    public void sendEndWay(long id) {
        Intent intent = new Intent(TAG);
        intent.putExtra(EXTRA_TYPE, END_WAY);
        intent.putExtra(EXTRA_WAY_ID, id);
        ctx.sendBroadcast(intent);
    }

    /**
     * Start moving of a {@link IDataNode}.
     * 
     * @param id
     *            the ID of the DataNode that will enter edit mode
     */
    public void sendMovePoint(long id) {
        Intent intent = new Intent(TAG);
        intent.putExtra(EXTRA_TYPE, MOVE_POINT);
        intent.putExtra(EXTRA_POINT_ID, id);
        ctx.sendBroadcast(intent);
    }

    /**
     * Signal an update of the way, so that it can be redrawn.
     * 
     * @param wayId
     *            id of the way that was changed
     * @param nodeId
     *            the Id of the new waypoint, -1 if the way just has to be
     *            updated without adding a new point (e.g. when removing one)
     */
    public void sendWayUpdate(long wayId, long nodeId) {
        Intent intent = new Intent(TAG);
        intent.putExtra(EXTRA_TYPE, UPDATE_OBJECT);
        intent.putExtra(EXTRA_POINT_ID, nodeId);
        intent.putExtra(EXTRA_WAY_ID, wayId);
        ctx.sendBroadcast(intent);
    }
}
