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

import org.mapsforge.android.maps.ArrayItemizedOverlay;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.EditText;
import de.fu.tracebook.R;
import de.fu.tracebook.core.bugs.Bug;
import de.fu.tracebook.core.bugs.BugManager;
import de.fu.tracebook.core.overlays.BugOverlayItem.BugType;

public class BugOverlay extends ArrayItemizedOverlay {

    private Context context;

    public BugOverlay(Context context) {
        super(context.getResources().getDrawable(R.drawable.card_marker_bug),
                context);
        this.context = context;
    }

    public void showEditDialog(final Bug b) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this.context);
        final EditText edit = new EditText(context);
        edit.setText(b.getDescription());

        builder.setTitle(R.string.alert_bugoverlay_edit);
        builder.setView(edit);

        builder.setPositiveButton(
                context.getResources().getString(R.string.alert_global_ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        b.setDescription(edit.getText().toString());
                    }
                });
        builder.setNegativeButton(
                context.getResources().getString(R.string.alert_global_cancel),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        builder.show();
    }

    @Override
    protected boolean onTap(int index) {
        final BugOverlayItem item = (BugOverlayItem) createItem(index);

        AlertDialog.Builder builder = new AlertDialog.Builder(this.context);

        if (item.getTitle() != null) {
            builder.setTitle(item.getTitle() + ": ");
        } else {
            builder.setTitle("Bug: ");

        }
        if (item.getBug().getDescription() != null) {
            builder.setMessage(item.getBug().getDescription());
        }
        builder.setPositiveButton(
                context.getResources().getString(R.string.alert_global_ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        if (item.getType() == BugType.USERBUG) {
            builder.setNegativeButton(
                    context.getResources().getString(
                            R.string.alert_bugoverlay_edit),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            showEditDialog(item.getBug());
                        }
                    });
        }
        builder.setNeutralButton(
                context.getResources().getString(
                        R.string.alert_bugoverlay_delete),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        BugOverlay.this.removeItem(item);
                        BugOverlay.this.requestRedraw();
                        BugManager.getInstance().remove(item.getBug());
                    }
                });

        builder.show();
        return true;
    };
}
