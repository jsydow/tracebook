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
 * The DAO-object for tracks. Each object represents a row in the database.
 */
public class NewDBTrack implements NewDBObject {

    private static final String CREATE = "CREATE TABLE IF NOT EXISTS tracks "
            + "( name TEXT PRIMARY KEY," + " datetime TEXT,"
            + " comment TEXT );";
    private static final String DROP = "DROP TABLE IF EXISTS tracks";
    private static final String TABLENAME = "tracks";

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
     * Retrieve a list of all names of all tracks stored in the database.
     * 
     * @return The list of all track names.
     */
    public static List<String> getAllNames() {
        List<String> ret = new ArrayList<String>();

        SQLiteDatabase db = DBOpenHelper.getInstance().getReadableDatabase();
        Cursor result = db.query(TABLENAME, new String[] { "name" }, null,
                null, null, null, "name ASC");
        if (result.moveToFirst()) {
            do {
                ret.add(result.getString(result.getColumnIndex("name")));
            } while (result.moveToNext());
        }
        result.close();
        return ret;
    }

    /**
     * Retrieves a list of all tracks.
     * 
     * @return The list of tracks, may be empty.
     */
    public static List<NewDBTrack> getAllTracks() {
        List<NewDBTrack> ret = new ArrayList<NewDBTrack>();

        SQLiteDatabase db = DBOpenHelper.getInstance().getReadableDatabase();
        Cursor result = db.query(TABLENAME, new String[] { "name", "datetime",
                "comment" }, null, null, null, null, "name ASC");
        if (result.moveToFirst()) {
            do {
                ret.add(createNewObject(new NewDBTrack(), result));
            } while (result.moveToNext());
        }
        result.close();
        return ret;
    }

    /**
     * Retrieve a track from the database with a given name.
     * 
     * @param trackName
     *            The name of the track.
     * @return The track or null if does not exist.
     */
    public static NewDBTrack getById(String trackName) {
        return fillObject(trackName, new NewDBTrack());
    }

    private static NewDBTrack createNewObject(NewDBTrack track, Cursor crs) {
        track.name = crs.getString(crs.getColumnIndex("name"));
        track.datetime = crs.getString(crs.getColumnIndex("datetime"));
        track.comment = crs.getString(crs.getColumnIndex("comment"));
        track.oldname = track.name;
        return track;
    }

    private static NewDBTrack fillObject(String trackName, NewDBTrack track) {
        NewDBTrack ret = null;

        SQLiteDatabase db = DBOpenHelper.getInstance().getReadableDatabase();
        Cursor result = db.query(TABLENAME, new String[] { "name", "datetime",
                "comment" }, "name = '" + trackName + "'", null, null, null,
                null);
        if (result.moveToFirst()) {
            ret = createNewObject(track, result);
        }
        result.close();

        return ret;
    }

    /**
     * The comment of this track.
     */
    public String comment;

    /**
     * The date time.
     */
    public String datetime;

    /**
     * The name of the track. (primary key)
     */
    public String name;

    private String oldname;

    public void delete() {
        SQLiteDatabase db = DBOpenHelper.getInstance().getWritableDatabase();
        if (db.delete(TABLENAME, "name = '" + name + "'", null) == -1) {
            LogIt.e("Could not delete track");
        }
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

    }

    public void save() {
        SQLiteDatabase db = DBOpenHelper.getInstance().getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("datetime", datetime);
        values.put("name", name);
        values.put("comment", comment);
        if (db.update(TABLENAME, values, "name = '" + oldname + "'", null) == -1) {
            LogIt.e("Could not update track");
        }
        oldname = name;
    }

    public void update() {
        fillObject(name, this);
    }

}
