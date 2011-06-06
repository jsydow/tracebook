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
            + " isArea INTEGER," + " track INTEGER );";
    private final static String DROP = "DROP TABLE IF EXISTS pointslists";
    private final static String TABLENAME = "pointslists";

    public static String createTable() {
        return CREATE;
    }

    public static String dropTable() {
        return DROP;
    }

    public static NewDBPointsList getById(long nodeId) {
        NewDBPointsList ret = null;
        SQLiteDatabase db = DBOpenHelper.getInstance().getReadableDatabase();
        Cursor result = db.query("nodes", new String[] { "id", "datetime",
                "isArea", "track " }, "id = " + nodeId, null, null, null, null);
        if (result.moveToFirst()) {
            ret = createNewObject(result);
        } else {
            LogIt.e("Could not get a way with id " + nodeId);
        }
        result.close();

        return ret;
    }

    public static List<NewDBPointsList> getByTrack(long trackId) {
        List<NewDBPointsList> ret = new ArrayList<NewDBPointsList>();

        SQLiteDatabase db = DBOpenHelper.getInstance().getReadableDatabase();
        Cursor result = db.query(TABLENAME, new String[] { "id", "datetime",
                "isArea", "track " }, "track = " + trackId, null, null, null,
                "id ASC");
        if (result.moveToFirst()) {
            do {
                ret.add(createNewObject(result));
            } while (result.moveToNext());
        } else {
            LogIt.e("Could not get a way with track id " + trackId);
        }
        result.close();

        return ret;
    }

    private static NewDBPointsList createNewObject(Cursor crs) {
        NewDBPointsList ret = new NewDBPointsList();
        ret.id = crs.getLong(crs.getColumnIndex("id"));
        ret.datetime = crs.getString(crs.getColumnIndex("datetime"));
        ret.isArea = crs.getInt(crs.getColumnIndex("isArea")) != 0;
        ret.track = crs.getLong(crs.getColumnIndex("track"));
        return ret;
    }

    public String datetime;
    public long id;
    public boolean isArea;
    public long track;

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
        values.put("isArea", isArea);
        if (db.insert(TABLENAME, null, values) == -1) {
            LogIt.e("Could not insert way");
        }
        db.close();

    }

    public void update() {
        SQLiteDatabase db = DBOpenHelper.getInstance().getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("datetime", datetime);
        values.put("track", track);
        values.put("isArea", isArea);
        if (db.update(TABLENAME, values, "id = " + id, null) == -1) {
            LogIt.e("Could not update way");
        }
        db.close();
    }
}
