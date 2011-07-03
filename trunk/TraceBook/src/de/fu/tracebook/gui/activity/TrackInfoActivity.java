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

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import de.fu.tracebook.R;
import de.fu.tracebook.core.data.StorageFactory;
import de.fu.tracebook.util.Helper;

/**
 * This activity show info about a track. It is possible to edit the comment and
 * export the track.
 */
public class TrackInfoActivity extends Activity {

    public void backBtn(View v) {
        finish();
    }

    public void exportBtn(View v) {
        (new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                StorageFactory.getStorage().serialize();
                return null;
            }
        }).execute();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Helper.setTheme(this);
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        setTitle(R.string.string_trackinfoActivity_title);
        setContentView(R.layout.activity_trackinfoactivity);

        // Set status bar
        Helper.setStatusBar(this,
                getResources().getString(R.string.tv_statusbar_addpointTitle),
                getResources().getString(R.string.tv_statusbar_addpointDesc),
                R.id.ly_trackinfoActivity_statusbar, false);
    }
}
