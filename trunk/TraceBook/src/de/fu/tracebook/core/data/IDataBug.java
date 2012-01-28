/*======================================================================
 *
 * This file is part of TraceBook.
 *
 * TraceBook is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published 
 * by the Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version.
 *
 * TraceBook is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public 
 * License along with TraceBook. If not, see 
 * <http://www.gnu.org/licenses/>.
 *
 =====================================================================*/

package de.fu.tracebook.core.data;

import org.mapsforge.android.maps.GeoPoint;

/**
 * A map bug. A map bug is a place where the map data contains an error. A bug
 * has description and a position.
 */
public interface IDataBug {
    /**
     * Simple getter-method for the description of this bug.
     * 
     * @return The description of this bug.
     */
    public String getDescription();

    /**
     * Simple getter-method for the position of this bug.
     * 
     * @return The position of this bug.
     */
    public GeoPoint getPosition();

    /**
     * Removes this but from the database if this is a user created bug.
     * Otherwise this method does nothing.
     */
    public void removeFromDb();

    /**
     * Set the description of this bug.
     * 
     * @param description
     *            The description of this bug.
     */
    public void setDescription(String description);
}
