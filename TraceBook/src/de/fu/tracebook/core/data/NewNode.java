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

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.mapsforge.android.maps.GeoPoint;

import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.dao.Dao;

import de.fu.tracebook.core.data.implementation.DBMedia;
import de.fu.tracebook.core.data.implementation.DBNode;
import de.fu.tracebook.core.data.implementation.DBPointsList;
import de.fu.tracebook.core.data.implementation.DBTrack;
import de.fu.tracebook.core.data.implementation.DataOpenHelper;
import de.fu.tracebook.util.LogIt;

/**
 * The IDataNode implementation using ORMLite. It represents a single coordinate
 * and can be a point of interest.
 * 
 */
public class NewNode extends Updateable implements IDataNode {

    private long id;
    private DBNode thisNode;

    /**
     * Create a NewNode object out of an existing DBNode object that is already
     * in the database.
     * 
     * @param node
     *            The DBNode object.
     */
    public NewNode(DBNode node) {
        this.id = node.id;
        thisNode = node;
    }

    /**
     * Create a new Node that is inserted into the database.
     * 
     * @param coordinates
     *            The coordinates of this node.
     * @param way
     *            The parent way.
     */
    public NewNode(GeoPoint coordinates, DBPointsList way) {
        DBNode node = new DBNode();
        node.id = StorageFactory.getStorage().getID();
        if (coordinates != null) {
            node.latitude = coordinates.getLatitudeE6();
            node.longitude = coordinates.getLongitudeE6();
        }
        node.way = way;

        try {
            DataOpenHelper.getInstance().getNodeDAO().create(node);
            thisNode = DataOpenHelper.getInstance().getNodeDAO()
                    .queryForId(new Long(id));
        } catch (SQLException e) {
            LogIt.e("Could not create new node.");
        }
    }

    /**
     * Create a new Node that is inserted into the database.
     * 
     * @param coordinates
     *            The coordinates of this node.
     * @param track
     *            The parent track.
     */
    public NewNode(GeoPoint coordinates, DBTrack track) {
        DBNode node = new DBNode();
        node.id = StorageFactory.getStorage().getID();
        if (coordinates != null) {
            node.latitude = coordinates.getLatitudeE6();
            node.longitude = coordinates.getLongitudeE6();
        }
        node.track = track;

        try {
            DataOpenHelper.getInstance().getNodeDAO().create(node);
            thisNode = DataOpenHelper.getInstance().getNodeDAO()
                    .queryForId(new Long(id));
        } catch (SQLException e) {
            LogIt.e("Could not create new node.");
        }
    }

    public void addMedia(IDataMedia medium) {
        DBMedia media = new DBMedia();
        media.name = medium.getName();
        media.path = medium.getPath();
        thisNode.media.add(media);
        update();
    }

    public void deleteMedia(int mId) {
        CloseableIterator<DBMedia> media = thisNode.media.closeableIterator();
        while (media.hasNext()) {
            DBMedia m = media.next();
            if (m.id == id) {
                media.remove();
                NewMedia medium = new NewMedia(m);
                medium.delete();
            }
        }
        try {
            media.close();
        } catch (SQLException e) {
            // do nothing
        }
        update();

    }

    public GeoPoint getCoordinates() {
        return new GeoPoint(thisNode.latitude, thisNode.longitude);
    }

    public IDataPointsList getDataPointsList() {
        return new NewPointsList(thisNode.way);
    }

    public String getDatetime() {
        return thisNode.datetime;
    }

    /**
     * Returns the DBNode object represented by this NewNode.
     * 
     * @return The DBNode object.
     */
    public DBNode getDBNode() {
        return thisNode;
    }

    public long getId() {
        return id;
    }

    public List<IDataMedia> getMedia() {
        List<IDataMedia> media = new LinkedList<IDataMedia>();
        if (!thisNode.media.isEmpty()) {
            CloseableIterator<DBMedia> iter = thisNode.media
                    .closeableIterator();
            for (DBMedia m = iter.next(); iter.hasNext(); m = iter.next()) {
                media.add(new NewMedia(m));
            }
            try {
                iter.close();
            } catch (SQLException e) {
                // do nothing
            }
        }
        return media;
    }

    public Map<String, String> getTags() {
        return new TagMap(this, thisNode.tags);
    }

    public boolean hasAdditionalInfo() {
        return !(thisNode.tags.isEmpty() && thisNode.media.isEmpty());
    }

    public boolean isValid() {
        return thisNode.latitude != 0 || thisNode.longitude != 0;
    }

    public void setDataPointsList(DataPointsList way) {
        // do nothing
    }

    public void setDatetime(String datetime) {
        thisNode.datetime = datetime;
        update();
    }

    public void setLocation(GeoPoint gp) {
        thisNode.latitude = gp.getLatitudeE6();
        thisNode.longitude = gp.getLongitudeE6();
        update();

    }

    private Dao<DBNode, Long> getDao() {
        return DataOpenHelper.getInstance().getNodeDAO();
    }

    /**
     * Updates internal DBNode to Database.
     */
    @Override
    void update() {
        try {
            getDao().update(thisNode);
        } catch (SQLException e) {
            LogIt.e("Updating node failed.");
        }
    }

}
