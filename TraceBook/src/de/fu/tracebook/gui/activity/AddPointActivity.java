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
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.RemoteException;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import de.fu.tracebook.R;
import de.fu.tracebook.core.data.IDataMapObject;
import de.fu.tracebook.core.data.IDataNode;
import de.fu.tracebook.core.data.StorageFactory;
import de.fu.tracebook.core.data.db.TagDb;
import de.fu.tracebook.core.data.db.TagSearchResult;
import de.fu.tracebook.core.logger.ServiceConnector;
import de.fu.tracebook.core.media.PictureRecorder;
import de.fu.tracebook.core.media.Recorder;
import de.fu.tracebook.gui.adapter.GenericAdapter;
import de.fu.tracebook.gui.adapter.GenericAdapterData;
import de.fu.tracebook.gui.adapter.GenericItemDescription;
import de.fu.tracebook.util.GpsMessage;
import de.fu.tracebook.util.Helper;
import de.fu.tracebook.util.LogIt;

/**
 * The purpose of this activity is to add and edit tags to an DataMapObject
 * where an DataMapObject can be anything from POI to area.
 */
public class AddPointActivity extends ListActivity {

    private boolean isWay;

    /**
     * GenericAdapter for our ListView which we use in this activity.
     */
    GenericAdapter adapter;

    /**
     * Here we save a reference to the current DataMapObject which is in use.
     */
    IDataMapObject object;

    /**
     * PicutreRecoder to take pictures.
     */
    PictureRecorder pictureRecorder = new PictureRecorder();

    /**
     * The Method for the AddPointMeta Button, to save MetaData for this node.
     * 
     * @param view
     *            not used
     */
    public void addPointMetaBtn(View view) { // method signature including view
        // is required
        final Intent intent = new Intent(this, AddPointMetaActivity.class);

        intent.putExtra(isWay ? "WayId" : "NodeId", object.getId());
        startActivity(intent);
    }

    /**
     * The Method for the cancel Button to finish the Activity.
     * 
     * @param view
     *            not used
     */
    public void cancelBtn(View view) { // method signature including view is
        // required
        finish();
    }

    /**
     * List all media for a node or way.
     * 
     * @param v
     *            Not used.
     */
    public void listMediaBtn(View v) {
        final Intent intent = new Intent(this, ListMediaActivity.class);
        intent.putExtra(isWay ? "WayId" : "NodeId", object.getId());
        startActivity(intent);
    }

    /**
     * The Method for the makeMemo Button (MediaTags) to start recording a Memo
     * at the AddMemoActivty.
     * 
     * @param view
     *            unused
     */
    public void makeMemoBtn(View view) {
        final Intent intent = new Intent(this, AddMemoActivity.class);
        intent.putExtra(isWay ? "WayId" : "NodeId", object.getId());
        startActivity(intent);
    }

