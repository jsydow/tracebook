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

import java.util.ArrayList;
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

public class NewPointsList implements IDataPointsList {

    private long id;
    private List<NewNode> pois = new ArrayList<NewNode>();
    private NewDBPointsList thisWay;

    /**
     * Creates a NewPointsList object out of an existing database entry.
     * 
     * @param newway
     *            The way as saved in the database.
     */
    public NewPointsList(NewDBPointsList newway) {
        this.thisWay = newway;
        this.id = newway.id;
    }

    /**
     * Creates a new way.
     * 
     * @param track
     *            The track saved in the database.
     */
    public NewPointsList(NewDBTrack track) {
        thisWay = new NewDBPointsList();
        thisWay.datetime = NewTrack.getW3CFormattedTimeStamp();
        thisWay.track = track.name;
        thisWay.isArea = false;
        thisWay.insert();
        this.id = thisWay.id;
    }

    public void addMedia(IDataMedia medium) {
        NewDBMedia media = ((NewMedia) medium).getDBMedia();
        media.way = id;
        media.save();
    }

    public void addTag(String key, String value) {
        NewDBTag tag = new NewDBTag();
        tag.key = key;
        tag.value = value;
        tag.way = id;
        tag.insert();

    }

    /**
     * Deletes this way.
     */
    public void delete() {
        NewDBMedia.deleteByWay(id);
        NewDBTag.deleteByWay(id);
        for (IDataNode n : getNodes()) {
            ((NewNode) n).delete();
        }
        this.thisWay.delete();
    }

    public void deleteMedia(int id1) {
        Iterator<NewDBMedia> media = NewDBMedia.getByWay(id1).iterator();
        while (media.hasNext()) {
            NewDBMedia m = media.next();
            if (m.id == id1) {
                NewMedia medium = new NewMedia(m);
                medium.delete();
            }
        }
    }

    public IDataNode deleteNode(int nodeId) {
        // not needed: see NewTrack
        return null;
    }

    public void deleteTag(String key) {
        List<NewDBTag> tags = NewDBTag.getByWay(id);
        for (NewDBTag tag : tags) {
            if (tag.key == key) {
                tag.delete();
            }
        }
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
        if (o instanceof NewPointsList) {
            if (((NewPointsList) o).id == this.id) {
                return true;
            }
        }
        return false;
    }

    public String getDatetime() {
        return thisWay.datetime;
    }

    /**
     * Returns the DBPointsList that this PointsList represents.
     * 
     * @return The DBPointsList of this way.
     */
    public NewDBPointsList getDBPointsList() {
        return thisWay;
    }

    public long getId() {
        return thisWay.id;
    }

    public List<IDataMedia> getMedia() {
        List<IDataMedia> media = new LinkedList<IDataMedia>();
        List<NewDBMedia> dbmedia = NewDBMedia.getByWay(id);
        for (NewDBMedia m : dbmedia) {
            media.add(new NewMedia(m));
        }
        return media;
    }

    public IDataNode getNodeById(int id1) {
        for (NewNode node : pois) {
            if (node.getId() == id1) {
                node.getDBNode().update();
                return node;
            }
        }

        NewDBNode node = NewDBNode.getById(id1);
        if (node == null) {
            return null;
        }
        NewNode newnode = new NewNode(node);
        pois.add(newnode);
        return newnode;
    }

    public List<IDataNode> getNodes() {
        List<IDataNode> nodes = new LinkedList<IDataNode>();
        List<NewDBNode> dbnodes = NewDBNode.getByWay(id);
        for (NewDBNode n : dbnodes) {
            nodes.add(new NewNode(n));
        }
        return nodes;
    }

    public Map<String, String> getTags() {
        return new TagMap(NewDBTag.getByWay(id), null, this);
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

    public boolean isArea() {
        return thisWay.isArea;
    }

    public IDataNode newNode(GeoPoint location) {
        NewNode node = new NewNode(location, thisWay);
        return node;
    }

    public void setArea(boolean isArea) {
        thisWay.isArea = isArea;
        thisWay.save();
    }

    public void setDatetime(String datetime) {
        thisWay.datetime = datetime;
        thisWay.save();

    }

    public GeoPoint[] toGeoPointArray(GeoPoint additional) {
        List<IDataNode> nodes = this.getNodes();
        GeoPoint[] ret = new GeoPoint[nodes.size() + (isArea() ? 1 : 0)];
        int i = 0;
        for (IDataNode node : nodes) {
            ret[i] = node.getCoordinates();
            ++i;
        }
        if (isArea()) {
            ret[i] = ret[0];
        }

        return ret;
    }
}
