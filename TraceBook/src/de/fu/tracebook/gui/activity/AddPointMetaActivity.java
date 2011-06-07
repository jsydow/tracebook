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
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.content.res.XmlResourceParser;
import android.os.Bundle;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;
import de.fu.tracebook.R;
import de.fu.tracebook.core.data.IDataMapObject;
import de.fu.tracebook.core.data.StorageFactory;
import de.fu.tracebook.core.data.db.HistoryDb;
import de.fu.tracebook.core.data.db.TagSearchResult;
import de.fu.tracebook.core.logger.ServiceConnector;
import de.fu.tracebook.gui.adapter.GenericAdapter;
import de.fu.tracebook.gui.adapter.GenericAdapterData;
import de.fu.tracebook.gui.adapter.GenericItemDescription;
import de.fu.tracebook.util.Helper;
import de.fu.tracebook.util.LogIt;

/**
 * In this Activity you can choose your Tags via an AutoComplete feature. Tags
 * and values are grouped together. So the AutoComplete feature for values are
 * depended by their key values. For example when you choose "highway" only
 * highway related values will be given as a list for AutoComplete.
 */
public class AddPointMetaActivity extends ListActivity {

    /**
     * A simple enumeration class for tags.
     */
    enum Tags {
        /**
         * 
         */
        KEY,
        /**
         * 
         */
        USEFUL,
        /**
         * 
         */
        VALUE
    }

    private GenericAdapter adapter;

    private boolean lastUsed = true;

    /**
     * Reference to the current DataMapObject in use.
     */
    IDataMapObject node;

    /**
     * Cancel Button clicked.
     * 
     * @param view
     *            not used
     */
    public void cancelBtn(View view) {
        finish();
    }

    /**
     * Give all the tag category's from the Tag-XML.
     * 
     * @return a string array containing the category's
     */
    public String[] getCategoryTags() {
        // Test array
        String[] firstGroupTags = parseTags(Tags.KEY, "");
        return firstGroupTags;
    }

