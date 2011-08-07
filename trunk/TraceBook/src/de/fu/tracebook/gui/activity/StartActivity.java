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

package de.fu.tracebook.gui.activity;

import java.util.Locale;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import de.fu.tracebook.R;
import de.fu.tracebook.core.data.StorageFactory;
import de.fu.tracebook.core.data.db.TagDb;
import de.fu.tracebook.core.data.implementation.DBOpenHelper;
import de.fu.tracebook.core.logger.ServiceConnector;
import de.fu.tracebook.gui.view.HelpWebView;
import de.fu.tracebook.util.Helper;
import de.fu.tracebook.util.LogIt;

/**
 * Start activity of the application. Initialization of the database for the
 * FullTextSearch activity.
 */
public class StartActivity extends Activity {

    // TODO first start dialog, which explains help and statusbar

    /**
     * Called if the loadTrack button pressed. Start the LoadTrackActivity.
     * 
     * @param view
     *            The button.
     */
    public void loadTrackBtn(View view) {
        Intent intent = new Intent(this, LoadTrackActivity.class);
        startActivity(intent);
    }

    /**
     * Called if the newTrack button pressed. Start the NewTrackActivity and the
     * tracking notification for the user
     * 
     * @param view
     *            the view
     */
    public void newTrackBtn(View view) {

        // Start new track only if there is no current Track
        // resume otherwise
        if (Helper.currentTrack() == null)
            try {
                ServiceConnector.getLoggerService().startTrack();
            } catch (RemoteException e) {
                LogIt.e("Could not start new track as logger service cannot be reached.");
            }

        Intent intent = new Intent(this, MapsForgeActivity.class);
        startActivity(intent);
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
            }
        }).start();

        // DataOpenHelper helper = new DataOpenHelper(this);
        // helper.setInstance();
        DBOpenHelper helper = new DBOpenHelper(this);
        DBOpenHelper.setInstance(helper);

        // Initialize ServiceConnector
        ServiceConnector.startService(this);
        ServiceConnector.initService();

        // create TraceBook-folder if not exists
        StorageFactory.getStorage().ensureThatTraceBookDirExists();
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
        // show about dialog
        case R.id.opt_startActivity_about:
            intent = new Intent(this, HelpWebView.class);
            intent.putExtra("About", Locale.getDefault().getLanguage());
            startActivity(intent);
            return true;
            // go to preference menu
        case R.id.opt_startActivity_preferences:
            intent = new Intent(this, PreferencesActivity.class);
            startActivity(intent);
            return true;
            // show Help Dialog
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
        DBOpenHelper.getInstance().close();
        try {
            ServiceConnector.releaseService();
            ServiceConnector.stopService();
        } catch (IllegalArgumentException e) {
            LogIt.e("Releasing service failed.");
        }
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();

        Helper.stopUserNotification(this);
    }
}
