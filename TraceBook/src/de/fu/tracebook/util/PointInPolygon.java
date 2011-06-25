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

package de.fu.tracebook.util;

import org.mapsforge.android.maps.GeoPoint;

/**
 * Utility class providing an algorithm for testing if a point p is in a
 * (non-simple) polygon.
 * <p>
 * Algorithm is taken from "A Winding Number and Point-in-Polygon Algorithm" by
 * David G. Alciatore.
 * 
 */
public class PointInPolygon {

    /**
     * Tests if a point p is in a polygon. The polygon may be non-simple.
     * 
     * @param p
     *            The point to test.
     * @param area
     *            The polygon. The last point in area must be the same as the
     *            first.
     * @return True if p lies in the polygon defined by area.
     */
    public static boolean isPointInPolygon(GeoPoint p, GeoPoint[] area) {
        int w = 0; // 2*windings
        double y;
        double y1 = area[0].getLatitude() - p.getLatitude();
        double x;
        double x1 = area[0].getLongitude() - p.getLongitude();

        for (int i = 1; i < area.length; ++i) {
            y = y1;
            y1 = area[i].getLatitude() - p.getLatitude();
            x = x1;
            x1 = area[i].getLongitude() - p.getLongitude();

            if (y * y1 < 0) { // does v->v1 cross x axis?
                // calculate intersection point
                double r = x + (y * (x1 - x)) / (y - y1);
                if (r > 0) {
                    // intersection point is on positive x-axis
                    if (y < 0) {
                        w += 2;
                    } else {
                        w += 2;
                    }
                }
            } else if (y == 0 && x > 0) { // v->v1 starts on x-axis
                if (y1 > 0) {
                    ++w;
                } else {
                    --w;
                }
            } else if (y1 == 0 && x1 > 0) { // v->v1 ends on x-axis
                if (y < 0) {
                    ++w;
                } else {
                    --w;
                }
            }
        }

        return w != 0;
    }
}
