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

import android.graphics.Point;

/**
 * Utility class for calculating the distance from a point to a line.
 * <p>
 * Taken from Graphic Gems II Chapter I.3 - Distance from a Point to a line.
 */
public class PointLineDistance {

    /**
     * Calculates the square distance of the point p to the line from a to b.
     * 
     * @param p
     *            The point p.
     * @param a
     *            The point a which is start of the line.
     * @param b
     *            The point b which is end of the line.
     * @return The square distance of p to the line a to b.
     */
    public static double sqDistancePointLine(Point p, Point a, Point b) {
        double byMay = b.y - a.y;
        double bxMax = b.x - a.x;
        double pxMax = p.x - a.x;
        double pyMay = p.y - a.y;
        double bxMpx = b.x - p.x;
        double byMpy = b.y - p.y;
        // dot product
        double t = pxMax * bxMax + pyMay * byMay;

        if (t < 0) {
            // p beyond a
            return pxMax * pxMax + pyMay * pyMay;
        }

        t = bxMpx * bxMax + byMpy * byMay;

        if (t < 0) {
            // p beyond b
            return bxMpx * bxMpx + byMpy * byMpy;
        }
        // p between a and b
        double a2 = pyMay * bxMax - pxMax * byMay;
        return a2 * a2 / (bxMax * bxMax + byMay * byMay);
    }

}