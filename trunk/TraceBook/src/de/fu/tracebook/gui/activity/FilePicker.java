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

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import de.fu.tracebook.util.LogIt;
import de.fu.tracebook.R;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

/**
 * This is an activity that asks the user to pick a file with a certain
 * extension. The filename is then returned. The extension is supplied via the
 * intent that starts the activity. If no extension list is supplied all files
 * are displayed. It is basically a small file browser which lets the user
 * navigate through directories.
 * 
 */
public class FilePicker extends ListActivity {

    /**
     * A simple Wrapper for the ArrayAdapter which simply updates the ImageView
     * of the list item.
     * 
     */
    private static class FilePickerArrayAdapter extends
            ArrayAdapter<FileWrapper> {

        public FilePickerArrayAdapter(Context context, List<FileWrapper> objects) {
            super(context, R.layout.listview_filepicker, R.id.tv_listrow,
                    objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = super.getView(position, convertView, parent);

            FileWrapper f = getItem(position);
            ImageView iv = (ImageView) v.findViewById(R.id.iv_listrow);
            if (f.getFile().isDirectory()) {
                iv.setImageResource(R.drawable.ic_folder);
            } else {
                iv.setImageResource(R.drawable.ic_file);
            }

            return v;
        }

    }

    /**
     * Simple wrapper for File that overrides the toString() method.
     */
    private static class FileWrapper {
        private File file;

        FileWrapper(File f) {
            file = f;
        }

        @Override
        public String toString() {
            return file.getName();
        }

        /**
         * Returns the file object.
         * 
         * @return The file
         */
        File getFile() {
            return file;
        }
    }

    /**
     * This is the name of the extra information of the starting intent, that
     * has the allowed extensions. The data provided must be an array of
     * strings.
     */
    public static final String EXTENSIONS = "extensions";
    /**
     * This is the name of the extra information of the starting intent, that
     * has the root directory of the file manager. The provided data must be a
     * String. If this extra is not supplied then /mnt/sdcard is root-directory.
     */
    public static final String PATH = "path";

    /**
     * This is extra field.
     */
    protected static final String RESULT_CODE_ERROR = "error";

    /**
     * This is the extra field where the file name that is returned is stored.
     * It is only supplied when RESULT_OK is the return code.
     */
    protected static final String RESULT_CODE_FILE = "file";

    /**
     * This is the path of the current directory.
     */
    protected File currentFile;

    /**
     * All extensions that are allowed. Only files with such an extension are
     * displayed.
     */
    protected String[] extensions;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_filepicker);

        Bundle arguments = this.getIntent().getExtras();
        currentFile = Environment.getExternalStorageDirectory();
        if (arguments != null) {
            String path = arguments.getString(PATH);
            extensions = arguments.getStringArray(EXTENSIONS);
            if (path != null) {
                setPath(path);
            }
        }

        TextView tvPath = (TextView) this.findViewById(R.id.tv_filepicker_path);
        tvPath.setText(getResources().getString(R.string.tv_filepicker_currdir)
                + "\n" + currentFile.getAbsolutePath());

        updateAdapter();
    }

    /**
     * Returns from the activity with RESULT_CANCELED. No filename is returned.
     * 
     * @param msg
     *            Error message.
     */
    protected void cancel(String msg) {
        Intent result = new Intent();
        result.putExtra(RESULT_CODE_ERROR, msg);
        setResult(RESULT_CANCELED, result);
        finish();
    }

    /**
     * Creates a list of all suiting files and directories of the current
     * directory. These files are inserted in the list.
     * 
     * @return The list of files in this directory. Only files that have an
     *         allowed extension are supplied. Directories are before all files.
     */
    protected List<FileWrapper> getFileList() {
        // get all files and filter by extension
        File[] dirfiles = currentFile.listFiles(new FileFilter() {
            public boolean accept(File pathname) {
                if (pathname.isDirectory()) {
                    // directories are always accepted
                    return true;
                }
                if (extensions != null)
                    // files that have an allowed extension are allowed.
                    for (String ext : extensions) {
                        if (pathname.getName().endsWith(ext)) {
                            return true;
                        }
                    }
                else {
                    // no extensions provided means that all files are accepted.
                    return true;
                }
                return false;
            }
        });

        if (dirfiles != null) {
            List<FileWrapper> res = new ArrayList<FileWrapper>();
            for (File f : dirfiles) {
                res.add(new FileWrapper(f));
            }
            // Sort: directories first
            Collections.sort(res, new Comparator<FileWrapper>() {
                public int compare(FileWrapper object1, FileWrapper object2) {
                    if (object1.getFile().isDirectory()
                            && !object2.getFile().isDirectory()) {
                        return -1;
                    }
                    if (!object1.getFile().isDirectory()
                            && object2.getFile().isDirectory()) {
                        return 1;
                    }
                    return object1.getFile().compareTo(object2.getFile());
                }
            });

            return res;
        }
        return new ArrayList<FileWrapper>();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode) {
        case RESULT_OK:
            LogIt.d("got: " + data.getExtras().getString(RESULT_CODE_FILE));
            returnWithResult(data.getExtras().getString(RESULT_CODE_FILE));
            break;
        case RESULT_CANCELED:
            break;
        default:
            break;
        }
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        final FileWrapper file = (FileWrapper) l.getItemAtPosition(position);

        if (file.getFile().isFile()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setTitle(file.getFile().getName());
            builder.setMessage(this.getResources().getString(
                    R.string.alert_filepicker_message));

            builder.setPositiveButton(
                    this.getResources().getString(R.string.alert_global_ok),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            returnWithResult(file.getFile().getAbsolutePath());
                            dialog.cancel();
                        }
                    });

            builder.setNegativeButton(
                    this.getResources().getString(R.string.alert_global_cancel),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

            builder.show();
        } else if (file.getFile().isDirectory()) {
            Intent i = new Intent(this, FilePicker.class);
            i.putExtra(PATH, file.getFile().getAbsolutePath());
            i.putExtra(EXTENSIONS, extensions);
            this.startActivityForResult(i, RESULT_OK);
        }

        super.onListItemClick(l, v, position, id);
    }

    /**
     * Return from this activity providing a file name that was picked.
     * 
     * @param path
     *            The returned file name.
     */
    protected void returnWithResult(String path) {
        Intent result = new Intent();
        result.putExtra(RESULT_CODE_FILE, path);
        setResult(RESULT_OK, result);
        LogIt.d("Returns: " + path);
        finish();
    }

    /**
     * Set the current path and check whether it is allowed.
     * 
     * @param path
     *            The new path name.
     */
    protected void setPath(String path) {
        currentFile = new File(path);
        if (!currentFile.isDirectory()) {
            cancel(getResources().getString(
                    R.string.string_filepicker_error_invalid_path));
        }
    }

    /**
     * Updates the ListView.
     */
    protected void updateAdapter() {
        setListAdapter(new FilePickerArrayAdapter(this, getFileList()));
    }
}
