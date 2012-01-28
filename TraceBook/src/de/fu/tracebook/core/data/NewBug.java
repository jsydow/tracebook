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

import de.fu.tracebook.core.data.implementation.NewDBBug;

/**
 * Implementation of {@link IDataBug}.
 * 
 */
public class NewBug implements IDataBug {
    /**
     * The description of this Bug.
     */
    private String desc;

    private long id;

    /**
     * The location of this bug.
     */
    private GeoPoint point;

    /**
     * Initialising constructor.
     * 
     * @param bug
     *            The bug from a database.
     */
    public NewBug(NewDBBug bug) {
        this.desc = bug.description;
        this.id = bug.id;
        this.point = bug.point;
    }

    /**
     * Initialising constructor.
     * 
     * @param desc
     *            The description of the bug.
     * @param point
     *            The position of the bug.
     */
    public NewBug(String desc, GeoPoint point) {
        this.desc = desc;
        this.point = point;
    }

    public String getDescription() {
        return desc;
    }

    public GeoPoint getPosition() {
        return point;
    }

    public void removeFromDb() {
        NewDBBug dbbug = NewDBBug.getById(id);
        dbbug.delete();
    }

    public void setDescription(String description) {
        desc = description;
        NewDBBug bug = NewDBBug.getById(id);
        if (bug != null) {
            bug.description = description;
            bug.save();
        }
    }
}
