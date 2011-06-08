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

public class NewDBNode implements NewDBObject {

    private final static String CREATE = "CREATE TABLE IF NOT EXISTS nodes "
            + "( id INTEGER PRIMARY KEY AUTOINCREMENT," + " datetime TEXT,"
            + " latitude INTEGER," + " longitude INTEGER," + " way INTEGER,"
            + " track String );";
    private final static String DROP = "DROP TABLE IF EXISTS nodes";
    private final static String TABLENAME = "nodes";

    public static String createTable() {
        return CREATE;
    }

    public static void deleteByTrack(String trackId) {
        SQLiteDatabase db = DBOpenHelper.getInstance().getWritableDatabase();
        if (db.delete(TABLENAME, "track = " + trackId, null) == -1) {
            LogIt.e("Could not delete media");
        }
    }

    public static void deleteByWay(long wayId) {
        SQLiteDatabase db = DBOpenHelper.getInstance().getWritableDatabase();
        if (db.delete(TABLENAME, "way = " + wayId, null) == -1) {
            LogIt.e("Could not delete media");
        }
    }

    public static String dropTable() {
        return DROP;
    }

    public static NewDBNode getById(long nodeId) {
        return fillObject(nodeId, new NewDBNode());
    }

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

    public String datetime;
    public long id;
    public int latitude;
    public int longitude;
    public String track;

    public long way;

    public void delete() {
        SQLiteDatabase db = DBOpenHelper.getInstance().getWritableDatabase();
        if (db.delete(TABLENAME, "id = " + id, null) == -1) {
            LogIt.e("Could not delete node");
        }
        NewDBTag.deleteByWay(id);
        NewDBMedia.deleteByNode(id);
    }

    public void insert() {
        SQLiteDatabase db = DBOpenHelper.getInstance().getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("datetime", datetime);
        values.put("latitude", latitude);
        values.put("longitude", longitude);
        values.put("track", track);
        values.put("way", way);
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
        values.put("latitude", latitude);
        values.put("longitude", longitude);
        values.put("track", track);
        values.put("way", way);
        if (db.update(TABLENAME, values, "id = " + id, null) == -1) {
            LogIt.e("Could not update node");
        }
    }

    public void update() {
        fillObject(id, this);
    }

}
