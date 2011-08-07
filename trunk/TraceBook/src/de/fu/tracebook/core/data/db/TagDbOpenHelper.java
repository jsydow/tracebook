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

package de.fu.tracebook.core.data.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * The DB open helper automatically creates or recreates the database. Use it to
 * get a SQLiteDataBase-object.
 */
public class TagDbOpenHelper extends SQLiteOpenHelper {

    /**
     * The name of the database used by TraceBook.
     */
    private static final String DATABASE_NAME = "db_tracebook_tags";

    /**
     * By incrementing this number, the database is reseted. Never decrease!
     */
    private static final int DATABASE_VERSION = 24;

    /**
     * The description column of the table "dictionary".
     */
    static final String DICT_COLUMN_DESC = "description";

    /**
     * The image column of the table "dictionary".
     */
    static final String DICT_COLUMN_IMG = "image";

    /**
     * The key column of the table "dictionary".
     */
    static final String DICT_COLUMN_KEY = "key";

    /**
     * The keywords column of the table "dictionary".
     */
    static final String DICT_COLUMN_KEYWORDS = "keywords";

    /**
     * The language column of the table "dictionary".
     */
    static final String DICT_COLUMN_LANG = "language";

    /**
     * The wiki link column of the table "dictionary".
     */
    static final String DICT_COLUMN_LINK = "wikilink";

    /**
     * The name column of the table "dictionary".
     */
    static final String DICT_COLUMN_NAME = "name";

    /**
     * The value_type column of the table "dictionary".
     */
    static final String DICT_COLUMN_TYPE = "value_type";

    /**
     * The value column of the table "dictionary".
     */
    static final String DICT_COLUMN_VALUE = "value";

    /**
     * Table that contains all information for tags.
     */
    static final String DICT_NAME_OF_TABLE = "dictionary";

    /**
     * String to create table.
     */
    static final String DICT_TABLE_CREATE = "CREATE TABLE "
            + DICT_NAME_OF_TABLE + " (" + DICT_COLUMN_KEY + " TEXT, "
            + DICT_COLUMN_VALUE + " TEXT, " + DICT_COLUMN_NAME + " TEXT, "
            + DICT_COLUMN_DESC + " TEXT, " + DICT_COLUMN_LINK + " TEXT, "
            + DICT_COLUMN_IMG + " TEXT, " + DICT_COLUMN_LANG + " TEXT, "
            + DICT_COLUMN_KEYWORDS + " TEXT, " + DICT_COLUMN_TYPE + " TEXT);";

    /**
     * String to drop table.
     */
    static final String DICT_TABLE_DROP = "DROP TABLE IF EXISTS "
            + DICT_NAME_OF_TABLE;

    /**
     * The key column of the table "history".
     */
    static final String HISTORY_COLUMN_KEY = "key";

    /**
     * The last_use column of the table "history".
     */
    static final String HISTORY_COLUMN_LAST_USE = "last_use";

    /**
     * The use_count column of the table "history".
     */
    static final String HISTORY_COLUMN_USE_COUNT = "use_count";

    /**
     * The value column of the table "history".
     */
    static final String HISTORY_COLUMN_VALUE = "value";

    /**
     * Table that contains the history of used tags.
     */
    static final String HISTORY_NAME_OF_TABLE = "history";

    /**
     * String to create table.
     */
    static final String HISTORY_TABLE_CREATE = "CREATE TABLE "
            + HISTORY_NAME_OF_TABLE + " (" + HISTORY_COLUMN_KEY + " TEXT, "
            + HISTORY_COLUMN_VALUE + " TEXT, " + HISTORY_COLUMN_USE_COUNT
            + " INT, " + HISTORY_COLUMN_LAST_USE + " INT);";
    /**
     * String to drop table.
     */
    static final String HISTORY_TABLE_DROP = "DROP TABLE IF EXISTS "
            + HISTORY_NAME_OF_TABLE;

    /**
     * Returns a list of all columns for the dictionary table.
     * 
     * @return all columns
     */
    static String[] getDictColumns() {
        return new String[] { DICT_COLUMN_KEY, DICT_COLUMN_VALUE,
                DICT_COLUMN_NAME, DICT_COLUMN_DESC, DICT_COLUMN_LINK,
                DICT_COLUMN_IMG, DICT_COLUMN_LANG, DICT_COLUMN_TYPE };
    }

    /**
     * Returns the name of the table.
     * 
     * @return the tableName
     */
    static String getDictTableName() {
        return DICT_NAME_OF_TABLE;
    }

    /**
     * Returns the name of the table.
     * 
     * @return the tableName
     */
    static String getHistoryTableName() {
        return HISTORY_NAME_OF_TABLE;
    }

    /**
     * Creates a TagSearchResult object out of a cursor position.
     * 
     * @param result
     *            The initialized cursor.
     * @return The result object of the row.
     */
    static TagSearchResult getResultFromCursor(Cursor result) {
        String name = result.getString(result.getColumnIndex(DICT_COLUMN_NAME));
        String key = result.getString(result.getColumnIndex(DICT_COLUMN_KEY));
        String value = result.getString(result
                .getColumnIndex(DICT_COLUMN_VALUE));
        String description = result.getString(result
                .getColumnIndex(DICT_COLUMN_DESC));
        String wikilink = result.getString(result
                .getColumnIndex(DICT_COLUMN_LINK));
        String image = result.getString(result.getColumnIndex(DICT_COLUMN_IMG));
        String language = result.getString(result
                .getColumnIndex(DICT_COLUMN_LANG));
        String valueType = result.getString(result
                .getColumnIndex(DICT_COLUMN_TYPE));

        return new TagSearchResult(key, value, name, description, wikilink,
                image, language, valueType);
    }

    /**
     * Returns a list of key and value columns.
     * 
     * @return key and value
     */
    static String[] getTagColumns() {
        return new String[] { DICT_COLUMN_KEY, DICT_COLUMN_VALUE };
    }

    /**
     * Default constructor.
     * 
     * @param context
     *            A Context, probably the Activity that starts the SQL
     *            connection
     */
    TagDbOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DICT_TABLE_DROP);
        db.execSQL(HISTORY_TABLE_DROP);
        db.execSQL(DICT_TABLE_CREATE);
        db.execSQL(HISTORY_TABLE_CREATE);

        createIndex(db, "language_idx", DICT_NAME_OF_TABLE, "language");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DICT_TABLE_DROP);
        db.execSQL(HISTORY_TABLE_DROP);
        onCreate(db);
    }

    /**
     * Creates an index on the given table, if it does not exist yet.
     * 
     * @param db
     *            SQLite database object.
     * @param idx
     *            Name of the index to be created.
     * @param table
     *            Table to create the index on.
     * @param columns
     *            Columns to be included in the index. Order matters.
     */
    private void createIndex(SQLiteDatabase db, String idx, String table,
            String columns) {
        db.execSQL(String.format("CREATE INDEX IF NOT EXISTS %s ON %s (%s);",
                idx, table, columns));
    }
}
