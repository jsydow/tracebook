/*======================================================================
 *
 * This file is part of TraceBook.
 *
 * TraceBook is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published 
 * by the Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version.
 *
 * TraceBook is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public 
 * License along with TraceBook. If not, see 
 * <http://www.gnu.org/licenses/>.
 *
 =====================================================================*/

package de.fu.tracebook.util;

/**
 * A generic tuple element to store two values.
 * 
 * @param <A>
 *            first element type
 * @param <B>
 *            second element type
 */
public class Pair<A, B> {
    /**
     * First value stored in the Pair.
     */
    public A first;

    /**
     * Second value stored in the Pair.
     */
    public B second;

    /**
     * Creates a new Pair.
     * 
     * @param first
     *            first element
     * @param second
     *            second element
     */
    public Pair(A first, B second) {
        this.first = first;
        this.second = second;
    }
}
