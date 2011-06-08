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

import org.mapsforge.android.maps.ArrayItemizedOverlay;
import org.mapsforge.android.maps.OverlayItem;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import de.fu.tracebook.R;

public class BugOverlay extends ArrayItemizedOverlay {

    private Context context;

    public BugOverlay(Context context) {
        super(context.getResources().getDrawable(R.drawable.card_marker_bug),
                context);
        this.context = context;
    }

    @Override
    protected boolean onTap(int index) {
        final OverlayItem item = createItem(index);

        AlertDialog.Builder builder = new AlertDialog.Builder(this.context);

        builder.setTitle("id: ");
        if (item.getSnippet() != null) {
            builder.setMessage(item.getSnippet());
        }
        builder.setPositiveButton(
                context.getResources().getString(R.string.alert_global_ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        builder.setNegativeButton(
                context.getResources().getString(R.string.alert_global_ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO start edit bug window
                    }
                });
        builder.show();
        return true;
    }
}
