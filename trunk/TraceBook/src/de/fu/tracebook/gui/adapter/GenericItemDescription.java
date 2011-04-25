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

package de.fu.tracebook.gui.adapter;

import java.util.HashMap;
import java.util.Map;

/**
 * This class defines and handles the mapping of string tags with view resource
 * IDs. A reference of this class have to be connected to each instance of a
 * GenericAdapterData. So each item in a ListView can associate its view
 * elements.
 */
public class GenericItemDescription {
    private String itemNameTag = null;

    /**
     * Map to associate a String tag with an id.
     * 
     */
    Map<String, Integer> resourceIds = new HashMap<String, Integer>();

    /**
     * @param tag
     *            tag to associate the resource id
     * @param id
     *            the resource id
     */
    public void addResourceId(String tag, int id) {
        resourceIds.put(tag, Integer.valueOf(id));
    }

    /**
     * Returns the item tag that is used for toString() in the AdapterData.
     * 
     * @return The name of the tag that names the data.
     */
    public String getNameTag() {
        return itemNameTag;
    }

    /**
     * @param tag
     *            tag which is associated with a given resources id.
     * @return return the id of a resource which is associated with a given tag
     */
    public int getResourceId(String tag) {
        return resourceIds.get(tag).intValue();
    }

    /**
     * Sets the name of the tag that is used for the toString() method of the
     * AdapterData.
     * 
     * @param tag
     *            The name of the tag.
     */
    public void setNameTag(String tag) {
        itemNameTag = tag;
    }
}
