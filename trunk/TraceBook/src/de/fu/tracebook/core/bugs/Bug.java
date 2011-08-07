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

package de.fu.tracebook.core.bugs;

import org.mapsforge.android.maps.GeoPoint;

/**
 * Represents a bug in the open street map. Bugs are errors in the map.
 * 
 */
public class Bug {

    /**
     * The description of this Bug.
     */
    private String desc;

    /**
     * The location of this bug.
     */
    private GeoPoint point;

    /**
     * Initialising constructor.
     * 
     * @param desc
     *            The description of the bug.
     * @param point
     *            The position of the bug.
     */
    public Bug(String desc, GeoPoint point) {
        this.desc = desc;
        this.point = point;
    }

    /**
     * Simple getter-method for the description of this bug.
     * 
     * @return The description of this bug.
     */
    public String getDescription() {
        return desc;
    }

    /**
     * Simple getter-method for the position of this bug.
     * 
     * @return The position of this bug.
     */
    public GeoPoint getPosition() {
        return point;
    }

    /**
     * Set the description of this bug.
     * 
     * @param description
     *            The description of this bug.
     */
    public void setDescription(String description) {
        desc = description;
    }

}
