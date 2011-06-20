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

import android.util.Xml;
import de.fu.tracebook.R;
import de.fu.tracebook.core.data.NewTrack;
import de.fu.tracebook.core.data.StorageFactory;
import de.fu.tracebook.core.overlays.BugOverlayItem;
import de.fu.tracebook.core.overlays.BugOverlayItem.BugType;
import de.fu.tracebook.gui.activity.MapsForgeActivity;
import de.fu.tracebook.util.LogIt;

public class BugManager {

    private static BugManager instance;

    public static BugManager getInstance() {
        if (instance == null) {
            instance = new BugManager();
        }
        return instance;
    }

    List<Bug> bugs = new ArrayList<Bug>();
    List<Bug> osbugs = new ArrayList<Bug>();

    private BugManager() {
        // do nothing
    }

    public void addBug(Bug bug) {
        bugs.add(bug);
    }

    public void downloadBugs(final MapsForgeActivity activity,
            final GeoPoint pos) {
        if (pos == null) {
            return;
        }

        (new Thread() {
            @Override
            public void run() {

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
                    try {
                        URL url = new URL(osbUrl);
                        URLConnection conn = url.openConnection();
                        InputStream in = conn.getInputStream();
                        BufferedReader reader = new BufferedReader(
                                new InputStreamReader(in, "UTF-8"));
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

                        return;
                    } catch (IOException e) {
                        LogIt.e("Download error: " + e.getMessage());
                    }
                }
                LogIt.popup(
                        activity,
                        activity.getResources()
                                .getString(
                                        R.string.alert_mapsforgeactivity_faileddownload));
            }
        }).start();
    }

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

    public void remove(Bug bug) {
        if (!bugs.remove(bug)) {
            osbugs.remove(bug);
        }
    }

    public void serializeBugs() {
        String path = StorageFactory.getStorage().getTrack().getTrackDirPath()
                + File.separator + "bugs.xml";

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

    Bug extractBug(String line) {

        String description = "";
        double longitude = 0;
        double latitude = 0;

        List<String> lines = splitLine(line);

        if (lines.size() >= 5) {
            description = lines.get(3);
            for (int i = 4; i < lines.size() - 1; ++i) {
                description += lines.get(i);
            }
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