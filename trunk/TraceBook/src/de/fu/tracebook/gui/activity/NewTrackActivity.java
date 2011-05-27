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

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import org.mapsforge.android.maps.GeoPoint;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.TabActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TextView;
import android.widget.ToggleButton;
import de.fu.tracebook.R;
import de.fu.tracebook.core.data.IDataNode;
import de.fu.tracebook.core.data.IDataPointsList;
import de.fu.tracebook.core.data.IDataTrack;
import de.fu.tracebook.core.data.StorageFactory;
import de.fu.tracebook.core.logger.ServiceConnector;
import de.fu.tracebook.core.media.PictureRecorder;
import de.fu.tracebook.core.media.Recorder;
import de.fu.tracebook.gui.adapter.GenericAdapter;
import de.fu.tracebook.gui.adapter.GenericAdapterData;
import de.fu.tracebook.gui.adapter.GenericItemDescription;
import de.fu.tracebook.util.Helper;
import de.fu.tracebook.util.LogIt;

/**
 * The NewTrackActivity is the main activity to record, edit and see your ways,
 * areas and POIS. The activity is divided in three part via tabs. The first one
 * is the map view where you can see your collected way points in a convenient
 * way. The second one is the main tab where you can set the your POI's, ways
 * and areas. In the third one you can choose your collected, add new tags,
 * remove tags remove POIs etc.
 */
public class NewTrackActivity extends TabActivity {

    /**
     * The purpose of this OnTabListener is to update the different tab views
     * when changing the tabs. So you see always an updated view of your data.
     */
    static class MyListener implements OnTabChangeListener {

        /**
         * We use this to have a reference of our NewTrackActivity.
         */
        NewTrackActivity act;

        /**
         * Here we save a reference to our tab object in our NewTrackActivity.
         */
        TabHost tab;

        /**
         * We use a dirty trick to have a reference to our NewTrackActivty and
         * to our TabHost which is associated to it.
         * 
         * @param act
         *            reference to the NewTrackActivity
         * @param tab
         *            reference to the TabHost if the NewTrackActivty
         */
        public MyListener(NewTrackActivity act, TabHost tab) {
            this.act = act;
            this.tab = tab;
        }

        public void onTabChanged(String tabId) {
            String currentTab = tab.getCurrentTabTag();
            if (currentTab.equals(tabId)) {
                act.initListView();
                tab.invalidate();
            }

            ToggleButton areaToggle = (ToggleButton) act
                    .findViewById(R.id.tbtn_newtrackActivity_startArea);
            ToggleButton streetToggle = (ToggleButton) act
                    .findViewById(R.id.tbtn_newtrackActivity_startWay);

            try {
                if (ServiceConnector.getLoggerService().isLogging()) {
                    Button resume = (Button) act
                            .findViewById(R.id.btn_newtrackActivity_resume);
                    resume.setVisibility(8);
                } else {
                    Button resume = (Button) act
                            .findViewById(R.id.btn_newtrackActivity_resume);
                    resume.setVisibility(1);
                }
            } catch (RemoteException e) {

                e.printStackTrace();
            }

            if (Helper.currentTrack().getCurrentWay() == null) {
                streetToggle.setEnabled(true);
                streetToggle.setChecked(false);

                areaToggle.setEnabled(true);
                areaToggle.setChecked(false);
            } else {
                final boolean isArea = Helper.currentTrack().getCurrentWay()
                        .isArea();
                areaToggle.setEnabled(isArea);
                areaToggle.setChecked(isArea);

                streetToggle.setEnabled(!isArea);
                streetToggle.setChecked(!isArea);
            }
        }
    }

    /**
     * 
     */
    GenericAdapter adapter;

    /**
     * TextView which shows the current text for media buttons.
     */
    TextView mediaData;

    /**
     * Reference to a pictureRecorder to record pictures.
     */
    PictureRecorder pictureRecorder = new PictureRecorder();

