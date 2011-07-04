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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.app.ListActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.TextView;
import de.fu.tracebook.R;
import de.fu.tracebook.core.data.IDataMedia;
import de.fu.tracebook.core.data.IDataMediaHolder;
import de.fu.tracebook.core.data.StorageFactory;
import de.fu.tracebook.gui.adapter.GenericAdapter;
import de.fu.tracebook.gui.adapter.GenericAdapterData;
import de.fu.tracebook.gui.adapter.GenericItemDescription;
import de.fu.tracebook.util.Helper;
import de.fu.tracebook.util.LogIt;

/**
 * This activity shows a list of all media of a mediaholder.
 */
public class ListMediaActivity extends ListActivity {

    private IDataMediaHolder holder;

    /**
     * GenericAdapter for our ListView which we use in this activity.
     */
    GenericAdapter adapter;

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
                .getMenuInfo();

        GenericAdapterData itemData = adapter.getItem((int) info.id);
        IDataMedia media = (IDataMedia) itemData.getAdditional();

        switch (item.getItemId()) {
        case R.id.cm_listmedia_viewMedia:

            final String[] mimes = new String[] { "text/plain", "image/jpeg",
                    "audio/*", "video/*" };

            Intent intent = new Intent();
            intent.setAction(android.content.Intent.ACTION_VIEW);
            File file = new File(media.getPath() + File.separator
                    + media.getName());
            intent.setDataAndType(Uri.fromFile(file), mimes[media.getType()]);
            startActivity(intent);

            break;
        case R.id.cm_listmedia_delete:

            media.delete();
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
        inflater.inflate(R.menu.contextmenu_listmediaactivity, menu);
        menu.setHeaderIcon(android.R.drawable.ic_menu_edit);
        menu.setHeaderTitle(getResources().getString(
                R.string.cm_listmediaActivity_title));
    }

    private void initAdapter() {
        TextView info = (TextView) findViewById(R.id.tv_listmediaActivity_statusInfo);
        GenericItemDescription desc = new GenericItemDescription();

        final int[] imgs = { R.drawable.btn_notice, R.drawable.btn_photo,
                R.drawable.btn_memo, R.drawable.btn_video };

        desc.addResourceId("name", R.id.tv_listrow);
        desc.addResourceId("img", R.id.iv_listrow);

        List<GenericAdapterData> data = new ArrayList<GenericAdapterData>();
        List<IDataMedia> allMedia = holder.getMedia();
        Iterator<IDataMedia> iterator = allMedia.iterator();

        while (iterator.hasNext()) {
            IDataMedia media = iterator.next();

            GenericAdapterData item = new GenericAdapterData(desc);
            item.setText("name", media.getName());
            item.setImage("img", imgs[media.getType()]);
            item.setAdditional(media);

            data.add(item);
        }
        // TODO neues Layout
        adapter = new GenericAdapter(this, R.layout.listview_filepicker,
                R.id.list, data);

        setListAdapter(adapter);

        if (allMedia.isEmpty()) {
            info.setText(R.string.tv_listmediaActivity_noMedia);
        } else {
            info.setText(R.string.tv_listmediaActivity_availableMedia);
        }

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
        final Bundle extras = getIntent().getExtras();

        /*
         * Get the node of the sending Intent
         */
        if (extras != null) {
            long nodeId = extras.getLong("NodeId");
            long wayId = extras.getLong("WayId");
            String trackname = extras.getString("UseTrack");
            if (nodeId != 0) {
                holder = StorageFactory.getStorage().getTrack()
                        .getNodeById(nodeId);
                LogIt.d("node is: " + nodeId);
            } else if (wayId != 0) {
                holder = StorageFactory.getStorage().getTrack()
                        .getPointsListById(wayId);
                LogIt.d("way is: " + wayId);
            } else if (trackname != null) {
                holder = StorageFactory.getStorage().getTrack();
                LogIt.d("track is: " + trackname);
            } else {
                LogIt.d("Neither way nor node nor track ...");
                finish();
                return;
            }
        }

        setTitle(R.string.string_listmediaActivity_title);
        setContentView(R.layout.activity_listmediaactivity);

        registerForContextMenu(getListView());

        // Set status bar
        Helper.setStatusBar(this,
                getResources().getString(R.string.tv_statusbar_listmediaTitle),
                getResources().getString(R.string.tv_statusbar_listmediaDesc),
                R.id.ly_listmediaActivity_statusbar, false);

        initAdapter();
    }

}
