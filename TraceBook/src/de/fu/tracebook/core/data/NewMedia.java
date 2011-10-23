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

package de.fu.tracebook.core.data;

import java.io.File;

import de.fu.tracebook.core.data.implementation.NewDBMedia;
import de.fu.tracebook.util.LogIt;

/**
 * Implementation of IDataMedia using a SQLite database.
 */
public class NewMedia implements IDataMedia {

    private static String[] extensions = { ".txt", ".jpg", ".m4a", ".mp4" };

    /**
     * Given a filename this method retrieves the type of the medium.
     * 
     * @param filename
     *            The filename of the medium.
     * @return The type of the medium coded using the constants as integer,
     *         returns -1 if type of medium is not known or understood by
     *         TraceBook.
     */
    private static int getTypeFromFilename(String filename) {
        if (filename.endsWith(typeToExtension(TYPE_TEXT))) {
            return TYPE_TEXT;
        }
        if (filename.endsWith(typeToExtension(TYPE_AUDIO))) {
            return TYPE_AUDIO;
        }
        if (filename.endsWith(typeToExtension(TYPE_VIDEO))) {
            return TYPE_VIDEO;
        }
        if (filename.endsWith(typeToExtension(TYPE_PICTURE))) {
            return TYPE_PICTURE;
        }
        if (filename.endsWith(".3gp")) {
            return TYPE_VIDEO;
        }
        return -1;
    }

    /**
     * Returns the extension of files of this type. Format is ".***" like
     * ".jpg".
     * 
     * @param paramType
     *            The type-variable of this class/object.
     * @return The extension String or empty String if parameter type has
     *         illegal value.
     */
    private static String typeToExtension(int paramType) {
        if (paramType > TYPE_VIDEO || paramType < TYPE_TEXT)
            return "";
        return extensions[paramType];
    }

    private NewDBMedia thisMedium;

    /**
     * Create a NewMedia object from a DBMedia object from the database.
     * 
     * @param medium
     *            The existing medium.
     */
    public NewMedia(NewDBMedia medium) {
        this.thisMedium = medium;
    }

    /**
     * Create a new DBMedia object.
     * 
     * @param path
     *            The path to the medium.
     * @param filename
     *            The filename of the medium.
     */
    public NewMedia(String path, String filename) {
        this.thisMedium = new NewDBMedia();
        thisMedium.path = path;
        thisMedium.name = filename;
        thisMedium.insert();
    }

    public void delete() {
        File medium = new File(getFullPath());
        if (medium.isFile()) {
            if (!medium.delete()) {
                LogIt.w("Could not delete medium");
            }
        }
        thisMedium.delete();
    }

    /**
     * Returns the NewDBMedia object that is represented by this object. The
     * NewDBMedia is a kind of DAO-Object for the row of this medium in the
     * database.
     * 
     * @return The NewDBMedia object to this object.
     */
    public NewDBMedia getDBMedia() {
        return thisMedium;
    }

    public int getId() {
        return (int) thisMedium.id;
    }

    public String getName() {
        return thisMedium.name;
    }

    public String getPath() {
        return thisMedium.path;
    }

    public int getType() {
        return getTypeFromFilename(thisMedium.name);
    }

    public void setName(String newname) {
        File oldfile = new File(getFullPath());
        File newfile = new File(getPath() + File.separator + newname);
        boolean success = oldfile.renameTo(newfile);
        if (!success) {
            LogIt.e("Could not rename medium.");
        } else {
            thisMedium.name = newname;
            thisMedium.save();
        }

    }

    public void setType(int type) {
        // do nothing
    }

    /**
     * Getter-method. The returned String is enough to open the file.
     * 
     * @return The path to the medium on the devices medium. (Should generally
     *         be not null, except some idiot misused methods)
     */
    private String getFullPath() {
        return thisMedium.path + File.separator + thisMedium.name;
    }

}
