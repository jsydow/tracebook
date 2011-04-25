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

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import org.mapsforge.android.maps.GeoPoint;
import org.mapsforge.android.maps.OverlayItem;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xmlpull.v1.XmlSerializer;

import android.location.Location;
import de.fu.tracebook.gui.activity.MapsForgeActivity;
import de.fu.tracebook.util.LogIt;

/**
 * A node. A node can be a POI or an element of a list of way points that belong
 * to a Way or Area.
 */
public class DataNode extends DataMapObject {

    /**
     * 'nodenode" is a XML-node labeled "node". This method restores a DataNode
     * from such a XML-Node.
     * 
     * @param nodenode
     *            The XML-node
     * @return The new DataNode-object
     */
    public static DataNode deserialize(Node nodenode) {
        // the returned DataNode, must be initialized
        DataNode ret = new DataNode();

        // get all attributes
        NamedNodeMap nodeattributes = nodenode.getAttributes();
        // get Latitude
        final double lat = Double.parseDouble(nodeattributes
                .getNamedItem("lat").getNodeValue());
        // get Longitude
        final double lon = Double.parseDouble(nodeattributes
                .getNamedItem("lon").getNodeValue());
        ret.setLocation(new GeoPoint(lat, lon));
        // get time stamp
        ret.setDatetime(nodeattributes.getNamedItem("timestamp").getNodeValue());
        // get id
        // ret.setId(Integer.parseInt(nodeattributes.getNamedItem("id")
        // .getNodeValue()));

        // tags and media
        ret.deserializeMedia(nodenode);
        ret.deserializeTags(nodenode);

        return ret;
    }

    /**
     * The {@link GeoPoint} object associated with this node.
     */
    private GeoPoint coordinates;

    /**
     * The overlay Item used by the GUI, associated with a certain POI.
     */
    private OverlayItem overlayItem;

    /**
     * The {@link DataPointsList} object associated with this node. Null if this
     * node is not part of a DataPointsList.
     */
    private DataPointsList parentWay;

    /**
     * Default constructor. Longitude and Latitude stay unchanged.
     */
    public DataNode() {
        super();
        setLocation(new Location(""));
    }

    /**
     * This constructor initializes the Latitude and Longitude with data from a
     * {@link GeoPoint} object.
     * 
     * @param coordinates
     *            The {@link GeoPoint} of the new node. Initializes latitude and
     *            longitude.
     */
    public DataNode(GeoPoint coordinates) {
        super();
        setLocation(coordinates);
    }

    /**
     * Constructs a DataNode of a Way with given coordinates.
     * 
     * @param coordinates
     *            The coordinates of this node.
     * @param parentWay
     *            The parent way.
     */
    public DataNode(GeoPoint coordinates, DataPointsList parentWay) {
        super();
        this.coordinates = coordinates;
        this.parentWay = parentWay;
    }

    /**
     * This constructor initializes the Latitude and Longitude with data from a
     * Location object.
     * 
     * @param location
     *            The Location of the new node. Initializes latitude and
     *            longitude.
     */
    public DataNode(Location location) {
        super();
        setLocation(location);
    }

    /**
     * This constructor initializes the Latitude and Longitude with data from a
     * Location object, as well as the DataPointsList that contains this node.
     * 
     * @param loc
     *            The {@link Location} of the new node. Initializes latitude and
     *            longitude.
     * @param way
     *            The {@link DataPointsList} this node is belongs to
     */
    public DataNode(Location loc, DataPointsList way) {
        super();
        setLocation(loc);
        setDataPointsList(way);
    }

    /**
     * If this node is part of a {@link DataPointsList}, this function will
     * return a reference to this object. Otherwise the return value is null.
     * 
     * @return The DataPointsList this point is in. (can be null)
     */
    public DataPointsList getDataPointsList() {
        return parentWay;
    }

    /**
     * Getter-method.
     * 
     * @return The latitude.
     */
    public double getLat() {
        if (coordinates == null)
            return 0;
        return coordinates.getLatitude();
    }

