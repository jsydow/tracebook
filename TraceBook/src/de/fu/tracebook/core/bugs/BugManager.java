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

package de.fu.tracebook.core.bugs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.mapsforge.android.maps.GeoPoint;
import org.mapsforge.android.maps.OverlayItem;
import org.xmlpull.v1.XmlSerializer;

import android.os.AsyncTask;
import android.util.Xml;
import de.fu.tracebook.R;
import de.fu.tracebook.core.data.NewTrack;
import de.fu.tracebook.core.data.StorageFactory;
import de.fu.tracebook.core.overlays.BugOverlayItem;
import de.fu.tracebook.core.overlays.BugOverlayItem.BugType;
import de.fu.tracebook.gui.activity.MapsForgeActivity;
import de.fu.tracebook.util.LogIt;

/**
 * A manager class for all Bugs. A Bug is an error in the map data. The website
 * OpenStreetBugs.org provides some bugs that can be downloaded. This manager
 * can download these bugs.
 */
public final class BugManager {

    // TODO insert bug management into data storage and use database

    private static BugManager instance;

    /**
     * Get an instance of the bug manager.
     * 
     * @return An instance of the bugmanager, not null.
     */
    public static BugManager getInstance() {
        if (instance == null) {
            instance = new BugManager();
        }
        return instance;
    }

    /**
     * The list of bugs reported by the user.
     */
    List<Bug> bugs = new ArrayList<Bug>();

    /**
     * The list of OpenStreetBugs.
     */
    List<Bug> osbugs = new ArrayList<Bug>();

    private BugManager() {
        // do nothing
    }

    /**
     * Add a user created bug.
     * 
     * @param bug
     *            The bug to add.
     */
    public void addBug(Bug bug) {
        bugs.add(bug);
    }

