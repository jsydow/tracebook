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

package de.fu.tracebook.core.overlays;

import org.mapsforge.android.maps.ArrayItemizedOverlay;
import org.mapsforge.android.maps.MapActivity;
import org.mapsforge.android.maps.OverlayItem;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import de.fu.tracebook.R;
import de.fu.tracebook.core.data.IDataNode;
import de.fu.tracebook.core.data.IDataPointsList;
import de.fu.tracebook.core.data.StorageFactory;
import de.fu.tracebook.gui.activity.AddPointActivity;
import de.fu.tracebook.util.GpsMessage;
import de.fu.tracebook.util.Helper;
import de.fu.tracebook.util.LogIt;

/**
 * This class extends the {@link ArrayItemizedOverlay} in order to overwrite its
 * {@link #onTap(int)} method.
 */
public class DataNodeArrayItemizedOverlay extends ArrayItemizedOverlay {

    private class DefaultListener implements DialogInterface.OnClickListener {
        private final CharSequence[] items = {
                context.getResources().getString(
                        R.string.cm_DataNodeArrayItemizedOverlay_tag),
                context.getResources().getString(
                        R.string.cm_DataNodeArrayItemizedOverlay_move),
                context.getResources().getString(
                        R.string.cm_DataNodeArrayItemizedOverlay_delete) };

        private IDataNode node = null;

        private GpsMessage sender;

        public DefaultListener() {
            sender = new GpsMessage(context);
        }

        public CharSequence[] getItems() {
            return items;
        }

        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
            case 0: // Tag this
                final Intent intent = new Intent(context,
                        AddPointActivity.class);
                intent.putExtra("NodeId", node.getId());
                context.startActivity(intent);
                break;
            case 1: // move this
                sender.sendMovePoint(node.getId());
                break;
            case 2: // delete this
                IDataPointsList way = null;
                if (node != null) {
                    way = node.getDataPointsList();
                    if (Helper.currentTrack() != null
                            && Helper.currentTrack().deleteNode(node.getId())) {
                        // removeItem(StorageFactory.getStorage()
                        // .getOverlayManager().getOverlayItem(node));
                        sender.sendDiscardIntent();
                        if (way != null) { // we have to redraw the way
                            sender.sendWayUpdate(way.getId(), -1);
                        }
                        requestRedraw();
                    } else {
                        LogIt.popup(context,
                                "Can not delete Node id=" + node.getId());
                    }
                }
                break;
            default:
                break;
            }
        }

        public void setNode(IDataNode node) {
            this.node = node;
        }
    }

    private DefaultListener contextMenueListener;

    /**
     * Reference to the {@link MapActivity}.
     */
    Activity context;

    /**
     * Sets context and default marker.
     * 
     * @param context
     *            reference to the {@link MapActivity}
     */
    public DataNodeArrayItemizedOverlay(Activity context) {
        super(null, context);
        this.context = context;

        contextMenueListener = new DefaultListener();
    }

    @Override
    protected boolean onTap(int index) {
        final OverlayItem item = createItem(index);
        final IDataNode node = StorageFactory.getStorage().getOverlayManager()
                .getNode(item);

        AlertDialog.Builder builder = new AlertDialog.Builder(this.context);

        if (node != null) {
            contextMenueListener.setNode(node);
            builder.setItems(contextMenueListener.getItems(),
                    contextMenueListener);
            builder.show();
        }

        return true;
    }
}
