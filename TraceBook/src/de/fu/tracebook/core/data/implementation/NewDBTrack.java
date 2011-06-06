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

public class NewDBTrack implements NewDBObject {

    private final static String CREATE = "CREATE TABLE IF NOT EXISTS tracks "
            + "( name TEXT PRIMARY KEY," + " datetime TEXT,"
            + " comment TEXT );";
    private final static String DROP = "DROP TABLE IF EXISTS tracks";
    private final static String TABLENAME = "tracks";

    public static String createTable() {
        return CREATE;
    }

    public static String dropTable() {
        return DROP;
    }

    public static List<String> getAllNames() {
        List<String> ret = new ArrayList<String>();

        SQLiteDatabase db = DBOpenHelper.getInstance().getReadableDatabase();
        Cursor result = db.query(TABLENAME, new String[] { "name" }, null,
                null, null, null, "name ASC");
        if (result.moveToFirst()) {
            do {
                ret.add(result.getString(result.getColumnIndex("name")));
            } while (result.moveToNext());
        } else {
            LogIt.e("Could not get all track names ");
        }
        result.close();

        return ret;
    }

    public static NewDBTrack getById(String trackName) {
        NewDBTrack ret = null;
        SQLiteDatabase db = DBOpenHelper.getInstance().getReadableDatabase();
        Cursor result = db.query(TABLENAME, new String[] { "name", "datetime",
                "comment" }, "name = " + trackName, null, null, null, null);
        if (result.moveToFirst()) {
            ret = createNewObject(result);
        } else {
            LogIt.e("Could not get a track with name " + trackName);
        }
        result.close();

        return ret;
    }

    private static NewDBTrack createNewObject(Cursor crs) {
        NewDBTrack ret = new NewDBTrack();
        ret.name = crs.getString(crs.getColumnIndex("name"));
        ret.datetime = crs.getString(crs.getColumnIndex("datetime"));
        ret.comment = crs.getString(crs.getColumnIndex("comment"));
        ret.oldname = ret.name;
        return ret;
    }

    public String comment;
    public String datetime;
    public String name;
    private String oldname;

    public void delete() {
        SQLiteDatabase db = DBOpenHelper.getInstance().getWritableDatabase();
        if (db.delete(TABLENAME, "name = " + name, null) == -1) {
            LogIt.e("Could not delete track");
        }
        db.close();
    }

    public void insert() {
        SQLiteDatabase db = DBOpenHelper.getInstance().getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("datetime", datetime);
        values.put("name", name);
        values.put("comment", comment);
        if (db.insert(TABLENAME, null, values) == -1) {
            LogIt.e("Could not insert track");
        }
        oldname = name;
        db.close();

    }

    public void update() {
        SQLiteDatabase db = DBOpenHelper.getInstance().getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("datetime", datetime);
        values.put("name", name);
        values.put("comment", comment);
        if (db.update(TABLENAME, values, "name = " + oldname, null) == -1) {
            LogIt.e("Could not update track");
        }
        oldname = name;
        db.close();
    }

}