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

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.RemoteException;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import de.fu.tracebook.R;
import de.fu.tracebook.core.logger.ServiceConnector;
import de.fu.tracebook.util.Helper;

/**
 * This Activity show our preference menu for the application.
 */
public class PreferencesActivity extends PreferenceActivity {

    /**
     * A reference to the system wide PreferenceManager to avoid re initing it
     * all the time.
     */
    SharedPreferences appPreferences;

    /**
     * A reference to our special preference field in which we start the
     * FileManager intent.
     */
    Preference mapChooser;

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        final Activity thisActivity = this;
        Helper.setTheme(this);
        super.onCreate(savedInstanceState);

        appPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        this.addPreferencesFromResource(R.xml.tracebook_preference);
        setTitle(R.string.string_preferencesActivity_title);

        mapChooser = findPreference("mapsforgeMapFilePath");

        mapChooser
                .setOnPreferenceClickListener(new OnPreferenceClickListener() {

                    public boolean onPreferenceClick(Preference preference) {

                        Intent i = new Intent(thisActivity, FilePicker.class);
                        i.putExtra(FilePicker.EXTENSIONS,
                                new String[] { ".map" });
                        thisActivity.startActivityForResult(i,
                                FilePicker.RESULT_CODE_OK);

                        return true;
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data != null) {

            String filename = data.getExtras().getString(
                    FilePicker.RESULT_CODE_FILE);

            Editor editor = appPreferences.edit();

            editor.putString("mapsforgeMapFilePath", filename);
            editor.commit();
            mapChooser.setSummary(filename);

        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        mapChooser.setSummary(appPreferences.getString("mapsforgeMapFilePath",
                ""));
        try {
            if (ServiceConnector.getLoggerService().isLogging()) {
                Helper.startUserNotification(this,
                        R.drawable.ic_notification_active,
                        PreferencesActivity.class, true);
            } else {
                Helper.startUserNotification(this,
                        R.drawable.ic_notification_pause,
                        PreferencesActivity.class, false);
            }
        } catch (RemoteException e) {

            e.printStackTrace();
        }
    }
}
