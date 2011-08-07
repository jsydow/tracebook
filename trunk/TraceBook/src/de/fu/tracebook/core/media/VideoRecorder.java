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

package de.fu.tracebook.core.media;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.media.MediaRecorder;
import android.view.Surface;

/**
 * This class provides required methods for interacting with the MediaRecorder
 * in order to acquire a video file.
 */
public class VideoRecorder extends Recorder {
    private boolean isReady = false;
    private MediaRecorder recorder;

    // TODO fix ratio of preview

    /**
     * Because of the nature of recording a video with MediaRecorder, we have to
     * make sure a few certain properties have been set. Furthermore, we require
     * knowledge of the Surface object to show the recording preview in. All
     * those necessary steps are taken care of during preparation.
     * 
     * @param maxDuration
     *            Maximum duration of the video to be recorded in seconds.
     * @param surface
     *            The surface object we are going to display our video preview
     *            in.
     * @throws IOException
     *             Not used.
     */
    public void prepare(final int maxDuration, final Surface surface)
            throws IOException {
        filename = getNewFilename();
        recorder = new MediaRecorder();

        // Set media sources.
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        // Set output.
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        recorder.setOutputFile(getPath());

        recorder.setVideoFrameRate(25);
        recorder.setVideoSize(320, 240);

        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        recorder.setVideoEncoder(MediaRecorder.VideoEncoder.MPEG_4_SP);

        recorder.setPreviewDisplay(surface);

        if (maxDuration > 0) {
            recorder.setMaxDuration(maxDuration * 1000);
        }

        recorder.prepare();

        isReady = true;
    }

    /*
     * You can only start recording a video if prepare/2 has been called before.
     * Returns the filename if recording did start, null otherwise.
     * 
     * @see core.media.Recorder#start()
     */
    @Override
    public String start() {
        if (isReady && !isRecording) {
            recorder.start();
            isRecording = true;

            return filename;
        }

        return null;
    }

    @Override
    public void stop() {
        if (isRecording) {
            recorder.stop();
            recorder.reset();
            recorder.release();

            isRecording = false;
        }
    }

    @Override
    protected String getNewFilename() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        String newFilename = sdf.format(new Date());

        newFilename = "video_" + newFilename + ".mp4";

        return newFilename;
    }
}
