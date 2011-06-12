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

public class NewDBMedia implements NewDBObject {

    private final static String CREATE = "CREATE TABLE IF NOT EXISTS media "
            + "( id INTEGER PRIMARY KEY AUTOINCREMENT," + " name TEXT,"
            + " path TEXT," + " node INTEGER," + " way INTEGER,"
            + " track TEXT );";
    private final static String DROP = "DROP TABLE IF EXISTS media";
    private final static String TABLENAME = "media";

    public static String createTable() {
        return CREATE;
    }

    public static void deleteByNode(long nodeId) {
        SQLiteDatabase db = DBOpenHelper.getInstance().getWritableDatabase();
        if (db.delete(TABLENAME, "node = " + nodeId, null) == -1) {
            LogIt.e("Could not delete media");
        }
    }

    public static void deleteByTrack(String trackId) {
        SQLiteDatabase db = DBOpenHelper.getInstance().getWritableDatabase();
        if (db.delete(TABLENAME, "track = '" + trackId + "'", null) == -1) {
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

    public static NewDBMedia getById(long mediaId) {
        return fillObject(mediaId, new NewDBMedia());
    }

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

    public long id;
    public String name;
    public long node;
    public String path;
    public String track;

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
        values.put("node", node);
        values.put("track", track);
        values.put("way", way);
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
        values.put("node", node);
        values.put("track", track);
        values.put("way", way);
        if (db.update(TABLENAME, values, "id = " + id, null) == -1) {
            LogIt.e("Could not update media");
        }
    }

    public void update() {
        fillObject(id, this);
    }

}
