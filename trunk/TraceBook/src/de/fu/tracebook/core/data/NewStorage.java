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

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import android.os.Environment;

import com.j256.ormlite.dao.CloseableIterator;

import de.fu.tracebook.core.data.implementation.DBTrack;
import de.fu.tracebook.core.data.implementation.DataOpenHelper;
import de.fu.tracebook.util.LogIt;

/**
 * The IDataStorage implementation using ORMLite. It represents the root of all
 * data storage in TraceBook.
 */
public class NewStorage implements IDataStorage {

    /**
     * Return a String of the path to the TraceBook directory without an ending
     * / Path is like: /sdcard/TraceBook.
     * 
     * @return Path of the TraceBook directory.
     */
    public static String getTraceBookDirPath() {
        return Environment.getExternalStorageDirectory() + File.separator
                + "TraceBook";
    }

    /**
     * Deletes a directory and all files in it. If File is no directory nothing
     * is done.
     * 
     * @param dir
     *            The directory to delete
     */
    static void deleteDirectory(File dir) {
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            for (File f : files) {
                if (f.isFile()) {
                    if (!f.delete()) {
                        LogIt.e("Could not delete file " + f.getName()
                                + " in directory " + dir.getPath());
                    }
                }
            }
            if (!dir.delete()) {
                LogIt.e("Could not delete directory " + dir.getName());

            }
        }
    }

    /**
     * Completes a track directory name to a complete path. Note: Do not changed
     * as this method is misused somewhere.
     * 
     * @param dir
     *            Name of the track directory
     * @return The complete path to the track directory.
     */
    static String getTrackDirPath(String dir) {
        return DataStorage.getTraceBookDirPath() + File.separator + dir;
    }

    /**
     * Last given ID for a MapObject.
     */
    private int lastID;

    /**
     * The Overlaymanager
     */
    private OverlayManager overlays = new OverlayManager();

    /**
     * Currently active Track.
     */
    private IDataTrack track;

    public void deleteTrack(String trackname) {
        try {
            DBTrack existingTrack = DataOpenHelper.getInstance().getTrackDAO()
                    .queryForId(trackname);
            if (existingTrack == null) {
                return;
            }
            DataOpenHelper.getInstance().getTrackDAO().delete(existingTrack);
        } catch (SQLException e) {
            LogIt.e("Deleting track from database failed");
            return;
        }
        deleteDirectory(new File(getTrackDirPath(trackname)));

    }

    public IDataTrack deserializeTrack(String name) {
        try {
            DBTrack loadedTrack = DataOpenHelper.getInstance().getTrackDAO()
                    .queryForId(name);
            if (loadedTrack == null) {
                return null;
            }
            return new NewTrack(loadedTrack);
        } catch (SQLException e) {
            LogIt.e("Could not load track");
            return null;
        }
    }

    public boolean doesTrackExist(String trackname) {
        try {
            DBTrack existingTrack = DataOpenHelper.getInstance().getTrackDAO()
                    .queryForId(trackname);
            if (existingTrack == null) {
                return false;
            }
        } catch (SQLException e) {
            return false;
        }
        return true;
    }

    public void ensureThatTraceBookDirExists() {
        File dir = new File(getTraceBookDirPath());
        if (!dir.isDirectory()) {
            if (!dir.mkdir()) {
                LogIt.e("Could not create TraceBook-directory");
            }
        }
    }

    public List<String> getAllTracks() {
        List<String> names = new ArrayList<String>();
        CloseableIterator<DBTrack> tracks = DataOpenHelper.getInstance()
                .getTrackDAO().iterator();

        while (tracks.hasNext()) {
            names.add(tracks.next().name);
        }

        try {
            tracks.close();
        } catch (SQLException e) {
            LogIt.e("Closing cursor after getting list of all track names failed.");
        }
        return names;
    }

    public int getID() {
        return ++lastID;
    }

    public OverlayManager getOverlayManager() {
        return overlays;
    }

    public IDataTrack getTrack() {
        return track;
    }

    public DataTrackInfo getTrackInfo(String trackName) {
        IDataTrack infotrack = deserializeTrack(trackName);
        return new DataTrackInfo(infotrack.getName(), infotrack.getDatetime(),
                infotrack.getComment(), infotrack.getNodes().size(), infotrack
                        .getWays().size(), infotrack.getMedia().size());
    }

    public IDataTrack newTrack() {
        IDataTrack newtrack = new NewTrack();
        createNewTrackFolder(newtrack.getName());
        return newtrack;
    }

    public int renameTrack(String oldname, String newname) {
        IDataTrack oldtrack = deserializeTrack(oldname);
        if (oldtrack == null) {
            return -1;
        }
        return oldtrack.setName(newname);
    }

    public void serialize() {
        // TODO Auto-generated method stub

    }

    public IDataTrack setTrack(IDataTrack currentTrack) {
        track = currentTrack;
        return track;
    }

    public void unloadAllTracks() {
        // TODO Auto-generated method stub

    }

    /**
     * Creates new folder in .../TraceBook for this Track. Such a directory must
     * exist when track is serialized.
     */
    private void createNewTrackFolder(String name) {
        File dir = new File(DataStorage.getTraceBookDirPath() + File.separator
                + name);
        if (!dir.isDirectory()) {
            if (!dir.mkdir()) {
                LogIt.e("Could not create new track folder " + name);
            }
        }
    }

}