    /**
     * Will load Bugs from OpenStreetBugs. At least 100 Bugs are loaded. The
     * area is the current position +0.25 degrees in all directions.
     * 
     * @param activity
     *            The MapsForgeActivity which uses this BugManager.
     * @param pos
     *            The current position.
     */
    public void downloadBugs(final MapsForgeActivity activity,
            final GeoPoint pos) {
        if (pos == null) {
            return;
        }

        (new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                BufferedReader reader = null;
                Boolean ret = Boolean.FALSE;

                String osbUrl = "http://openstreetbugs.schokokeks.org/api/0.1/getBugs?b="
                        + (pos.getLatitude() - 0.25f)
                        + "&t="
                        + (pos.getLatitude() + 0.25f)
                        + "&l="
                        + (pos.getLongitude() - 0.25f)
                        + "&r="
                        + (pos.getLongitude() + 0.25f);

                LogIt.d("Url is: " + osbUrl);
                if (MapsForgeActivity.isOnline(activity)) {
                    ret = Boolean.TRUE;
                    try {
                        URL url = new URL(osbUrl);
                        URLConnection conn = url.openConnection();
                        InputStream in = conn.getInputStream();
                        reader = new BufferedReader(new InputStreamReader(in,
                                "UTF-8"));
                        osbugs.clear();

                        for (String line = reader.readLine(); line != null; line = reader
                                .readLine()) {
                            Bug b = extractBug(line);
                            if (b != null) {
                                osbugs.add(b);
                            }
                        }

                        LogIt.d("Found " + osbugs.size() + " bugs!");
                        activity.fillBugs();

                    } catch (IOException e) {
                        ret = Boolean.FALSE;
                        LogIt.e("Download error: " + e.getMessage());

                    } finally {
                        try {
                            if (reader != null) {
                                reader.close();
                            }
                        } catch (IOException e) {
                            // do nothing
                        }
                    }
                }
                return ret;
            }

            /*
             * (non-Javadoc)
             * 
             * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
             */
            @Override
            protected void onPostExecute(Boolean result) {
                if (!result.booleanValue()) {
                    LogIt.popup(
                            activity,
                            activity.getResources()
                                    .getString(
                                            R.string.alert_mapsforgeactivity_faileddownload));
                }
            }
        }).execute();

    }

    /**
     * Get OverlayItems for all bugs.
     * 
     * @return A list of all OverlayItems for all Bugs.
     */
    public Collection<OverlayItem> getBugs() {
        ArrayList<OverlayItem> items = new ArrayList<OverlayItem>();
        for (Bug b : bugs) {
            items.add(new BugOverlayItem(b, BugType.USERBUG));
        }
        for (Bug b : osbugs) {
            items.add(new BugOverlayItem(b, BugType.OPENSTREETBUG));
        }
        return items;
    }

    /**
     * Remove a bug.
     * 
     * @param bug
     *            The bug to remove.
     */
    public void remove(Bug bug) {
        if (!bugs.remove(bug)) {
            osbugs.remove(bug);
        }
    }

    /**
     * Serialises all Bugs. The bug are stored in bugs.xml in the directory of
     * the current track. The XML-file is OSM-compatible.
     */
    public void serializeBugs() {
        String path = StorageFactory.getStorage().getTrack().getTrackDirPath()
                + File.separator + "bugs.xml";

        if (size() < 1) {
            LogIt.w("Trying to save 0 Bugs into file. File is not generated.");
            return;
        }

        long id = -1;
        File file = new File(path);
        boolean fileCreated = false;
        try {
            fileCreated = file.createNewFile();
        } catch (IOException e) {
            // will not happen
        }

        if (fileCreated) {
            FileOutputStream fileos = null;
            try {
                fileos = new FileOutputStream(file);
            } catch (FileNotFoundException e) {
                LogIt.e("Could not open new file " + file.getPath());
                return;
            }

            XmlSerializer serializer = Xml.newSerializer();

            try {
                serializer.setOutput(fileos, "UTF-8");
                serializer.startDocument(null, Boolean.valueOf(true));
                serializer.startTag(null, "osm");

                serializer.attribute(null, "version", "0.6");
                serializer.attribute(null, "generator", "TraceBook");

                for (Bug b : bugs) {
                    serializer.startTag(null, "node");
                    serializer.attribute(null, "lat",
                            Double.toString(b.getPosition().getLatitude()));
                    serializer.attribute(null, "lon",
                            Double.toString(b.getPosition().getLongitude()));
                    serializer.attribute(null, "id", Long.toString(--id));
                    serializer.attribute(null, "timestamp",
                            NewTrack.getW3CFormattedTimeStamp());
                    serializer.attribute(null, "version", "1");

                    serializer.startTag(null, "tag");
                    serializer.attribute(null, "k", "bug");
                    serializer.attribute(null, "v", b.getDescription());
                    serializer.endTag(null, "tag");

                    serializer.endTag(null, "node");
                }

                serializer.endTag(null, "osm");
                serializer.flush();
            } catch (IllegalArgumentException e) {
                LogIt.e("Should not happen. Internal error.");
            } catch (IllegalStateException e) {
                LogIt.e("Should not happen. Internal error.");
            } catch (IOException e) {
                LogIt.e("Error while reading file.");
            } finally {
                try {
                    fileos.close();
                } catch (IOException e) {
                    LogIt.e("Error closing file: " + e.getMessage());
                }
            }

        }
    }

    /**
     * Returns the number of user recorded bugs.
     * 
     * @return The size of the list of user recorded bugs.
     */
    public int size() {
        return bugs.size();
    }

    /**
     * Used for parsing the loaded OpenStreetBugs. Splits a line according to
     * the needs for parsing the lines.
     */
    private List<String> splitLine(String line) {
        List<String> splits = new LinkedList<String>();

        String tmp = line;
        for (int ind = tmp.indexOf(","); ind >= 0; ind = tmp.indexOf(",")) {
            splits.add(tmp.substring(0, ind));
            tmp = tmp.substring(ind + 1);
        }
        splits.add(tmp);

        return splits;
    }

    /**
     * Used for parsing the loaded OpenStreetBugs. Extracts a bug from a line.
     * 
     * @param line
     *            The line to parse.
     * @return The parsed bug.
     */
    Bug extractBug(String line) {

        String description = "";
        double longitude = 0;
        double latitude = 0;

        List<String> lines = splitLine(line);

        if (lines.size() >= 5) {
            StringBuilder desc = new StringBuilder();
            desc.append(lines.get(3));
            for (int i = 4; i < lines.size() - 1; ++i) {
                desc.append(lines.get(i));
            }
            description = desc.toString();
            longitude = Double.parseDouble(lines.get(1).trim());
            latitude = Double.parseDouble(lines.get(2).trim());
        }
        // LogIt.w(">" + lines.get(lines.size() - 1) + "<");
        if (lines.get(lines.size() - 1).charAt(1) != '0') {
            return null;
        }

        return new Bug(description, new GeoPoint(latitude, longitude));
    }
}
