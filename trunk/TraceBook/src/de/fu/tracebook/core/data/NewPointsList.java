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

public class NewPointsList extends Updateable implements IDataPointsList {

    private long id;
    private DBPointsList thisWay;

    public NewPointsList(DBPointsList newway) {
        this.thisWay = newway;
        this.id = newway.id;
    }

    public NewPointsList(DBTrack track) {
        DBPointsList way = new DBPointsList();
        way.id = StorageFactory.getStorage().getID();
        way.datetime = NewTrack.getW3CFormattedTimeStamp();
        way.track = track;
        way.isArea = false;
        this.id = way.id;
        try {
            DataOpenHelper.getInstance().getPointslistDAO().create(way);
            thisWay = DataOpenHelper.getInstance().getPointslistDAO()
                    .queryForId(new Long(id));
        } catch (SQLException e) {
            LogIt.e("Could not create new node.");
        }
    }

    public void addMedia(IDataMedia medium) {
        DBMedia media = new DBMedia();
        media.name = medium.getName();
        media.path = medium.getPath();
        thisWay.media.add(media);
        update();

    }

    public void deleteMedia(int id) {
        CloseableIterator<DBMedia> media = thisWay.media.closeableIterator();
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

    public IDataNode deleteNode(int nodeId) {
        DBNode node;
        try {
            node = DataOpenHelper.getInstance().getNodeDAO()
                    .queryForId(new Long(id));
            if (node == null) {
                return null;
            }
            DataOpenHelper.getInstance().getNodeDAO().delete(node);
        } catch (SQLException e) {
            LogIt.e("Could not delete node.");
            return null;
        }
        return new NewNode(node);
    }

    public String getDatetime() {
        return thisWay.datetime;
    }

    /**
     * Returns the DBPointsList that this PointsList represents.
     * 
     * @return The DBPointsList of this way.
     */
    public DBPointsList getDBPointsList() {
        return thisWay;
    }

    public long getId() {
        return thisWay.id;
    }

    public List<IDataMedia> getMedia() {
        List<IDataMedia> media = new LinkedList<IDataMedia>();
        if (!thisWay.media.isEmpty()) {
            CloseableIterator<DBMedia> iter = thisWay.media.closeableIterator();
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

    public IDataNode getNodeById(int nodeId) {
        DBNode node;
        try {
            node = DataOpenHelper.getInstance().getNodeDAO()
                    .queryForId(new Long(id));
        } catch (SQLException e) {
            LogIt.e("Could not get node");
            return null;
        }
        if (node == null) {
            return null;
        }

        return new NewNode(node);
    }

    public List<IDataNode> getNodes() {
        List<IDataNode> nodes = new LinkedList<IDataNode>();
        if (!thisWay.nodes.isEmpty()) {
            CloseableIterator<DBNode> iter = thisWay.nodes.closeableIterator();
            for (DBNode n = iter.next(); iter.hasNext(); n = iter.next()) {
                nodes.add(new NewNode(n));
            }
            try {
                iter.close();
            } catch (SQLException e) {
                // do nothing
            }
        }
        return nodes;
    }

    public Map<String, String> getTags() {
        return new TagMap(this, thisWay.tags);
    }

    public boolean hasAdditionalInfo() {
        return !(thisWay.media.isEmpty() && thisWay.tags.isEmpty());
    }

    public boolean isArea() {
        return thisWay.isArea;
    }

    public IDataNode newNode(GeoPoint location) {
        NewNode node = new NewNode(location, thisWay);
        thisWay.nodes.add(node.getDBNode());
        update();
        return node;
    }

    public void setArea(boolean isArea) {
        thisWay.isArea = isArea;
        update();
    }

    public void setDatetime(String datetime) {
        thisWay.datetime = datetime;
        update();

    }

    public GeoPoint[] toGeoPointArray(GeoPoint additional) {
        // TODO Auto-generated method stub
        return null;
    }

    private Dao<DBPointsList, Long> getDao() {
        return DataOpenHelper.getInstance().getPointslistDAO();
    }

    void reinit() {
        try {
            thisWay = DataOpenHelper.getInstance().getPointslistDAO()
                    .queryForId(new Long(id));
        } catch (SQLException e) {
            LogIt.e("Could not reinit way.");
        }
    }

    /**
     * Updates internal DBNode to Database.
     */
    @Override
    void update() {
        try {
            getDao().update(thisWay);
        } catch (SQLException e) {
            LogIt.e("Updating node failed.");
        }
    }

}
