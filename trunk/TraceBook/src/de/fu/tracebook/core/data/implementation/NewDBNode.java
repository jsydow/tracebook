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
 * The DAO-object for nodes. Each object represents a row in the database.
 */
public class NewDBNode implements NewDBObject {

    private static final String CREATE = "CREATE TABLE IF NOT EXISTS nodes "
            + "( id INTEGER PRIMARY KEY AUTOINCREMENT," + " datetime TEXT,"
            + " latitude INTEGER," + " longitude INTEGER," + " way INTEGER,"
            + " track TEXT );";
    private static final String DROP = "DROP TABLE IF EXISTS nodes";
    private static final String TABLENAME = "nodes";

    /**
     * Returns a string that creates the table for this object.
     * 
     * @return The create table string.
     */
    public static String createTable() {
        return CREATE;
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
     * Retrieve a node from the database with a given id.
     * 
     * @param nodeId
     *            The id of the node.
     * @return The node or null if does not exist.
     */
    public static NewDBNode getById(long nodeId) {
        return fillObject(nodeId, new NewDBNode());
    }

    /**
     * Retrieves a list of all nodes that belong to the given track.
     * 
     * @param name
     *            The name of the track.
     * @return The list of nodes, may be empty.
     */
    public static List<NewDBNode> getByTrack(String name) {
        List<NewDBNode> ret = new ArrayList<NewDBNode>();

        SQLiteDatabase db = DBOpenHelper.getInstance().getReadableDatabase();
        Cursor result = db.query(TABLENAME, new String[] { "id", "datetime",
                "latitude", "longitude", "way", "track " }, "track = '" + name
                + "'", null, null, null, "id ASC");
        if (result.moveToFirst()) {
            do {
                ret.add(createNewObject(new NewDBNode(), result));
            } while (result.moveToNext());
        }
        result.close();

        return ret;
    }

    /**
     * Retrieves a list of all nodes that belong to the given way.
     * 
     * @param wayId
     *            The id of the way.
     * @return The list of nodes, may be empty.
     */
    public static List<NewDBNode> getByWay(long wayId) {
        List<NewDBNode> ret = new ArrayList<NewDBNode>();

        SQLiteDatabase db = DBOpenHelper.getInstance().getReadableDatabase();
        Cursor result = db.query(TABLENAME, new String[] { "id", "datetime",
                "latitude", "longitude", "way", "track " }, "way = " + wayId,
                null, null, null, "id ASC");
        if (result.moveToFirst()) {
            do {
                ret.add(createNewObject(new NewDBNode(), result));
            } while (result.moveToNext());
        }
        result.close();

        return ret;
    }

    private static NewDBNode createNewObject(NewDBNode node, Cursor crs) {
        node.id = crs.getLong(crs.getColumnIndex("id"));
        node.datetime = crs.getString(crs.getColumnIndex("datetime"));
        node.latitude = crs.getInt(crs.getColumnIndex("latitude"));
        node.longitude = crs.getInt(crs.getColumnIndex("longitude"));
        node.way = crs.getLong(crs.getColumnIndex("way"));
        node.track = crs.getString(crs.getColumnIndex("track"));
        return node;
    }

    private static NewDBNode fillObject(long nodeId, NewDBNode node) {
        NewDBNode ret = null;
        SQLiteDatabase db = DBOpenHelper.getInstance().getReadableDatabase();
        Cursor result = db.query(TABLENAME, new String[] { "id", "datetime",
                "latitude", "longitude", "way", "track " }, "id = " + nodeId,
                null, null, null, null);
        if (result.moveToFirst()) {
            ret = createNewObject(node, result);
        }
        result.close();

        return ret;
    }

    /**
     * The date time.
     */
    public String datetime;

    /**
     * The latitude*1000000.
     */
    public long id;

    /**
     * 
     */
    public int latitude;

    /**
     * The longitude*1000000.
     */
    public int longitude;

    /**
     * The name of the track this node belongs to.
     */
    public String track;

    /**
     * The id of the way this node belongs to.
     */
    public long way;

    public void delete() {
        SQLiteDatabase db = DBOpenHelper.getInstance().getWritableDatabase();
        if (db.delete(TABLENAME, "id = " + id, null) == -1) {
            LogIt.e("Could not delete node");
        }
    }

    public void insert() {
        SQLiteDatabase db = DBOpenHelper.getInstance().getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("datetime", datetime);
        values.put("latitude", Integer.valueOf(latitude));
        values.put("longitude", Integer.valueOf(longitude));
        values.put("track", track);
        values.put("way", Long.valueOf(way));
        long rowID = db.insert(TABLENAME, null, values);
        if (rowID == -1) {
            LogIt.e("Could not insert node");
        } else {
            this.id = rowID;
        }

    }

    public void save() {
        SQLiteDatabase db = DBOpenHelper.getInstance().getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("datetime", datetime);
        values.put("latitude", Integer.valueOf(latitude));
        values.put("longitude", Integer.valueOf(longitude));
        values.put("track", track);
        values.put("way", Long.valueOf(way));
        if (db.update(TABLENAME, values, "id = " + id, null) == -1) {
            LogIt.e("Could not update node");
        }
    }

    public void update() {
        fillObject(id, this);
    }

}
