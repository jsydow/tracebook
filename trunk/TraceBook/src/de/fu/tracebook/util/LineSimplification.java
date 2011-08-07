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

package de.fu.tracebook.util;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Point;

/**
 * Simplifies a line. After simplification a line has less or equal number of
 * way points. The coordinates of the way points do not change. Just some way
 * points that do not provide much information about the course of the way are
 * removed from the way.
 */
public final class LineSimplification {

    /**
     * Simplifies a line by removing way points that change the way at most by e
     * pixels. This reduces the number of way points of the way. The unused way
     * points are set to null. The parameter "in" is changed by this!
     * 
     * @param in
     *            The way to simplify.
     * @param e
     *            The distance how far a point has to change the way in order to
     *            be kept.
     */
    public static void simplify(List<Point> in, double e) {
        simplify(in, 0, in.size() - 1, e);
    }

    /**
     * Simplifies a line by removing way points that change the way at most by e
     * pixels. This reduces the number of way points of the way.
     * 
     * @param in
     *            The way to simplify.
     * @param e
     *            The distance how far a point has to change the way in order to
     *            be kept.
     * @return The simplified way.
     */
    public static List<Point> simplifyLine(List<Point> in, double e) {
        ArrayList<Point> ret = new ArrayList<Point>(in);

        int size = ret.size();
        // simplify way
        simplify(ret, 0, size - 1, e);

        // move null-elements to the end of the array.
        int i = 0;
        int j = 0;
        while (true) {
            if (ret.get(i) == null) {
                if (j < i) {
                    j = i;
                }
                while (j < size && ret.get(j) == null) {
                    ++j;
                }
                if (j >= size) {
                    break;
                }
                ret.set(i, ret.get(j));
                ret.set(j, null);
            }

            ++i;
            if (i >= size) {
                break;
            }
        }

        // remove null-elements.
        return ret.subList(0, i);
    }

    /**
     * The Douglas-Peucker-algorithm. It simplifies a line by removing
     * unnecessary nodes. The complexity is O(n * log n) for average cases and
     * O(n*n) in the worst case since it works similar to quicksort.
     * 
     * @param in
     *            The way.
     * @param i
     *            The index of the starting way point of the line used to
     *            simplify.
     * @param j
     *            The index of the ending way point of the line used to
     *            simplify.
     * @param e
     *            The distance how far a point has to change the way in order to
     *            be kept.
     */
    private static void simplify(List<Point> in, int i, int j, double e) {
        int farthest = i;
        double dist = 0;

        // no point between i and j that could be eliminated?
        // also anchor for recurrence
        if (j - i < 2) {
            return;
        }

        // find point with largest distance to line i to j
        for (int x = i; x <= j; ++x) {
            double d = PointLineDistance.sqDistancePointLine(in.get(x),
                    in.get(i), in.get(j));
            if (d > dist) {
                dist = d;
                farthest = x;
            }
        }

        // actual algorithm:
        if (dist > e * e) {
            // Split line because distance was to large
            simplify(in, i, farthest, e);
            simplify(in, farthest, j, e);
        } else {
            // delete all point between i and j
            for (int x = i + 1; x < j; ++x) {
                in.set(x, null);
            }
        }
    }

    private LineSimplification() {
        // make constructor private
    }

}
