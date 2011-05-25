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

package de.fu.tracebook.core.data;

import org.mapsforge.android.maps.GeoPoint;
import org.mapsforge.android.maps.OverlayItem;

import de.fu.tracebook.gui.activity.MapsForgeActivity;

public interface IDataNode {

    /**
     * Converts the DataNode to a GeoPoint.
     * 
     * @return A GeoPoint with the coordinates of the DataNode.
     */
    GeoPoint getCoordinates();

    /**
     * If this node is part of a {@link DataPointsList}, this function will
     * return a reference to this object. Otherwise the return value is null.
     * 
     * @return The DataPointsList this point is in. (can be null)
     */
    IDataPointsList getDataPointsList();

    /**
     * Getter-method.
     * 
     * @return A reference to the OverlayItem that is drawn and handled by
     *         MapsForge's overlay.
     */
    OverlayItem getOverlayItem();

    /**
     * A Point may have been added uninitialized, in this case it does not
     * contain any valid positional data - this may be added later once a GPS
     * fix is obtained.
     * 
     * @return true if the Node contains data of a valid GPS fix
     */
    boolean isValid();

    /**
     * Associates this DataNode with a {@link DataPointsList}, meaning this
     * point is part of the way.
     * 
     * @param way
     *            The way that contains this point.
     */
    void setDataPointsList(DataPointsList way);

    /**
     * Sets the position of this DataNode to the location of the GeoPoint.
     * 
     * @param gp
     *            New position of the node.
     */
    void setLocation(GeoPoint gp);

    /**
     * Set the {@link OverlayItem}, used by {@link MapsForgeActivity}.
     * 
     * @param overlayItem
     *            The new OverlayItem.
     */
    void setOverlayItem(OverlayItem overlayItem);

}
