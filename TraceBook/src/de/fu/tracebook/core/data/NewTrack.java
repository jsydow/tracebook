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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

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
 * An Implementation of IDataTrack using ORMLite. It represents a tracking
 * session.
 * 
 */
public class NewTrack implements IDataTrack {

    /**
     * Creates a time stamp of the current time which can be used as a filename.
     * 
     * @return The time stamp String.
     */
    private static String getFilenameCompatibleTimeStamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        return sdf.format(new Date());
    }

    private String name;
    private DBTrack thisTrack;
    private IDataPointsList way;

    /**
     * Creates a whole new track and inserts it into the database.
     */
    public NewTrack() {
        this.name = getFilenameCompatibleTimeStamp();
        DBTrack track = new DBTrack();
        track.name = this.name;
        track.datetime = this.name;
        try {
            DataOpenHelper.getInstance().getTrackDAO().create(track);
        } catch (SQLException e) {
            LogIt.e("Could not create new track.");
        }
        thisTrack = track;
    }

    /**
     * Create a NewTrack object from a track that is already in the database.
     * 
     * @param track
     *            The track which is in the database.
     */
    public NewTrack(DBTrack track) {
        this.name = track.name;
        thisTrack = track;
    }

    public void addMedia(IDataMedia medium) {
        DBMedia media = new DBMedia();
        media.name = medium.getName();
        media.path = medium.getPath();
        thisTrack.media.add(media);
        update();
    }

    public void deleteMedia(int id) {
        CloseableIterator<DBMedia> media = thisTrack.media.closeableIterator();
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

    public boolean deleteNode(int id) {
        DBNode node;
        try {
            node = DataOpenHelper.getInstance().getNodeDAO()
                    .queryForId(new Long(id));
            if (node == null) {
                return false;
            }
            DataOpenHelper.getInstance().getNodeDAO().delete(node);
            StorageFactory.getStorage().getOverlayManager()
                    .invalidateOverlayOfNode(new NewNode(node));
        } catch (SQLException e) {
            LogIt.e("Could not delete node.");
            return false;
        }
        return true;
    }

    public void deleteWay(int id) {
        try {
            DBPointsList pointslist = DataOpenHelper.getInstance()
                    .getPointslistDAO().queryForId(new Long(id));
            if (pointslist == null) {
                return;
            }
            DataOpenHelper.getInstance().getPointslistDAO().delete(pointslist);
        } catch (SQLException e) {
            LogIt.e("Could not delete way.");
        }
    }

    public String getComment() {
        return thisTrack.comment;
    }

    public IDataPointsList getCurrentWay() {
        return way;
    }

    public IDataMapObject getDataMapObjectById(int id) {
        IDataNode node = getNodeById(id);
        if (node != null) {
            return node;
        }

        return getPointsListById(id);
    }

    public String getDatetime() {
        return thisTrack.datetime;
    }

    public List<IDataMedia> getMedia() {
        List<IDataMedia> media = new LinkedList<IDataMedia>();
        if (!thisTrack.media.isEmpty()) {
            CloseableIterator<DBMedia> iter = thisTrack.media
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

    public String getName() {
        return name;
    }

    public IDataNode getNodeById(int id) {
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
        if (!thisTrack.nodes.isEmpty()) {
            CloseableIterator<DBNode> iter = thisTrack.nodes
                    .closeableIterator();
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

    public IDataPointsList getPointsListById(int id) {
        DBPointsList newway;
        try {
            newway = DataOpenHelper.getInstance().getPointslistDAO()
                    .queryForId(new Long(id));
        } catch (SQLException e) {
            LogIt.e("Could not get node");
            return null;
        }
        if (newway == null) {
            return null;
        }

        return new NewPointsList(newway);
    }

    public String getTrackDirPath() {
        return NewStorage.getTrackDirPath(name);
    }

    public List<IDataPointsList> getWays() {
        List<IDataPointsList> ways = new LinkedList<IDataPointsList>();
        if (!thisTrack.ways.isEmpty()) {
            CloseableIterator<DBPointsList> iter = thisTrack.ways
                    .closeableIterator();
            for (DBPointsList p = iter.next(); iter.hasNext(); p = iter.next()) {
                ways.add(new NewPointsList(p));
            }

            try {
                iter.close();
            } catch (SQLException e) {
                // do nothing
            }
        }
        return ways;
    }

    public IDataNode newNode(GeoPoint coordinates) {
        NewNode node = new NewNode(coordinates);
        thisTrack.nodes.add(node.getDBNode());
        update();
        return node;
    }

    public IDataPointsList newWay() {
        NewPointsList newway = new NewPointsList();
        thisTrack.ways.add(newway.getDBPointsList());
        update();
        return newway;
    }

    public IDataMedia saveText(String text) {
        File txtfile = new File(getTrackDirPath() + File.separator
                + getFilenameCompatibleTimeStamp() + ".txt");
        try {
            if (txtfile.createNewFile()) {
                BufferedWriter buf = new BufferedWriter(new FileWriter(txtfile));
                buf.write(text);
                buf.close();
            } else {
                LogIt.w("Text file with this timestamp already exists.");
            }
        } catch (IOException e) {
            LogIt.e("Error while writing text file.");
            return null;
        }
        return new DataMedia(txtfile.getParent(), txtfile.getName());
    }

    public void setComment(String comment) {
        this.thisTrack.comment = comment;
        update();
    }

    public IDataPointsList setCurrentWay(IDataPointsList currentWay) {
        this.way = currentWay;
        return way;
    }

    public void setDatetime(String datetime) {
        this.thisTrack.datetime = datetime;
        update();
    }

    public int setName(String newname) {
        int result;
        DBTrack track;
        try {
            track = getDao().queryForId(name);
            if (track == null) {
                return -3;
            }
            getDao().updateId(track, newname);
            result = renameTrack(newname);
            if (result == 0) {
                this.name = newname;
            }
        } catch (SQLException e) {
            LogIt.e("Setting new name for track failed.");
        }
        return 0;
    }

    private Dao<DBTrack, String> getDao() {
        return DataOpenHelper.getInstance().getTrackDAO();
    }

    /**
     * Renames a Track on the devices memory.
     * 
     * @param newname
     *            The new name of the Track
     * @return Returns 0 if renaming was successful, -1 if track could not be
     *         found, -2 if there is a track with the new track name and -3 if
     *         the renaming fails otherwise.
     */
    private int renameTrack(String newname) {
        File trackdir = new File(getTrackDirPath());
        if (trackdir.isDirectory()) {
            File newtrackdir = new File(NewStorage.getTrackDirPath(newname));
            if (!newtrackdir.isDirectory()) {
                if (!trackdir.renameTo(newtrackdir)) {
                    LogIt.w("Could not rename Track.");
                    return -3;
                }
            } else {
                LogIt.w("Track of new trackname already exists.");
                return -2;
            }
        } else {
            LogIt.w("Could not find Track " + getName());
            return -1;
        }
        return 0;
    }

    private void update() {
        try {
            getDao().update(thisTrack);
        } catch (SQLException e) {
            LogIt.e("Updating track failed.");
        }
    }

}
