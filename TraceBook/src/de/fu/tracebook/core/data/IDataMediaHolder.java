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
 * Any object that can have media attached to it.
 */
public interface IDataMediaHolder {

    /**
     * Add a new medium to this object. Does nothing if parameter is null.
     * 
     * @param medium
     *            The Media object.
     */
    void addMedia(IDataMedia medium);

    /**
     * Deletes a medium from the working memory (i.e. that object) and the
     * devices memory. Warning: Make sure no other object has a reference to
     * this medium!
     * 
     * @param id
     *            The id of the medium to be deleted.
     */
    void deleteMedia(int id);

    /**
     * Getter-method. The creation time string.
     * 
     * @return The creation time of this object as String. (Could be null)
     */
    String getDatetime();

    /**
     * Getter-method that returns a list of all media. The returned List is the
     * one stored in the MediaHolder. Changing the returned List will therefore
     * change this list.
     * 
     * @return The list of all media. (not null)
     */
    List<IDataMedia> getMedia();

    /**
     * Set the Creation time of this MediaHolder. Used to restore an old
     * MediaHolder while deserialization.
     * 
     * @param datetime
     *            The new time stamp as String.
     */
    void setDatetime(String datetime);

}
