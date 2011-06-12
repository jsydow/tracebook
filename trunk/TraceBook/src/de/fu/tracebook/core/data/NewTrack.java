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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.mapsforge.android.maps.GeoPoint;

import de.fu.tracebook.core.data.implementation.NewDBMedia;
import de.fu.tracebook.core.data.implementation.NewDBNode;
import de.fu.tracebook.core.data.implementation.NewDBPointsList;
import de.fu.tracebook.core.data.implementation.NewDBTrack;
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

    /**
     * Creates a time stamp of the current time formatted according to W3C.
     * 
     * @return A time stamp String.
     */
    static String getW3CFormattedTimeStamp() {
        SimpleDateFormat sdf = new SimpleDateFormat(
                "yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        return sdf.format(new Date());
    }

    private boolean isNew;
    private String name;
    private NewDBTrack thisTrack;

    private IDataPointsList way;

    /**
     * Creates a whole new track and inserts it into the database.
     */
    public NewTrack() {
        this.name = getFilenameCompatibleTimeStamp();
        thisTrack = new NewDBTrack();
        thisTrack.name = this.name;
        thisTrack.datetime = this.name;
        thisTrack.insert();
        isNew = true;
    }

    /**
     * Create a NewTrack object from a track that is already in the database.
     * 
     * @param track
     *            The track which is in the database.
     */
    public NewTrack(NewDBTrack track) {
        this.name = track.name;
        thisTrack = track;
        isNew = false;
    }

    public void addMedia(IDataMedia medium) {
        NewDBMedia media = ((NewMedia) medium).getDBMedia();
        media.track = name;
        media.save();
    }

    public void delete() {
        NewDBMedia.deleteByTrack(name);
        for (IDataNode n : getNodes()) {
            ((NewNode) n).delete();
        }
        for (IDataPointsList w : getWays()) {
            ((NewPointsList) w).delete();
        }
        thisTrack.delete();
    }

    public void deleteMedia(int id) {
        Iterator<NewDBMedia> media = NewDBMedia.getByTrack(name).iterator();
        while (media.hasNext()) {
            NewDBMedia m = media.next();
            if (m.id == id) {
                NewMedia medium = new NewMedia(m);
                medium.delete();
            }
        }
    }

    public boolean deleteNode(int id) {
        NewDBNode node;
        node = NewDBNode.getById(id);
        if (node == null) {
            return false;
        }
        node.delete();
        StorageFactory.getStorage().getOverlayManager()
                .invalidateOverlayOfNode(new NewNode(node));
        return true;
    }

    public void deleteWay(int id) {
        NewDBPointsList pointslist = NewDBPointsList.getById(id);
        if (pointslist == null) {
            return;
        }
        pointslist.delete();

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
        List<NewDBMedia> dbmedia = NewDBMedia.getByTrack(name);
        for (NewDBMedia m : dbmedia) {
            media.add(new NewMedia(m));
        }
        return media;
    }

    public String getName() {
        return name;
    }

    public IDataNode getNodeById(int id) {
        NewDBNode node = NewDBNode.getById(id);
        if (node == null) {
            return null;
        }
        return new NewNode(node);
    }

    public List<IDataNode> getNodes() {
        List<IDataNode> nodes = new LinkedList<IDataNode>();
        List<NewDBNode> dbnodes = NewDBNode.getByTrack(name);
        for (NewDBNode n : dbnodes) {
            nodes.add(new NewNode(n));
        }
        return nodes;
    }

    public IDataPointsList getPointsListById(int id) {
        NewDBPointsList way = NewDBPointsList.getById(id);
        if (way == null) {
            return null;
        }
        return new NewPointsList(way);
    }

    public String getTrackDirPath() {
        return NewStorage.getTrackDirPath(name);
    }

    public List<IDataPointsList> getWays() {
        List<IDataPointsList> ways = new LinkedList<IDataPointsList>();
        List<NewDBPointsList> dbnodes = NewDBPointsList.getByTrack(name);
        for (NewDBPointsList pl : dbnodes) {
            ways.add(new NewPointsList(pl));
        }
        return ways;
    }

    public boolean isNew() {
        return isNew;
    }

    public IDataNode newNode(GeoPoint coordinates) {
        NewNode node = new NewNode(coordinates, thisTrack);
        return node;
    }

    public IDataPointsList newWay() {
        NewPointsList newway = new NewPointsList(thisTrack);
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
        return StorageFactory.getMediaObject(txtfile.getParent(),
                txtfile.getName());
    }

    public void setComment(String comment) {
        thisTrack.comment = comment;
        thisTrack.save();
    }

    public IDataPointsList setCurrentWay(IDataPointsList currentWay) {
        way = currentWay;
        return way;
    }

    public void setDatetime(String datetime) {
        thisTrack.datetime = datetime;
        thisTrack.save();
    }

    public int setName(String newname) {
        thisTrack.name = newname;
        thisTrack.save();
        return renameTrack(newname);
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
}
