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

import java.util.List;

import org.mapsforge.android.maps.GeoPoint;

/**
 * A way or an area.
 */
public interface IDataPointsList extends IDataMapObject {

    /**
     * This method deletes a Node on the working memory and devices memory
     * completely.
     * 
     * @param l
     *            The id of the node to be deleted. If this node does not exist
     *            nothing is done.
     * @return A reference to the deleted DataNode object if it exists, null
     *         otherwise.
     */
    IDataNode deleteNode(long l);

    /**
     * Searches for a Node in this Track by the specified id.
     * 
     * @param nodeId
     *            The id of the Node that is being searched for.
     * @return The DataNode where get_id() == id, or null if not found.
     */
    IDataNode getNodeById(int nodeId);

    /**
     * Getter-method that returns a list of all nodes. The returned List is the
     * one stored in this object. Changing the returned List will therefore
     * change this list
     * 
     * @return The list of all nodes stored in this object. (not null)
     */
    List<IDataNode> getNodes();

    /**
     * Getter-method.
     * 
     * @return True if object resembles an Area.
     */
    boolean isArea();

    /**
     * Add a new Node at the end of the list. Call this method if you want to
     * extend the Way or Area. Additionally the Location-constructor of the
     * DataNode is called.
     * 
     * @param location
     *            The Location of this node. Node is initialized with this
     *            location.
     * @return The newly created DataNode.
     */
    IDataNode newNode(GeoPoint location);

    /**
     * Setter-method.
     * 
     * @param isArea
     *            Whether object is an Area.
     */
    void setArea(boolean isArea);

    /**
     * Returns an array of GeoPoints representing the current way for being
     * displayed in a RouteOverlay. If isArea() is true, the first point will be
     * added as last point, this is a requirement of the RouteOverlay.
     * 
     * @param additional
     *            additional GeoPoint to be added to the way, may be null
     * 
     * @return The array of GeoPoints. (not null)
     */
    GeoPoint[] toGeoPointArray(GeoPoint additional);

}
