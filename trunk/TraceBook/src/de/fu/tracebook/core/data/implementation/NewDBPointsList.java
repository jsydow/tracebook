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

public class NewDBPointsList implements NewDBObject {

    private final static String CREATE = "CREATE TABLE IF NOT EXISTS pointslists "
            + "( id INTEGER PRIMARY KEY AUTOINCREMENT,"
            + " datetime TEXT,"
            + " isarea INTEGER," + " track TEXT );";
    private final static String DROP = "DROP TABLE IF EXISTS pointslists";
    private final static String TABLENAME = "pointslists";

    public static String createTable() {
        return CREATE;
    }

    public static String dropTable() {
        return DROP;
    }

    public static NewDBPointsList getById(long wayId) {
        return fillObject(wayId, new NewDBPointsList());
    }

    public static List<NewDBPointsList> getByTrack(String name) {
        List<NewDBPointsList> ret = new ArrayList<NewDBPointsList>();

        SQLiteDatabase db = DBOpenHelper.getInstance().getReadableDatabase();
        Cursor result = db.query(TABLENAME, new String[] { "id", "datetime",
                "isarea", "track " }, "track = '" + name + "'", null, null,
                null, "id ASC");
        if (result.moveToFirst()) {
            do {
                ret.add(createNewObject(new NewDBPointsList(), result));
            } while (result.moveToNext());
        }
        result.close();

        return ret;
    }

    private static NewDBPointsList createNewObject(NewDBPointsList way,
            Cursor crs) {
        way.id = crs.getLong(crs.getColumnIndex("id"));
        way.datetime = crs.getString(crs.getColumnIndex("datetime"));
        way.isArea = crs.getInt(crs.getColumnIndex("isarea")) != 0;
        way.track = crs.getString(crs.getColumnIndex("track"));
        return way;
    }

    private static NewDBPointsList fillObject(long wayId, NewDBPointsList way) {
        NewDBPointsList ret = null;
        SQLiteDatabase db = DBOpenHelper.getInstance().getReadableDatabase();
        Cursor result = db.query(TABLENAME, new String[] { "id", "datetime",
                "isarea", "track " }, "id = " + wayId, null, null, null, null);
        if (result.moveToFirst()) {
            ret = createNewObject(way, result);
        }
        result.close();

        return ret;
    }

    public String datetime;
    public long id;
    public boolean isArea;
    public String track;

    public void delete() {
        SQLiteDatabase db = DBOpenHelper.getInstance().getWritableDatabase();
        if (db.delete(TABLENAME, "id = " + id, null) == -1) {
            LogIt.e("Could not delete way");
        }
        db.close();
    }

    public void insert() {
        SQLiteDatabase db = DBOpenHelper.getInstance().getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("datetime", datetime);
        values.put("track", track);
        values.put("isarea", isArea);
        long rowID = db.insert(TABLENAME, null, values);
        if (rowID == -1) {
            LogIt.e("Could not insert way");
        } else {
            this.id = rowID;
        }
        db.close();

    }

    public void save() {
        SQLiteDatabase db = DBOpenHelper.getInstance().getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("datetime", datetime);
        values.put("track", track);
        values.put("isarea", isArea);
        if (db.update(TABLENAME, values, "id = " + id, null) == -1) {
            LogIt.e("Could not update way");
        }
        db.close();
    }

    public void update() {
        fillObject(id, this);
    }
}
