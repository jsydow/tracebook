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
import java.util.List;

import android.os.Environment;
import de.fu.tracebook.core.data.implementation.NewDBTrack;
import de.fu.tracebook.core.overlays.OverlayManager;
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
     * Creates new folder in .../TraceBook for this Track. Such a directory must
     * exist when track is serialized.
     */
    static void createNewTrackFolder(String name) {
        File dir = new File(NewStorage.getTraceBookDirPath() + File.separator
                + name);
        if (!dir.isDirectory()) {
            if (!dir.mkdir()) {
                LogIt.e("Could not create new track folder " + name);
            }
        }
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
        NewStorage.createNewTrackFolder(dir);
        return NewStorage.getTraceBookDirPath() + File.separator + dir;
    }

    /**
     * Last given ID for a MapObject.
     */
    private int lastID = 0;

    /**
     * The Overlaymanager
     */
    private OverlayManager overlays = new OverlayManager();

    /**
     * Currently active Track.
     */
    private IDataTrack track;

    NewStorage() {
        // do nothing, just reduce visibility.
    }

    public void deleteTrack(String trackname) {
        NewDBTrack dbtrack = NewDBTrack.getById(trackname);
        if (dbtrack == null) {
            return;
        }

        NewTrack existingTrack = new NewTrack(dbtrack);
        existingTrack.delete();
        deleteDirectory(new File(getTrackDirPath(trackname)));

    }

    public IDataTrack deserializeTrack(String name) {
        NewDBTrack loadedTrack = NewDBTrack.getById(name);
        if (loadedTrack == null) {
            return null;
        }
        return new NewTrack(loadedTrack);
    }

    public boolean doesTrackExist(String trackname) {
        if (NewDBTrack.getById(trackname) == null) {
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

    public List<String> getAllTrackNames() {
        return NewDBTrack.getAllNames();
    }

    public List<IDataTrack> getAllTracks() {
        List<IDataTrack> ret = new ArrayList<IDataTrack>();
        for (NewDBTrack t : NewDBTrack.getAllTracks()) {
            ret.add(new NewTrack(t));
        }
        return ret;
    }

    public int getID() {
        return --lastID;
    }

    public OverlayManager getOverlayManager() {
        return overlays;
    }

    public IDataTrack getTrack() {
        return track;
    }

    public IDataTrack newTrack() {
        IDataTrack newtrack = new NewTrack();
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
        new DataSerializer().serialize((NewTrack) track);
    }

    public IDataTrack setTrack(IDataTrack currentTrack) {
        track = currentTrack;
        return track;
    }

    public void unloadAllTracks() {
        this.track = null;
    }

}
