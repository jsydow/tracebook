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

import org.mapsforge.android.maps.GeoPoint;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import de.fu.tracebook.util.LogIt;

/**
 * The DAO-object for bugs. Each object represents a row in the database.
 */
public class NewDBBug implements NewDBObject {

    private static final String CREATE = "CREATE TABLE IF NOT EXISTS bugs "
            + "( id INTEGER PRIMARY KEY AUTOINCREMENT," + " description TEXT,"
            + " latitude INTEGER," + " longitude INTEGER," + " track TEXT );";
    private static final String DROP = "DROP TABLE IF EXISTS bugs";
    private static final String TABLENAME = "bugs";

    /**
     * Returns a string that creates the table for this object.
     * 
     * @return The create table string.
     */
    public static String createTable() {
        return CREATE;
    }

    /**
     * Delete all bugs that belong to the given track.
     * 
     * @param trackId
     *            The name of the track.
     */
    public static void deleteByTrack(String trackId) {
        SQLiteDatabase db = DBOpenHelper.getInstance().getWritableDatabase();
        if (db.delete(TABLENAME, "track = '" + trackId + "'", null) == -1) {
            LogIt.e("Could not delete bug");
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
     * Retrieve a bug from the database with a given id.
     * 
     * @param bugId
     *            The id of the bug.
     * @return The bug or null if does not exist.
     */
    public static NewDBBug getById(long bugId) {
        return fillObject(bugId, new NewDBBug());
    }

    /**
     * Retrieves a list of all bugs that belong to the given track.
     * 
     * @param bugId
     *            The name of the track.
     * @return The list of bugs, may be empty.
     */
    public static List<NewDBBug> getByTrack(String bugId) {
        List<NewDBBug> ret = new ArrayList<NewDBBug>();

        SQLiteDatabase db = DBOpenHelper.getInstance().getReadableDatabase();
        Cursor result = db.query(TABLENAME, new String[] { "id", "description",
                "latitude", "longitude", "track " }, "track = '" + bugId + "'",
                null, null, null, "id ASC");
        if (result.moveToFirst()) {
            do {
                ret.add(createNewObject(new NewDBBug(), result));
            } while (result.moveToNext());
        }
        result.close();

        return ret;
    }

    private static NewDBBug createNewObject(NewDBBug ret, Cursor crs) {
        ret.id = crs.getLong(crs.getColumnIndex("id"));
        ret.description = crs.getString(crs.getColumnIndex("description"));
        int latitude = crs.getInt(crs.getColumnIndex("latitude"));
        int longitude = crs.getInt(crs.getColumnIndex("longitude"));
        ret.point = new GeoPoint(latitude, longitude);
        ret.track = crs.getString(crs.getColumnIndex("track"));
        return ret;
    }

    private static NewDBBug fillObject(long bugId, NewDBBug bug) {
        NewDBBug ret = null;
        SQLiteDatabase db = DBOpenHelper.getInstance().getReadableDatabase();
        Cursor result = db.query(TABLENAME, new String[] { "id", "description",
                "latitude", "longitude", "track " }, "id = " + bugId, null,
                null, null, null);
        if (result.moveToFirst()) {
            ret = createNewObject(bug, result);
        }
        result.close();
        return ret;
    }

    /**
     * The description of a bug.
     */
    public String description;
    /**
     * The unique id of a bug in the database.
     */
    public long id;
    /**
     * The position of the bug.
     */
    public GeoPoint point;
    /**
     * The track that the bug belongs to.
     */
    public String track;

    public void delete() {
        SQLiteDatabase db = DBOpenHelper.getInstance().getWritableDatabase();
        if (db.delete(TABLENAME, "id = " + id, null) == -1) {
            LogIt.e("Could not delete bugs");
        }
    }

    public void insert() {
        SQLiteDatabase db = DBOpenHelper.getInstance().getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("description", description);
        values.put("latitude",
                Integer.valueOf(point != null ? point.getLatitudeE6() : 0));
        values.put("longitude",
                Integer.valueOf(point != null ? point.getLongitudeE6() : 0));
        values.put("track", track);
        long rowID = db.insert(TABLENAME, null, values);
        if (rowID == -1) {
            LogIt.e("Could not insert bugs");
        } else {
            this.id = rowID;
        }
    }

    public void save() {
        SQLiteDatabase db = DBOpenHelper.getInstance().getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("description", description);
        values.put("latitude",
                Integer.valueOf(point != null ? point.getLatitudeE6() : 0));
        values.put("longitude",
                Integer.valueOf(point != null ? point.getLongitudeE6() : 0));
        values.put("track", track);
        if (db.update(TABLENAME, values, "id = " + id, null) == -1) {
            LogIt.e("Could not update bugs");
        }
    }

    public void update() {
        fillObject(id, this);
    }

}
