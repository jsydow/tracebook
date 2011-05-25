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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.mapsforge.android.maps.GeoPoint;
import org.mapsforge.android.maps.OverlayItem;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlSerializer;

import android.util.Xml;
import de.fu.tracebook.util.LogIt;

/**
 * A Track. Consists of Ways, Areas, POIs and additional Media. A Track is a
 * "tracking session". Normally the user will edit just one Track for one use of
 * the program. All data he collects with this one usage in then grouped in a
 * Track. A Track is not like a simple Way from place A to B but can contain it.
 */
public class DataTrack extends DataMediaHolder {

    /**
     * Deletes a Track with all its contents from the devices memory.
     * 
     * @param trackname
     *            The name of the track/directory.
     */
    public static void delete(String trackname) {
        DataStorage.deleteDirectory(new File(getTrackDirPath(trackname)));
    }

    /**
     * This method loads a Track from the devices memory. It uses the
     * appropriate ContentProvider. Note: Currently a stub. Note: The parameter
     * name may change if another name is better suited for retrieving the Track
     * correctly.
     * 
     * @param name
     *            The name of the Track as stored on the memory.
     * @return The deserialized DataTrack object or null if such a Track does
     *         not exist.
     */
    public static DataTrack deserialize(String name) {

        // cache all nodes
        List<DataNode> allnodes = new LinkedList<DataNode>();
        // XML-file
        File track = new File(getPathOfTrackTbTFile(name));
        // Track that should be filled/initialized
        DataTrack ret = new DataTrack(track.getParentFile().getName());
        DataTrackInfo info = DataTrackInfo.deserialize(name);
        if (info != null) {
            ret.setComment(info.getComment());
            ret.setDatetime(info.getTimestamp());
        }

        if (track.isFile()) {
            DocumentBuilderFactory factory = DocumentBuilderFactory
                    .newInstance();

            try {
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document dom = builder.parse(track);

                // get <osm>-element
                Element osmelement = dom.getDocumentElement(); // root-element
                if (osmelement == null)
                    throw new SAXException();

                // get all nodes
                NodeList nodeelements = osmelement.getElementsByTagName("node");
                if (nodeelements == null)
                    throw new SAXException();
                for (int i = 0; i < nodeelements.getLength(); ++i) {
                    // generate a node
                    allnodes.add(DataNode.deserialize(nodeelements.item(i)));
                }

                // all ways
                NodeList wayelements = osmelement.getElementsByTagName("way");
                if (wayelements == null)
                    throw new SAXException();
                for (int i = 0; i < wayelements.getLength(); ++i) {
                    // generate ways
                    DataPointsList dpl = DataPointsList.deserialize(
                            wayelements.item(i), allnodes);
                    // add them
                    ret.addWay(dpl);
                }

                // all media
                NodeList medianodes = osmelement.getElementsByTagName("link");
                if (medianodes == null)
                    throw new SAXException();
                for (int i = 0; i < medianodes.getLength(); ++i) {
                    // get attributes of "link"-node
                    NamedNodeMap attributes = medianodes.item(i)
                            .getAttributes();
                    // path to medium is value of href-attribute
                    Node path = attributes.getNamedItem("href");
                    // path to medium is path to track directory + media name
                    ret.addMedia(DataMedia.deserialize(ret.getTrackDirPath()
                            + File.separator + path.getNodeValue()));
                }

                // nodes -> POIs, all nodes that are still in allnodes are POIs
                // DataNode.deserialize erase those, that are part of a way
                ret.getNodes().addAll(allnodes);

            } catch (IOException e) {
                LogIt.e("TrackDeserialisation", "Error while reading XML file.");
                return null;
            } catch (ParserConfigurationException e) {
                LogIt.e("TrackDeserialisation", "XML parser doesn't work.");
                return null;
            } catch (SAXException e) {
                LogIt.e("TrackDeserialisation", "Error while parsing XML file.");
                return null;
            }
        } else {
            LogIt.e("TrackDeserialisation",
                    "Track was not found. Path should be " + track.getPath());
            return null;
        }

        return ret;
    }

