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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import de.fu.tracebook.R;
import de.fu.tracebook.core.data.IDataTrack;
import de.fu.tracebook.core.data.StorageFactory;
import de.fu.tracebook.gui.adapter.GenericAdapter;
import de.fu.tracebook.gui.adapter.GenericAdapterData;
import de.fu.tracebook.gui.adapter.GenericItemDescription;
import de.fu.tracebook.util.Helper;
import de.fu.tracebook.util.LogIt;

/**
 * The Class LoadTrackActivity list all saved Track in a list view. With a
 * context menu the user have following options:
 * <p>
 * <ul>
 * <li>delete a track</li>
 * <li>rename a track</li>
 * <li>show all track information</li>
 * <li>load a track</li>
 * </ul>
 */
public class LoadTrackActivity extends ListActivity {

    /**
     * Task to delete a track.
     */
    class DeleteTrackTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... args) {
            if (args.length > 0) {
                StorageFactory.getStorage().deleteTrack(args[0]);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            updateAdapter();
            super.onPostExecute(result);
        }

    }

    /**
     * List of loaded TrackInfo.
     */
    protected final List<GenericAdapterData> data = new ArrayList<GenericAdapterData>();

    /**
     * GenericAdapter for our ListView which we use in this activity.
     */
    GenericAdapter adapter;

    /**
     * The text that is in the search text box.
     */
    String searchText = "";

    /**
     * Should the list be sorted by name? If not then the list is sorted by
     * time.
     */
    boolean sortByName = true;

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    /**
     * This method create the ContextMenu for the Item which was selected. Fill
     * the Adapter with the new MetaData and draw the ListView again.
     */
    @Override
    public boolean onContextItemSelected(MenuItem item) {

        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
                .getMenuInfo();

        GenericAdapterData datum = adapter.getItem((int) info.id);

        final String trackname = datum.getText("TrackName");

        switch (item.getItemId()) {
        // load Track
        case R.id.cm_loadtrackActivity_load:
            IDataTrack track = StorageFactory.getStorage().deserializeTrack(
                    trackname);
            if (track != null) {
                StorageFactory.getStorage().setTrack(track);
                final Intent intent = new Intent(this, MapsForgeActivity.class);
                startActivity(intent);
            } else {
                LogIt.e("Track to load was not found or is corrupt.");
                LogIt.popup(this,
                        "Track to load could not be opened. Missing or corrupt.");
            }

            return true;

            // rename
        case R.id.cm_loadtrackActivity_rename:

            final AlertDialog.Builder alert = new AlertDialog.Builder(this);
            final EditText input = new EditText(this);
            alert.setView(input);
            alert.setTitle(getResources().getString(
                    R.string.alert_loadtrackActivity_rename));
            alert.setPositiveButton(
                    getResources().getString(R.string.alert_global_ok),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,
                                int whichButton) {
                            // rename Track
                            String value = input.getText().toString().trim();

                            int res = StorageFactory.getStorage().renameTrack(
                                    trackname, value);
                            switch (res) {
                            case 0:
                                break;
                            case -1:
                                LogIt.e("Track to rename was not found or is corrupt.");
                                break;
                            case -2:
                                LogIt.e("There is already a track with this name.");
                                break;
                            case -3:
                                LogIt.e("Track could not be renamed.");
                                break;
                            default:
                                break;
                            }

                            updateAdapter();

                        }
                    });

            alert.setNegativeButton(
                    getResources().getString(R.string.alert_global_cancel),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,
                                int whichButton) {
                            dialog.cancel();
                        }
                    });
            alert.show();

            return true;

            // show track info
        case R.id.cm_loadtrackActivity_info:
            IDataTrack trackinfo = StorageFactory.getStorage()
                    .deserializeTrack(trackname);

            final Dialog infoDialog = new Dialog(this);
            infoDialog.setContentView(R.layout.dialog_trackinfo);
            infoDialog.setTitle(R.string.string_trackInfoDialog_title);
            infoDialog.setCancelable(true);

            // set up name
            TextView textname = (TextView) infoDialog
                    .findViewById(R.id.tv_trackInfoDialog_name);
            textname.setText(trackinfo.getName());

            // set up comment
            TextView textcomment = (TextView) infoDialog
                    .findViewById(R.id.tv_trackInfoDialog_comment);
            textcomment.setText(trackinfo.getComment());

            // set up time
            TextView texttime = (TextView) infoDialog
                    .findViewById(R.id.tv_trackInfoDialog_timestamp);
            texttime.setText(trackinfo.getDatetime());

            // set up button
            Button button = (Button) infoDialog
                    .findViewById(R.id.btn_trackInfoDialog_back);
            button.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    infoDialog.cancel();
                }
            });
            // now that the dialog is set up, it's time to show it
            infoDialog.show();

            return true;

            // delete Track
        case R.id.cm_loadtrackActivity_delete:
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(
                    getResources().getString(
                            R.string.alert_loadtrackActivity_deleteTrack))
                    .setPositiveButton(
                            getResources().getString(R.string.alert_global_yes),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface d, int id) {
                                    new DeleteTrackTask()
                                            .execute(new String[] { trackname });

                                }
                            })
                    .setNegativeButton(
                            getResources().getString(R.string.alert_global_no),
                            new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface d, int which) {
                                    d.cancel();
                                }
                            });
            builder.show();
            break;
        default:
            break; // do nothing
        }
        return super.onContextItemSelected(item);
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

        // remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        getApplicationContext()
                .setTheme(android.R.style.Theme_Black_NoTitleBar);
        updateAdapter();
        setTitle(R.string.string_loadtrackActivity_title);
        setContentView(R.layout.activity_loadtrackactivity);
        registerForContextMenu(getListView());

        setTextChangedListenerToSearchBox((EditText) findViewById(R.id.et_statusbar_search));

        // Set status bar
        Helper.setStatusBar(this,
                getResources().getString(R.string.tv_statusbar_loadtrackTitle),
                getResources().getString(R.string.tv_statusbar_loadtrackDesc),
                R.id.ly_loadtrackActivity_statusbar, true);

    }

    /**
     * Create ContextMenu for this activity.
     */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.contextmenu_loadtrackactivity, menu);
        menu.setHeaderIcon(android.R.drawable.ic_menu_compass);
        menu.setHeaderTitle(getResources().getString(
                R.string.cm_loadtrackActivity_title));
    }

    /**
     * This method inflate the options menu for this activity.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.optionsmenu_loadtrackactivity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
        case R.id.opt_loadtrack_sort:
            if (sortByName) {
                item.setTitle(getResources().getString(
                        R.string.opt_loadtrack_sortByName));
                sortByName = false;
            } else {
                item.setTitle(getResources().getString(
                        R.string.opt_loadtrack_sortByTimestamp));
                sortByName = true;
            }
            updateAdapter();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * The Method for the preference image Button from the status bar. The
     * Method starts the PreferenceActivity.
     * 
     * @param v
     *            not used
     */
    public void statusBarPrefBtn(View v) {
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
        if (searchBox.getVisibility() == View.GONE) {
            searchBox.setVisibility(1);
            setTextChangedListenerToSearchBox(searchBox);
        } else
            searchBox.setVisibility(View.GONE);
    }

    /**
     * This Method for the two (title and description) button from the status
     * bar. This method starts the dialog with all activity informations.
     * 
     * @param v
     *            not used
     */
    public void statusBarTitleBtn(View v) {
        Helper.setActivityInfoDialog(this,
                getResources().getString(R.string.tv_statusbar_loadtrackTitle),
                getResources().getString(R.string.tv_statusbar_loadtrackDesc));
    }

    private void setTextChangedListenerToSearchBox(EditText etFilter) {

        if (etFilter == null)
            return;

        etFilter.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {
                // nothing done here
            }

            public void beforeTextChanged(CharSequence s, int start, int count,
                    int after) {
                // nothing done here
            }

            public void onTextChanged(CharSequence s, int start, int before,
                    int count) {
                if (s.toString() != null) {
                    searchText = s.toString();
                    textSearchUpdate();
                }
            }

        });
    }

    /**
     * Fills the adapter with the data that are stored in datalist.
     * 
     * @param datalist
     *            The data used for filling the adapter.
     */
    protected void fillAdapter(final List<GenericAdapterData> datalist) {
        final Activity thisActivity = this;
        this.runOnUiThread(new Runnable() {
            public void run() {
                adapter = new GenericAdapter(thisActivity,
                        R.layout.listview_loadtrack, R.id.list, datalist);

                setListAdapter(adapter);

                getListView().setTextFilterEnabled(true);

                adapter.notifyDataSetChanged();
            }
        });
    }

    /**
     * If a ListItem selected, the method will load the track.
     */
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {

        GenericAdapterData datum = adapter.getItem(position);
        final String trackname = datum.getText("TrackName");
        final Intent intent = new Intent(this, MapsForgeActivity.class);

        if (StorageFactory.getStorage().doesTrackExist(trackname)) {
            if (StorageFactory.getStorage().getTrack() != null) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(getResources().getString(
                        R.string.alert_loadtrackActivity_stopCurrentTrack));
                builder.setMessage(
                        getResources()
                                .getString(
                                        R.string.alert_loadtrackActivity_stopCurrentTrack))
                        .setCancelable(false)
                        .setPositiveButton(
                                getResources().getString(
                                        R.string.alert_global_yes),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                            int id1) {
                                        StorageFactory.getStorage().setTrack(
                                                StorageFactory.getStorage()
                                                        .deserializeTrack(
                                                                trackname));
                                        startActivity(intent);
                                        finish();

                                    }
                                })
                        .setNegativeButton(
                                getResources().getString(
                                        R.string.alert_global_no),
                                new DialogInterface.OnClickListener() {

                                    public void onClick(DialogInterface dialog,
                                            int which) {
                                        dialog.cancel();
                                    }
                                });
                builder.show();

            } else {
                StorageFactory.getStorage()
                        .setTrack(
                                StorageFactory.getStorage().deserializeTrack(
                                        trackname));
                startActivity(intent);
            }

        } else {
            LogIt.e("Track to load was not found or is corrupt.");
            LogIt.popup(this,
                    "Track to load could not be opened. Missing or corrupt.");
        }
        super.onListItemClick(l, v, position, id);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (searchText == null) {
            searchText = "";
        }

        Helper.stopUserNotification(this);
        updateAdapter();

    }

    /**
     * If search text changes this method updates the list.
     */
    protected void textSearchUpdate() {
        if (searchText != null) {
            List<GenericAdapterData> datalist = new ArrayList<GenericAdapterData>();
            for (GenericAdapterData gda : data) {
                if (gda.getText("TrackName").contains(searchText)) {
                    datalist.add(gda);
                }
            }
            fillAdapter(datalist);
        }
    }

    /**
     * Updates the list.
     */
    void updateAdapter() {
        final LoadTrackActivity thisActivity = this;
        (new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                GenericItemDescription desc = new GenericItemDescription();

                desc.addResourceId("TrackName", R.id.tv_listviewloadtrack_track);
                desc.addResourceId("TrackComment",
                        R.id.tv_listviewloadtrack_comment);
                desc.setNameTag("TrackName");
                String comment = null;

                // get all TrackInfo-objects
                List<IDataTrack> trackInfos = StorageFactory.getStorage()
                        .getAllTracks();

                // sort
                if (sortByName) {
                    Collections.sort(trackInfos, new Comparator<IDataTrack>() {
                        public int compare(IDataTrack info1, IDataTrack info2) {
                            return info1.getName().compareToIgnoreCase(
                                    info2.getName());
                        }
                    });
                } else {
                    Collections.sort(trackInfos, new Comparator<IDataTrack>() {
                        public int compare(IDataTrack info1, IDataTrack info2) {
                            return info2.getDatetime().compareToIgnoreCase(
                                    info1.getDatetime());
                        }
                    });
                }

                // fill adapter
                data.clear();
                for (IDataTrack trackinfo : trackInfos) {
                    GenericAdapterData dataItem = new GenericAdapterData(desc);
                    dataItem.setText("TrackName", trackinfo.getName());

                    if (trackinfo.getComment() != null) {
                        if (trackinfo.getComment().length() > 80) {
                            comment = getResources().getString(
                                    R.string.string_loadtrackactivity_comment)
                                    + trackinfo.getComment().trim()
                                            .substring(0, 77) + "...";
                        } else if (trackinfo.getComment().length() > 0) {
                            comment = getResources().getString(
                                    R.string.string_loadtrackactivity_comment)
                                    + trackinfo.getComment() + "...";
                        } else {
                            comment = getResources()
                                    .getString(
                                            R.string.string_loadtrackactivity_nocomment);
                        }
                    } else {
                        comment = getResources().getString(
                                R.string.string_loadtrackactivity_nocomment);
                    }

                    dataItem.setText("TrackComment", comment);

                    data.add(dataItem);

                }

                thisActivity.fillAdapter(data);
                return null;
            }
        }).execute();
    }
}
