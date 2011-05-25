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
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.mapsforge.android.maps.GeoPoint;
import org.mapsforge.android.maps.OverlayWay;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xmlpull.v1.XmlSerializer;

import de.fu.tracebook.util.LogIt;

/**
 * WayPointList objects are any objects that consist of a series of nodes like
 * Areas and Ways.
 */
public class DataPointsList extends DataMapObject implements IDataPointsList {
    /**
     * Way node is a XML-node labeled "way". This method restores a
     * DataPointsList from such a XML-Node.
     * 
     * @param waynode
     *            A XML-node
     * @param allnodes
     *            All DataNodes that were already retrieved from that XML-file
     * @return The new DataPointsList
     */
    static DataPointsList deserialize(Node waynode, List<DataNode> allnodes) {
        // the returned DataPointsList
        DataPointsList ret = new DataPointsList(false);

        // get all attributes
        NamedNodeMap nodeattributes = waynode.getAttributes();
        // get time stamp
        ret.setDatetime(nodeattributes.getNamedItem("timestamp").getNodeValue());
        // get id
        // ret.setId(Integer.parseInt(nodeattributes.getNamedItem("id")
        // .getNodeValue()));

        // tags and media
        ret.deserializeMedia(waynode);
        ret.deserializeTags(waynode);

        // node references
        // for all <nd>-child nodes
        NodeList metanodes = waynode.getChildNodes();
        for (int i = 0; i < metanodes.getLength(); ++i) {

            // is <nd>-node?
            if (metanodes.item(i).getNodeName().equals("nd")) {

                // get id of the node referenced
                int nodeId = Integer.parseInt(metanodes.item(i).getAttributes()
                        .getNamedItem("ref").getNodeValue());
                // search for this node in allnodes
                ListIterator<DataNode> it = allnodes.listIterator();

                while (it.hasNext()) {
                    DataNode dn = it.next();
                    if (dn.getId() == nodeId) {
                        // remove from allnodes this node
                        it.remove();
                        // add
                        ret.nodes.add(dn);
                    }
                }
            }
        }

        // is this Way an Area?
        String value = ret.getTags().get("area");
        if (value != null) {
            if (value.equals("yes")) {
                ret.setArea(true);
            }
        }

        return ret;
    }

    /**
     * Route Object for MapsForge.
     */
    private OverlayWay overlayRoute;

    /**
     * Is this Object an Area?
     */
    protected boolean isArea;

    /**
     * The list of nodes of this object. First node is first element in this
     * list.
     */
    protected LinkedList<DataNode> nodes;

