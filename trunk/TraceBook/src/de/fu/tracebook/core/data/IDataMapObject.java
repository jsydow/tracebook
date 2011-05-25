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

import java.util.Map;

public interface IDataMapObject extends IDataMediaHolder {

    /**
     * Getter-method.
     * 
     * @return The id of the object.
     */
    int getId();

    /**
     * Getter-method for a all tags stored as a Map of String. Tags that are no
     * tags in OSM are: name, lat, lon, timestamp. Mind that changes in the
     * returned Map change this object in the same way.
     * 
     * @return Map of all tags. (Not null)
     */
    Map<String, String> getTags();

    /**
     * Checks whether additional information like Tags or Media data are
     * available for this {@link DataMapObject}.
     * 
     * @return True if tags or media exist.
     */
    boolean hasAdditionalInfo();

}
