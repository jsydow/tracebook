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

import java.util.List;

import de.fu.tracebook.core.overlays.OverlayManager;

/**
 * DataStorage is the class from where the user can access all tracking data.
 */
public interface IDataStorage {

    /**
     * Deletes a track completely.
     * 
     * @param trackname
     *            The name of the track to delete.
     */
    void deleteTrack(String trackname);

    /**
     * Loads the complete Track (with everything it contains) into working
     * memory. If such a Track does not exist nothing is done.
     * 
     * @param name
     *            The name of the Track.
     * @return The deserialized Track or null if track does not exist.
     */
    IDataTrack deserializeTrack(String name);

    /**
     * Tests if this track exists.
     * 
     * @param trackname
     *            The name of the track.
     * @return True if track exists.
     */
    boolean doesTrackExist(String trackname);

    /**
     * Will create the TraceBook directory if it does not exist.
     */
    void ensureThatTraceBookDirExists();

    /**
     * Returns a list of the names of all tracks that are currently stored in
     * this DataStorage object. The names can be used as argument to getTrack().
     * 
     * @return List of the names of all tracks. If there are no tracks stored
     *         then the list will be empty.
     */
    List<String> getAllTrackNames();

    /**
     * Returns a list of all tracks stored in the database.
     * 
     * @return The list of tracks.
     */
    List<IDataTrack> getAllTracks();

    /**
     * Create a new unique id to use for a new map object.
     * 
     * @return A new unique id > 0.
     */
    int getID();

    /**
     * Returns the OverlayManager. Successive calls will returns the same
     * object.
     * 
     * @return An OverlayManager.
     */
    OverlayManager getOverlayManager();

    /**
     * Getter-method.
     * 
     * @return The currently edited Track is returned.(may be null)
     */
    IDataTrack getTrack();

    /**
     * Create a new Track in working memory. Don't forget to serialize it!
     * 
     * @return The newly created Track.
     */
    IDataTrack newTrack();

    /**
     * Renames a track.
     * 
     * @param oldname
     *            The old name of the track.
     * @param newname
     *            The new name of the track.
     * @return Returns 0 if renaming was successful, -1 if track could not be
     *         found, -2 if there is a track with the new track name and -3 if
     *         the renaming fails otherwise.
     */
    int renameTrack(String oldname, String newname);

    /**
     * Will serialize all tracks that are currently stored in this DataStorage.
     */
    void serialize();

    /**
     * Setter-method for the currently edited Track.
     * 
     * @param currentTrack
     *            The new currently edited Track.
     * @return The parameter currentTrack is simple returned for further use.
     */
    IDataTrack setTrack(IDataTrack currentTrack);

    /**
     * Unloads all tracks from memory without saving them!
     */
    void unloadAllTracks();

}
