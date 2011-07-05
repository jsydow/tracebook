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

package de.fu.tracebook.core.data.implementation;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import de.fu.tracebook.util.LogIt;

/**
 * The DAO-object for media. Each object represents a row in the database.
 */
public class NewDBMedia implements NewDBObject {

    private static final String CREATE = "CREATE TABLE IF NOT EXISTS media "
            + "( id INTEGER PRIMARY KEY AUTOINCREMENT," + " name TEXT,"
            + " path TEXT," + " node INTEGER," + " way INTEGER,"
            + " track TEXT );";
    private static final String DROP = "DROP TABLE IF EXISTS media";
    private static final String TABLENAME = "media";

    /**
     * Returns a string that creates the table for this object.
     * 
     * @return The create table string.
     */
    public static String createTable() {
        return CREATE;
    }

    /**
     * Delete all media that belong to the given node.
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
     * Delete all media that belong to the given track.
     * 
     * @param trackId
     *            The name of the track.
     */
    public static void deleteByTrack(String trackId) {
        SQLiteDatabase db = DBOpenHelper.getInstance().getWritableDatabase();
        if (db.delete(TABLENAME, "track = '" + trackId + "'", null) == -1) {
            LogIt.e("Could not delete media");
        }
    }

    /**
     * Delete all media that belong to the given way.
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
     * Retrieve a medium from the database with a given id.
     * 
     * @param mediaId
     *            The id of the medium.
     * @return The medium or null if does not exist.
     */
    public static NewDBMedia getById(long mediaId) {
        return fillObject(mediaId, new NewDBMedia());
    }

    /**
     * Retrieves a list of all media that belong to the given node.
     * 
     * @param nodeId
     *            The id of the node.
     * @return The list of media, may be empty.
     */
    public static List<NewDBMedia> getByNode(long nodeId) {
        List<NewDBMedia> ret = new ArrayList<NewDBMedia>();

        SQLiteDatabase db = DBOpenHelper.getInstance().getReadableDatabase();
        Cursor result = db.query(TABLENAME, new String[] { "id", "name",
                "path", "node", "way", "track " }, "node = " + nodeId, null,
                null, null, "id ASC");
        if (result.moveToFirst()) {
            do {
                ret.add(createNewObject(new NewDBMedia(), result));
            } while (result.moveToNext());
        }
        result.close();

        return ret;
    }

    /**
     * Retrieves a list of all media that belong to the given track.
     * 
     * @param trackId
     *            The name of the track.
     * @return The list of media, may be empty.
     */
    public static List<NewDBMedia> getByTrack(String trackId) {
        List<NewDBMedia> ret = new ArrayList<NewDBMedia>();

        SQLiteDatabase db = DBOpenHelper.getInstance().getReadableDatabase();
        Cursor result = db.query(TABLENAME, new String[] { "id", "name",
                "path", "node", "way", "track " }, "track = '" + trackId + "'",
                null, null, null, "id ASC");
        if (result.moveToFirst()) {
            do {
                ret.add(createNewObject(new NewDBMedia(), result));
            } while (result.moveToNext());
        }
        result.close();

        return ret;
    }

    /**
     * Retrieves a list of all media that belong to the given way.
     * 
     * @param wayId
     *            The id of the way.
     * @return The list of media, may be empty.
     */
    public static List<NewDBMedia> getByWay(long wayId) {
        List<NewDBMedia> ret = new ArrayList<NewDBMedia>();

        SQLiteDatabase db = DBOpenHelper.getInstance().getReadableDatabase();
        Cursor result = db.query(TABLENAME, new String[] { "id", "name",
                "path", "node", "way", "track " }, "way = " + wayId, null,
                null, null, "id ASC");
        if (result.moveToFirst()) {
            do {
                ret.add(createNewObject(new NewDBMedia(), result));
            } while (result.moveToNext());
        }
        result.close();

        return ret;
    }

    private static NewDBMedia createNewObject(NewDBMedia ret, Cursor crs) {
        ret.id = crs.getLong(crs.getColumnIndex("id"));
        ret.path = crs.getString(crs.getColumnIndex("path"));
        ret.name = crs.getString(crs.getColumnIndex("name"));
        ret.node = crs.getLong(crs.getColumnIndex("node"));
        ret.way = crs.getLong(crs.getColumnIndex("way"));
        ret.track = crs.getString(crs.getColumnIndex("track"));
        return ret;
    }

    private static NewDBMedia fillObject(long mediaId, NewDBMedia media) {
        NewDBMedia ret = null;
        SQLiteDatabase db = DBOpenHelper.getInstance().getReadableDatabase();
        Cursor result = db.query(TABLENAME, new String[] { "id", "name",
                "path", "node", "way", "track " }, "id = " + mediaId, null,
                null, null, null);
        if (result.moveToFirst()) {
            ret = createNewObject(media, result);
        }
        result.close();
        return ret;
    }

    /**
     * The id of this medium.
     */
    public long id;

    /**
     * The name of this medium.
     */
    public String name;

    /**
     * The id of the node that this medium belongs to.
     */
    public long node;

    /**
     * The path of the file of this medium.
     */
    public String path;

    /**
     * The id of the track that this medium belongs to.
     */
    public String track;

    /**
     * The id of the way that this medium belongs to.
     */
    public long way;

    public void delete() {
        SQLiteDatabase db = DBOpenHelper.getInstance().getWritableDatabase();
        if (db.delete(TABLENAME, "id = " + id, null) == -1) {
            LogIt.e("Could not delete media");
        }
    }

    public void insert() {
        SQLiteDatabase db = DBOpenHelper.getInstance().getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("path", path);
        values.put("node", new Long(node));
        values.put("track", track);
        values.put("way", new Long(way));
        long rowID = db.insert(TABLENAME, null, values);
        if (rowID == -1) {
            LogIt.e("Could not insert media");
        } else {
            this.id = rowID;
        }

    }

    public void save() {
        SQLiteDatabase db = DBOpenHelper.getInstance().getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("path", path);
        values.put("node", new Long(node));
        values.put("track", track);
        values.put("way", new Long(way));
        if (db.update(TABLENAME, values, "id = " + id, null) == -1) {
            LogIt.e("Could not update media");
        }
    }

    public void update() {
        fillObject(id, this);
    }

}
