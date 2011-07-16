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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ListView;
import de.fu.tracebook.R;
import de.fu.tracebook.core.data.db.TagDb;
import de.fu.tracebook.core.data.db.TagSearchResult;
import de.fu.tracebook.core.logger.ServiceConnector;
import de.fu.tracebook.gui.adapter.GenericAdapter;
import de.fu.tracebook.gui.adapter.GenericAdapterData;
import de.fu.tracebook.gui.adapter.GenericItemDescription;
import de.fu.tracebook.util.Helper;

/**
 * The FullTextSearchActivity deals with a full text search on the description
 * of tags. You will get a list of tags and their description and can choose it
 * and the tag and key will set in the previous activity.
 * <p>
 * The activity set a custom TextWatcher class to listen on input changes. As
 * soon as the user is tipping something it will be recognized and a new
 * SearchThread will be started. Right now threads are careering sequencenumbers
 * for their search result to avoid older threads form overwriting result of new
 * threads.
 * 
 */
public class FullTextSearchActivity extends ListActivity {

    // TODO complete tag database

    /**
     * The text watcher tracks changes in the search edit box. As soon as some
     * text matches with the description text, the result will be displayed in
     * the list view.
     */
    static class MyTextWatcher implements TextWatcher {

        /**
         * The thread currently used for searching the DB.
         */
        private Thread searchThread = null;

        /**
         * Reference to the FullTextSearchActivity to update the list view.
         */
        FullTextSearchActivity act;

        /**
         * 
         * @param act
         *            reference to the FullTextSearchActivity to update the list
         *            view.
         */
        public MyTextWatcher(FullTextSearchActivity act) {
            this.act = act;
        }

        public void afterTextChanged(Editable s) {
            // do nothing
        }

        public void beforeTextChanged(CharSequence s, int start, int count,
                int after) {
            // do nothing

        }

        public void onTextChanged(CharSequence s, int start, int before,
                int count) {
            final String searchText = s.toString().trim();

            // if (searchThread != null)
            // searchThread.stop();

            searchThread = new SearchThread(act, act.increaseIndex(),
                    searchText);
            searchThread.start();
        }
    }

    /**
     * A simple thread class which deals with search jobs in our database.
     */
    static class SearchThread extends Thread {

        /**
         * Reference to the FullSearchActivity.
         */
        FullTextSearchActivity act;

        /**
         * Current resolution index of this thread to prevent old thread from
         * overriding result form new threads.
         */
        int resIndex;

        /**
         * Reference to a search string.
         */
        String searchText;

        /**
         * 
         * The constructor takes different references to perform the search task
         * and to tell the gui to update its content.
         * 
         * @param act
         *            Reference to the FullTextSearchActivity
         * @param resIndex
         *            current result index
         * @param searchText
         *            search text to search for
         */
        public SearchThread(FullTextSearchActivity act, int resIndex,
                String searchText) {
            this.act = act;
            this.resIndex = resIndex;
            this.searchText = searchText;
        }

        @Override
        public void run() {
            TagDb db = new TagDb(act);

            List<TagSearchResult> result = db.getTag(searchText, Locale
                    .getDefault().getLanguage());
            act.fillResultsToList(result, resIndex);
        }
    }

    private int currResIndex;
    private List<TagSearchResult> currTagSearchResult;

    /**
     * We use this to get Tag Information about the press Item in our ListView.
     */
    TagSearchResult ts;

