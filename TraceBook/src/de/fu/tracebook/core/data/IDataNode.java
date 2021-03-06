/*======================================================================
 *
 * This file is part of TraceBook.
 *
 * TraceBook is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published 
 * by the Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version.
 *
 * TraceBook is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public 
 * License along with TraceBook. If not, see 
 * <http://www.gnu.org/licenses/>.
 *
 =====================================================================*/

package de.fu.tracebook.core.data;

import org.mapsforge.android.maps.GeoPoint;

/**
 * A single point on the map. Each point has coordinates assigned to them.
 */
public interface IDataNode extends IDataMapObject {

    /**
     * Converts the DataNode to a GeoPoint.
     * 
     * @return A GeoPoint with the coordinates of the DataNode.
     */
    GeoPoint getCoordinates();

    /**
     * If this node is part of a {@link IDataPointsList}, this function will
     * return a reference to this object. Otherwise the return value is null.
     * 
     * @return The DataPointsList this point is in. (can be null)
     */
    IDataPointsList getDataPointsList();

    /**
     * A Point may have been added uninitialised, in this case it does not
     * contain any valid positional data - this may be added later once a GPS
     * fix is obtained.
     * 
     * @return true if the Node contains data of a valid GPS fix
     */
    boolean isValid();

    /**
     * Associates this DataNode with a {@link IDataPointsList}, meaning this
     * point is part of the way.
     * 
     * @param way
     *            The way that contains this point.
     */
    void setDataPointsList(IDataPointsList way);

    /**
     * Sets the position of this DataNode to the location of the GeoPoint.
     * 
     * @param gp
     *            New position of the node.
     */
    void setLocation(GeoPoint gp);

}
