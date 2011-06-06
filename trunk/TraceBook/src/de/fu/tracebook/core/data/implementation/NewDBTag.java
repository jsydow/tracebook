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

public class NewDBTag implements NewDBObject {

    private final static String CREATE = "CREATE TABLE IF NOT EXISTS tags "
            + "( id INTEGER PRIMARY KEY AUTOINCREMENT," + " key TEXT,"
            + " value TEXT," + " node INTEGER," + " way INTEGER );";
    private final static String DROP = "DROP TABLE IF EXISTS tags";
    private final static String TABLENAME = "tags";

    public static String createTable() {
        return CREATE;
    }

    public static String dropTable() {
        return DROP;
    }

    public static NewDBTag getById(long tagID) {
        NewDBTag ret = null;
        SQLiteDatabase db = DBOpenHelper.getInstance().getReadableDatabase();
        Cursor result = db.query(TABLENAME, new String[] { "id", "key",
                "value", "node", "way" }, "id = " + tagID, null, null, null,
                null);
        if (result.moveToFirst()) {
            ret = createNewObject(result);
        } else {
            LogIt.e("Could not get a tag with id " + tagID);
        }
        result.close();

        return ret;
    }

    public static List<NewDBTag> getByNode(long nodeId) {
        List<NewDBTag> ret = new ArrayList<NewDBTag>();

        SQLiteDatabase db = DBOpenHelper.getInstance().getReadableDatabase();
        Cursor result = db.query(TABLENAME, new String[] { "id", "key",
                "value", "node", "way" }, "node = " + nodeId, null, null, null,
                "id ASC");
        if (result.moveToFirst()) {
            do {
                ret.add(createNewObject(result));
            } while (result.moveToNext());
        } else {
            LogIt.e("Could not get a tag with node id " + nodeId);
        }
        result.close();

        return ret;
    }

    public static List<NewDBTag> getByWay(long wayId) {
        List<NewDBTag> ret = new ArrayList<NewDBTag>();

        SQLiteDatabase db = DBOpenHelper.getInstance().getReadableDatabase();
        Cursor result = db.query(TABLENAME, new String[] { "id", "key",
                "value", "node", "way" }, "way = " + wayId, null, null, null,
                "id ASC");
        if (result.moveToFirst()) {
            do {
                ret.add(createNewObject(result));
            } while (result.moveToNext());
        } else {
            LogIt.e("Could not get a tag with way id " + wayId);
        }
        result.close();

        return ret;
    }

    private static NewDBTag createNewObject(Cursor crs) {
        NewDBTag ret = new NewDBTag();
        ret.id = crs.getLong(crs.getColumnIndex("id"));
        ret.key = crs.getString(crs.getColumnIndex("key"));
        ret.value = crs.getString(crs.getColumnIndex("value"));
        ret.node = crs.getLong(crs.getColumnIndex("node"));
        ret.way = crs.getLong(crs.getColumnIndex("way"));
        return ret;
    }

    public long id;
    public String key;
    public long node;
    public String value;
    public long way;

    public void delete() {
        SQLiteDatabase db = DBOpenHelper.getInstance().getWritableDatabase();
        if (db.delete(TABLENAME, "id = " + id, null) == -1) {
            LogIt.e("Could not delete tag");
        }
        db.close();
    }

    public void insert() {
        SQLiteDatabase db = DBOpenHelper.getInstance().getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("key", key);
        values.put("value", value);
        values.put("node", node);
        values.put("way", way);
        if (db.insert(TABLENAME, null, values) == -1) {
            LogIt.e("Could not insert tag");
        }
        db.close();

    }

    public void update() {
        SQLiteDatabase db = DBOpenHelper.getInstance().getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("key", key);
        values.put("value", value);
        values.put("node", node);
        values.put("way", way);
        if (db.update(TABLENAME, values, "id = " + id, null) == -1) {
            LogIt.e("Could not update tag");
        }
        db.close();
    }

}
