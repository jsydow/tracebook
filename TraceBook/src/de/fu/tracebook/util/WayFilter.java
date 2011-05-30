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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.mapsforge.android.maps.GeoPoint;

import de.fu.tracebook.core.data.IDataNode;
import de.fu.tracebook.core.data.StorageFactory;

/**
 * This class offers methods used to filter unnecessary waypoints. It is
 * intended to first smoothen the way to eliminate outliners, and then to filter
 * the points of the way to remove redundant data.
 */
public final class WayFilter {
    /**
     * Removes all insignificant points from the way, a threshold is calculated
     * automatically by the average derivation of the points.
     * 
     * @param nodes
     *            List of {@link IDataNode}s representing the way or area
     * @param weight
     *            Weight with which the average derivation of points is
     *            multiplied to form the threshold
     */
    public static void filterPoints(List<IDataNode> nodes, double weight) {
        boolean calibrate = true;
        double threshold = 0;

        if (nodes.size() < 3)
            return;

        // we first iterate once to get the threshold, in the second run we
        // actually remove the points
        while (calibrate) {
            if (threshold != 0)
                calibrate = false;

            IDataNode firstNode = null;
            IDataNode pending = null;
            Iterator<IDataNode> iter = nodes.iterator();

            while (iter.hasNext()) {
                IDataNode n = iter.next();
                if (n == null || !n.isValid()) {
                    iter.remove();
                    continue;
                }

                if (firstNode == null) {
                    firstNode = n;
                    continue;
                }

                if (pending != null) {
                    if (calibrate) {
                        threshold += calculateArea(firstNode.getCoordinates(),
                                pending.getCoordinates(), n.getCoordinates());
                    } else if (calculateArea(firstNode.getCoordinates(),
                            pending.getCoordinates(), n.getCoordinates()) < threshold
                            * weight
                            && !n.hasAdditionalInfo() && iter.hasNext()) {
                        StorageFactory.getStorage().getOverlayManager()
                                .invalidateOverlayOfNode(n);
                        iter.remove();
                    }
                    firstNode = pending;
                }

                pending = n;
            }
            threshold /= nodes.size();
            LogIt.log("filterPoints", "Average: " + threshold, 1);
        }
    }

    /**
     * Smoothen the {@link IDataNode}s by calculating the mean of 3 consecutive
     * points.
     * 
     * @param nodes
     *            The list of DataNodes representing the way
     * @param weight
     *            the factor by which the original point is higher weighted then
     *            it's bufferSize-1 successors
     * @param bufferSize
     *            the amount of points to use for the calculation
     */
    public static void smoothenPoints(List<IDataNode> nodes, int weight,
            int bufferSize) {
        smoothenPointsMiddle(nodes, weight, bufferSize);
    }

    /**
     * Smoothen the {@link IDataNode}s by calculating the mean of 3 consecutive
     * points.
     * 
     * @param nodes
     *            The list of DataNodes representing the way
     * @param weight
     *            the factor by which the original point is higher weighted then
     *            it's bufferSize-1 successors
     * @param bufferSize
     *            the amount of points to use for the calculation
     */
    public static void smoothenPointsFirst(List<IDataNode> nodes, int weight,
            int bufferSize) {
        if (nodes == null)
            return;

        Queue<IDataNode> ringbuffer = new LinkedList<IDataNode>();

        for (IDataNode n : nodes) {
            if (!n.isValid())
                continue;

            ringbuffer.add(n);

            if (ringbuffer.size() < bufferSize)
                continue;

            double latsum = 0;
            double lonsum = 0;
            boolean first = true;
            for (IDataNode nr : ringbuffer)
                if (first) {
                    first = false;
                    latsum += nr.getCoordinates().getLatitude() * weight;
                    lonsum += nr.getCoordinates().getLongitude() * weight;
                } else {
                    latsum += nr.getCoordinates().getLatitude();
                    lonsum += nr.getCoordinates().getLongitude();
                }

            // the ring buffer is an element smaller now
            ringbuffer.poll().setLocation(
                    new GeoPoint(
                            latsum / ((double) ringbuffer.size() + weight),
                            lonsum / ((double) ringbuffer.size() + weight)));

        }
    }

    /**
     * Smoothen the {@link IDataNode}s by calculating the mean of multiple
     * consecutive points.
     * 
     * @param nodes
     *            The list of DataNodes representing the way
     * @param weight
     *            the factor by which the original point is higher weighted then
     *            it's bufferSize-1 successors
     * @param bufferSize
     *            the amount of points to use for the calculation
     */
    public static void smoothenPointsMiddle(List<IDataNode> nodes, int weight,
            int bufferSize) {
        if (nodes == null)
            return;

        LinkedList<IDataNode> ringbuffer = new LinkedList<IDataNode>();

        for (IDataNode n : nodes) {
            if (!n.isValid())
                continue;

            ringbuffer.add(n);

            if (ringbuffer.size() < bufferSize)
                continue;

            double latsum = 0;
            double lonsum = 0;
            for (int i = 0; i < ringbuffer.size(); i++)
                if (i == ringbuffer.size() / 2) {
                    latsum += ringbuffer.get(i).getCoordinates().getLatitude()
                            * weight;
                    lonsum += ringbuffer.get(i).getCoordinates().getLongitude()
                            * weight;
                } else {
                    latsum += ringbuffer.get(i).getCoordinates().getLatitude();
                    lonsum += ringbuffer.get(i).getCoordinates().getLongitude();
                }

            // the ringbuffer is an element smaller now
            ringbuffer
                    .get(ringbuffer.size() / 2)
                    .setLocation(
                            new GeoPoint(
                                    latsum
                                            / ((double) (ringbuffer.size() - 1) + weight),
                                    lonsum
                                            / ((double) (ringbuffer.size() - 1) + weight)));
            ringbuffer.poll();

        }
    }

    /**
     * Calculates the area spanned by the parallelogram defined by the points a,
     * b and c.
     * 
     * @param a
     *            first point
     * @param b
     *            second point
     * @param c
     *            third point
     * @return the area of the parallelogram (twice the area of the triangle)
     *         defined by those 3 points.
     */
    static double calculateArea(GeoPoint a, GeoPoint b, GeoPoint c) {
        return Math.abs((a.getLongitude() - c.getLongitude())
                * (b.getLatitude() - a.getLatitude())
                - (a.getLongitude() - b.getLongitude())
                * (c.getLatitude() - a.getLatitude()));
    }

    private WayFilter() {
        // Empty constructor. Does nothing.
    }
}