    /**
     * Constructor which initializes the Object as an Area.
     * 
     * @param isArea
     *            Whether object is an Area.
     */
    public DataPointsList(boolean isArea) {
        super();
        nodes = new LinkedList<DataNode>();
        overlayRoute = new OverlayWay();
        this.isArea = isArea;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu.tracebook.core.data.IDataPointsList#deleteNode(int)
     */
    public IDataNode deleteNode(int nodeId) {
        ListIterator<DataNode> lit = nodes.listIterator();
        DataNode dn;
        while (lit.hasNext()) {
            dn = lit.next();
            if (dn.getId() == nodeId) {
                lit.remove();
                return dn;
            }
        }

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu.tracebook.core.data.IDataPointsList#getNodeById(int)
     */
    public DataNode getNodeById(int nodeId) {
        for (DataNode dn : nodes) {
            if (dn.getId() == nodeId) {
                return dn;
            }
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu.tracebook.core.data.IDataPointsList#getNodes()
     */
    public List<IDataNode> getNodes() {
        return new LinkedList<IDataNode>(nodes);
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu.tracebook.core.data.IDataPointsList#getOverlayRoute()
     */
    public OverlayWay getOverlayRoute() {
        return overlayRoute;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu.tracebook.core.data.IDataPointsList#isArea()
     */
    public boolean isArea() {
        return isArea;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * de.fu.tracebook.core.data.IDataPointsList#newNode(org.mapsforge.android
     * .maps.GeoPoint)
     */
    public DataNode newNode(GeoPoint location) {
        DataNode dn = new DataNode(location, this);
        nodes.add(dn);
        return dn;
    }

    /**
     * Serializes all nodes sequentially.
     * 
     * @param serializer
     *            An XmlSerializer that is initialized.
     * @param shouldSerialiseMedia
     *            Should the media be also serialized? If yes then the resulting
     *            XML-file is not conform to OSM.
     */
    public void serializeNodes(XmlSerializer serializer,
            boolean shouldSerialiseMedia) {
        for (DataNode dn : nodes) {
            dn.serialize(serializer, shouldSerialiseMedia);
        }
        return;
    }

    /**
     * Serializes a way as way-tag. The nodes are referenced like in OSM using a
     * nd tag with a ref attribute.
     * 
     * @param serializer
     *            An XmlSerializer that is initialized.
     * @param shouldSerialiseMedia
     *            Should media also be serialized? Adding media means that the
     *            resulting XML-file is not valid to OSM.
     */
    public void serializeWay(XmlSerializer serializer,
            boolean shouldSerialiseMedia) {
        if (nodes.size() > 0) {
            try {
                serializer.startTag(null, "way");
                serializer.attribute(null, "version", "1");
                serializer.attribute(null, "timestamp", getDatetime());
                serializer.attribute(null, "id", Integer.toString(getId()));

                for (DataNode dn : nodes) {
                    serializer.startTag(null, "nd");

                    serializer.attribute(null, "ref",
                            Integer.toString(dn.getId()));

                    serializer.endTag(null, "nd");
                }
                if (this.isArea && nodes.size() > 0) {
                    DataNode lastNode = nodes.getFirst();
                    serializer.startTag(null, "nd");
                    serializer.attribute(null, "ref",
                            Integer.toString(lastNode.getId()));
                    serializer.endTag(null, "nd");

                    serializer.startTag(null, "tag");
                    serializer.attribute(null, "k", "area");
                    serializer.attribute(null, "v", "yes");
                    serializer.endTag(null, "tag");
                }

                serializeTags(serializer);
                if (shouldSerialiseMedia) {
                    serializeMedia(serializer);
                }

                serializer.endTag(null, "way");

            } catch (IllegalArgumentException e) {
                LogIt.e("WaySerialisation", "Should not happen");
            } catch (IllegalStateException e) {
                LogIt.e("WaySerialisation", "Illegal state");
            } catch (IOException e) {
                LogIt.e("WaySerialisation", "Could not serialize way");
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu.tracebook.core.data.IDataPointsList#setArea(boolean)
     */
    public void setArea(boolean isArea) {
        this.isArea = isArea;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * de.fu.tracebook.core.data.IDataPointsList#setOverlayRoute(org.mapsforge
     * .android.maps.OverlayWay)
     */
    public void setOverlayRoute(OverlayWay overlayRoute) {
        this.overlayRoute = overlayRoute;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * de.fu.tracebook.core.data.IDataPointsList#toGeoPointArray(org.mapsforge
     * .android.maps.GeoPoint)
     */
    public GeoPoint[] toGeoPointArray(GeoPoint additional) {
        GeoPoint[] tmp = new GeoPoint[nodes.size()
                + (additional != null ? 1 : 0) + (isArea() ? 1 : 0)];
        GeoPoint first = null;

        int i = 0;
        for (IDataNode n : nodes) {
            tmp[i] = n.getCoordinates();
            if (first == null)
                first = tmp[i];
            ++i;
        }

        if (additional != null)
            tmp[i++] = additional;

        if (isArea())
            tmp[i++] = tmp[0];

        return tmp;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * de.fu.tracebook.core.data.IDataPointsList#updateOverlayRoute(org.mapsforge
     * .android.maps.GeoPoint)
     */
    public void updateOverlayRoute(GeoPoint additional) {
        overlayRoute.setWayData(toGeoPointArray(additional));
    }
}