    /**
     * Called if the addPointButton pressed. Switch to the AddPointActivity, to
     * insert Meta-Tags for the last Node.
     * 
     * @param view
     *            not used
     */
    public void addPointBtn(View view) {
        int nodeId = 0;

        long start = System.currentTimeMillis();
        StorageFactory.getStorage().getTrack().getDataMapObjectById(1);
        long end = System.currentTimeMillis() - start;
        LogIt.d("TraceBookOperation", "@@@@@@ Took " + end + " ms");

        try {
            nodeId = ServiceConnector.getLoggerService().createPOI(false);
        } catch (RemoteException e) {
            LogIt.e("###############", "no service: " + e.getMessage());
        }
        LogIt.d("TraceBook", "nodeid " + nodeId);

        final Intent intent = new Intent(this, AddPointActivity.class);
        intent.putExtra("DataNodeId", nodeId);
        startActivity(intent);
    }

    /**
     * @param view
     *            not used
     */
    public void editCommentBtn(View view) {
        final IDataTrack track = StorageFactory.getStorage().getTrack();
        final AlertDialog.Builder alert = new AlertDialog.Builder(this);
        final EditText input = new EditText(this);
        input.setText(track.getComment());
        alert.setView(input);
        alert.setTitle(getResources().getString(
                R.string.alert_newtrackActivity_addTrackNotice));
        alert.setPositiveButton(
                getResources().getString(R.string.alert_global_ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String value = input.getText().toString().trim();

                        track.setComment(value);

                    }
                });

