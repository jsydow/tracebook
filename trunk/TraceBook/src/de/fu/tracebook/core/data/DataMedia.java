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

import java.io.File;
import java.io.IOException;

import org.xmlpull.v1.XmlSerializer;

import de.fu.tracebook.util.LogIt;

/**
 * This is an object that refers to a medium. The medium itself is stored on the
 * background memory of the device. Only the name and path of the actual medium
 * is stored in this object.
 */
public class DataMedia implements IDataMedia {

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

    /**
     * This method loads a medium reference from the devices memory.
     * 
     * @param path
     *            The complete path to the medium.
     * @return The deserialized DataMedia object or null if medium doesn't
     *         exist.
     */
    static DataMedia deserialize(String path) {
        File medium = new File(path);
        if (medium.exists()) {
            return new DataMedia(medium.getParent(), medium.getName());
        }
        LogIt.w("Media", "Medium was not found. Was trying to load a medium.");
        return null;
    }

    /**
     * The internal id for this medium.
     */
    private int id;

    /**
     * This name is the displayed name and filename (contains extension).
     */
    private String name;

    /**
     * The path to the file of the medium on the memory. This path+name should
     * be sufficient to open the file. Path is therefore the base name.
     */
    private String path;

    /**
     * This is the type of the medium. Use the TYPE_****-constants!
     */
    private int type;

    /**
     * Constructor that initializes the medium.
     * 
     * @param path
     *            Path to the file (basename).
     * @param name
     *            Name of the medium (filename).
     */
    public DataMedia(String path, String name) {
        super();
        this.id = StorageFactory.getStorage().getID();
        this.path = path;
        this.name = name;
        this.type = getTypeFromFilename(name);
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu.tracebook.core.data.IDataMedia#delete()
     */
    public void delete() {
        File medium = new File(getFullPath());
        if (medium.isFile()) {
            if (!medium.delete()) {
                LogIt.w("Media", "Could not delete medium");
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu.tracebook.core.data.IDataMedia#getId()
     */
    public int getId() {
        return id;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu.tracebook.core.data.IDataMedia#getName()
     */
    public String getName() {
        return name;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu.tracebook.core.data.IDataMedia#getPath()
     */
    public String getPath() {
        return path;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu.tracebook.core.data.IDataMedia#getType()
     */
    public int getType() {
        return type;
    }

    /**
     * Generates a link tag for this medium.
     * 
     * @param serializer
     *            The initialized XmlSerialiser.
     */
    public void serialize(XmlSerializer serializer) {
        try {
            serializer.startTag(null, "link");
            // serializer.attribute(null, "type", typeToString(type)); not used
            serializer.attribute(null, "href", name);
            serializer.endTag(null, "link");
        } catch (IllegalArgumentException e) {
            LogIt.e("MediaSerialisation", "Should not happen");
        } catch (IllegalStateException e) {
            LogIt.e("MediaSerialisation", "Illegal state");
        } catch (IOException e) {
            LogIt.e("MediaSerialisation", "Could not serialize medium " + name);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu.tracebook.core.data.IDataMedia#setName(java.lang.String)
     */
    public void setName(String newname) {
        File oldfile = new File(getFullPath());
        File newfile = new File(getPath() + File.separator + newname);
        boolean success = oldfile.renameTo(newfile);
        if (!success) {
            LogIt.e("MediaRenaming", "Could not rename medium.");
        } else {
            this.name = newname;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu.tracebook.core.data.IDataMedia#setPath(java.lang.String)
     */
    public void setPath(String path) {
        if (path != null) {
            this.path = path;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu.tracebook.core.data.IDataMedia#setType(int)
     */
    public void setType(int type) {
        this.type = type;
    }

    /**
     * Getter-method. The returned String is enough to open the file.
     * 
     * @return The path to the medium on the devices medium. (Should generally
     *         be not null, except some idiot misused methods)
     */
    private String getFullPath() {
        return path + File.separator + name;
    }
}
