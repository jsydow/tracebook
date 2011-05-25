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

import java.util.Collection;
import java.util.List;

import org.mapsforge.android.maps.GeoPoint;
import org.mapsforge.android.maps.OverlayItem;

public interface IDataTrack {

    /**
     * Returns a list of {@link OverlayItem}s whose {@link DataNode} does no
     * longer exist. For efficiency reasons they are stored in a list, so when
     * e.g. a way is deleted, the Overlay will not be redrawn for every deleted
     * waypoint, but receive a notification that invalid OverlayItems exist when
     * the removal procedure has finished.
     * 
     * @return The list of invalid OverlayItems. It will be cleared by this
     *         call.
     */
    Collection<OverlayItem> clearInvalidItems();

    /**
     * This method deletes a Node (POI) of this Track or a node of one of the
     * ways of this track from the devices memory and the working memory. If
     * this node does not exist nothing is done.
     * 
     * @param id
     *            The id of the POI to delete.
     * @return A reference to the deleted DataNode object if it exists, null
     *         otherwise..
     */
    IDataNode deleteNode(int id);

    /**
     * This method deletes a Way of this Track from the devices memory and the
     * working memory. If the Way does not exist nothing is done.
     * 
     * @param id
     *            The id of the Way to delete.
     */
    void deleteWay(int id);

    /**
     * Getter-method.
     * 
     * @return The comment of the Track.
     */
    String getComment();

    /**
     * Getter-method. The currently edited Way.
     * 
     * @return The current Way. Current Way can be null if not initialized.
     */
    DataPointsList getCurrentWay();

    /**
     * Search the whole track for an DataMapObject by id. This may be a DataNode
     * or DataPointsList.
     * 
     * @param id
     *            The id of the DataMapObject that is being searched for.
     * @return The DataMapObject where get_id()==id or null if there is not such
     *         an object.
     */
    DataMapObject getDataMapObjectById(int id);

    /**
     * Getter-method.
     * 
     * @return The name of the Track.
     */
    String getName();

    /**
     * Get a DataNode with a given id.
     * 
     * @param id
     *            The id of the DataNode
     * @return The DataNode or null if there is none with such an id.
     */
    DataNode getNodeById(int id);

    /**
     * Tries to find the {@link DataNode} containing the given
     * {@link OverlayItem}.
     * 
     * @param item
     *            The OverlayItem that should be searched for.
     * @return The DataNode containing the OverlayItem, or null if no DataNode
     *         with the OverlayItem was found
     */
    DataNode getNodeByOverlayItem(OverlayItem item);

    /**
     * Getter-method that returns a list of all nodes. The returned List is the
     * one stored in this object. Changing the returned List will therefore
     * change this list
     * 
     * @return All POI's of this Track. (not null)
     */
    List<DataNode> getNodes();

    /**
     * Get a DataPointsList with a given id.
     * 
     * @param id
     *            The id of the DataPointsList
     * @return The DataPointsList or null if there is none with such an id.
     */
    DataPointsList getPointsListById(int id);

    /**
     * Returns the complete absolute path to this Track directory.
     * 
     * @return path to the track directory
     */
    String getTrackDirPath();

    /**
     * Getter-method that returns a list of all Ways. The returned List is the
     * one stored in this object. Changing the returned List will therefore
     * change this list
     * 
     * @return All ways of this Track. (not null)
     */
    List<DataPointsList> getWays();

    /**
     * Marks an {@link OverlayItem} as invalid so it can be removed later.
     * 
     * @param overlayItem
     *            The outdated OverlayItem
     */

    void invalidateOverlayItem(OverlayItem overlayItem);

    /**
     * Create a new Node (i.e. POI) and add it to the Track
     * 
     * @param coordinates
     *            The GeoPoint object to be used for constructing the new Node.
     * @return The newly created POI.
     */
    DataNode newNode(GeoPoint coordinates);

    /**
     * Create a new Way/Area in this Track.
     * 
     * @return The newly created Way.
     */
    DataPointsList newWay();

    /**
     * This method saves a String to a .txt file and generates a
     * DataMedia-object which can be added to any DataMediaHolder.
     * 
     * @param text
     *            The text to save
     * @return DataMedia object which references the Text just saved.
     */
    IDataMedia saveText(String text);

    /**
     * Setter-method.
     * 
     * @param comment
     *            The new comment of the Track.
     */
    void setComment(String comment);

    /**
     * Sets a Way as currently edited Way. Setter-method.
     * 
     * @param currentWay
     *            The new currently edited Way.
     * @return Returns the parameter currentWay for further use.
     */
    IDataPointsList setCurrentWay(DataPointsList currentWay);

    /**
     * Setter-method. Renames a Track in the devices and working memory.
     * 
     * @param newname
     *            The new name of the DataTrack
     * @return Returns 0 if renaming was successful, -1 if track could not be
     *         found, -2 if there is a track with the new track name and -3 if
     *         the renaming fails otherwise.
     */
    int setName(String newname);

}
