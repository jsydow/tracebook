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

package de.fu.tracebook.core.data.implementation;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import de.fu.tracebook.util.LogIt;

/**
 * The DAO-object for tags. Each object represents a row in the database.
 */
public class NewDBTag implements NewDBObject {

    private static final String CREATE = "CREATE TABLE IF NOT EXISTS tags "
            + "( id INTEGER PRIMARY KEY AUTOINCREMENT," + " key TEXT,"
            + " value TEXT," + " node INTEGER," + " way INTEGER );";
    private static final String DROP = "DROP TABLE IF EXISTS tags";
    private static final String TABLENAME = "tags";

    /**
     * Returns a string that creates the table for this object.
     * 
     * @return The create table string.
     */
    public static String createTable() {
        return CREATE;
    }

    /**
     * Delete all tags that belong to the given node.
     * 
     * @param nodeId
     *            The id of the node.
     */
    public static void deleteByNode(long nodeId) {
        SQLiteDatabase db = DBOpenHelper.getInstance().getWritableDatabase();
        if (db.delete(TABLENAME, "node = " + nodeId, null) == -1) {
            LogIt.e("Could not delete media");
        }
    }

    /**
     * Delete all tags that belong to the given way.
     * 
     * @param wayId
     *            The id of the way.
     */
    public static void deleteByWay(long wayId) {
        SQLiteDatabase db = DBOpenHelper.getInstance().getWritableDatabase();
        if (db.delete(TABLENAME, "way = " + wayId, null) == -1) {
            LogIt.e("Could not delete media");
        }
    }

    /**
     * Returns a string that drops the table for this object.
     * 
     * @return The drop table string.
     */
    public static String dropTable() {
        return DROP;
    }

    /**
     * Retrieve a tag from the database with a given id.
     * 
     * @param tagID
     *            The id of the way.
     * @return The tag or null if does not exist.
     */
    public static NewDBTag getById(long tagID) {
        return fillObject(tagID, new NewDBTag());
    }

    /**
     * Retrieves a list of all tags that belong to the given node.
     * 
     * @param nodeId
     *            The id of the node.
     * @return The list of tags, may be empty.
     */
    public static List<NewDBTag> getByNode(long nodeId) {
        List<NewDBTag> ret = new ArrayList<NewDBTag>();

        SQLiteDatabase db = DBOpenHelper.getInstance().getReadableDatabase();
        Cursor result = db.query(TABLENAME, new String[] { "id", "key",
                "value", "node", "way" }, "node = " + nodeId, null, null, null,
                "id ASC");
        if (result.moveToFirst()) {
            do {
                ret.add(createNewObject(new NewDBTag(), result));
            } while (result.moveToNext());
        }
        result.close();

        return ret;
    }

    /**
     * Retrieves a list of all tags that belong to the given way.
     * 
     * @param wayId
     *            The id of the way.
     * @return The list of tags, may be empty.
     */
    public static List<NewDBTag> getByWay(long wayId) {
        List<NewDBTag> ret = new ArrayList<NewDBTag>();

        SQLiteDatabase db = DBOpenHelper.getInstance().getReadableDatabase();
        Cursor result = db.query(TABLENAME, new String[] { "id", "key",
                "value", "node", "way" }, "way = " + wayId, null, null, null,
                "id ASC");
        if (result.moveToFirst()) {
            do {
                ret.add(createNewObject(new NewDBTag(), result));
            } while (result.moveToNext());
        }
        result.close();

        return ret;
    }

    private static NewDBTag createNewObject(NewDBTag tag, Cursor crs) {
        tag.id = crs.getLong(crs.getColumnIndex("id"));
        tag.key = crs.getString(crs.getColumnIndex("key"));
        tag.value = crs.getString(crs.getColumnIndex("value"));
        tag.node = crs.getLong(crs.getColumnIndex("node"));
        tag.way = crs.getLong(crs.getColumnIndex("way"));
        return tag;
    }

    private static NewDBTag fillObject(long tagId, NewDBTag tag) {
        NewDBTag ret = null;
        SQLiteDatabase db = DBOpenHelper.getInstance().getReadableDatabase();
        Cursor result = db.query(TABLENAME, new String[] { "id", "key",
                "value", "node", "way" }, "id = " + tagId, null, null, null,
                null);
        if (result.moveToFirst()) {
            ret = createNewObject(tag, result);
        }
        result.close();

        return ret;
    }

    /**
     * The id of the tag. (primary key)
     */
    public long id;

    /**
     * The key.
     */
    public String key;

    /**
     * The id of the node this tag is attached to.
     */
    public long node;

    /**
     * The value.
     */
    public String value;

    /**
     * The id of the way this tag is attached to.
     */
    public long way;

    public void delete() {
        SQLiteDatabase db = DBOpenHelper.getInstance().getWritableDatabase();
        if (db.delete(TABLENAME, "id = " + id, null) == -1) {
            LogIt.e("Could not delete tag");
        }
    }

    public void insert() {
        SQLiteDatabase db = DBOpenHelper.getInstance().getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("key", key);
        values.put("value", value);
        values.put("node", Long.valueOf(node));
        values.put("way", Long.valueOf(way));
        long rowID = db.insert(TABLENAME, null, values);
        if (rowID == -1) {
            LogIt.e("Could not insert tag");
        } else {
            this.id = rowID;
        }

    }

    public void save() {
        SQLiteDatabase db = DBOpenHelper.getInstance().getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("key", key);
        values.put("value", value);
        values.put("node", Long.valueOf(node));
        values.put("way", Long.valueOf(way));
        if (db.update(TABLENAME, values, "id = " + id, null) == -1) {
            LogIt.e("Could not update tag");
        }
    }

    public void update() {
        fillObject(id, this);
    }

}
