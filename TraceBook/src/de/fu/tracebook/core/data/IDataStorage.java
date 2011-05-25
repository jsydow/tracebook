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

/**
 * DataStorage is the class from where the user can access all tracking data.
 */
public interface IDataStorage {

    void deleteTrack(String trackname);

    /**
     * Loads the complete Track (with everything it contains) into working
     * memory. If such a Track does not exist nothing is done.
     * 
     * @param name
     *            The name of the Track.
     * @return The deserialized Track or null if track does not exist.
     */
    DataTrack deserializeTrack(String name);

    boolean doesTrackExist(String trackname);

    /**
     * Returns a list of the names of all tracks that are currently stored in
     * this DataStorage object. The names can be used as argument to getTrack().
     * 
     * @return List of the names of all tracks. If there are no tracks stored
     *         then the list will be empty.
     */
    List<String> getAllTracks();

    /**
     * Create a new unique id to use for a new map object.
     * 
     * @return A new unique id > 0.
     */
    int getID();

    /**
     * Getter-method.
     * 
     * @return The currently edited Track is returned.(may be null)
     */
    IDataTrack getTrack();

    /**
     * @param trackName
     *            The name of the track.
     * @return The DataTrackInfo for that track or null.
     */
    DataTrackInfo getTrackInfo(String trackName);

    /**
     * Create a new Track in working memory. Don't forget to serialize it!
     * 
     * @return The newly created Track.
     */
    IDataTrack newTrack();

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
