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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.mapsforge.android.maps.GeoPoint;

import de.fu.tracebook.core.data.implementation.NewDBMedia;
import de.fu.tracebook.core.data.implementation.NewDBNode;
import de.fu.tracebook.core.data.implementation.NewDBPointsList;
import de.fu.tracebook.core.data.implementation.NewDBTag;
import de.fu.tracebook.core.data.implementation.NewDBTrack;

/**
 * The IDataNode implementation using ORMLite. It represents a single coordinate
 * and can be a point of interest.
 * 
 */
public class NewNode implements IDataNode {

    private long id;
    private long outputNodeId = 0;
    private NewDBNode thisNode;

    /**
     * Create a new Node that is inserted into the database.
     * 
     * @param coordinates
     *            The coordinates of this node.
     * @param way
     *            The parent way.
     */
    public NewNode(GeoPoint coordinates, NewDBPointsList way) {
        thisNode = new NewDBNode();
        if (coordinates != null) {
            thisNode.latitude = coordinates.getLatitudeE6();
            thisNode.longitude = coordinates.getLongitudeE6();
        }
        thisNode.way = way.id;
        thisNode.datetime = NewTrack.getW3CFormattedTimeStamp();
        thisNode.insert();
        this.id = thisNode.id;
    }

    /**
     * Create a new Node that is inserted into the database.
     * 
     * @param coordinates
     *            The coordinates of this node.
     * @param track
     *            The parent track.
     */
    public NewNode(GeoPoint coordinates, NewDBTrack track) {
        thisNode = new NewDBNode();
        if (coordinates != null) {
            thisNode.latitude = coordinates.getLatitudeE6();
            thisNode.longitude = coordinates.getLongitudeE6();
        }
        thisNode.datetime = NewTrack.getW3CFormattedTimeStamp();
        thisNode.track = track.name;
        thisNode.insert();
        this.id = thisNode.id;
    }

    /**
     * Create a NewNode object out of an existing DBNode object that is already
     * in the database.
     * 
     * @param node
     *            The DBNode object.
     */
    public NewNode(NewDBNode node) {
        this.id = node.id;
        thisNode = node;
    }

    public void addMedia(IDataMedia medium) {
        NewDBMedia media = ((NewMedia) medium).getDBMedia();
        media.node = id;
        media.save();
    }

    public void addTag(String key, String value) {
        NewDBTag tag = new NewDBTag();
        tag.key = key;
        tag.value = value;
        tag.node = id;
        tag.insert();
    }

    /**
     * Deletes this node from Database.
     */
    public void delete() {
        NewDBMedia.deleteByNode(id);
        NewDBTag.deleteByNode(id);
        thisNode.delete();
    }

    public void deleteMedia(int mId) {
        Iterator<NewDBMedia> media = NewDBMedia.getByNode(id).iterator();
        while (media.hasNext()) {
            NewDBMedia m = media.next();
            if (m.id == id) {
                NewMedia medium = new NewMedia(m);
                medium.delete();
            }
        }
    }

    public void deleteTag(String key) {
        NewDBTag.deleteByNode(id);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof NewNode) {
            if (((NewNode) o).id == this.id) {
                return true;
            }
        }
        return false;
    }

    public GeoPoint getCoordinates() {
        return new GeoPoint(thisNode.latitude, thisNode.longitude);
    }

    public IDataPointsList getDataPointsList() {
        NewDBPointsList way = NewDBPointsList.getById(thisNode.way);
        if (way == null) {
            return null;
        }
        return new NewPointsList(way);
    }

    public String getDatetime() {
        return thisNode.datetime;
    }

    /**
     * Returns the DBNode object represented by this NewNode.
     * 
     * @return The DBNode object.
     */
    public NewDBNode getDBNode() {
        return thisNode;
    }

    public long getId() {
        return id;
    }

    public List<IDataMedia> getMedia() {
        List<IDataMedia> media = new LinkedList<IDataMedia>();
        List<NewDBMedia> dbmedia = NewDBMedia.getByNode(id);
        for (NewDBMedia m : dbmedia) {
            media.add(new NewMedia(m));
        }
        return media;
    }

    /**
     * @return the outputNode
     */
    public long getOutputId() {
        if (outputNodeId == 0) {
            outputNodeId = StorageFactory.getStorage().getID();
        }
        return outputNodeId;
    }

    public Map<String, String> getTags() {
        return new TagMap(NewDBTag.getByNode(id), this, null);
    }

    public boolean hasAdditionalInfo() {
        return !(NewDBTag.getByNode(id).isEmpty() && NewDBMedia.getByNode(id)
                .isEmpty());
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return (int) getId();
    }

    public boolean isValid() {
        return thisNode.latitude != 0 || thisNode.longitude != 0;
    }

    public void setDataPointsList(IDataPointsList way) {
        thisNode.way = way.getId();
        thisNode.save();
    }

    /**
     * Set the track that this node belongs to.
     * 
     * @param track
     *            The track this node belongs to.
     */
    public void setDataTrack(IDataTrack track) {
        thisNode.track = track.getName();
        thisNode.save();
    }

    public void setDatetime(String datetime) {
        thisNode.datetime = datetime;
        thisNode.save();
    }

    public void setLocation(GeoPoint gp) {
        thisNode.latitude = gp.getLatitudeE6();
        thisNode.longitude = gp.getLongitudeE6();
        thisNode.save();
    }
}