    /**
     * Getter-method.
     * 
     * @return The longitude.
     */
    public double getLon() {
        if (coordinates == null)
            return 0;
        return coordinates.getLongitude();
    }

    /**
     * Getter-method.
     * 
     * @return A reference to the OverlayItem that is drawn and handled by
     *         MapsForge's overlay.
     */
    public OverlayItem getOverlayItem() {
        return overlayItem;
    }

    /**
     * A Point may have been added uninitialized, in this case it does not
     * contain any valid positional data - this may be added later once a GPS
     * fix is obtained.
     * 
     * @return true if the Node contains data of a valid GPS fix
     */
    public boolean isValid() {
        return coordinates != null;
    }

    /**
     * Serializes a node using a XmlSerializer. It generates a <node>-tag.
     * 
     * @param serializer
     *            An XmlSerializer that is initialized.
     * @param shouldSerialiseMedia
     *            Should media also be serialized? Adding media means that the
     *            resulting XML-file is not valid to OSM.
     */
    public void serialize(XmlSerializer serializer, boolean shouldSerialiseMedia) {
        DecimalFormatSymbols dfs = new DecimalFormatSymbols();
        dfs.setDecimalSeparator('.');
        DecimalFormat df = new DecimalFormat("0.0000000", dfs);

        try {
            serializer.startTag(null, "node");
            serializer.attribute(null, "lat", df.format(this.getLat()));
            serializer.attribute(null, "lon", df.format(this.getLon()));
            serializer.attribute(null, "id", Integer.toString(this.getId()));
            serializer.attribute(null, "timestamp", this.getDatetime());
            serializer.attribute(null, "version", "1");

            serializeTags(serializer);
            if (shouldSerialiseMedia) {
                serializeMedia(serializer);
            }

            serializer.endTag(null, "node");

        } catch (IllegalArgumentException e) {
            LogIt.e("NodeSerialisation", "Should not happen");
        } catch (IllegalStateException e) {
            LogIt.e("NodeSerialisation", "Illegal state");
        } catch (IOException e) {
            LogIt.e("NodeSerialisation", "Could not serialize node");
        }
    }

    /**
     * Associates this DataNode with a {@link DataPointsList}, meaning this
     * point is part of the way.
     * 
     * @param way
     *            The way that contains this point.
     */
    public void setDataPointsList(DataPointsList way) {
        this.parentWay = way;
    }

    /**
     * Sets the position of this DataNode to the location of the GeoPoint.
     * 
     * @param gp
     *            New position of the node.
     */
    public void setLocation(GeoPoint gp) {
        this.coordinates = gp;

        if (overlayItem != null)
            overlayItem.setPoint(coordinates);
    }

    /**
     * Set the latitude and longitude to the position given by the Location
     * object which is received from the GPS module.
     * 
     * @param location
     *            The Location object that the GPS module delivers.
     */
    public void setLocation(Location location) {
        if (location == null)
            this.coordinates = null;
        else
            setLocation(new GeoPoint(location.getLatitude(),
                    location.getLongitude()));
    }

    /**
     * Set the {@link OverlayItem}, used by {@link MapsForgeActivity}.
     * 
     * @param overlayItem
     *            The new OverlayItem.
     */
    public void setOverlayItem(OverlayItem overlayItem) {
        this.overlayItem = overlayItem;
        this.overlayItem.setPoint(coordinates);
    }

    /**
     * Converts the DataNode to a GeoPoint.
     * 
     * @return A GeoPoint with the coordinates of the DataNode.
     */
    public GeoPoint toGeoPoint() {
        return coordinates;
    }

    /**
     * Returns a String with id and longitude and latitude of this node.
     * 
     * @return String "id=<id> (<longitude>, <latitude>)"
     */
    @Override
    public String toString() {
        return "id=" + getId() + " (" + getLon() + ", " + getLat() + ")";
    }
}