    /**
     * This method create a AlertDailog Box to fill in the notice for the actual
     * node.
     * 
     * @param view
     *            not used
     */
    public void makeNoticeBtn(View view) {
        final AlertDialog.Builder alert = new AlertDialog.Builder(this);
        final EditText input = new EditText(this);
        alert.setView(input);
        alert.setTitle(getResources()
                .getString(R.string.alert_global_addNotice));
        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String values = input.getText().toString().trim();

                object.addMedia(StorageFactory.getStorage().getTrack()
                        .saveText(values));
            }
        });

        alert.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.cancel();
                    }
                });
        alert.show();

    }

    /**
     * The Method for the makePicture Button. The Method starts the standard
     * cameraActivty.
     * 
     * @param view
     *            not used
     */
    public void makePictureBtn(View view) {
        pictureRecorder.startIntent(this);
    }

    /**
     * The Method for the makeVideo Button (MediaTags) to start the
     * RecordActivity.
     * 
     * @param view
     *            not used
     */
    public void makeVideoBtn(View view) {
        final Intent intent = new Intent(this, RecordVideoActivity.class);
        intent.putExtra(isWay ? "WayId" : "NodeId", object.getId());
        startActivity(intent);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    /**
     * This method create the ContextMenu for the Item which was selected. 1.
     * deleteTag: delete the selected MetaTag from the node. 2. renameTag: open
     * AddPointMetaActivity to rename the selected MetaTag. Fill the Adapter
     * with the new MetaData and draw the ListView again.
     */
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
                .getMenuInfo();

        GenericAdapterData itemData = adapter.getItem((int) info.id);

        switch (item.getItemId()) {
        case R.id.cm_addpointActivity_deleteTag:

            object.deleteTag(itemData.getText("NodeKey"));
            setNodeInformation();
            initAdapter();
            adapter.notifyDataSetChanged();
            return true;
        case R.id.cm_addpointActivity_renameTag:
            final Intent intent = new Intent(this, AddPointMetaActivity.class);

            intent.putExtra(isWay ? "WayId" : "NodeId", object.getId());
            intent.putExtra("DataNodeKey", itemData.getText("NodeKey"));
            intent.putExtra("DataNodeValue", itemData.getText("NodeValue"));
            startActivity(intent);

            //$FALL-THROUGH$
        default:
            return super.onContextItemSelected(item);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Helper.setTheme(this);
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        final Bundle extras = getIntent().getExtras();

        /*
         * Get the node of the sending Intent
         */
        if (extras != null) {
            long nodeId = extras.getLong("NodeId");
            long wayId = extras.getLong("WayId");
            if (nodeId != 0) {
                object = StorageFactory.getStorage().getTrack()
                        .getNodeById(nodeId);
                LogIt.d("node is: " + nodeId);
                isWay = false;
            } else if (wayId != 0) {
                object = StorageFactory.getStorage().getTrack()
                        .getPointsListById(wayId);
                LogIt.d("way is: " + wayId);
                isWay = true;
            } else {
                LogIt.d("Neither way nor node ...");
                finish();
                return;
            }
        } else {
            LogIt.d("Object data missing, finishing... ");
            finish();
            return;
        }

        setTitle(R.string.string_addpointActivity_title);
        setContentView(R.layout.activity_addpointactivity);

        LayoutInflater bInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout layoutHolder = (LinearLayout) findViewById(R.id.ly_addpointaAtivity_metaMediaBtnPoint);
        bInflater.inflate(R.layout.dynamic_metamediabuttons, layoutHolder);

        if (object == null) {
            LogIt.e("node null");
            finish();
            return;
        }

        registerForContextMenu(getListView());
        setNodeInformation();

        // Set status bar
        Helper.setStatusBar(this,
                getResources().getString(R.string.tv_statusbar_addpointTitle),
                getResources().getString(R.string.tv_statusbar_addpointDesc),
                R.id.ly_addpointActivity_statusbar, false);

        initAdapter();
    }

    /**
     * Create ContextMenu for this activity.
     */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.contextmenu_addpointactivity, menu);
        menu.setHeaderIcon(android.R.drawable.ic_menu_edit);
        menu.setHeaderTitle(getResources().getString(
                R.string.cm_addpointActivity_title));
    }

    @Override
    public void onDestroy() {
        // We do do not want to store empty nodes
        if (object != null && !object.hasAdditionalInfo()
                && object instanceof IDataNode
                && ((IDataNode) object).getDataPointsList() == null) {
            LogIt.d("POI is empty, will not keep it");
            Helper.currentTrack().deleteNode(object.getId());
            (new GpsMessage(this)).sendDiscardIntent();
        }

        super.onDestroy();
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
     * 
     * @param v
     *            note used
     */
    public void statusBarTitleBtn(View v) {
        Helper.setActivityInfoDialog(this,
                getResources().getString(R.string.tv_statusbar_addpointTitle),
                getResources().getString(R.string.tv_statusbar_addpointDesc));
    }

    /**
     * Initialization of our CustomAdapter and fill the RowData with the
     * MetaInformation out of the two StringArrays category and value. Set this
     * Adapter for our ListView.
     */
    private void initAdapter() {
        GenericItemDescription desc = new GenericItemDescription();

        desc.addResourceId("NodeKey", R.id.title);
        desc.addResourceId("NodeValue", R.id.detail);

        List<GenericAdapterData> data = new ArrayList<GenericAdapterData>();
        Iterator<Entry<String, String>> iterator = object.getTags().entrySet()
                .iterator();

        while (iterator.hasNext()) {
            Map.Entry<String, String> pairs = iterator.next();

            GenericAdapterData item = new GenericAdapterData(desc);
            item.setText("NodeKey", pairs.getKey());
            item.setText("NodeValue", pairs.getValue());

            data.add(item);
        }
        adapter = new GenericAdapter(this, R.layout.listview_addpoint,
                R.id.list, data);

        setListAdapter(adapter);
    }

    /**
     * Get the last node and create a String-Array with all MetaData from the
     * Meta-HashMap of this Node. If the HashMap contain no MetaData, the method
     * returns an empty array.
     */
    private void setNodeInformation() {
        TextView nodeIdTv = (TextView) findViewById(R.id.tv_addpointActivity_nodeId);
        TextView nodeInfo = (TextView) findViewById(R.id.tv_addpointActivity_allocateMeta);
        TextView titleCat = (TextView) findViewById(R.id.tv_addpointActivity_titleListViewCat);
        TextView titleVal = (TextView) findViewById(R.id.tv_addpointActivity_titleListViewVal);

        Map<String, String> tagMap = object.getTags();
        nodeIdTv.setText(getResources().getString(
                R.string.tv_addpointActivity_nodeId)
                + " " + object.getId());

        if (tagMap.size() != 0) {
            nodeInfo.setText(R.string.tv_addpointActivity_MetaData);
            titleCat.setVisibility(1);
            titleVal.setVisibility(1);

        } else {
            nodeInfo.setText(R.string.tv_addpointActivity_noMetaData);
            titleCat.setVisibility(8);
            titleVal.setVisibility(8);

        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
        case Recorder.TAKE_PHOTO_CODE:
            if (resultCode == Activity.RESULT_OK) {
                pictureRecorder.appendFileToObject(object);
            }
            break;
        default:
            break;
        }
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        GenericAdapterData data = adapter.getItem(position);
        String key = data.getText("NodeKey");
        String value = data.getText("NodeValue");
        String language = Locale.getDefault().getLanguage();

        TagDb db = new TagDb(getBaseContext());
        TagSearchResult tag = db.getDetails(key, value, language);
        Dialog infoDialog = Helper.makeInfoDialog(this, this, tag, "Close",
                false);

        infoDialog.show();
    }

    /**
     * OnResume is called when the activity was adjournment and we come back to
     * this activity. This method update the ListInformation with the MetaTags
     * of the Node.
     */
    @Override
    protected void onResume() {
        super.onResume();

        // Set text views and fill the map with meta information about the
        // actual
        // node
        setNodeInformation();

        // Initializes Adapter and fill with new Information
        initAdapter();
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

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}
