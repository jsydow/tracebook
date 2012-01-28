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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * SQLiteOpenHelper for the database containing all the data recorded.
 * <p>
 * This class is a singleton. Use getInstance() to receive an instance. It must
 * be initialised though by an activity as it needs a context.
 */
public class DBOpenHelper extends SQLiteOpenHelper {

    private static DBOpenHelper instance;

    private static final String NAME = "tracebookdb";
    private static final int VERSION = 4;

    /**
     * Returns an instance of this class.
     * 
     * @return Instance of this class.
     */
    public static DBOpenHelper getInstance() {
        return instance;
    }

    /**
     * Set the instance of this class.
     * 
     * @param instance
     *            The instance of this class.
     */
    public static void setInstance(DBOpenHelper instance) {
        DBOpenHelper.instance = instance;
    }

    /**
     * Default constructor.
     * 
     * @param context
     *            The context of creation.
     */
    public DBOpenHelper(Context context) {
        super(context, NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(NewDBNode.createTable());
        db.execSQL(NewDBTag.createTable());
        db.execSQL(NewDBTrack.createTable());
        db.execSQL(NewDBPointsList.createTable());
        db.execSQL(NewDBMedia.createTable());
        db.execSQL(NewDBBug.createTable());
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(NewDBNode.dropTable());
        db.execSQL(NewDBTag.dropTable());
        db.execSQL(NewDBTrack.dropTable());
        db.execSQL(NewDBPointsList.dropTable());
        db.execSQL(NewDBMedia.dropTable());
        db.execSQL(NewDBBug.dropTable());
        onCreate(db);
    }

}