    /**
     * Checks if a track is already saved on the SD-card. It checks whether
     * there is a file .../TraceBook/trackfilename/track.tbt which only exists
     * if the track was saved some time before.
     * 
     * @param name
     *            The name of th track to check.
     * @return True if there is such a track, false otherwise.
     */
    public static boolean exists(String name) {
        File trackfile = new File(getPathOfTrackTbTFile(name));
        return trackfile.exists();
    }

    /**
     * Creates a time stamp of the current time which can be used as a filename.
     * 
     * @return The time stamp String.
     */
    public static String getFilenameCompatibleTimeStamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        return sdf.format(new Date());
    }

    /**
     * Returns the complete String of the path to the track.tbt file of a track.
     * 
     * @param trackname
     *            The String of the track name
     * @return The path as String.
     */
    public static String getPathOfTrackTbTFile(String trackname) {
        return getTrackDirPath(trackname) + File.separator + "track.tbt";
    }

    /**
     * Completes a track directory name to a complete path. Note: Do not changed
     * as this method is misused somewhere.
     * 
     * @param dir
     *            Name of the track directory
     * @return The complete path to the track directory.
     */
    public static String getTrackDirPath(String dir) {
        return DataStorage.getTraceBookDirPath() + File.separator + dir;
    }

    /**
     * Renames a Track on the devices memory.
     * 
     * @param oldTrackName
     *            Name of the track as it is on the devices memory.
     * @param newTrackName
     *            The new name of the track.
     * @return Returns 0 if renaming was successful, -1 if track could not be
     *         found, -2 if there is a track with the new track name and -3 if
     *         the renaming fails otherwise.
     */
    public static int rename(String oldTrackName, String newTrackName) {
        DataTrack oldTrack = new DataTrack(oldTrackName);
        return oldTrack.renameTrack(newTrackName);
    }

    /**
     * Open a file as FileOutputStream deleting any old existing file.
     * 
     * @param file
     *            The file to be opened.
     * @return The opened FileOutPutStream or null.
     */
    static FileOutputStream openFile(File file) {
        try {
            if (file.exists()) {
                if (!file.delete()) {
                    LogIt.e("OpenFile", "Deleting old file " + file.getName()
                            + " failed");
                    return null;
                }
            }
            if (!file.createNewFile()) {
                LogIt.e("OpenFile", "Creating new file " + file.getName()
                        + " failed");
                return null;
            }

        } catch (IOException e) {
            LogIt.e("OpenFile", "Could not create new file " + file.getPath());
        }
        FileOutputStream fileos = null;
        try {
            fileos = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            LogIt.e("OpenFile", "Could not open new file " + file.getPath());
        }
        return fileos;
    }

    /**
     * A Comment of this track.
     */
    private String comment;

    /**
     * The currently edited Way.
     */
    private DataPointsList currentWay;

    /**
     * This list stores all discarded OverlayItems, MapsActivity will poll and
     * remove them.
     */
    private Queue<OverlayItem> invalidItems;

    /**
     * Display name of the Track. Serves as id and should therefore be unique.
     * Is initialized with the DateTime of the first creation of this object.
     */
    private String name;

    /**
     * All POI's of this track.
     */
    private List<DataNode> nodes;

    /**
     * All Ways and Areas of this track.
     */
    private List<DataPointsList> ways;

    /**
     * Constructor which initializes the Track, each Track must have a Date
     * time.
     */
    public DataTrack() {
        super();
        ways = new LinkedList<DataPointsList>();
        nodes = new LinkedList<DataNode>();
        invalidItems = new ConcurrentLinkedQueue<OverlayItem>();
        this.name = getFilenameCompatibleTimeStamp();
        this.comment = "";
        createNewTrackFolder();
    }

    /**
     * Initializing constructor.
     * 
     * @param name
     *            The display and folder name of the Track.
     */
    public DataTrack(String name) {
        this();
        this.name = name;
    }

    /**
     * Initializing constructor. Note: comment is not implemented yet.
     * 
     * @param name
     *            See constructor DataTrack(Datetime, Name).
     * @param comment
     *            Comment that may be displayed for this Track.
     */
    public DataTrack(String name, String comment) {
        this(name);
        this.comment = comment;
    }

    /**
     * Returns a list of {@link OverlayItem}s whose {@link DataNode} does no
     * longer exist. For efficiency reasons they are stored in a list, so when
     * e.g. a way is deleted, the Overlay will not be redrawn for every deleted
     * waypoint, but receive a notification that invalid OverlayItems exist when
     * the removal procedure has finished.
     * 
     * @return The list of invalid OverlayItems. It will be cleared by this
     *         call.
     */
    public Collection<OverlayItem> clearInvalidItems() {
        Collection<OverlayItem> tmp = invalidItems;
        invalidItems = new ConcurrentLinkedQueue<OverlayItem>();
        return tmp;
    }

    /**
     * Creates new folder in .../TraceBook for this Track. Such a directory must
     * exist when track is serialized.
     */
    public void createNewTrackFolder() {
        File dir = new File(DataStorage.getTraceBookDirPath() + File.separator
                + name);
        if (!dir.isDirectory()) {
            if (!dir.mkdir()) {
                LogIt.e("DataStorage", "Could not create new track folder "
                        + name);
            }
        }
    }

    /**
     * This method deletes a Node (POI) of this Track or a node of one of the
     * ways of this track from the devices memory and the working memory. If
     * this node does not exist nothing is done.
     * 
     * @param id
     *            The id of the POI to delete.
     * @return A reference to the deleted DataNode object if it exists, null
     *         otherwise..
     */
    public IDataNode deleteNode(int id) {
        ListIterator<DataNode> lit = nodes.listIterator();
        DataNode dn;
        while (lit.hasNext()) {
            dn = lit.next();
            if (dn.getId() == id) {
                if (dn.getOverlayItem() != null)
                    invalidItems.add(dn.getOverlayItem());
                lit.remove();
                return dn;
            }
        }

        for (DataPointsList dpl : getWays()) {
            IDataNode dldn = dpl.deleteNode(id);
            if (dldn != null) { // deleted
                if (dldn.getOverlayItem() != null)
                    invalidItems.add(dldn.getOverlayItem());
                return dldn;
            }
        }
        return null;
    }

    /**
     * This method deletes a Way of this Track from the devices memory and the
     * working memory. If the Way does not exist nothing is done.
     * 
     * @param id
     *            The id of the Way to delete.
     */
    public void deleteWay(int id) {
        ListIterator<DataPointsList> lit = ways.listIterator();
        DataPointsList dpl;
        while (lit.hasNext()) {
            dpl = lit.next();
            if (dpl.getId() == id) {
                lit.remove();
                break;
            }
        }
    }

    /**
     * Getter-method.
     * 
     * @return The comment of the Track.
     */
    public String getComment() {
        return comment;
    }

    /**
     * Getter-method. The currently edited Way.
     * 
     * @return The current Way. Current Way can be null if not initialized.
     */
    public DataPointsList getCurrentWay() {
        return currentWay;
    }

    /**
     * Search the whole track for an DataMapObject by id. This may be a DataNode
     * or DataPointsList.
     * 
     * @param id
     *            The id of the DataMapObject that is being searched for.
     * @return The DataMapObject where get_id()==id or null if there is not such
     *         an object.
     */
    public DataMapObject getDataMapObjectById(int id) {

        DataMapObject res = getNodeById(id);
        if (res != null) {
            return res;
        }

        for (DataPointsList dpl : getWays()) {
            res = dpl.getNodeById(id);
            if (res != null) {
                return res;
            }
        }

        res = getPointsListById(id);
        return res;
    }

    /**
     * Getter-method.
     * 
     * @return The name of the Track.
     */
    public String getName() {
        return name;
    }

    /**
     * Get a DataNode with a given id.
     * 
     * @param id
     *            The id of the DataNode
     * @return The DataNode or null if there is none with such an id.
     */
    public DataNode getNodeById(int id) {
        for (DataNode dn : nodes) {
            if (dn.getId() == id) {
                return dn;
            }
        }

        for (DataPointsList dpl : ways) {
            DataNode dn = dpl.getNodeById(id);
            if (dn != null)
                return dn;
        }

        return null;
    }

    /**
     * Tries to find the {@link DataNode} containing the given
     * {@link OverlayItem}.
     * 
     * @param item
     *            The OverlayItem that should be searched for.
     * @return The DataNode containing the OverlayItem, or null if no DataNode
     *         with the OverlayItem was found
     */
    public DataNode getNodeByOverlayItem(OverlayItem item) {
        if (item == null)
            return null;

        for (DataNode dn : nodes)
            if (item.equals(dn.getOverlayItem()))
                return dn;

        for (DataPointsList dpl : ways)
            for (DataNode dn : dpl.getNodes())
                if (item.equals(dn.getOverlayItem()))
                    return dn;

        return null;
    }

    /**
     * Getter-method that returns a list of all nodes. The returned List is the
     * one stored in this object. Changing the returned List will therefore
     * change this list
     * 
     * @return All POI's of this Track. (not null)
     */
    public List<DataNode> getNodes() {
        return nodes;
    }

    /**
     * Get a DataPointsList with a given id.
     * 
     * @param id
     *            The id of the DataPointsList
     * @return The DataPointsList or null if there is none with such an id.
     */
    public DataPointsList getPointsListById(int id) {
        for (DataPointsList dpl : ways) {
            if (dpl.getId() == id) {
                return dpl;
            }
        }
        return null;
    }

    /**
     * Returns the complete absolute path to this Track directory.
     * 
     * @return path to the track directory
     */
    public String getTrackDirPath() {
        return DataStorage.getTraceBookDirPath() + File.separator + name;
    }

    /**
     * Getter-method that returns a list of all Ways. The returned List is the
     * one stored in this object. Changing the returned List will therefore
     * change this list
     * 
     * @return All ways of this Track. (not null)
     */
    public List<DataPointsList> getWays() {
        return ways;
    }

    /**
     * Marks an {@link OverlayItem} as invalid so it can be removed later.
     * 
     * @param overlayItem
     *            The outdated OverlayItem
     */

    public void invalidateOverlayItem(OverlayItem overlayItem) {
        if (overlayItem != null)
            invalidItems.add(overlayItem);
    }

    /**
     * Create a new Node (i.e. POI) and add it to the Track
     * 
     * @param coordinates
     *            The GeoPoint object to be used for constructing the new Node.
     * @return The newly created POI.
     */
    public DataNode newNode(GeoPoint coordinates) {
        DataNode dn = new DataNode(coordinates, null);
        nodes.add(dn);
        return dn;
    }

    /**
     * Create a new Way/Area in this Track.
     * 
     * @return The newly created Way.
     */
    public DataPointsList newWay() {
        DataPointsList dpl = new DataPointsList();
        ways.add(dpl);
        return dpl;
    }

    /**
     * This method saves a String to a .txt file and generates a
     * DataMedia-object which can be added to any DataMediaHolder.
     * 
     * @param text
     *            The text to save
     * @return DataMedia object which references the Text just saved.
     */
    public DataMedia saveText(String text) {
        File txtfile = new File(getTrackDirPath() + File.separator
                + getFilenameCompatibleTimeStamp() + ".txt");
        try {
            if (txtfile.createNewFile()) {
                BufferedWriter buf = new BufferedWriter(new FileWriter(txtfile));
                buf.write(text);
                buf.close();
            } else {
                LogIt.w("MediaSavingText",
                        "Text file with this timestamp already exists.");
            }
        } catch (IOException e) {
            LogIt.e("MediaSavingText", "Error while writing text file.");
            return null;
        }
        return new DataMedia(txtfile.getParent(), txtfile.getName());
    }

    /**
     * Serializes a track to a XML-file stored on the SD-card in folder
     * TraceBook/<track name>. Also serializes all Media. The XML-file is
     * therefore not OSM compatible.
     */
    public void serialize() {
        serialize(true);
    }

    /**
     * Serializes a track to a XML-file stored on the SD-card in folder
     * TraceBook/<track name>.
     * 
     * @param shouldSerialiseMedia
     *            Should media also be serialized? Adding media means that the
     *            resulting XML-file is not valid to OSM.
     */
    public void serialize(boolean shouldSerialiseMedia) {
        int totalMedia = media.size();

        LogIt.d("DataTrack", "Ways: " + ways.size() + ", POIs: " + nodes.size());

        if (!(new File(getTrackDirPath()).isDirectory())) {
            createNewTrackFolder();
        }

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

            for (DataNode dn : nodes) {
                dn.serialize(serializer, shouldSerialiseMedia);
                totalMedia += dn.getMedia().size();
            }
            for (DataPointsList dpl : ways) {
                dpl.serializeNodes(serializer, shouldSerialiseMedia);
                totalMedia += dpl.getMedia().size();
            }
            for (DataPointsList dpl : ways) {
                dpl.serializeWay(serializer, shouldSerialiseMedia);
            }

            serializeMedia(serializer);

            serializer.endTag(null, "osm");
            serializer.flush();
        } catch (IllegalArgumentException e) {
            LogIt.e("DataTrackSerialisation",
                    "Should not happen. Internal error.");
        } catch (IllegalStateException e) {
            LogIt.e("DataTrackSerialisation",
                    "Should not happen. Internal error.");
        } catch (IOException e) {
            LogIt.e("DataTrackSerialisation", "Error while reading file.");
        } finally {
            try {
                fileos.close();
            } catch (IOException e) {
                LogIt.e("TrackInfo", "Error closing file: " + e.getMessage());
            }
        }

        (new DataTrackInfo(name, getDatetime(), comment, nodes.size(),
                ways.size(), totalMedia)).serialize();

    }

    /**
     * Setter-method.
     * 
     * @param comment
     *            The new comment of the Track.
     */
    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * Sets a Way as currently edited Way. Setter-method.
     * 
     * @param currentWay
     *            The new currently edited Way.
     * @return Returns the parameter currentWay for further use.
     */
    public DataPointsList setCurrentWay(DataPointsList currentWay) {
        this.currentWay = currentWay;
        return currentWay;
    }

    /**
     * Setter-method. Renames a Track in the devices and working memory.
     * 
     * @param newname
     *            The new name of the DataTrack
     * @return Returns 0 if renaming was successful, -1 if track could not be
     *         found, -2 if there is a track with the new track name and -3 if
     *         the renaming fails otherwise.
     */
    public int setName(String newname) {
        int res = renameTrack(newname);
        if (res == 0) {
            this.name = newname;
        }
        return res;
    }

    /**
     * Adds a way to the ways of this Track.
     * 
     * @param way
     *            the DataPointsList to be added
     */
    private void addWay(DataPointsList way) {
        ways.add(way);
    }

    /**
     * Renames a Track on the devices memory.
     * 
     * @param newname
     *            The new name of the Track
     * @return Returns 0 if renaming was successful, -1 if track could not be
     *         found, -2 if there is a track with the new track name and -3 if
     *         the renaming fails otherwise.
     */
    private int renameTrack(String newname) {
        File trackdir = new File(getTrackDirPath());
        if (trackdir.isDirectory()) {
            File newtrackdir = new File(getTrackDirPath(newname));
            if (!newtrackdir.isDirectory()) {
                if (!trackdir.renameTo(newtrackdir)) {
                    LogIt.w("RenamingTrack", "Could not rename Track.");
                    return -3;
                }
            } else {
                LogIt.w("RenamingTrack",
                        "Track of new trackname already exists.");
                return -2;
            }
        } else {
            LogIt.w("RenamingTrack", "Could not find Track " + getName());
            return -1;
        }
        return 0;
    }
}
