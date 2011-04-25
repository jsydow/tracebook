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
import java.util.Locale;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.RemoteException;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import de.fu.tracebook.R;
import de.fu.tracebook.core.data.db.TagDb;
import de.fu.tracebook.core.logger.ServiceConnector;
import de.fu.tracebook.gui.view.HelpWebView;
import de.fu.tracebook.util.Helper;
import de.fu.tracebook.util.LogIt;

/**
 * Start activity of the application. Initialization of the database for the
 * FullTextSearch activity.
 */
public class StartActivity extends Activity {

    /**
     * Called if the loadTrack button pressed. Start the LoadTrackActivity.
     * 
     * @param view
     *            the view
     */
    public void loadTrackBtn(View view) {
        Intent intent = new Intent(this, LoadTrackActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.zoom_enter, R.anim.zoom_exit);
    }

    /**
     * Called if the newTrack button pressed. Start the NewTrackActivity and the
     * tracking notification for the user
     * 
     * @param view
     *            the view
     */
    public void newTrackBtn(View view) {

        if (Helper.currentTrack() == null)
            try {
                ServiceConnector.getLoggerService().startTrack();
            } catch (RemoteException e) {
                e.printStackTrace();
            }

        Intent intent = new Intent(this, NewTrackActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.zoom_enter, R.anim.zoom_exit);
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Helper.setTheme(this);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_startactivity);
        setTitle(R.string.string_startActivity_title);
        final TagDb db = new TagDb(this);

        (new Thread() {
            @Override
            public void run() {

                if (db.getRowCountForLanguage("de") < 1) {
                    db.initDbWithFile(R.raw.tags_de);
                }
                if (db.getRowCountForLanguage("en") < 1) {
                    db.initDbWithFile(R.raw.tags_en);
                }

                if (db.getRowCountForLanguage("tr") < 1) {
                    db.initDbWithFile(R.raw.tags_tr);
                }

                if (db.getRowCountForLanguage("pl") < 1) {
                    db.initDbWithFile(R.raw.tags_pl);
                }

                if (db.getRowCountForLanguage("fr") < 1) {
                    db.initDbWithFile(R.raw.tags_fr);
                }

            }
        }).start();

        // Initialize ServiceConnector
        ServiceConnector.startService(this);
        ServiceConnector.initService();

        // create TraceBook-folder
        File dir = new File(Environment.getExternalStorageDirectory()
                + File.separator + "TraceBook");
        if (!dir.isDirectory()) {
            if (!dir.mkdir()) {
                LogIt.e("TraceBookMainActiviy",
                        "Could not create TraceBook-directory");
            }
        }
    }

    @Override
    /**
     * Initialization of the option menu for the MainActivity.
     * 
     *  @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
     */
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.optionsmenu_startactivity, menu);
        return true;
    }

    /**
     * Functionality of all items of the option menu.
     * 
     * @param item
     *            the item
     * @return true, if successful
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        final Intent intent;
        switch (item.getItemId()) {
        case R.id.opt_startActivity_close:

            try {
                ServiceConnector.getLoggerService().stopTrack();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            ServiceConnector.stopService();
            finish();
            return true;
        case R.id.opt_startActivity_about:
            intent = new Intent(this, HelpWebView.class);
            intent.putExtra("About", Locale.getDefault().getLanguage());
            startActivity(intent);
            return true;
        case R.id.opt_startActivity_preferences:
            intent = new Intent(this, PreferencesActivity.class);
            startActivity(intent);
            return true;
        case R.id.opt_startActivity_help:
            intent = new Intent(this, HelpWebView.class);
            intent.putExtra("Help", Locale.getDefault().getLanguage());
            startActivity(intent);
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * @see android.app.Activity#onDestroy()
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            ServiceConnector.releaseService();
        } catch (IllegalArgumentException e) {
            LogIt.e("TraceBook", "Service not connected.");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        Helper.stopUserNotification(this);
        Button startresumeBtn = (Button) findViewById(R.id.btn_startActivity_newTrack);
        if (Helper.currentTrack() == null)
            startresumeBtn.setText(getResources().getString(
                    R.string.btn_startActivity_newTrack));
        else
            startresumeBtn.setText(getResources().getString(
                    R.string.btn_startActivity_reseumeTrack));
    }
}
