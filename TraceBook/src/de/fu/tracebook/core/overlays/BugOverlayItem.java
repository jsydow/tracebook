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

package de.fu.tracebook.core.overlays;

import org.mapsforge.android.maps.OverlayItem;

import de.fu.tracebook.core.bugs.Bug;

public class BugOverlayItem extends OverlayItem {

    public static enum BugType {
        OPENSTREETBUG("OpenStreetBug"), USERBUG("Bug");

        private String description;

        BugType(String desc) {
            description = desc;
        }

        public String getDesc() {
            return description;
        }
    }

    private Bug bug;
    private BugType type;

    public BugOverlayItem(Bug bug, BugType type) {
        super(bug.getPosition(), type.getDesc(), null);
        this.bug = bug;
        this.type = type;
    }

    /**
     * @return the bug
     */
    public Bug getBug() {
        return bug;
    }

    /**
     * @return the type
     */
    public BugType getType() {
        return type;
    }

}
