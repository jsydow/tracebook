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

/**
 * A factory for the DataStorage. It returns an implementation of IDataStorage.
 */
public class StorageFactory {

    private static IDataStorage instance;

    /**
     * @return Valid instance of IDataStorage
     */
    public static IDataStorage getStorage() {
        if (instance == null)
            instance = new DataStorage();
        return instance;
    }
}
