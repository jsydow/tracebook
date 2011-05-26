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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.os.Environment;
import de.fu.tracebook.util.LogIt;

/**
 * The class that holds all Data. The class has 0 to several Tracks. Each Track
 * consists of Nodes and PointLists (Area/Way). PointsLists consist of Nodes.
 * 
 * In the implementation u see there is a difference between the track names and
 * the tracks themselves. The names-list contains all the names of the tracks
 * that are in the working memory and on the devices memory. It may not be
 * perfectly synchronized with the actual tracks available as it is updated only
 * when needed. These names can be used to actually load a Track completely into
 * memory. The primary reason for the names is the list of all Tracks without
 * loading them all.
 */
public final class DataStorage implements IDataStorage {

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
                        LogIt.e("DeleteDirectory",
                                "Could not delete file " + f.getName()
                                        + " in directory " + dir.getPath());
                    }
                }
            }
            if (!dir.delete()) {
                LogIt.e("DeleteDirectory",
                        "Could not delete directory " + dir.getName());

            }
        }
    }

    /**
     * Last given ID for a MapObject.
     */
    private int lastID;

    /**
     * A List of all possible track names on the working memory and devices
     * memory.
     */
    private List<String> names;

    /**
     * Currently active Track.
     */
    private DataTrack track;

    /**
     * Default private constructor for Singleton implementation.
     */
    DataStorage() {
        names = Collections.synchronizedList(new ArrayList<String>());
        retrieveTrackNames();
        lastID = 1;
    }

    public void deleteTrack(String trackname) {
        DataTrack.delete(trackname);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * de.fu.tracebook.core.data.IDataStorage#deserializeTrack(java.lang.String)
     */
    public DataTrack deserializeTrack(String name) {
        DataTrack dt = DataTrack.deserialize(name, false);
        return dt;
    }

    public boolean doesTrackExist(String trackname) {
        return DataTrack.exists(trackname);
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu.tracebook.core.data.IDataStorage#getAllTracks()
     */
    public List<String> getAllTracks() {
        retrieveTrackNames();
        return names;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu.tracebook.core.data.IDataStorage#getID()
     */
    public synchronized int getID() {
        lastID--;
        return lastID;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu.tracebook.core.data.IDataStorage#getTrack()
     */
    public IDataTrack getTrack() {
        return track;
    }

    public DataTrackInfo getTrackInfo(String trackName) {
        return DataTrackInfo.deserialize(trackName);
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu.tracebook.core.data.IDataStorage#newTrack()
     */
    public IDataTrack newTrack() {
        IDataTrack dt = new DataTrack();
        return dt;
    }

    public int renameTrack(String oldname, String newname) {
        return DataTrack.rename(oldname, newname);
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu.tracebook.core.data.IDataStorage#serialize()
     */
    public void serialize() {
        track.serialize();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * de.fu.tracebook.core.data.IDataStorage#setTrack(de.fu.tracebook.core.
     * data.DataTrack)
     */
    public IDataTrack setTrack(IDataTrack currentTrack) {
        this.track = (DataTrack) currentTrack;
        return currentTrack;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu.tracebook.core.data.IDataStorage#unloadAllTracks()
     */
    public void unloadAllTracks() {
        setTrack(null);
    }

    /**
     * Load the list of all Tracks that are stored on the devices memory. It
     * empties the current list of track names. These names can be returned by
     * getAllTracks().
     */
    private void retrieveTrackNames() {
        File tracebookdir = new File(getTraceBookDirPath());

        if (tracebookdir.isDirectory()) {
            names.clear();
            File[] dirs = tracebookdir.listFiles();

            for (File f : dirs) {
                // is directory?
                if (f.isDirectory()) {
                    // does track.tbt exist?
                    File tracktbt = new File(DataTrack.getPathOfTrackTbTFile(f
                            .getName()));
                    if (tracktbt.isFile()) {
                        names.add(f.getName());
                    } else {
                        // not track.tbt -> delete directory.
                        deleteDirectory(f);
                    }
                }
            }

        } else {
            LogIt.w("TraceBookDirectory",
                    "The TraceBook directory path doesn't point to a directory! wtf?");
        }
    }

}