        alert.setNegativeButton(
                getResources().getString(R.string.alert_global_cancel),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.cancel();
                    }
                });
        alert.show();

        // TODO test
        IDataTrack currtrack = StorageFactory.getStorage().getTrack();
        for (int i = 0; i < 500; ++i) {
            IDataNode node = currtrack.newNode(new GeoPoint(10, 10));
            node.getTags().put("motorway", "highway");
        }
        for (int i = 0; i < 10; ++i) {
            IDataPointsList way = currtrack.newWay();
            for (int j = 0; j < 400; ++j) {
                IDataNode node = way.newNode(new GeoPoint(10, 10));
                node.getTags().put("motorway", "highway");
            }
        }
    }

    /**
     * @param view
     *            unused
     */
    public void makeMemoBtn(View view) {
        final Intent intent = new Intent(this, AddMemoActivity.class);
        intent.putExtra("DataNodeId", StorageFactory.getStorage().getTrack()
                .getCurrentWay().getId());
        startActivity(intent);
    }

    /**
     * @param view
     *            unused
     */
    public void makeNoticeBtn(View view) {
        final AlertDialog.Builder alert = new AlertDialog.Builder(this);
        final EditText input = new EditText(this);
        alert.setView(input);
        alert.setTitle(getResources()
                .getString(R.string.alert_global_addNotice));
        alert.setPositiveButton(
                getResources().getString(R.string.alert_global_ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String value = input.getText().toString().trim();

                        StorageFactory
                                .getStorage()
                                .getTrack()
                                .getCurrentWay()
                                .addMedia(
                                        StorageFactory.getStorage().getTrack()
                                                .saveText(value));
                        LogIt.popup(getApplicationContext(), getResources()
                                .getString(R.string.alert_global_addedNotice));
                    }
                });

        alert.setNegativeButton(
                getResources().getString(R.string.alert_global_cancel),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.cancel();
                    }
                });
        alert.show();

    }

    /**
     * @param view
     *            not used
     */
    public void makePictureBtn(View view) {
        pictureRecorder.startIntent(this);
    }

    /**
     * @param view
     *            not used
     */
    public void makeVideoBtn(View view) {
        final Intent intent = new Intent(this, RecordVideoActivity.class);
        intent.putExtra("DataNodeId", StorageFactory.getStorage().getTrack()
                .getCurrentWay().getId());
        startActivity(intent);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
                .getMenuInfo();

        GenericAdapterData data = adapter.getItem((int) info.id);

        int nodeId = Integer.parseInt(data.getText("NodeId").trim());

        switch (item.getItemId()) {
        case R.id.cm_editmapobjects_delete:
            StorageFactory.getStorage().getTrack().deleteNode(nodeId);
            StorageFactory.getStorage().getTrack().deleteWay(nodeId);
            initListView();
            return true;
        case R.id.cm_editmapobjects_edit:
            final Intent intent = new Intent(this, AddPointActivity.class);
            intent.putExtra("DataNodeId", nodeId);
            startActivity(intent);
            return true;
        default:
            return super.onContextItemSelected(item);
        }
    }

    /**
     * Create activity.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Helper.setTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newtrackactivity);
        setTitle(R.string.string_newtrackActivity_title);
        // Initialize TabHost
        initTabHost();

        // Initialize ListView
        initListView();

        // Initialize ServiceConnector
        // ServiceConnector.startService(this);
        // ServiceConnector.initService();

        setButtonList(false, 0);

        TabHost myTabHost = getTabHost();
        myTabHost.setOnTabChangedListener(new MyListener(this, myTabHost));

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
     * This method resume the logging if the resume button pressed.
     * 
     * @param view
     *            not used
     */
    public void resumeBtn(View view) {
        try {
            ServiceConnector.getLoggerService().resumeLogging();
            Button resume = (Button) findViewById(R.id.btn_newtrackActivity_resume);
            resume.setVisibility(8);
            Helper.startUserNotification(this,
                    R.drawable.ic_notification_active, NewTrackActivity.class,
                    true);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method is called if startArea-ToggleButton pressed. Start and stop area
     * tracking.
     * 
     * @param view
     *            not used
     */
    public void startAreaTbtn(View view) {
        ToggleButton areaToggle = (ToggleButton) findViewById(R.id.tbtn_newtrackActivity_startArea);
        ToggleButton streetToggle = (ToggleButton) findViewById(R.id.tbtn_newtrackActivity_startWay);
        String check = areaToggle.getText().toString();
        if (check.equals(areaToggle.getTextOn().toString())) {
            streetToggle.setEnabled(false);
            setButtonList(true, 2);
            try {
                ServiceConnector.getLoggerService().beginArea(false);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            streetToggle.setEnabled(true);
            setButtonList(false, 0);
            try {
                ServiceConnector.getLoggerService().endWay();
            } catch (RemoteException e) {
                e.printStackTrace();
            }

        }

    }

    /**
     * Method is called if startWay-ToggleButton pressed. Start and stop way
     * tracking.
     * 
     * @param view
     *            not used
     */
    public void startWayTbtn(View view) {
        ToggleButton streetToggle = (ToggleButton) findViewById(R.id.tbtn_newtrackActivity_startWay);
        ToggleButton areaToggle = (ToggleButton) findViewById(R.id.tbtn_newtrackActivity_startArea);
        String check = streetToggle.getText().toString();
        if (check.equals(streetToggle.getTextOn().toString())) {
            areaToggle.setEnabled(false);
            setButtonList(true, 1);
            try {
                ServiceConnector.getLoggerService().beginWay(false);

            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            areaToggle.setEnabled(true);
            setButtonList(false, 0);
            try {
                ServiceConnector.getLoggerService().endWay();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 
     * Called if the stopTrackButton pressed. Stop the actual tracking and
     * returns to the main activity.
     * 
     * @param view
     *            not used
     */
    public void stopTrackBtn(View view) {

        Helper.alertStopTracking(this);

    }

    private void checkGpsStatus() {
        LocationManager loc = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Get the app's shared preferences
        SharedPreferences appPreferences = PreferenceManager
                .getDefaultSharedPreferences(this);

        if (!loc.isProviderEnabled(LocationManager.GPS_PROVIDER)
                && appPreferences.getBoolean("check_GPSbyStartTracking", true)) {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setTitle(this.getResources().getString(
                    R.string.alert_NewTrackActivity_NoGpsTitle));
            builder.setMessage(this.getResources().getString(
                    R.string.alert_NewTrackActivity_NoGpsMessage));

            builder.setPositiveButton(
                    this.getResources().getString(R.string.alert_global_yes),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                            Intent intent = new Intent(
                                    Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(intent);
                            dialog.cancel();
                        }
                    });

            builder.setNegativeButton(
                    this.getResources().getString(R.string.alert_global_no),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

            builder.show();

        }

    }

    /**
     * Initialization the TabHost with all three tabs: 1. mapView (MapsForge) 2.
     * NewTab 3. EditTab
     */
    private void initTabHost() {
        // Initialize TabHost
        TabHost tabHost = getTabHost();

        // Initialize TabHost
        tabHost.addTab(tabHost
                .newTabSpec("map_tab")
                .setIndicator(
                        getResources().getString(
                                R.string.tab_newtrackActivity_map))
                .setContent(new Intent(this, MapsForgeActivity.class)));
        // new Intent(this, MapsForgeActivity.class))
        tabHost.addTab(tabHost
                .newTabSpec("new_tab")
                .setIndicator(
                        getResources().getString(
                                R.string.tab_newtrackActivity_new))
                .setContent(R.id.tab_newtrackActivity_new));
        tabHost.addTab(tabHost
                .newTabSpec("edit_tab")
                .setIndicator(
                        getResources().getString(
                                R.string.tab_newtrackActivity_edit))
                .setContent(R.id.tab_newtrackactivity_edit));

        // set the default tap to our MapTab
        tabHost.setCurrentTab(1);

    }

    /**
     * This method set the visibility of the media buttons at the bottom of the
     * activity for street and area mapping. In top of the Buttons, the TextView
     * mediaData signals the user for for what type of mapping the mediaData
     * will be saved.
     * 
     * @param active
     *            turn the visibility of the ButtonList and TextView on/off
     * @param button
     *            signals the method which button was selected. 1 for street
     *            ToggleButton 2 for area toggle button and else 0
     */
    private void setButtonList(boolean active, int button) {
        int visible = 8;
        if (active)
            visible = 1;

        mediaData = (TextView) findViewById(R.id.tv_newtrackActivity_setButtonList);
        if (button == 1)
            mediaData.setText(R.string.tv_newtrackActivity_setButtonList1);
        else if (button == 2)
            mediaData.setText(R.string.tv_newtrackActivity_setButtonList2);

        mediaData.setVisibility(visible);

        LayoutInflater bInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout layoutHolder = (LinearLayout) findViewById(R.id.ly_newtrackActivity_metaMediaBtnNew);
        bInflater.inflate(R.layout.dynamic_metamediabuttons, layoutHolder);

        Button makePictureBtn = (Button) layoutHolder
                .findViewById(R.id.btn_addMetaMedia_makePicture);
        makePictureBtn.setVisibility(visible);
        Button makeVideoBtn = (Button) layoutHolder
                .findViewById(R.id.btn_addMetaMedia_makeVideo);
        makeVideoBtn.setVisibility(visible);
        Button makeMemoBtn = (Button) layoutHolder
                .findViewById(R.id.btn_addMetaMedia_makeMemo);
        makeMemoBtn.setVisibility(visible);
        Button makeNoticeBtn = (Button) layoutHolder
                .findViewById(R.id.btn_addMetaMedia_makeNotice);
        makeNoticeBtn.setVisibility(visible);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IDataTrack dt = StorageFactory.getStorage().getTrack();

        switch (requestCode) {
        case Recorder.TAKE_PHOTO_CODE:
            if (resultCode == Activity.RESULT_OK) {
                pictureRecorder.appendFileToObject(dt.getCurrentWay());
            }
            break;
        default:
            break;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.ActivityGroup#onDestroy()
     */
    @Override
    protected void onDestroy() {
        ServiceConnector.releaseService();
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initListView();
        checkGpsStatus();
        try {
            if (ServiceConnector.getLoggerService().isLogging()) {
                Helper.startUserNotification(this,
                        R.drawable.ic_notification_active,
                        NewTrackActivity.class, true);
                Button resume = (Button) findViewById(R.id.btn_newtrackActivity_resume);
                resume.setVisibility(8);
            } else {
                Helper.startUserNotification(this,
                        R.drawable.ic_notification_pause,
                        NewTrackActivity.class, false);
                Button resume = (Button) findViewById(R.id.btn_newtrackActivity_resume);
                resume.setVisibility(1);
            }
        } catch (RemoteException e) {

            e.printStackTrace();
        }
    }

    /**
     * Initialize ListView and Adapter with the list of saved POI, streets and
     * areas. To show a customizable ListView the method use the GenericAdapter
     * from gui.adapter. The Method implements also the OnItemClickListener to
     * edit the selected item.
     */
    void initListView() {
        final Intent intent = new Intent(this, AddPointActivity.class);
        // Initialize ListView for EditTab
        ListView listView = (ListView) findViewById(R.id.tracks_lvw);
        registerForContextMenu(listView);
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(2);

        GenericItemDescription desc = new GenericItemDescription();
        desc.addResourceId("NodeId", R.id.tv_listviewedit_id);
        desc.addResourceId("NodeCoord", R.id.tv_listviewedit_coordinates);
        desc.addResourceId("NodeImg", R.id.iv_listviewedit_image);
        desc.addResourceId("NodeStats", R.id.tv_listviewedit_stats);
        desc.addResourceId("WayPOIs", R.id.tv_listviewedit_poiCount);

        List<IDataNode> nodeList = StorageFactory.getStorage().getTrack()
                .getNodes();

        List<IDataPointsList> wayList = StorageFactory.getStorage().getTrack()
                .getWays();

        List<GenericAdapterData> listData = new ArrayList<GenericAdapterData>();

        for (IDataNode dn : nodeList) {
            GenericAdapterData item = new GenericAdapterData(desc);

            item.setText("NodeId", " " + dn.getId());
            item.setText(
                    "NodeCoord",
                    getResources().getString(
                            R.string.string_newtrackactivity_list_lat)
                            + nf.format(dn.getCoordinates().getLatitude())
                            + getResources().getString(
                                    R.string.string_newtrackactivity_list_lon)
                            + nf.format(dn.getCoordinates().getLatitude()));

            item.setImage("NodeImg", R.drawable.ic_node);
            item.setText(
                    "NodeStats",
                    getResources().getString(
                            R.string.string_newtrackactivity_list_media)
                            + dn.getMedia().size());

            listData.add(item);
        }

        for (IDataPointsList dn : wayList) {
            GenericAdapterData item = new GenericAdapterData(desc);
            item.setText("NodeId", " " + dn.getId());

            if (dn.getNodes().size() > 0) {
                IDataNode start = dn.getNodes().get(0);
                IDataNode end = dn.getNodes().get(dn.getNodes().size() - 1);

                String endCoord = dn.isArea() ? "" : (getResources().getString(
                        R.string.string_newtrackactivity_list_end)
                        + getResources().getString(
                                R.string.string_newtrackactivity_list_lat)
                        + nf.format(end.getCoordinates().getLatitude())
                        + " "
                        + getResources().getString(
                                R.string.string_newtrackactivity_list_lon) + nf
                        .format(end.getCoordinates().getLongitude()));

                item.setText(
                        "NodeCoord",
                        getResources().getString(
                                R.string.string_newtrackactivity_list_start)
                                + getResources()
                                        .getString(
                                                R.string.string_newtrackactivity_list_lat)
                                + nf.format(start.getCoordinates()
                                        .getLatitude())
                                + " "
                                + getResources()
                                        .getString(
                                                R.string.string_newtrackactivity_list_lon)
                                + nf.format(start.getCoordinates()
                                        .getLongitude()) + endCoord);
            }

            item.setImage("NodeImg", dn.isArea() ? R.drawable.ic_area
                    : R.drawable.ic_way);
            item.setText(
                    "NodeStats",
                    getResources().getString(
                            R.string.string_newtrackactivity_list_media)
                            + " " + dn.getMedia().size());
            item.setText(
                    "WayPOIs",
                    getResources().getString(
                            R.string.string_newtrackactivity_list_pointcount)
                            + " " + dn.getNodes().size());
            listData.add(item);

        }

        adapter = new GenericAdapter(this, R.layout.listview_edit,
                R.id.tracks_lvw, listData);

        listView.setAdapter(adapter);

        // Get selected item
        listView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {

                GenericAdapterData data = adapter.getItem(position);
                LogIt.d(ACTIVITY_SERVICE, "NodeID: " + data.getText("NodeId"));

                int nodeId = Integer.parseInt(data.getText("NodeId").trim());
                intent.putExtra("DataNodeId", nodeId);
                startActivity(intent);
            }
        });

    }
}
