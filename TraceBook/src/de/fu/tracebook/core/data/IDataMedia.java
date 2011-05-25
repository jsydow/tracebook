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

public interface IDataMedia {

    /**
     * Media type constants. Type audio.
     */
    public static final int TYPE_AUDIO = 2;
    /**
     * Media type constants. Type picture.
     */
    public static final int TYPE_PICTURE = 1;
    /**
     * Media type constants. Type text.
     */
    public static final int TYPE_TEXT = 0;
    /**
     * Media type constants. Type video.
     */
    public static final int TYPE_VIDEO = 3;

    /**
     * Deletes a medium on the devices memory. Note: Make sure that there is no
     * reference to this medium anymore.
     */
    void delete();

    /**
     * Getter-method.
     * 
     * @return The unique id of the medium.
     */
    int getId();

    /**
     * Getter-method.
     * 
     * @return The name of the medium as it is displayed. (Should generally be
     *         not null, except some idiot misused methods)
     */
    String getName();

    /**
     * Getter-method. Returns path to the directory the medium is in.
     * 
     * @return The path to the medium on the devices medium. (Should generally
     *         be not null, except some idiot misused methods)
     */
    String getPath();

    /**
     * Getter-method.
     * 
     * @return The type of the medium.
     */
    int getType();

    /**
     * Setter-method. Changing the name may have no impact on serialization. On
     * next deserialization the old name may appear again.
     * 
     * @param newname
     *            The new name for the medium.
     */
    void setName(String newname);

    /**
     * Setter-method. The path should normally not be changed but one never
     * knows. Method does nothing if parameter is null.
     * 
     * @param path
     *            The new path of the medium.
     */
    void setPath(String path);

    /**
     * Setter-method.
     * 
     * @param type
     *            The new type of this medium.
     */
    void setType(int type);

}
