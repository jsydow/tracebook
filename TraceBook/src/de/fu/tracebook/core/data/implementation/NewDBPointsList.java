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
 * The DAO-object for ways and areas. Each object represents a row in the
 * database.
 */
public class NewDBPointsList implements NewDBObject {

    private static final String CREATE = "CREATE TABLE IF NOT EXISTS pointslists "
            + "( id INTEGER PRIMARY KEY AUTOINCREMENT,"
            + " datetime TEXT,"
            + " isarea INTEGER," + " track TEXT );";
    private static final String DROP = "DROP TABLE IF EXISTS pointslists";
    private static final String TABLENAME = "pointslists";

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
     * Retrieve a way from the database with a given id.
     * 
     * @param wayId
     *            The id of the way.
     * @return The way or null if does not exist.
     */
    public static NewDBPointsList getById(long wayId) {
        return fillObject(wayId, new NewDBPointsList());
    }

    /**
     * Retrieves a list of all ways that belong to the given track.
     * 
     * @param name
     *            The name of the track.
     * @return The list of ways, may be empty.
     */
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

    /**
     * The date time.
     */
    public String datetime;

    /**
     * The id of the way. (primary key)
     */
    public long id;

    /**
     * Is this way an area?
     */
    public boolean isArea;

    /**
     * The name of the track this way belongs to.
     */
    public String track;

    public void delete() {
        SQLiteDatabase db = DBOpenHelper.getInstance().getWritableDatabase();
        if (db.delete(TABLENAME, "id = " + id, null) == -1) {
            LogIt.e("Could not delete way");
        }
    }

    public void insert() {
        SQLiteDatabase db = DBOpenHelper.getInstance().getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("datetime", datetime);
        values.put("track", track);
        values.put("isarea", Boolean.valueOf(isArea));
        long rowID = db.insert(TABLENAME, null, values);
        if (rowID == -1) {
            LogIt.e("Could not insert way");
        } else {
            this.id = rowID;
        }

    }

    public void save() {
        SQLiteDatabase db = DBOpenHelper.getInstance().getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("datetime", datetime);
        values.put("track", track);
        values.put("isarea", Boolean.valueOf(isArea));
        if (db.update(TABLENAME, values, "id = " + id, null) == -1) {
            LogIt.e("Could not update way");
        }
    }

    public void update() {
        fillObject(id, this);
    }
}
