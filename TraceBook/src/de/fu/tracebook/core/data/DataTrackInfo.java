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
import java.io.FileOutputStream;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlSerializer;

import de.fu.tracebook.util.LogIt;
import android.util.Xml;

/**
 * A class that simply holds information of a track. It is a class that only
 * provides information but cannot edit them. Use deserialize() to get such an
 * object.
 */
public class DataTrackInfo {
    private static final String LOG_TAG = "DataTrackInfo";

    /**
     * Deserializes a DataTrackInfo from an info.xml file of a track.
     * 
     * @param trackname
     *            Name of a track as String.
     * @return The info to this specific track or null if there is not such a
     *         track.
     */
    public static DataTrackInfo deserialize(String trackname) {
        DataTrackInfo info = new DataTrackInfo();
        info.name = trackname;

        File trackinfo = new File(DataTrack.getTrackDirPath(trackname)
                + File.separator + "info.xml");
        if (trackinfo.isFile()) {
            DocumentBuilderFactory factory = DocumentBuilderFactory
                    .newInstance();

            try {
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document dom = builder.parse(trackinfo);

                // get <trackinfo>-element
                Element trackinfoelement = dom.getDocumentElement(); // root-element
                if (trackinfoelement == null)
                    throw new SAXException();

                NodeList nodeelements = trackinfoelement
                        .getElementsByTagName("data");
                if (nodeelements == null)
                    throw new SAXException();
                // for each child
                for (int i = 0; i < nodeelements.getLength(); ++i) {

                    // get key and value attribute
                    NamedNodeMap nnm = nodeelements.item(i).getAttributes();
                    String key = nnm.getNamedItem("key").getNodeValue();
                    String value = nnm.getNamedItem("value").getNodeValue();

                    // what is key?
                    if (key != null && value != null) {
                        if (key.equals("timestamp")) {
                            info.timestamp = value;
                        } else if (key.equals("comment")) {
                            info.comment = value;
                        } else if (key.equals("pois")) {
                            info.numberOfPOIs = Integer.parseInt(value);
                        } else if (key.equals("ways")) {
                            info.numberOfWays = Integer.parseInt(value);
                        } else if (key.equals("media")) {
                            info.numberOfMedia = Integer.parseInt(value);
                        }
                    } else {
                        LogIt.w("DeserialisingDataTrackInfo",
                                "XML-file is invalid. A data-node has no key-attribute.");
                    }
                }

            } catch (ParserConfigurationException e) {
                LogIt.e("DeserialisingDataTrackInfo", "This should not happen!");
            } catch (SAXException e) {
                LogIt.e("DeserialisingDataTrackInfo", "XML-file is not valid.!");
            } catch (IOException e) {
                LogIt.e("DeserialisingDataTrackInfo",
                        "Error while reading XML file!");
            }
        } else {
            info.comment = "";
        }
        return info;
    }

    private String comment;

    private String name;
    private int numberOfMedia;
    private int numberOfPOIs;
    private int numberOfWays;
    private String timestamp;

    private DataTrackInfo() {
        timestamp = "";
        comment = "";
        name = "";
        numberOfPOIs = -1;
        numberOfWays = -1;
        numberOfMedia = -1;
    }

    /**
     * An initializing constructor.
     * 
     * @param timestamp
     *            The time stamp of a track.
     * @param name
     *            The name of a track (Its directory name).
     * @param comment
     *            The comment of a track.
     * @param numberOfPOIs
     *            The number of points of interest a track has.
     * @param numberOfWays
     *            The number of ways a track has.
     * @param numberOfMedia
     *            The total number of media a track has.
     */
    DataTrackInfo(String name, String timestamp, String comment,
            int numberOfPOIs, int numberOfWays, int numberOfMedia) {
        this.timestamp = timestamp;
        this.name = name;
        this.comment = comment;
        this.numberOfPOIs = numberOfPOIs;
        this.numberOfWays = numberOfWays;
        this.numberOfMedia = numberOfMedia;
    }

    /**
     * Getter-method for the comment of a track.
     * 
     * @return The comment as String.
     */
    public String getComment() {
        return comment;
    }

    /**
     * Getter-method for the name of a track.
     * 
     * @return The name as String.
     */
    public String getName() {
        return name;
    }

    /**
     * Getter-method for the number of media of a track.
     * 
     * @return The number of media.
     */
    public int getNumberOfMedia() {
        return numberOfMedia;
    }

    /**
     * Getter-method for the number of POIs of a track.
     * 
     * @return The number of POIs.
     */
    public int getNumberOfPOIs() {
        return numberOfPOIs;
    }

    /**
     * Getter-method for the number of ways of a track.
     * 
     * @return The number of ways.
     */
    public int getNumberOfWays() {
        return numberOfWays;
    }

    /**
     * Getter-method for the time stamp of a track.
     * 
     * @return The time stamp as String.
     */
    public String getTimestamp() {
        return timestamp;
    }

    /**
     * Serializes the track info. Creates a info.xml file in the directory of
     * the track.
     */
    public void serialize() {
        File file = new File(DataTrack.getTrackDirPath(name) + File.separator
                + "info.xml");
        FileOutputStream fileos = DataTrack.openFile(file);
        if (fileos == null) {
            return;
        }
        XmlSerializer serializer = Xml.newSerializer();

        try {
            serializer.setOutput(fileos, "UTF-8");
            serializer.startDocument(null, Boolean.valueOf(true));
            serializer.startTag(null, "info");

            serializer.startTag(null, "data");
            serializer.attribute(null, "key", "timestamp");
            serializer.attribute(null, "value", timestamp);
            serializer.endTag(null, "data");

            serializer.startTag(null, "data");
            serializer.attribute(null, "key", "comment");
            serializer.attribute(null, "value", comment);
            serializer.endTag(null, "data");

            serializer.startTag(null, "data");
            serializer.attribute(null, "key", "pois");
            serializer.attribute(null, "value", Integer.toString(numberOfPOIs));
            serializer.endTag(null, "data");

            serializer.startTag(null, "data");
            serializer.attribute(null, "key", "ways");
            serializer.attribute(null, "value", Integer.toString(numberOfWays));
            serializer.endTag(null, "data");

            serializer.startTag(null, "data");
            serializer.attribute(null, "key", "media");
            serializer
                    .attribute(null, "value", Integer.toString(numberOfMedia));
            serializer.endTag(null, "data");

            serializer.endTag(null, "info");
            serializer.flush();
        } catch (IllegalArgumentException e) {
            LogIt.e(LOG_TAG, e.getMessage());
        } catch (IllegalStateException e) {
            LogIt.e(LOG_TAG, e.getMessage());
        } catch (IOException e) {
            LogIt.e(LOG_TAG, e.getMessage());
        } finally {
            try {
                fileos.close();
            } catch (IOException e) {
                LogIt.e(LOG_TAG, "Unable to read file: " + e.getMessage());
            }
        }

    }

}
