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

package de.fu.tracebook.core.data.implementation;

/**
 * Interface for DAO-Objects.
 */
public interface NewDBObject {

    /**
     * Delete this entry.
     */
    void delete();

    /**
     * Insert this entry.
     */
    void insert();

    /**
     * Update the row in the database with the data stored in this object.
     */
    void save();

    /**
     * Update this object with the data of the row in the database that belong
     * to this object.
     */
    void update();
}
