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

import java.io.IOException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.Window;
import de.fu.tracebook.R;
import de.fu.tracebook.core.data.DataMapObject;
import de.fu.tracebook.core.data.StorageFactory;
import de.fu.tracebook.core.logger.ServiceConnector;
import de.fu.tracebook.core.media.AudioRecorder;
import de.fu.tracebook.util.Helper;
import de.fu.tracebook.util.LogIt;

/**
 * The Class AddMemoActivity, start the recording of the notice. The user can
 * talk after a 2 seconds progress bar dialog.
 * 
 */
public class AddMemoActivity extends Activity {
    /**
     * Here we save a reference to the current DataMapObject which is in use.
     */
    DataMapObject node;

    /**
     * Preferences for this activity.
     */
    SharedPreferences preferences;
    /**
     * The object that is responsible for recording (and attaching) the audio
     * file to our data structure.
     */
    AudioRecorder recorder = new AudioRecorder();

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Helper.setTheme(this);
        super.onCreate(savedInstanceState);
        // If status bar visible remove the activity title bar.
        if (Helper.checkStatusbarVisibility(this))
            this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        final Bundle extras = getIntent().getExtras();

        if (extras != null) {
            int nodeId = extras.getInt("DataNodeId");
            node = StorageFactory.getStorage().getTrack()
                    .getDataMapObjectById(nodeId);
        }

        preferences = PreferenceManager
                .getDefaultSharedPreferences(getBaseContext());
        setContentView(R.layout.activity_addmemoactivity);
        setTitle(R.string.string_addmemoActivity_title);
        startMemo();
    }

    @Override
    public void onDestroy() {
        stopMemo();
        super.onDestroy();
    }

    @Override
    public void onStop() {
        stopMemo();

        LogIt.popup(this,
                getResources().getString(R.string.toast_recordingFinished));

        super.onStop();
    }

    /**
     * This method show for 2 seconds a progressDialog. After 2 Seconds the
     * recording will be started.
     */
    public void startMemo() {
        final int maxDuration = 1000 * 60 * Integer.parseInt(preferences
                .getString("lst_maxVideoRecording", "0"));

        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.setMessage(getResources().getString(
                R.string.alert_addmemoactivity_progressdialog));
        dialog.setCancelable(false);
        dialog.show();
        (new Thread() {
            @Override
            public void run() {
                try {
                    int step = 0;

                    while (step < 50) {
                        Thread.sleep(2000 / 50);
                        step++;
                        dialog.incrementProgressBy(2);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                dialog.dismiss();

                try {
                    recorder.prepare(maxDuration);

                    if (maxDuration > 0) {
                        (new Thread() {
                            @Override
                            public void run() {
                                recorder.start();

                                try {
                                    Thread.sleep(maxDuration);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }

                                stopMemo();
                                finish();
                            }
                        }).start();
                    } else {
                        recorder.start();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * This method starts the audio recording/.
     * 
     * @param view
     *            no used
     */
    public void startMemoBtn(View view) {
        if (!recorder.isRecording()) {
            startMemo();
        }
    }

    /**
     * This method stops the audio recording and finish the {@link Activity}.
     * 
     * @param view
     *            no used
     */
    public void stopMemoBtn(View view) {
        stopMemo();

        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            if (ServiceConnector.getLoggerService().isLogging()) {
                Helper.startUserNotification(this,
                        R.drawable.ic_notification_active,
                        NewTrackActivity.class, true);
            } else {
                Helper.startUserNotification(this,
                        R.drawable.ic_notification_pause,
                        NewTrackActivity.class, false);
            }
        } catch (RemoteException e) {

            e.printStackTrace();
        }
    }

    /**
     * Stops recording the audio file and appends the new media object to our
     * node, if we were recording, at all.
     */
    void stopMemo() {
        if (recorder.isRecording()) {
            recorder.stop();
            recorder.appendFileToObject(node);
        }
    }
}
