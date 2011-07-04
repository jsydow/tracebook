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

package de.fu.tracebook.core.data;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.xmlpull.v1.XmlSerializer;

import android.util.Xml;
import de.fu.tracebook.util.LogIt;

/**
 * Serialises a track to xml-file (actually a .tbt-file). The file format is
 * nearly equivalent to the OSM XML-format. The only difference is the link-tag
 * which is for meta media.
 */
public class DataSerializer {

    /**
     * Returns the complete String of the path to the track.tbt file of a track.
     * 
     * @param trackname
     *            The String of the track name
     * @return The path as String.
     */
    public static String getPathOfTrackTbTFile(String trackname) {
        return NewStorage.getTrackDirPath(trackname) + File.separator
                + "track.tbt";
    }

    /**
     * Generates a link tag for this medium.
     * 
     * @param serializer
     *            The initialized XmlSerialiser.
     */
    private void serializeMedia(XmlSerializer serializer, IDataMedia media) {
        try {
            serializer.startTag(null, "link");
            serializer.attribute(null, "href", media.getName());
            serializer.endTag(null, "link");
        } catch (IllegalArgumentException e) {
            LogIt.e("Should not happen");
        } catch (IllegalStateException e) {
            LogIt.e("Illegal state");
        } catch (IOException e) {
            LogIt.e("Could not serialize medium " + media.getName());
        }
    }

    private void serializeNode(XmlSerializer serializer, IDataNode dn) {
        DecimalFormatSymbols dfs = new DecimalFormatSymbols();
        dfs.setDecimalSeparator('.');
        DecimalFormat df = new DecimalFormat("0.0000000", dfs);

        try {
            serializer.startTag(null, "node");
            serializer.attribute(null, "lat",
                    df.format(dn.getCoordinates().getLatitude()));
            serializer.attribute(null, "lon",
                    df.format(dn.getCoordinates().getLongitude()));
            serializer.attribute(null, "id",
                    Long.toString(((NewNode) dn).getOutputId()));
            serializer.attribute(null, "timestamp", dn.getDatetime());
            serializer.attribute(null, "version", "1");

            serializeTags(serializer, dn.getTags());
            for (IDataMedia m : dn.getMedia()) {
                serializeMedia(serializer, m);
            }

            serializer.endTag(null, "node");

        } catch (IllegalArgumentException e) {
            LogIt.e("Should not happen");
        } catch (IllegalStateException e) {
            LogIt.e("Illegal state");
        } catch (IOException e) {
            LogIt.e("Could not serialize node");
        }
    }

    private void serializeTags(XmlSerializer serializer,
            Map<String, String> tags) {
        try {
            for (Entry<String, String> tag : tags.entrySet()) {

                serializer.startTag(null, "tag");
                serializer.attribute(null, "k", tag.getKey());
                serializer.attribute(null, "v", tag.getValue());
                serializer.endTag(null, "tag");
            }
        } catch (IllegalArgumentException e) {
            LogIt.e("Should not happen");
        } catch (IllegalStateException e) {
            LogIt.e("Illegal state");
        } catch (IOException e) {
            LogIt.e("Could not serialize tags");
        }
        return;
    }

    private void serializeWay(XmlSerializer serializer, IDataPointsList dpl) {
        try {
            List<IDataNode> nodes = dpl.getNodes();
            serializer.startTag(null, "way");
            serializer.attribute(null, "version", "1");
            serializer.attribute(null, "timestamp", dpl.getDatetime());
            serializer.attribute(null, "id",
                    Long.toString(StorageFactory.getStorage().getID()));

            for (IDataNode dn : nodes) {
                serializer.startTag(null, "nd");

                serializer.attribute(null, "ref",
                        Long.toString(((NewNode) dn).getOutputId()));

                serializer.endTag(null, "nd");
            }
            if (dpl.isArea() && !nodes.isEmpty()) {
                IDataNode lastNode = nodes.get(0);
                serializer.startTag(null, "nd");
                serializer.attribute(null, "ref",
                        Long.toString(lastNode.getId()));
                serializer.endTag(null, "nd");

                serializer.startTag(null, "tag");
                serializer.attribute(null, "k", "area");
                serializer.attribute(null, "v", "yes");
                serializer.endTag(null, "tag");
            }

            serializeTags(serializer, dpl.getTags());
            for (IDataMedia m : dpl.getMedia()) {
                serializeMedia(serializer, m);
            }

            serializer.endTag(null, "way");

        } catch (IllegalArgumentException e) {
            LogIt.e("Should not happen");
        } catch (IllegalStateException e) {
            LogIt.e("Illegal state");
        } catch (IOException e) {
            LogIt.e("Could not serialize way");
        }
    }

    /**
     * Open a file as FileOutputStream deleting any old existing file.
     * 
     * @param file
     *            The file to be opened.
     * @return The opened FileOutPutStream or null.
     */
    FileOutputStream openFile(File file) {
        try {
            if (file.exists()) {
                if (!file.delete()) {
                    LogIt.e("Deleting old file " + file.getName() + " failed");
                    return null;
                }
            }
            if (!file.createNewFile()) {
                LogIt.e("Creating new file " + file.getName() + " failed");
                return null;
            }

        } catch (IOException e) {
            LogIt.e("Could not create new file " + file.getPath());
        }
        FileOutputStream fileos = null;
        try {
            fileos = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            LogIt.e("Could not open new file " + file.getPath());
        }
        return fileos;
    }

    /**
     * Serialises a track.
     * 
     * @param track
     *            The track to serialise.
     */
    void serialize(NewTrack track) {
        String name = track.getName();

        File xmlfile = new File(getPathOfTrackTbTFile(name));

        FileOutputStream fileos = openFile(xmlfile);
        if (fileos == null) {
            return;
        }

        XmlSerializer serializer = Xml.newSerializer();

        try {
            serializer.setOutput(fileos, "UTF-8");
            serializer.startDocument(null, Boolean.valueOf(true));
            serializer.startTag(null, "osm");

            serializer.attribute(null, "version", "0.6");
            serializer.attribute(null, "generator", "TraceBook");

            for (IDataMedia m : track.getMedia()) {
                serializeMedia(serializer, m);
            }
            for (IDataNode dn : track.getNodes()) {
                serializeNode(serializer, dn);
            }
            List<IDataPointsList> waylist = track.getWays();
            for (IDataPointsList dpl : waylist) {
                for (IDataNode dn : dpl.getNodes()) {
                    serializeNode(serializer, dn);
                }
            }
            for (IDataPointsList dpl : waylist) {
                serializeWay(serializer, dpl);
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
