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
import android.database.sqlite.SQLiteQueryBuilder;
import de.fu.tracebook.core.data.DataTrackInfo;
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
        }
        result.close();
        return ret;
    }

    public static NewDBTrack getById(String trackName) {
        return fillObject(trackName, new NewDBTrack());
    }

    public static DataTrackInfo getTrackInfo(String trackname) {
        String name = trackname;
        String datetime = "";
        String comment = null;
        int nodes = 0;
        int ways = 0;

        SQLiteDatabase db = DBOpenHelper.getInstance().getReadableDatabase();
        SQLiteQueryBuilder query = new SQLiteQueryBuilder();
        query.setTables("tracks LEFT OUTER JOIN pointslists ON tracks.name = pointslists.track LEFT OUTER JOIN nodes ON tracks.name = nodes.track");

        Cursor result = query.query(db, new String[] { "name", "datetime",
                "comment", "SUM(pointslists.id)  AS waycount",
                "SUM(nodes.id) AS nodecount" }, "tracks.name = '" + trackname
                + "'", null, "tracks.name", null, null);
        if (result.moveToFirst()) {
            datetime = result.getString(result.getColumnIndex("datetime"));
            comment = result.getString(result.getColumnIndex("comment"));
            nodes = result.getInt(result.getColumnIndex("nodecount"));
            ways = result.getInt(result.getColumnIndex("waycount"));
        }
        result.close();
        return new DataTrackInfo(name, datetime, comment, nodes, ways, 0);
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

    public String comment;
    public String datetime;

    public String name;

    private String oldname;

    public void delete() {
        SQLiteDatabase db = DBOpenHelper.getInstance().getWritableDatabase();
        if (db.delete(TABLENAME, "name = '" + name + "'", null) == -1) {
            LogIt.e("Could not delete track");
        }
        NewDBMedia.deleteByTrack(name);
        NewDBNode.deleteByTrack(name);
        NewDBPointsList.deleteByTrack(name);
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
