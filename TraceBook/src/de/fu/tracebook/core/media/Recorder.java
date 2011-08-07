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

import java.io.File;

import de.fu.tracebook.core.data.IDataMedia;
import de.fu.tracebook.core.data.IDataMediaHolder;
import de.fu.tracebook.core.data.StorageFactory;
import de.fu.tracebook.util.LogIt;

/**
 * Mother of all TraceBook media files. This class is the common ancestor of all
 * implementations for acquiring media files in TraceBook.
 * 
 * 
 */
public abstract class Recorder {
    /**
     * Request code for onActivityResult callback. RECORD_AUDIO_CODE = 100000;
     */
    public static final int RECORD_AUDIO_CODE = 100000;

    /**
     * Request code for onActivityResult callback. RECORD_VIDIO_CODE = 100001;
     */
    public static final int RECORD_VIDEO_CODE = 100001;

    /**
     * Request code for onActivityResult callback. TAKE_PHOTO_CODE = 100002;
     */
    public static final int TAKE_PHOTO_CODE = 100002;

    /**
     * Base directory of our media file.
     */
    protected String baseDir = "";

    /**
     * File name of our media file.
     */
    protected String filename = "";

    /**
     * Are we currently recording?
     */
    protected boolean isRecording = false;

    /**
     * Construct0r! Sets the base directory according to our current track.
     */
    public Recorder() {
        baseDir = StorageFactory.getStorage().getTrack().getTrackDirPath();
    }

    /**
     * Attaches the recorded media file to the given node of our data structure.
     * 
     * @param parent
     *            Node to attach the recorded file to.
     * @return A reference to the created media file object in the global data
     *         structure.
     */
    public IDataMedia appendFileToObject(IDataMediaHolder parent) {
        IDataMedia dm = StorageFactory.getMediaObject(getBaseDir(),
                getFilename());
        LogIt.d("appending " + dm.getName());
        parent.addMedia(dm);
        LogIt.d("size " + parent.getMedia().size());
        return dm;
    }

    /**
     * @return Base directory of the media file.
     */
    public final String getBaseDir() {
        return baseDir;
    }

    /**
     * @return Name of the media file.
     */
    public final String getFilename() {
        return filename;
    }

    /**
     * @return Full path to the media file.
     */
    public final String getPath() {
        return getBaseDir() + File.separator + getFilename();
    }

    /**
     * @return Whether the recorder is recording.
     */
    public final boolean isRecording() {
        return isRecording;
    }

    /**
     * This method starts recording a media file, if it is a continuous process,
     * such as recording audio or video files.
     * 
     * For one-shot acquisition (e. g. taking pictures), start() and stop()
     * should not be used, because of misleading semantics.
     * 
     * @return Filename of the created media file.
     */
    public abstract String start();

    /**
     * This method stops recording a media file, if it is a continuous process,
     * such as recording audio or video files.
     * 
     * For one-shot acquisition (e. g. taking pictures), start() and stop()
     * should not be used, because of misleading semantics.
     */
    public abstract void stop();

    /**
     * WARNING: Because of the nature of how the full path to the file is
     * determined, we blatantly assume the returned filename to *NOT* point to
     * an existing file yet. If such a file does exist for any reason, it will
     * most likely be overwritten!
     * 
     * @return New filename for the media file to be created.
     */
    protected abstract String getNewFilename();
}
