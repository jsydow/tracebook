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

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.mapsforge.android.maps.GeoPoint;
import org.mapsforge.android.maps.OverlayItem;
import org.mapsforge.android.maps.OverlayWay;

import android.content.Context;
import de.fu.tracebook.core.data.IDataMapObject;
import de.fu.tracebook.core.data.IDataNode;
import de.fu.tracebook.core.data.IDataPointsList;
import de.fu.tracebook.util.BiMap;
import de.fu.tracebook.util.Helper;
import de.fu.tracebook.util.LogIt;

/**
 * The OverlayManager manages the mapping from Items to Overlays and back.
 */
public class OverlayManager {
    // TODO delete and use special overlays

    private Queue<OverlayItem> invalidItems = new ConcurrentLinkedQueue<OverlayItem>();
    private BiMap<OverlayItem, IDataNode> overlayItemToNode = new BiMap<OverlayItem, IDataNode>();
    private BiMap<OverlayWay, IDataPointsList> routeToPointsList = new BiMap<OverlayWay, IDataPointsList>();

    /**
     * Returns a list of all OverlayItems marked as invalid and clears the list.
     * 
     * @return All OverlayItems marked as invalid.
     */
    public Collection<OverlayItem> getAndClearInvalidOverlayItems() {
        Collection<OverlayItem> ret = invalidItems;
        invalidItems.clear();
        return ret;
    }

    /**
     * Mapping from an OverlayItem to a Node.
     * 
     * @param item
     *            The OverlayItem of a node.
     * @return The Node which belongs to the given OverlayItem
     */
    public IDataNode getNode(OverlayItem item) {
        return overlayItemToNode.get(item);
    }

    /**
     * Mapping from a Node to an OverlayItem.
     * 
     * @param node
     *            The Node.
     * @return The OverlayItem belonging to that node.
     */
    public OverlayItem getOverlayItem(IDataMapObject node) {
        return overlayItemToNode.inverse().get(node);
    }

    /**
     * Get the overlays for all nodes.
     * 
     * @param ctx
     *            A context. Used to get a drawable.
     * @return The overlays for all nodes.
     */
    public Collection<? extends OverlayItem> getOverlayItems(Context ctx) {
        List<OverlayItem> items = new LinkedList<OverlayItem>();
        for (IDataNode n : Helper.currentTrack().getNodes()) {
            OverlayItem item = getOverlayItem(n);
            if (item == null) {
                OverlayItem newItem = Helper.getOverlayItem(ctx);
                setOverlayItem(newItem, n);
                newItem.setPoint(n.getCoordinates());
                item = newItem;
            }
            items.add(item);
        }
        return items;
    }

    /**
     * Mapping from way to way overlay.
     * 
     * @param way
     *            The way.
     * @return The overlay for the given way.
     */
    public OverlayWay getOverlayRoute(IDataPointsList way) {
        OverlayWay res = routeToPointsList.inverse().get(way);
        if (res == null) {
            LogIt.d("new overlayroute");
            res = new OverlayWay();
            setWayOverlay(res, way);
        }

        return res;
    }

    /**
     * Mapping from way overlay to way.
     * 
     * @param way
     *            The way overlay.
     * @return The way belonging to the way overlay.
     */
    public IDataPointsList getPointsList(OverlayWay way) {
        return routeToPointsList.get(way);
    }

    /**
     * Invalidates the overlay of a node.
     * 
     * @param node
     *            The node to invalidate.
     */
    public void invalidateOverlayOfNode(IDataNode node) {
        OverlayItem item = getOverlayItem(node);
        if (item != null) {
            invalidItems.add(item);
            overlayItemToNode.remove(item);
        }
    }

    /**
     * Remove the mapping of a way.
     * 
     * @param way
     *            The way to remove.
     */
    public void removeWay(IDataPointsList way) {
        routeToPointsList.inverse().remove(way);
    }

    /**
     * Set the location of an overlay item of a node.
     * 
     * @param node
     *            The node to update the position of its overlay item.
     * @param gp
     *            The new position.
     */
    public void setLocation(IDataNode node, GeoPoint gp) {
        OverlayItem item = getOverlayItem(node);
        if (item != null)
            item.setPoint(gp);
    }

    /**
     * Add a mapping overlay item to node.
     * 
     * @param item
     *            The overlay item.
     * @param node
     *            The node.
     */
    public void setOverlayItem(OverlayItem item, IDataNode node) {
        overlayItemToNode.put(item, node);
    }

    /**
     * Add a mapping for a way overlay to a way.
     * 
     * @param overlay
     *            The way overlay.
     * @param way
     *            The way.
     */
    public void setWayOverlay(OverlayWay overlay, IDataPointsList way) {
        routeToPointsList.put(overlay, way);
    }

    /**
     * Updates an way overlay. Refills the overlay with the way points of the
     * given way.
     * 
     * @param way
     *            The way to update.
     * @param additional
     *            Additional waypoint appended to the way. May be null.
     */
    public void updateOverlayRoute(IDataPointsList way, GeoPoint additional) {
        LogIt.e("updateroute:");
        GeoPoint[][] wps = new GeoPoint[][] { way.toGeoPointArray(additional) };
        LogIt.e(wps.length + " " + wps[0].length);
        getOverlayRoute(way).setWayData(wps);
    }
}
