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

/**
 * A class that simply holds information of a track. It is a class that only
 * provides information but cannot edit them. Use deserialize() to get such an
 * object.
 */
public class DataTrackInfo {

    /**
     * Deserializes a DataTrackInfo from an info.xml file of a track.
     * 
     * @param trackname
     *            Name of a track as String.
     * @return The info to this specific track or null if there is not such a
     *         track.
     */
    static DataTrackInfo deserialize(String trackname) {
        DataTrackInfo info = new DataTrackInfo();

        return info;
    }

    private String comment;

    private String name;
    private int numberOfMedia;
    private int numberOfPOIs;
    private int numberOfWays;
    private String timestamp;

    /**
     * An initializing constructor.
     * 
     * @param timestamp
     *            The time stamp of a track.
     * @param name
     *            The name of a track (Its directory name).
     * @param comment
     *            The comment of a track.
     * @param numberOfPOIs
     *            The number of points of interest a track has.
     * @param numberOfWays
     *            The number of ways a track has.
     * @param numberOfMedia
     *            The total number of media a track has.
     */
    public DataTrackInfo(String name, String timestamp, String comment,
            int numberOfPOIs, int numberOfWays, int numberOfMedia) {
        this.timestamp = timestamp;
        this.name = name;
        this.comment = comment;
        this.numberOfPOIs = numberOfPOIs;
        this.numberOfWays = numberOfWays;
        this.numberOfMedia = numberOfMedia;
    }

    DataTrackInfo() {
        timestamp = "";
        comment = "";
        name = "";
        numberOfPOIs = -1;
        numberOfWays = -1;
        numberOfMedia = -1;
    }

    /**
     * Getter-method for the comment of a track.
     * 
     * @return The comment as String.
     */
    public String getComment() {
        return comment;
    }

    /**
     * Getter-method for the name of a track.
     * 
     * @return The name as String.
     */
    public String getName() {
        return name;
    }

    /**
     * Getter-method for the number of media of a track.
     * 
     * @return The number of media.
     */
    public int getNumberOfMedia() {
        return numberOfMedia;
    }

    /**
     * Getter-method for the number of POIs of a track.
     * 
     * @return The number of POIs.
     */
    public int getNumberOfPOIs() {
        return numberOfPOIs;
    }

    /**
     * Getter-method for the number of ways of a track.
     * 
     * @return The number of ways.
     */
    public int getNumberOfWays() {
        return numberOfWays;
    }

    /**
     * Getter-method for the time stamp of a track.
     * 
     * @return The time stamp as String.
     */
    public String getTimestamp() {
        return timestamp;
    }

    /**
     * Serializes the track info. Creates a info.xml file in the directory of
     * the track.
     */
    void serialize() {
        // do nothing
    }

}
