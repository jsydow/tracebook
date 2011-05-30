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

package de.fu.tracebook.core.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * @author js
 * 
 */
public class DataDb extends SQLiteOpenHelper {

    /**
     * The name of the database used by TraceBook.
     */
    private static final String DATABASE_NAME = "db_tracebook_data";
    /**
     * By incrementing this number, the database is reseted. Never decrease!
     */
    private static final int DATABASE_VERSION = 1;

    /**
     * @param context
     */
    public DataDb(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase arg0) {
        // TODO Auto-generated method stub

        createIndex(arg0, "idx_tracebook", "TODO", "TODO");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
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
