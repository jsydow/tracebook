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

import de.fu.tracebook.util.LogIt;

/**
 * A node. A node can be a POI or an element of a list of way points that belong
 * to a Way or Area.
 */
public class DataNode extends DataMapObject implements IDataNode {

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
        DataNode ret;

        // get all attributes
        NamedNodeMap nodeattributes = nodenode.getAttributes();
        // get Latitude
        final double lat = Double.parseDouble(nodeattributes
                .getNamedItem("lat").getNodeValue());
        // get Longitude
        final double lon = Double.parseDouble(nodeattributes
                .getNamedItem("lon").getNodeValue());
        ret = new DataNode(new GeoPoint(lat, lon), null);
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
     * Constructs a DataNode of a Way with given coordinates.
     * 
     * @param coordinates
     *            The coordinates of this node.
     * @param parentWay
     *            The parent way.
     */
    public DataNode(GeoPoint coordinates, DataPointsList parentWay) {
        super();
        setLocation(coordinates);
        this.parentWay = parentWay;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu.tracebook.core.data.IDataNode#getCoordinates()
     */
    public GeoPoint getCoordinates() {
        if (coordinates == null) {
            return new GeoPoint(0, 0);
        }
        return coordinates;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu.tracebook.core.data.IDataNode#getDataPointsList()
     */
    public DataPointsList getDataPointsList() {
        return parentWay;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu.tracebook.core.data.IDataNode#getOverlayItem()
     */
    public OverlayItem getOverlayItem() {
        return overlayItem;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu.tracebook.core.data.IDataNode#isValid()
     */
    public boolean isValid() {
        return coordinates.getLatitudeE6() != 0
                || coordinates.getLongitudeE6() != 0;
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
            serializer.attribute(null, "lat",
                    df.format(this.getCoordinates().getLatitude()));
            serializer.attribute(null, "lon",
                    df.format(this.getCoordinates().getLongitude()));
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

    /*
     * (non-Javadoc)
     * 
     * @see
     * de.fu.tracebook.core.data.IDataNode#setDataPointsList(de.fu.tracebook
     * .core.data.DataPointsList)
     */
    public void setDataPointsList(DataPointsList way) {
        this.parentWay = way;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * de.fu.tracebook.core.data.IDataNode#setLocation(org.mapsforge.android
     * .maps.GeoPoint)
     */
    public void setLocation(GeoPoint gp) {
        if (gp != null) {
            this.coordinates = gp;
        } else {
            this.coordinates = new GeoPoint(0, 0);
        }

        if (overlayItem != null)
            overlayItem.setPoint(coordinates);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * de.fu.tracebook.core.data.IDataNode#setOverlayItem(org.mapsforge.android
     * .maps.OverlayItem)
     */
    public void setOverlayItem(OverlayItem overlayItem) {
        this.overlayItem = overlayItem;
        this.overlayItem.setPoint(coordinates);
    }
}
