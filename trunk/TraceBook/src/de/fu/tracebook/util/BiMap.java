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

package de.fu.tracebook.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class BiMap<K, V> implements Map<K, V> {

    private HashMap<V, K> backward = new HashMap<V, K>();
    private HashMap<K, V> forward = new HashMap<K, V>();

    public BiMap() {
        // do nothing
    }

    private BiMap(HashMap<K, V> forward, HashMap<V, K> backward) {
        this.backward = backward;
        this.forward = forward;
    }

    public void clear() {
        forward.clear();
        backward.clear();
    }

    public boolean containsKey(Object key) {
        return forward.containsKey(key);
    }

    public boolean containsValue(Object value) {
        return backward.containsKey(value);
    }

    public Set<java.util.Map.Entry<K, V>> entrySet() {
        return forward.entrySet();
    }

    public V get(Object key) {
        return forward.get(key);
    }

    public BiMap<V, K> inverse() {
        return new BiMap<V, K>(backward, forward);
    }

    public boolean isEmpty() {
        return forward.isEmpty();
    }

    public Set<K> keySet() {
        return forward.keySet();
    }

    public V put(K key, V value) {

        if (backward.containsKey(value) || forward.containsKey(key)) {
            return value;
        }
        backward.put(value, key);
        forward.put(key, value);
        return value;
    }

    public void putAll(Map<? extends K, ? extends V> arg0) {
        for (Entry<? extends K, ? extends V> e : arg0.entrySet()) {
            put(e.getKey(), e.getValue());
        }
    }

    public V remove(Object key) {
        V value = forward.remove(key);
        backward.remove(value);
        return value;
    }

    public int size() {
        return forward.size();
    }

    public Collection<V> values() {
        return forward.values();
    }

}
