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

import android.app.ListActivity;
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
import de.fu.tracebook.R;
import de.fu.tracebook.core.data.IDataMapObject;
import de.fu.tracebook.core.data.IDataNode;
import de.fu.tracebook.core.data.IDataPointsList;
import de.fu.tracebook.core.data.StorageFactory;
import de.fu.tracebook.core.logger.ServiceConnector;
import de.fu.tracebook.gui.adapter.GenericAdapter;
import de.fu.tracebook.gui.adapter.GenericAdapterData;
import de.fu.tracebook.gui.adapter.GenericItemDescription;
import de.fu.tracebook.util.Helper;

/**
 * This activity show a list of all recorded data in this track.
 */
public class ListDataActivity extends ListActivity {

    private static class AdditionalAdapterData {
        boolean isWay;
        IDataMapObject object;

        public AdditionalAdapterData() {
            // do nothing
        }
    }

    /**
     * The adapter where the list items are stored.
     */
    GenericAdapter adapter;

    /**
     * The description for the adapter data.
     */
    GenericItemDescription description;

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
                .getMenuInfo();

        GenericAdapterData data = adapter.getItem((int) info.id);

        AdditionalAdapterData add = (AdditionalAdapterData) data
                .getAdditional();

        switch (item.getItemId()) {
        case R.id.cm_editmapobjects_delete:
            if (add.isWay) {
                StorageFactory.getStorage().getTrack()
                        .deleteWay(add.object.getId());
            } else {
                StorageFactory.getStorage().getTrack()
                        .deleteNode(add.object.getId());

            }
            initListView();
            return true;
        case R.id.cm_editmapobjects_edit:
            final Intent intent = new Intent(this, AddPointActivity.class);
            intent.putExtra(add.isWay ? "WayId" : "NodeId", add.object.getId());
            startActivity(intent);
            return true;
        default:
            return super.onContextItemSelected(item);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.contextmenu_editmapobjects, menu);
        menu.setHeaderIcon(android.R.drawable.ic_menu_edit);
        menu.setHeaderTitle(getResources().getString(
                R.string.cm_editmapobjects_title));
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
                getResources().getString(R.string.tv_statusbar_listDataTitle),
                getResources().getString(R.string.tv_statusbar_listDataDesc));
    }

    private void initItemDescription() {
        description = new GenericItemDescription();
        description.addResourceId("id", R.id.tv_listviewedit_id);
        description.addResourceId("coords", R.id.tv_listviewedit_coordinates);
        description.addResourceId("img", R.id.iv_listviewedit_image);
        description.addResourceId("stats", R.id.tv_listviewedit_stats);
        description.addResourceId("pois", R.id.tv_listviewedit_poiCount);
    }

    /**
     * Fill the list view with data.
     */
    private void initListView() {
        final NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(2);

        (new AsyncTask<Void, Void, Void>() {

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

            /**
             * Fills a GenericAdapterData with the information from a node.
             * 
             * @param node
             *            The node.
             * @return The GenericAdapterData filled with the values from the
             *         node.
             */
            private GenericAdapterData fillNode(IDataNode node) {
                GenericAdapterData item = new GenericAdapterData(description);
                // set the id
                item.setText("id", " " + node.getId());

                // set the coordinates string
                item.setText("coords", coordString(node.getCoordinates()));

                // set the image
                item.setImage("img", R.drawable.ic_node);

                // set the number of media
                item.setText(
                        "stats",
                        getResources().getString(
                                R.string.string_newtrackactivity_list_media)
                                + node.getMedia().size());

                // add node to item
                AdditionalAdapterData add = new AdditionalAdapterData();
                add.isWay = false;
                add.object = node;
                item.setAdditional(add);

                return item;
            }

            /**
             * Fills a GenericAdapterData with the information from a way.
             * 
             * @param way
             *            The way.
             * @return The GenericAdapterData filled with the values from the
             *         way.
             */
            private GenericAdapterData fillWay(IDataPointsList way) {
                GenericAdapterData item = new GenericAdapterData(description);

                // set the id
                item.setText("id", " " + way.getId());

                // set the coordinates of beginning (+ end)
                List<IDataNode> nodes = way.getNodes();
                if (nodes.size() > 0) {
                    IDataNode start = nodes.get(0);
                    IDataNode end = nodes.get(nodes.size() - 1);

                    String endCoord = way.isArea() ? ""
                            : (getResources().getString(
                                    R.string.string_newtrackactivity_list_end) + coordString(end
                                    .getCoordinates()));

                    item.setText(
                            "coords",
                            getResources()
                                    .getString(
                                            R.string.string_newtrackactivity_list_start)
                                    + coordString(start.getCoordinates())
                                    + endCoord);
                }

                // set the image
                item.setImage("img", way.isArea() ? R.drawable.ic_area
                        : R.drawable.ic_way);

                // set the number of media
                item.setText(
                        "stats",
                        getResources().getString(
                                R.string.string_newtrackactivity_list_media)
                                + " " + way.getMedia().size());

                // set the number of waypoints
                item.setText(
                        "pois",
                        getResources()
                                .getString(
                                        R.string.string_newtrackactivity_list_pointcount)
                                + " " + way.getNodes().size());

                // add node to item
                AdditionalAdapterData add = new AdditionalAdapterData();
                add.isWay = true;
                add.object = way;
                item.setAdditional(add);

                return item;
            }

            @Override
            protected Void doInBackground(Void... params) {
                List<IDataNode> nodeList = StorageFactory.getStorage()
                        .getTrack().getNodes();

                List<IDataPointsList> wayList = StorageFactory.getStorage()
                        .getTrack().getWays();

                List<GenericAdapterData> listData = new ArrayList<GenericAdapterData>();

                for (IDataNode dn : nodeList) {
                    listData.add(fillNode(dn));
                }

                for (IDataPointsList dn : wayList) {
                    listData.add(fillWay(dn));
                }

                adapter = new GenericAdapter(ListDataActivity.this,
                        R.layout.listview_edit, R.id.list, listData);

                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                getListView().setAdapter(adapter);
            }

        }).execute();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Helper.setTheme(this);
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        setTitle(R.string.string_listDataActivity_title);
        setContentView(R.layout.activity_listdataactivity);

        // Set status bar
        Helper.setStatusBar(this,
                getResources().getString(R.string.tv_statusbar_listDataTitle),
                getResources().getString(R.string.tv_statusbar_listDataDesc),
                R.id.ly_listDataActivity_statusbar, false);

        registerForContextMenu(getListView());
        initItemDescription();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initListView();
        try {
            if (ServiceConnector.getLoggerService().isLogging()) {
                Helper.startUserNotification(this,
                        R.drawable.ic_notification_active,
                        ListDataActivity.class, true);
            } else {
                Helper.startUserNotification(this,
                        R.drawable.ic_notification_pause,
                        ListDataActivity.class, false);
            }
        } catch (RemoteException e) {

            e.printStackTrace();
        }
    }

}
