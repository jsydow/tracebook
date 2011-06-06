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

    private final static String create = "CREATE TABLE IF NOT EXISTS nodes "
            + "( id INTEGER PRIMARY KEY AUTOINCREMENT," + " datetime TEXT,"
            + " latitude INTEGER," + " longitude INTEGER," + " way INTEGER,"
            + " track INTEGER );";
    private final static String drop = "DROP TABLE IF EXISTS nodes";

    public static String createTable() {
        return create;
    }

    public static String dropTable() {
        return drop;
    }

    public static NewDBNode getById(long nodeId) {
        NewDBNode ret = null;
        SQLiteDatabase db = DBOpenHelper.getInstance().getReadableDatabase();
        Cursor result = db.query("nodes", new String[] { "id", "datetime",
                "latitude", "longitude", "way", "track " }, "id = " + nodeId,
                null, null, null, null);
        if (result.moveToFirst()) {
            ret = createNewObject(result);
        } else {
            LogIt.e("Could not get a node with id " + nodeId);
        }
        result.close();

        return ret;
    }

    public static List<NewDBNode> getByTrack(long trackId) {
        List<NewDBNode> ret = new ArrayList<NewDBNode>();

        SQLiteDatabase db = DBOpenHelper.getInstance().getReadableDatabase();
        Cursor result = db.query("nodes", new String[] { "id", "datetime",
                "latitude", "longitude", "way", "track " }, "track = "
                + trackId, null, null, null, null);
        if (result.moveToFirst()) {
            do {
                ret.add(createNewObject(result));
            } while (result.moveToNext());
        } else {
            LogIt.e("Could not get a node with track id " + trackId);
        }
        result.close();

        return ret;
    }

    public static List<NewDBNode> getByWay(long wayId) {
        List<NewDBNode> ret = new ArrayList<NewDBNode>();

        SQLiteDatabase db = DBOpenHelper.getInstance().getReadableDatabase();
        Cursor result = db.query("nodes", new String[] { "id", "datetime",
                "latitude", "longitude", "way", "track " }, "way = " + wayId,
                null, null, null, null);
        if (result.moveToFirst()) {
            do {
                ret.add(createNewObject(result));
            } while (result.moveToNext());
        } else {
            LogIt.e("Could not get a node with way id " + wayId);
        }
        result.close();

        return ret;
    }

    private static NewDBNode createNewObject(Cursor crs) {
        NewDBNode ret = new NewDBNode();
        ret.id = crs.getLong(crs.getColumnIndex("id"));
        ret.datetime = crs.getString(crs.getColumnIndex("datetime"));
        ret.latitude = crs.getInt(crs.getColumnIndex("latitude"));
        ret.longitude = crs.getInt(crs.getColumnIndex("longitude"));
        ret.way = crs.getLong(crs.getColumnIndex("way"));
        ret.track = crs.getLong(crs.getColumnIndex("track"));
        return ret;
    }

    public String datetime;
    public long id;
    public int latitude;
    public int longitude;
    public long track;

    public long way;

    public void delete() {
        SQLiteDatabase db = DBOpenHelper.getInstance().getWritableDatabase();
        if (db.delete("nodes", "id = " + id, null) == -1) {
            LogIt.e("Could not delete node");
        }
        db.close();
    }

    public void insert() {
        SQLiteDatabase db = DBOpenHelper.getInstance().getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("datetime", datetime);
        values.put("latitude", latitude);
        values.put("longitude", longitude);
        values.put("track", track);
        values.put("way", way);
        if (db.insert("nodes", null, values) == -1) {
            LogIt.e("Could not insert node");
        }
        db.close();

    }

    public void update() {
        SQLiteDatabase db = DBOpenHelper.getInstance().getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("datetime", datetime);
        values.put("latitude", latitude);
        values.put("longitude", longitude);
        values.put("track", track);
        values.put("way", way);
        if (db.update("nodes", values, "id = " + id, null) == -1) {
            LogIt.e("Could not update node");
        }
        db.close();
    }

}
