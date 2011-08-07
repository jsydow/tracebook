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
    
    // TODO ship tag database as sql-database instead of xml file

    /**
     * By incrementing this number, the database is reseted. Never decrease!
     */
    private static final int DATABASE_VERSION = 24;
    /**
     * The name of the database used by TraceBook.
     */
    private static final String DATABASE_NAME = "db_tracebook_tags";
    /**
     * Table that contains all information for tags.
     */
    private static final String DICT_TABLE_NAME = "dictionary";
    /**
     * Table that contains the history of used tags.
     */
    private static final String HISTORY_TABLE_NAME = "history";
    /**
     * The name column of the table "dictionary".
     */
    static final String DICT_COLUMN_NAME = "name";
    /**
     * The key column of the table "dictionary".
     */
    static final String DICT_COLUMN_KEY = "key";
    /**
     * The value column of the table "dictionary".
     */
    static final String DICT_COLUMN_VALUE = "value";
    /**
     * The description column of the table "dictionary".
     */
    static final String DICT_COLUMN_DESC = "description";
    /**
     * The wiki link column of the table "dictionary".
     */
    static final String DICT_COLUMN_LINK = "wikilink";
    /**
     * The image column of the table "dictionary".
     */
    static final String DICT_COLUMN_IMG = "image";
    /**
     * The language column of the table "dictionary".
     */
    static final String DICT_COLUMN_LANG = "language";
    /**
     * The keywords column of the table "dictionary".
     */
    static final String DICT_COLUMN_KEYWORDS = "keywords";
    /**
     * The value_type column of the table "dictionary".
     */
    static final String DICT_COLUMN_TYPE = "value_type";

    /**
     * The key column of the table "history".
     */
    static final String HISTORY_COLUMN_KEY = "key";
    /**
     * The value column of the table "history".
     */
    static final String HISTORY_COLUMN_VALUE = "value";
    /**
     * The last_use column of the table "history".
     */
    static final String HISTORY_COLUMN_LAST_USE = "last_use";
    /**
     * The use_count column of the table "history".
     */
    static final String HISTORY_COLUMN_USE_COUNT = "use_count";
    /**
     * String to create table.
     */
    private static final String DICT_TABLE_CREATE = "CREATE TABLE "
            + DICT_TABLE_NAME + " (" + DICT_COLUMN_KEY + " TEXT, "
            + DICT_COLUMN_VALUE + " TEXT, " + DICT_COLUMN_NAME + " TEXT, "
            + DICT_COLUMN_DESC + " TEXT, " + DICT_COLUMN_LINK + " TEXT, "
            + DICT_COLUMN_IMG + " TEXT, " + DICT_COLUMN_LANG + " TEXT, "
            + DICT_COLUMN_KEYWORDS + " TEXT, " + DICT_COLUMN_TYPE + " TEXT);";
    /**
     * String to create table.
     */
    private static final String HISTORY_TABLE_CREATE = "CREATE TABLE "
            + HISTORY_TABLE_NAME + " (" + HISTORY_COLUMN_KEY + " TEXT, "
            + HISTORY_COLUMN_VALUE + " TEXT, " + HISTORY_COLUMN_USE_COUNT
            + " INT, " + HISTORY_COLUMN_LAST_USE + " INT);";
    /**
     * String to drop table.
     */
    private static final String DICT_TABLE_DROP = "DROP TABLE IF EXISTS "
            + DICT_TABLE_NAME;
    /**
     * String to drop table.
     */
    private static final String HISTORY_TABLE_DROP = "DROP TABLE IF EXISTS "
            + HISTORY_TABLE_NAME;

    /**
     * Returns the name of the table.
     * 
     * @return the tableName
     */
    static String getDictTableName() {
        return DICT_TABLE_NAME;
    }

    /**
     * Returns the name of the table.
     * 
     * @return the tableName
     */
    static String getHistoryTableName() {
        return HISTORY_TABLE_NAME;
    }

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
     * Returns a list of key and value columns.
     * 
     * @return key and value
     */
    static String[] getTagColumns() {
        return new String[] { DICT_COLUMN_KEY, DICT_COLUMN_VALUE };
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

        createIndex(db, "language_idx", DICT_TABLE_NAME, "language");
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
