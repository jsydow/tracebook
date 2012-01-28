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

import java.util.Collection;
import java.util.List;

import org.mapsforge.android.maps.GeoPoint;
import org.mapsforge.android.maps.OverlayItem;

import de.fu.tracebook.gui.activity.MapsForgeActivity;

/**
 * The bug manager manages user created bugs and openstreetbugs.
 */
public interface IBugManager {
    /**
     * Add a user created bug.
     * 
     * @param bug
     *            The bug to add.
     */
    public void addBug(IDataBug bug);

    /**
     * Will load Bugs from OpenStreetBugs. At least 100 Bugs are loaded. The
     * area is the current position +0.25 degrees in all directions.
     * 
     * @param activity
     *            The MapsForgeActivity which uses this BugManager.
     * @param pos
     *            The current position.
     */
    public void downloadBugs(final MapsForgeActivity activity,
            final GeoPoint pos);

    /**
     * Returns all Bugs as OverlayItems to be displayed on a map.
     * 
     * @return All Bugs as OverlayItems.
     */
    public Collection<OverlayItem> getBugOverlays();

    /**
     * Get all bugs.
     * 
     * @return A list of all Bugs.
     */
    public List<IDataBug> getBugs();

    /**
     * Remove a bug.
     * 
     * @param bug
     *            The bug to remove.
     */
    public void remove(IDataBug bug);

    /**
     * Returns the number of user recorded bugs.
     * 
     * @return The size of the list of user recorded bugs.
     */
    public int size();

}
