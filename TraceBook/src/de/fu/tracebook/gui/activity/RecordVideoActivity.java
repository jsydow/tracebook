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
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import de.fu.tracebook.R;
import de.fu.tracebook.core.data.IDataMediaHolder;
import de.fu.tracebook.core.data.StorageFactory;
import de.fu.tracebook.core.logger.ServiceConnector;
import de.fu.tracebook.core.media.VideoRecorder;
import de.fu.tracebook.util.Helper;
import de.fu.tracebook.util.LogIt;

/**
 * Activity that starts recording a video and stops recording it upon hitting a
 * button each. This activity is closed automatically once recording the video
 * has been stopped.
 */
public class RecordVideoActivity extends Activity implements
        SurfaceHolder.Callback {

    private IDataMediaHolder node;
    private SurfaceHolder surfaceHolder;

    /**
     * Preferences for this activity.
     */
    SharedPreferences preferences;
    /**
     * Recorder for this activity.
     */
    VideoRecorder recorder = new VideoRecorder();

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Helper.setTheme(this);
        super.onCreate(savedInstanceState);
        final Bundle extras = getIntent().getExtras();

        if (extras != null) {
            int nodeId = extras.getInt("NodeId");
            int wayId = extras.getInt("WayId");
            if (nodeId != 0) {
                node = StorageFactory.getStorage().getTrack()
                        .getNodeById(nodeId);
            } else if (wayId != 0) {
                node = StorageFactory.getStorage().getTrack()
                        .getPointsListById(wayId);
            } else {
                node = StorageFactory.getStorage().getTrack();
            }
        }

        preferences = PreferenceManager
                .getDefaultSharedPreferences(getBaseContext());
        setContentView(R.layout.activity_recordvideoactivity);
        setTitle(R.string.string_startActivity_title);

        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.sfv_recordvideoActivity_camera);

        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    @Override
    public void onDestroy() {
        stopRecording();
        super.onDestroy();
    }

    /**
     * This function is called when the "Start recording" button is clicked.
     * 
     * @param view
     *            Not used.
     */
    public void onRecordBtn(View view) {
        final int maxDuration = 1000 * 60 * Integer.parseInt(preferences
                .getString("lst_maxVideoRecording", "0"));

        if (!recorder.isRecording()) {
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

                        stopRecording();
                        finish();
                    }
                }).start();
            } else {
                recorder.start();
            }
        }
    }

    /**
     * This function is called when the "Stop recording" button is clicked.
     * 
     * @param view
     *            Not used.
     */
    public void onRecordStop(View view) {
        if (recorder.isRecording()) {
            stopRecording();
        }

        finish();
    }

    @Override
    public void onStop() {
        stopRecording();

        LogIt.popup(this,
                getResources().getString(R.string.toast_recordingFinished));

        super.onStop();
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width,
            int height) {
        // Does nothing. Literally.
    }

    public void surfaceCreated(SurfaceHolder holder) {
        int maxDuration = 60 * Integer.parseInt(preferences.getString(
                "lst_maxVideoRecording", "0"));

        try {
            recorder.prepare(maxDuration, holder.getSurface());
        } catch (IOException e) {
            LogIt.e(e.toString());
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // Does nothing. Literally.
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            if (ServiceConnector.getLoggerService().isLogging()) {
                Helper.startUserNotification(this,
                        R.drawable.ic_notification_active,
                        MapsForgeActivity.class, true);
            } else {
                Helper.startUserNotification(this,
                        R.drawable.ic_notification_pause,
                        MapsForgeActivity.class, false);
            }
        } catch (RemoteException e) {

            e.printStackTrace();
        }
    }

    /**
     * Stops recording the video and appends the new media object to our node,
     * if we were recording, at all.
     */
    void stopRecording() {
        if (recorder.isRecording()) {
            recorder.stop();
            recorder.appendFileToObject(node);
        }
    }
}
