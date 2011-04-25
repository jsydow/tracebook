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

package de.fu.tracebook.core.data.db;

import java.util.List;
import java.util.Vector;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import de.fu.tracebook.util.LogIt;

/**
 * Provide comfortable access to the history database.
 */
public class HistoryDb {
    private SQLiteDatabase db;
    private TagDbOpenHelper helper;

    /**
     * Constructor, opens the database.
     * 
     * @param context
     *            A context, probably the activity that uses the database.
     */
    public HistoryDb(Context context) {
        super();
        helper = new TagDbOpenHelper(context);
    }

    /**
     * Retrieves the history of used tags.
     * 
     * @param mostUsed
     *            If resulting list should be ordered by most used tags or
     *            recently used ones.
     * @param length
     *            The length of the returned list.
     * @return The history as list. Only key and value fields of the
     *         TagSearchResult are used.
     */
    public List<TagSearchResult> getHistory(boolean mostUsed, int length) {
        openDb();

        if (db != null && db.isOpen()) {
            List<TagSearchResult> tags = new Vector<TagSearchResult>();

            fillTagListWithHistoryResults(tags, mostUsed, length);

            closeDb();
            return tags;
        } else {
            LogIt.e("TagDataBase", "Could not open Database.");
            return null;
        }
    }

    /**
     * If a tag is used by the user. This method should be called so that the
     * database is updated.
     * 
     * @param key
     *            The key of the tag.
     * @param value
     *            The value of the tag.
     */
    public void updateTag(String key, String value) {

        if (rowsCountWithTag(key, value) > 0) {
            SQLiteDatabase wdb = helper.getWritableDatabase();
            if (wdb != null && wdb.isOpen()) {
                wdb.execSQL("UPDATE " + TagDbOpenHelper.getHistoryTableName()
                        + " SET " + TagDbOpenHelper.HISTORY_COLUMN_USE_COUNT
                        + "=" + TagDbOpenHelper.HISTORY_COLUMN_USE_COUNT
                        + "+1, " + TagDbOpenHelper.HISTORY_COLUMN_LAST_USE
                        + "=" + System.currentTimeMillis() + " WHERE "
                        + TagDbOpenHelper.HISTORY_COLUMN_KEY + "='" + key
                        + "' AND " + TagDbOpenHelper.HISTORY_COLUMN_VALUE
                        + "='" + value + "'");
                wdb.close();
            } else {
                LogIt.e("HistoryDb", "Could not open database to write.");
            }
        } else {
            SQLiteDatabase wdb = helper.getWritableDatabase();
            if (wdb != null && wdb.isOpen()) {
                ContentValues values = new ContentValues();
                values.put(TagDbOpenHelper.HISTORY_COLUMN_LAST_USE, Long
                        .valueOf(System.currentTimeMillis()));
                values.put(TagDbOpenHelper.HISTORY_COLUMN_KEY, key);
                values.put(TagDbOpenHelper.HISTORY_COLUMN_VALUE, value);
                values.put(TagDbOpenHelper.HISTORY_COLUMN_USE_COUNT, Integer
                        .valueOf(1));
                wdb.insert(TagDbOpenHelper.getHistoryTableName(), null, values);
                wdb.close();
            } else {
                LogIt.e("HistoryDb", "Could not open database to write.");
            }
        }
        return;
    }

    /**
     * Closes the database. Dot not forget to call this method!
     */
    private void closeDb() {
        db.close();
    }

    /**
     * Self-explaining. Fills a given tag list with tag that are retrieved from
     * the database.
     * 
     * @param tags
     *            The list that should be filled.
     * @param mostUsed
     *            Ordering: most used is first in list, or recently used is
     *            first. True means most used tag will be first.
     * @param length
     *            The number of tags that are returned.
     */
    private void fillTagListWithHistoryResults(List<TagSearchResult> tags,
            boolean mostUsed, int length) {
        String orderBy = null;
        if (mostUsed) {
            orderBy = TagDbOpenHelper.HISTORY_COLUMN_USE_COUNT + " DESC";
        } else {
            orderBy = TagDbOpenHelper.HISTORY_COLUMN_LAST_USE + " DESC";
        }

        Cursor result = db.query(TagDbOpenHelper.getHistoryTableName(),
                TagDbOpenHelper.getTagColumns(), null, null, null, null,
                orderBy, Integer.valueOf(length).toString());

        if (result.moveToFirst()) {
            while (!result.isAfterLast()) {
                // insert row to tags list
                tags
                        .add(new TagSearchResult(
                                result
                                        .getString(result
                                                .getColumnIndex(TagDbOpenHelper.HISTORY_COLUMN_KEY)),
                                result
                                        .getString(result
                                                .getColumnIndex(TagDbOpenHelper.HISTORY_COLUMN_VALUE)),
                                null, null, null, null, null, null));

                result.moveToNext();
            }
        }
        result.close();

    }

    /**
     * Establishes read only access to the database.
     */
    private void openDb() {
        if (db != null && db.isOpen()) {
            db.close();
        }

        db = helper.getReadableDatabase();
    }

    /**
     * Returns the number of rows that are in the database with a given tag.
     * 
     * @param key
     *            The value of the tag.
     * @param value
     *            The value of the tag.
     * @return The number of rows.
     */
    private int rowsCountWithTag(String key, String value) {
        int rowCount = 0;

        openDb();

        if (db != null && db.isOpen()) {
            Cursor crs = db.query(TagDbOpenHelper.getHistoryTableName(),
                    new String[] { "COUNT(*)" },
                    TagDbOpenHelper.HISTORY_COLUMN_KEY + "='" + key + "' AND "
                            + TagDbOpenHelper.HISTORY_COLUMN_VALUE + "='"
                            + value + "'", null, null, null, null);

            if (crs.moveToFirst()) {
                rowCount = crs.getInt(0);
            }
            crs.close();
            closeDb();
        } else {
            LogIt.e("TagDataBase", "Could not open Database.");
        }
        return rowCount;
    }

    /**
     * Returns the TagDbOpenHelper object. Should not be used.
     * 
     * @return The {@link TagDbOpenHelper} variable.
     */
    TagDbOpenHelper getHelper() {
        return helper;
    }
}