    /**
     * Generate the all linked values for the category tag.
     * 
     * @param category
     *            Category to get the values from
     * @return return a string array with values for the given category
     */
    public String[] getValues(String category) {
        // return the value tags for the selected category tag
        String[] valueTags = parseTags(Tags.VALUE, category);
        return valueTags;
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

        setContentView(R.layout.activity_addpointmetaactivity);
        setTitle(R.string.string_addpointmetaActivity_title);

        final AutoCompleteTextView autoComplVal = (AutoCompleteTextView) findViewById(R.id.ac_addpointmetaActivity_value);
        final AutoCompleteTextView autoComplCat = (AutoCompleteTextView) findViewById(R.id.ac_addpointmetaActivity_categorie);

        final Bundle extras = getIntent().getExtras();

        if (extras != null) {
            int nodeId = (int) extras.getLong("DataNodeId");
            node = StorageFactory.getStorage().getTrack()
                    .getDataMapObjectById(nodeId);

            if (extras.containsKey("DataNodeKey")) {
                String key = extras.getString("DataNodeKey");
                autoComplCat.setText(key);
            }
            if (extras.containsKey("DataNodeValue")) {
                String value = extras.getString("DataNodeValue");
                autoComplVal.setText(value);
            }

            ArrayAdapter<String> firstGroupAdapter = new ArrayAdapter<String>(
                    this, android.R.layout.simple_dropdown_item_1line,
                    getCategoryTags());
            autoComplCat.setAdapter(firstGroupAdapter);

            /**
             * If the focus is at the AutoCompleteTextView autoComplVal we call
             * the method getValues to generate the AutoComplete String[]
             */
            autoComplVal.setOnFocusChangeListener(new OnFocusChangeListener() {

                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus) {
                        String cat = autoComplCat.getText().toString();

                        // autoComplVal.setText(cat.toCharArray(),0,cat.length());
                        ArrayAdapter<String> valueTagAdapter = new ArrayAdapter<String>(
                                v.getContext(),
                                android.R.layout.simple_dropdown_item_1line,
                                getValues(cat));
                        autoComplVal.setAdapter(valueTagAdapter);

                    }
                }
            });
        }

        fillListView();

        // Set status bar
        Helper.setStatusBar(
                this,
                getResources().getString(
                        R.string.tv_statusbar_addpointmetaTitle),
                getResources()
                        .getString(R.string.tv_statusbar_addpointmetaDesc),
                R.id.ly_addpointMetaAtivity_statusbar, false);

    }

    /**
     * This method inflate the options menu for this activity.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.optionsmenu_addpointmetaactivity, menu);
        setOptionsMenuItemTitle(lastUsed,
                menu.findItem(R.id.opt_addpointmetaactivity_sort));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
        case R.id.opt_addpointmetaactivity_sort:
            setLastUsed(!lastUsed);
            setOptionsMenuItemTitle(!lastUsed, item);

            fillListView();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Save the MetaData to the Node-Meta and go back to the information
     * activity.
     * 
     * @param view
     *            not used
     */
    public void saveBtn(View view) {
        final AutoCompleteTextView autoComplVal = (AutoCompleteTextView) findViewById(R.id.ac_addpointmetaActivity_value);
        final AutoCompleteTextView autoComplCat = (AutoCompleteTextView) findViewById(R.id.ac_addpointmetaActivity_categorie);

        if (node != null) {
            node.addTag(autoComplCat.getText().toString(), autoComplVal
                    .getText().toString());
        }
        HistoryDb db = new HistoryDb(this);
        db.updateTag(autoComplCat.getText().toString(), autoComplVal.getText()
                .toString());
        finish();
    }

    /**
     * Start FullTextSearch Activity.
     * 
     * @param view
     *            not used
     */
    public void searchBtn(View view) {

        final AutoCompleteTextView autoComplCat = (AutoCompleteTextView) findViewById(R.id.ac_addpointmetaActivity_categorie);
        String tag = autoComplCat.getText().toString();

        final Intent intent = new Intent(AddPointMetaActivity.this,
                FullTextSearchActivity.class);

        intent.putExtra("TagValue", tag);
        // It is important to use a number > -1 to get the
        // startActivityForResult working right
        startActivityForResult(intent, 0);

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
        Helper.setActivityInfoDialog(
                this,
                getResources().getString(
                        R.string.tv_statusbar_addpointmetaTitle),
                getResources()
                        .getString(R.string.tv_statusbar_addpointmetaDesc));
    }

    private void fillListView() {
        HistoryDb db = new HistoryDb(this);

        SharedPreferences appPreferences = PreferenceManager
                .getDefaultSharedPreferences(this);

        lastUsed = getPreferenceLastUsed();
        int historySize = Integer.parseInt(appPreferences.getString(
                "lst_setHistorySize", "10"));

        List<TagSearchResult> result = db.getHistory(!lastUsed, historySize);

        GenericItemDescription desc = new GenericItemDescription();
        desc.addResourceId("Key", R.id.tv_history_key);
        desc.addResourceId("Value", R.id.tv_history_value);

        List<GenericAdapterData> data = new ArrayList<GenericAdapterData>();

        for (TagSearchResult res : result) {
            GenericAdapterData item = new GenericAdapterData(desc);

            item.setText("Key", res.getKey());
            item.setText("Value", res.getValue());
            data.add(item);
        }

        adapter = new GenericAdapter(this, R.layout.listview_taghistory,
                R.id.list, data);

        setListAdapter(adapter);
        adapter.notifyDataSetChanged();

    }

    private boolean getPreferenceLastUsed() {
        SharedPreferences appPreferences = PreferenceManager
                .getDefaultSharedPreferences(this);
        return appPreferences.getBoolean("check_showMostRecentTagsInHistory",
                true);
    }

    /**
     * This method parses all the tags in the XML-MetaTag files.
     * 
     * @param tagType
     *            tag type in which we are interested in
     * @param parentName
     *            parent name means the tag value for which we are searching
     * @return a String array with all tags
     */
    private String[] parseTags(Tags tagType, String parentName) {
        int next;
        boolean inParent = false;
        XmlResourceParser parser = this.getResources().getXml(R.xml.tags);
        ArrayList<String> tagStrings = new ArrayList<String>();

        try {
            String tag = "";
            next = parser.getEventType();
            while (next != XmlPullParser.END_DOCUMENT) {
                if (next == XmlPullParser.START_TAG) {
                    tag = parser.getName();
                    switch (tagType) {
                    case KEY:
                        if (tag.equals("key")) {
                            tagStrings.add(parser.getAttributeValue(null, "v"));
                        }
                        break;
                    case VALUE:
                        if ((tag.equals("key"))
                                && (parser.getAttributeValue(null, "v"))
                                        .equals(parentName)) {
                            inParent = true;
                        } else if ((tag.equals("key"))
                                && !(parser.getAttributeValue(null, "v"))
                                        .equals(parentName)) {
                            inParent = false;
                        } else if (inParent && (tag.equals("value"))) {
                            tagStrings.add(parser.getAttributeValue(null, "v"));
                        }
                        break;
                    case USEFUL:
                        if ((tag.equals("value"))
                                && (parser.getAttributeValue(null, "v"))
                                        .equals(parentName)) {
                            inParent = true;
                        } else if ((tag.equals("value"))
                                && !(parser.getAttributeValue(null, "v"))
                                        .equals(parentName)) {
                            inParent = false;
                        } else if (inParent && (tag.equals("useful"))) {
                            tagStrings.add(parser.getAttributeValue(null, "v"));
                        }
                        break;
                    default:
                        break;
                    }
                }

                next = parser.next();
            }

        } catch (XmlPullParserException xe) {
            parser.close();
            LogIt.e("Couldn't parse tags from xml");
            return new String[0];
        } catch (IOException e) {
            parser.close();
            LogIt.e("Couldn't parse tags from xml");
            return new String[0];
        } finally {
            parser.close();
        }
        String[] tagStringsArray = new String[tagStrings.size()];
        return tagStrings.toArray(tagStringsArray);
    }

    /**
     * 
     * @param isLastUsedYes
     *            From now on show the most recently used tag first?
     */
    private void setLastUsed(boolean isLastUsedYes) {
        if (isLastUsedYes) {
            lastUsed = true;
            setPreferenceLastUsed(true);
        } else {
            lastUsed = false;
            setPreferenceLastUsed(false);
        }
    }

    /**
     * Sets the title of the options menu item.
     * 
     * @param isLastUsedYes
     *            From now on show the most recently used tag first?
     * @param item
     *            The menu item.
     */
    private void setOptionsMenuItemTitle(boolean isLastUsedYes, MenuItem item) {
        if (!isLastUsedYes) {
            item.setTitle(getResources().getString(
                    R.string.opt_addpointmetaactivity_lastUsed));
        } else {
            item.setTitle(getResources().getString(
                    R.string.opt_addpointmetaactivity_mostUsed));
        }
    }

    private void setPreferenceLastUsed(boolean isLastUsedYes) {
        Editor edit = PreferenceManager.getDefaultSharedPreferences(this)
                .edit();
        edit.putBoolean("check_showMostRecentTagsInHistory", isLastUsedYes);
        edit.commit();
        return;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // data == null when the user has used the back button to exit the
        // previous activity
        if (data == null)
            return;

        final AutoCompleteTextView autoComplVal = (AutoCompleteTextView) findViewById(R.id.ac_addpointmetaActivity_value);
        final AutoCompleteTextView autoComplCat = (AutoCompleteTextView) findViewById(R.id.ac_addpointmetaActivity_categorie);

        final Bundle extras = data.getExtras();

        if (extras != null) {
            if (extras.containsKey("DataNodeKey")) {
                String key = extras.getString("DataNodeKey");
                autoComplCat.setText(key);
            }
            if (extras.containsKey("DataNodeValue")) {
                String value = extras.getString("DataNodeValue");
                autoComplVal.setText(value);
            }
        }

    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {

        final AutoCompleteTextView autoComplVal = (AutoCompleteTextView) findViewById(R.id.ac_addpointmetaActivity_value);
        final AutoCompleteTextView autoComplCat = (AutoCompleteTextView) findViewById(R.id.ac_addpointmetaActivity_categorie);

        GenericAdapterData data = adapter.getItem(position);
        autoComplVal.setText(data.getText("Value"));
        autoComplCat.setText(data.getText("Key"));

        super.onListItemClick(l, v, position, id);
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

}
