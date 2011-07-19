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

package de.fu.tracebook.gui.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;
import de.fu.tracebook.R;
import de.fu.tracebook.core.data.IDataTrack;
import de.fu.tracebook.core.data.StorageFactory;
import de.fu.tracebook.core.logger.ServiceConnector;
import de.fu.tracebook.util.Helper;

/**
 * This activity show info about a track. It is possible to edit the comment and
 * export the track.
 */
public class TrackInfoActivity extends Activity {

    private class TextUpdater extends AsyncTask<Void, Void, TextViewData> {

        TextUpdater() {
            // do nothing
        }

        @Override
        protected TextViewData doInBackground(Void... params) {
            TextViewData tvd = new TextViewData();

            IDataTrack track = StorageFactory.getStorage().getTrack();

            tvd.name = track.getName();
            tvd.date = track.getDatetime();
            tvd.poiNumber = track.getNodes().size();
            tvd.wayNumber = track.getWays().size();
            tvd.comment = track.getComment();
            return tvd;
        }

        @Override
        protected void onPostExecute(TextViewData result) {
            TextView tvTrack = (TextView) TrackInfoActivity.this
                    .findViewById(R.id.tv_trackinfoActivity_trackname);
            tvTrack.setText(getResources().getString(
                    R.string.tv_trackInfoDialog_trackname)
                    + " " + result.name);

            TextView tvTime = (TextView) TrackInfoActivity.this
                    .findViewById(R.id.tv_trackinfoActivity_timestamp);
            tvTime.setText(getResources().getString(
                    R.string.tv_trackInfoDialog_timestamp)
                    + " " + result.date);

            TextView tvWays = (TextView) TrackInfoActivity.this
                    .findViewById(R.id.tv_trackinfoActivity_wayNumber);
            tvWays.setText(getResources().getString(
                    R.string.tv_trackInfoDialog_ways)
                    + " " + result.wayNumber);

            TextView tvPois = (TextView) TrackInfoActivity.this
                    .findViewById(R.id.tv_trackinfoActivity_poiNumber);
            tvPois.setText(getResources().getString(
                    R.string.tv_trackInfoDialog_pois)
                    + " " + result.poiNumber);

            EditText etComment = (EditText) TrackInfoActivity.this
                    .findViewById(R.id.et_trackinfoActivity_comment);
            etComment.setText(result.comment);
        }

    }

    private static class TextViewData {
        String comment;
        String date;
        String name;
        int poiNumber;
        int wayNumber;

        public TextViewData() {
            // do nothing
        }
    }

    /**
     * Executed when the back button is pressed.
     * 
     * @param v
     *            Not used.
     */
    public void backBtn(View v) {
        saveComment();
        finish();
    }

    /**
     * Executed when the export button is pressed.
     * 
     * @param v
     *            Not used.
     */
    public void exportBtn(View v) {
        (new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                saveComment();
                StorageFactory.getStorage().serialize();
                return null;
            }
        }).execute();
    }

    @Override
    public void onBackPressed() {
        backBtn(null);
    }

    /**
     * Executed when the export button is pressed.
     * 
     * @param v
     *            Not used.
     */
    public void renameBtn(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final EditText input = new EditText(this);
        input.setHint(StorageFactory.getStorage().getTrack().getName());
        builder.setView(input);
        builder.setTitle(getResources().getString(
                R.string.alert_trackinfoActivity_rename));
        builder.setPositiveButton(
                getResources().getString(R.string.alert_global_ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        // set track name
                        String value = input.getText().toString().trim();
                        if (!value.equals("")) {
                            StorageFactory.getStorage().getTrack()
                                    .setName(value);
                            (new TextUpdater()).execute();
                        }

                    }
                }).setNegativeButton(
                getResources().getString(R.string.alert_global_cancel),
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        builder.show();
    }

    /**
     * The Method for the preference image Button from the status bar. The
     * Method starts the PreferenceActivity.
     * 
     * @param view
     *            not used
     */
    public void statusBarPrefBtn(View view) {
        final Intent intent = new Intent(this, PreferencesActivity.class);
        startActivity(intent);
    }

    /**
     * This Method for the two (title and description) button from the status
     * bar. This method starts the dialog with all activity informations.
     * 
     * @param v
     *            not used
     */
    public void statusBarTitleBtn(View v) {
        Helper.setActivityInfoDialog(this,
                getResources().getString(R.string.tv_statusbar_trackInfoTitle),
                getResources().getString(R.string.tv_statusbar_trackInfoTDesc));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Helper.setTheme(this);
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        setTitle(R.string.string_trackinfoActivity_title);
        setContentView(R.layout.activity_trackinfoactivity);

        // Set status bar
        Helper.setStatusBar(this,
                getResources().getString(R.string.tv_statusbar_addpointTitle),
                getResources().getString(R.string.tv_statusbar_addpointDesc),
                R.id.ly_trackinfoActivity_statusbar, false);

        (new TextUpdater()).execute();
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            if (ServiceConnector.getLoggerService().isLogging()) {
                Helper.startUserNotification(this,
                        R.drawable.ic_notification_active,
                        TrackInfoActivity.class, true);
            } else {
                Helper.startUserNotification(this,
                        R.drawable.ic_notification_pause,
                        TrackInfoActivity.class, false);
            }
        } catch (RemoteException e) {

            e.printStackTrace();
        }
    }

    /**
     * Will save the comment in the edit text into the track.
     */
    void saveComment() {
        EditText etComment = (EditText) TrackInfoActivity.this
                .findViewById(R.id.et_trackinfoActivity_comment);

        StorageFactory.getStorage().getTrack()
                .setComment(etComment.getText().toString());
    }
}
