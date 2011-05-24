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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
 * 
 * It is a Singleton!
 */
public final class DataStorage {

    /**
     * Singleton instance.
     */
    private static DataStorage instance;

    /**
     * Singleton implementation. This method returns the one and only instance
     * of this class.
     * 
     * @return The instance of this class.
     */
    public static synchronized DataStorage getInstance() {
        if (instance == null)
            instance = new DataStorage();
        return instance;
    }

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
     * This method removes duplicates in a list of Strings.
     * 
     * @param list
     *            The list of Strings.
     */
    static void removeDuplicatesInStringList(List<String> list) {
        synchronized (list) {
            Set<String> tmp = new HashSet<String>(list);
            list.clear();
            list.addAll(tmp);
            Collections.sort(list);
        }
        return;
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
    private DataStorage() {
        names = Collections.synchronizedList(new ArrayList<String>());
        retrieveTrackNames();
        lastID = 1;
    }

    /**
     * Loads the complete Track (with everything it contains) into working
     * memory. If such a Track does not exist nothing is done.
     * 
     * @param name
     *            The name of the Track.
     * @return The deserialized Track or null if track does not exist.
     */
    public DataTrack deserializeTrack(String name) {
        DataTrack dt = DataTrack.deserialize(name);
        return dt;
    }

    /**
     * Returns a list of the names of all tracks that are currently stored in
     * this DataStorage object. The names can be used as argument to getTrack().
     * 
     * @return List of the names of all tracks. If there are no tracks stored
     *         then the list will be empty.
     */
    public List<String> getAllTracks() {
        retrieveTrackNames();
        return names;
    }

    /**
     * Create a new unique id to use for a new map object.
     * 
     * @return A new unique id > 0.
     */
    public synchronized int getID() {
        lastID--;
        return lastID;
    }

    /**
     * Getter-method.
     * 
     * @return The currently edited Track is returned.(may be null)
     */
    public DataTrack getTrack() {
        return track;
    }

    /**
     * Create a new Track in working memory. Don't forget to serialize it!
     * 
     * @return The newly created Track.
     */
    public DataTrack newTrack() {
        DataTrack dt = new DataTrack();
        return dt;
    }

    /**
     * Load the list of all Tracks that are stored on the devices memory. It
     * empties the current list of track names. These names can be returned by
     * getAllTracks().
     */
    public void retrieveTrackNames() {
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

    /**
     * Will serialize all tracks that are currently stored in this DataStorage.
     */
    public void serialize() {
        track.serialize();
    }

    /**
     * Setter-method for the currently edited Track.
     * 
     * @param currentTrack
     *            The new currently edited Track.
     * @return The parameter currentTrack is simple returned for further use.
     */
    public DataTrack setTrack(DataTrack currentTrack) {
        this.track = currentTrack;
        return currentTrack;
    }

    /**
     * Unloads all tracks from memory without saving them!
     */
    public void unloadAllTracks() {
        setTrack(null);
        retrieveTrackNames();
    }

}
