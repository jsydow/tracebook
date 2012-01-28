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

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import org.mapsforge.android.maps.GeoPoint;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.EditText;
import de.fu.tracebook.R;
import de.fu.tracebook.core.data.IDataBug;
import de.fu.tracebook.core.data.StorageFactory;
import de.fu.tracebook.core.logger.ServiceConnector;
import de.fu.tracebook.gui.adapter.GenericAdapter;
import de.fu.tracebook.gui.adapter.GenericAdapterData;
import de.fu.tracebook.gui.adapter.GenericItemDescription;
import de.fu.tracebook.util.Helper;

public class BugListActivity extends ListActivity {

    /**
     * GenericAdapter for our ListView which we use in this activity.
     */
    GenericAdapter adapter;

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
                .getMenuInfo();

        GenericAdapterData itemData = adapter.getItem((int) info.id);
        final IDataBug bug = (IDataBug) itemData.getAdditional();

        switch (item.getItemId()) {
        case R.id.cm_listbugs_editBug:

            // TODO
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            final EditText input = new EditText(this);
            input.setHint(bug.getDescription());
            builder.setView(input);
            builder.setTitle(getResources().getString(
                    R.string.alert_bugListActivity_edit));
            builder.setPositiveButton(
                    getResources().getString(R.string.alert_global_ok),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                            // set track name
                            String value = input.getText().toString().trim();
                            if (!value.equals("")) {
                                bug.setDescription(value);
                                initAdapter();
                            }
                            dialog.cancel();
                        }
                    }).setNegativeButton(
                    getResources().getString(R.string.alert_global_cancel),
                    new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

            builder.show();

            break;
        case R.id.cm_listbugs_deleteBug:

            bug.removeFromDb();
            initAdapter();

            break;
        }

        return true;
    }

    /**
     * Create ContextMenu for this activity.
     */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();

        inflater.inflate(R.menu.contextmenu_listbugsactivity, menu);
        menu.setHeaderIcon(android.R.drawable.ic_menu_edit);
        menu.setHeaderTitle(getResources().getString(
                R.string.cm_listBugsActivity_title));
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
        Helper.setActivityInfoDialog(this,
                getResources().getString(R.string.tv_statusbar_listBugsTitle),
                getResources().getString(R.string.tv_statusbar_listBugsDesc));
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Helper.setTheme(this);
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        setTitle(R.string.string_listBugsActivity_title);
        setContentView(R.layout.activity_buglistactivity);

        registerForContextMenu(getListView());

        // Set status bar
        Helper.setStatusBar(this,
                getResources().getString(R.string.tv_statusbar_listBugsTitle),
                getResources().getString(R.string.tv_statusbar_listBugsDesc),
                R.id.ly_bugListActivity_statusbar, false);

        initAdapter();
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            if (ServiceConnector.getLoggerService().isLogging()) {
                Helper.startUserNotification(this,
                        R.drawable.ic_notification_active,
                        ListMediaActivity.class, true);
            } else {
                Helper.startUserNotification(this,
                        R.drawable.ic_notification_pause,
                        ListMediaActivity.class, false);
            }
        } catch (RemoteException e) {

            e.printStackTrace();
        }
    }

    void initAdapter() {
        final NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(2);

        (new AsyncTask<Void, Void, List<GenericAdapterData>>() {

            /**
             * Assembles a String containing the latitude and longitude.
             * 
             * @param coords
             *            The coordinates.
             * @return The String of the coordinates.
             */
            private String coordString(GeoPoint coords) {
                return getResources().getString(
                        R.string.string_newtrackactivity_list_lat)
                        + nf.format(coords.getLatitude())
                        + " "
                        + getResources().getString(
                                R.string.string_newtrackactivity_list_lon)
                        + nf.format(coords.getLongitude());
            }

            @Override
            protected List<GenericAdapterData> doInBackground(Void... arg0) {
                GenericItemDescription desc = new GenericItemDescription();
                desc.addResourceId("description", android.R.id.text1);
                desc.addResourceId("pos", android.R.id.text2);

                List<GenericAdapterData> data = new ArrayList<GenericAdapterData>();
                List<IDataBug> bugs = StorageFactory.getBugManager().getBugs();

                for (IDataBug bug : bugs) {
                    GenericAdapterData item = new GenericAdapterData(desc);
                    item.setText("description", bug.getDescription());

                    String pos = coordString(bug.getPosition());

                    item.setText("pos", pos);
                    item.setAdditional(bug);

                    data.add(item);
                }
                return data;
            }

            @Override
            protected void onPostExecute(List<GenericAdapterData> result) {
                adapter = new GenericAdapter(BugListActivity.this,
                        android.R.layout.two_line_list_item, R.id.list, result);

                setListAdapter(adapter);
            }
        }).execute();
    }

}
