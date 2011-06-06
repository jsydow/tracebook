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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import de.fu.tracebook.core.data.implementation.NewDBTag;

public class TagMap extends HashMap<String, String> {

    private NewNode node;
    private List<NewDBTag> tags;
    private NewPointsList way;

    /**
     * Creates a tag map.
     * 
     * @param tags
     *            The tags.
     * @param node
     *            The node, can be null if tags belong to way.
     * @param way
     *            The way, can be null if tags belong to node.
     */
    TagMap(List<NewDBTag> tags, NewNode node, NewPointsList way) {
        this.tags = tags;
        this.way = way;
        this.node = node;
        HashMap<String, String> tmp = new HashMap<String, String>();

        for (NewDBTag t : tags) {
            tmp.put(t.key, t.value);
        }
        this.putAll(tmp);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.HashMap#put(java.lang.Object, java.lang.Object)
     */
    @Override
    public String put(String key, String value) {
        NewDBTag tag = new NewDBTag();
        tag.key = key;
        tag.value = value;
        if (node != null) {
            tag.node = node.getId();
        }
        if (way != null) {
            tag.way = way.getId();
        }
        tag.insert();
        tags.add(tag);

        return super.put(key, value);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.HashMap#remove(java.lang.Object)
     */
    @Override
    public String remove(Object key) {

        Iterator<NewDBTag> iter = tags.iterator();
        while (iter.hasNext()) {
            NewDBTag t = iter.next();
            if (t.key.equals(key)) {
                t.delete();
                iter.remove();
            }
        }

        return super.remove(key);
    }

}
