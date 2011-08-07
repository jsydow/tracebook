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
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;

/**
 * This class handles creating the intent for taking a photo with the built-in
 * camera. You *will* have to make sure the picture file is attached to the data
 * structure appropriately, using the onActivityResult callback provided by the
 * activity using PictureRecorder.
 * 
 * 
 */
public class PictureRecorder extends Recorder {
    /*
     * (non-Javadoc)
     * 
     * @see core.media.Recorder#start()
     */
    @Override
    public String start() {
        // Nothing to start.
        return null;
    }

    /**
     * Creates an intent for acquiring an image and starts an activity for that
     * intent.
     * 
     * @param activity
     *            Activity the onActivityResult is called on.
     */
    public void startIntent(Activity activity) {
        final Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        filename = getNewFilename();

        i.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(getPath())));
        activity.startActivityForResult(i, TAKE_PHOTO_CODE);
    }

    /*
     * (non-Javadoc)
     * 
     * @see core.media.Recorder#stop()
     */
    @Override
    public void stop() {
        // Nothing to stop.
    }

    /*
     * (non-Javadoc)
     * 
     * @see core.media.Recorder#getNewFilename()
     */
    @Override
    protected String getNewFilename() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        String newFilename = sdf.format(new Date());

        newFilename = "image_" + newFilename + ".jpg";

        return newFilename;
    }
}