    /**
     * Fill the ListView with the tag results. Since we are using threads to do
     * our search it can happen the we have multiple thread which are trying to
     * update the list view. To prevent this we generate result indexes to
     * prevent older thread from overwriting data from new tracks.
     * 
     * @param tags
     *            List of the TagSearchResult
     * @param resIndex
     *            current result index of the thread
     */
    public synchronized void fillResultsToList(List<TagSearchResult> tags,
            int resIndex) {

        // an old thread is trying to give us a result so we prevent it.
        if (getResIndex() < resIndex)
            return;

        currTagSearchResult = tags;

        GenericItemDescription desc = new GenericItemDescription();
        desc.addResourceId("Comment",
                R.id.tv_listviewfulltextsearch_description);
        desc.addResourceId("Category", R.id.tv_listviewfulltextsearch_category);
        desc.addResourceId("Value", R.id.tv_listviewfulltextsearch_value);

        List<GenericAdapterData> data = new ArrayList<GenericAdapterData>();

        for (TagSearchResult res : tags) {
            GenericAdapterData item = new GenericAdapterData(desc);

            item.setText("Comment", res.getDescription());
            item.setText("Category", res.getKey());
            item.setText("Value", res.getValue());
            data.add(item);
        }

        final GenericAdapter adapter = new GenericAdapter(this,
                R.layout.listview_fulltextsearch, R.id.list, data);

        this.runOnUiThread(new Runnable() {
            public void run() {
                setListAdapter(adapter);
                adapter.notifyDataSetChanged();

            }

        });
    }

    /**
     * @return return the current result index
     */
    public synchronized int getResIndex() {
        return currResIndex;
    }

    /**
     * 
     * @return returns the next result index
     */
    public synchronized int increaseIndex() {
        return ++currResIndex;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Helper.setTheme(this);
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_fulltextsearchactivity);
        setTitle(R.string.string_fulltextsearchActivity_title);

        // Set status bar
        Helper.setStatusBar(
                this,
                getResources().getString(
                        R.string.tv_statusbar_fulltextsearchTitle),
                getResources().getString(
                        R.string.tv_statusbar_fulltextsearchDesc),
                R.id.ly_fulltextsearchActivity_statusbar, true);

        EditText editBox = checkEditText();

        final Bundle extras = getIntent().getExtras();

        if (editBox != null) {
            editBox.addTextChangedListener(new MyTextWatcher(this));

            if (extras != null) {
                String tagValue = extras.getString("TagValue");
                if (tagValue != null) {
                    editBox.setText(tagValue);
                }
            }

        }

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
     * This Method for the search image Button from the status bar. This method
     * change the visibility of edit text view below the status bar.
     * 
     * @param v
     *            not used
     */
    public void statusBarSearchBtn(View v) {
        EditText searchBox = (EditText) findViewById(R.id.et_statusbar_search);
        if (searchBox.getVisibility() == 8)
            searchBox.setVisibility(1);
        else
            searchBox.setVisibility(8);
    }

    /**
     * This Method for the two (title and description) button from the status
     * bar. This method starts the dialog with all activity informations.
     * 
     * @param v
     *            not used
     */
    public void statusBarTitleBtn(View v) {
        Helper.setActivityInfoDialog(
                this,
                getResources().getString(
                        R.string.tv_statusbar_fulltextsearchTitle),
                getResources().getString(
                        R.string.tv_statusbar_fulltextsearchDesc));
    }

    /**
     * This method check the status bar "status". If status bar is available the
     * edit text visibility of the fullTextSearchActivity change to invisible.
     * 
     * @return The correct and used edit text.
     */
    private EditText checkEditText() {

        // Get the app's shared preferences
        SharedPreferences appPreferences = PreferenceManager
                .getDefaultSharedPreferences(this);

        // Get the value for the status bar check box - default false
        if (appPreferences.getBoolean("check_visbilityStatusbar", false)) {
            EditText loadTrackSearch = (EditText) findViewById(R.id.et_fulltextsearchActivity_search);
            loadTrackSearch.setVisibility(8);
            EditText statusBarSearch = (EditText) findViewById(R.id.et_statusbar_search);
            statusBarSearch.setVisibility(1);
            return statusBarSearch;
        } else
            return (EditText) findViewById(R.id.et_fulltextsearchActivity_search);
    }

    /**
     * Show dialog for the selected Item with all tag informations. 1. Category
     * 2. Value 3. Description 4. Image 5. Wikipedia link
     * 
     */
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {

        super.onListItemClick(l, v, position, id);

        if (currTagSearchResult == null)
            return;

        ts = currTagSearchResult.get(position);

        Helper.makeInfoDialog(this, this, ts, "Apply", true);
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
}
