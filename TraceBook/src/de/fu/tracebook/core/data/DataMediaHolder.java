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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xmlpull.v1.XmlSerializer;

/**
 * Any object that can have media attached. Media can be added and retrieved.
 * This class is abstract.
 */
public abstract class DataMediaHolder implements IDataMediaHolder {
    /**
     * Creates a time stamp of the current time formatted according to W3C.
     * 
     * @return A time stamp String.
     */
    private static String getW3CFormattedTimeStamp() {
        SimpleDateFormat sdf = new SimpleDateFormat(
                "yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        return sdf.format(new Date());
    }

    /**
     * The Creation time.
     */
    private String datetime;

    /**
     * The list of Media.
     */
    protected List<DataMedia> media;

    /**
     * Default constructor.
     */
    public DataMediaHolder() {
        media = new LinkedList<DataMedia>();
        this.datetime = getW3CFormattedTimeStamp();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * de.fu.tracebook.core.data.IDataMediaHolder#addMedia(de.fu.tracebook.core
     * .data.DataMedia)
     */
    public void addMedia(IDataMedia medium) {
        if (medium != null) {
            media.add((DataMedia) medium);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu.tracebook.core.data.IDataMediaHolder#deleteMedia(int)
     */
    public void deleteMedia(int id) {
        ListIterator<DataMedia> lit = media.listIterator();
        IDataMedia dm;
        while (lit.hasNext()) {
            dm = lit.next();
            if (dm.getId() == id) {
                dm.delete();
                lit.remove();
                break;
            }
        }
    }

    /**
     * "a_node" is a node which has "link" nodes. This method restores the
     * DataMedia-objects from these "link" nodes.
     * 
     * @param aNode
     *            An XML-node.
     */
    public void deserializeMedia(Node aNode) {
        NodeList metanodes = aNode.getChildNodes();

        for (int i = 0; i < metanodes.getLength(); ++i) {
            if (metanodes.item(i).getNodeName().equals("link")) {

                NamedNodeMap attributes = metanodes.item(i).getAttributes();
                Node path = attributes.getNamedItem("href");
                // misuse of getTrackDirPath
                addMedia(DataMedia.deserialize(DataTrack.getTrackDirPath(path
                        .getNodeValue())));

            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu.tracebook.core.data.IDataMediaHolder#getDatetime()
     */
    public String getDatetime() {
        return datetime;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu.tracebook.core.data.IDataMediaHolder#getMedia()
     */
    public List<DataMedia> getMedia() {
        return media;
    }

    /**
     * This method generates the media-tags ("link") for a
     * DataMediaHolder-object. The enclosing tag must be opened.
     * 
     * @param serializer
     *            An XmlSerializer that is initialized.
     */
    public void serializeMedia(XmlSerializer serializer) {
        for (DataMedia m : media) {
            m.serialize(serializer);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * de.fu.tracebook.core.data.IDataMediaHolder#setDatetime(java.lang.String)
     */
    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }
}
