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

package de.fu.tracebook.core.data;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.mapsforge.android.maps.GeoPoint;
import org.mapsforge.android.maps.OverlayItem;
import org.mapsforge.android.maps.OverlayWay;

import android.content.Context;
import de.fu.tracebook.util.BiMap;
import de.fu.tracebook.util.Helper;

public class OverlayManager {

    private Queue<OverlayItem> invalidItems = new ConcurrentLinkedQueue<OverlayItem>();
    private BiMap<OverlayItem, IDataNode> overlayItemToNode = new BiMap<OverlayItem, IDataNode>();
    private BiMap<OverlayWay, IDataPointsList> routeToPointsList = new BiMap<OverlayWay, IDataPointsList>();

    public void addInvalidOverlayItem(OverlayItem item) {
        invalidItems.add(item);
    }

    public Collection<OverlayItem> getAndClearInvalidOverlayItems() {
        Collection<OverlayItem> ret = invalidItems;
        invalidItems.clear();
        return ret;
    }

    public IDataNode getNodeByOverlayItem(OverlayItem item) {
        return overlayItemToNode.get(item);
    }

    public OverlayItem getOverlayItem(IDataMapObject node) {
        return overlayItemToNode.inverse().get(node);
    }

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

    public OverlayWay getOverlayRoute(IDataPointsList way) {
        OverlayWay res = routeToPointsList.inverse().get(way);
        if (res == null) {
            res = new OverlayWay();
        }

        return res;
    }

    public void invalidateOverlayOfNode(IDataNode node) {
        OverlayItem item = getOverlayItem(node);
        if (item != null) {
            invalidItems.add(item);
        }
    }

    public void setLocation(IDataNode node, GeoPoint gp) {
        OverlayItem item = getOverlayItem(node);
        if (item != null)
            item.setPoint(gp);
    }

    public void setOverlayItem(OverlayItem item, IDataNode node) {
        overlayItemToNode.put(item, node);
    }

    public void setWayOverlay(OverlayWay overlay, IDataPointsList way) {
        routeToPointsList.put(overlay, way);
    }

    public void updateOverlayRoute(IDataPointsList way, GeoPoint additional) {
        getOverlayRoute(way).setWayData(way.toGeoPointArray(additional));
    }
}
