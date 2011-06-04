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

import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.dao.ForeignCollection;

import de.fu.tracebook.core.data.implementation.DBTag;

public class TagMap extends HashMap<String, String> {

    private Updateable object;
    private ForeignCollection<DBTag> tags;

    /**
     * Creates a tag map.
     * 
     * @param object
     *            The object to update after change.
     * @param tags
     *            The tags.
     */
    TagMap(Updateable object, ForeignCollection<DBTag> tags) {
        this.object = object;
        this.tags = tags;
        for (DBTag t : tags) {
            this.put(t.key, t.value);
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.HashMap#put(java.lang.Object, java.lang.Object)
     */
    @Override
    public String put(String key, String value) {
        DBTag tag = new DBTag();
        tag.key = key;
        tag.value = value;
        tags.add(tag);
        object.update();

        return super.put(key, value);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.HashMap#remove(java.lang.Object)
     */
    @Override
    public String remove(Object key) {

        CloseableIterator<DBTag> iter = tags.closeableIterator();
        while (iter.hasNext()) {
            DBTag t = iter.next();
            if (t.key.equals(key)) {
                iter.remove();
            }
        }
        object.update();

        return super.remove(key);
    }

}
