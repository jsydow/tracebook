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

import java.sql.SQLException;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import de.fu.tracebook.util.LogIt;

/**
 * @author js
 * 
 */
public class DataOpenHelper extends OrmLiteSqliteOpenHelper {

    private static DataOpenHelper instance = null;

    private static final String name = "ormtracebookdb";
    private static final int version = 2;

    /**
     * @return the instance
     */
    public static DataOpenHelper getInstance() {
        return instance;
    }

    private Dao<DBMedia, Object> mediaDAO = null;

    private Dao<DBNode, Long> nodeDAO = null;
    private Dao<DBPointsList, Long> pointslistDAO = null;
    private Dao<DBTag, Object> tagDAO = null;

    private Dao<DBTrack, String> trackDAO = null;

    /**
     * @param context
     *            context
     */
    public DataOpenHelper(Context context) {
        super(context, name, null, version);
    }

    // TODO create dao objects here

    /**
     * @return the mediaDAO
     */
    public Dao<DBMedia, Object> getMediaDAO() {
        if (mediaDAO == null) {
            try {
                mediaDAO = getDao(DBMedia.class);
            } catch (SQLException e) {
                LogIt.e(LogIt.TRACEBOOK_TAG, "Could not create DAO object.");
            }
        }

        return mediaDAO;
    }

    /**
     * @return the nodeDAO
     */
    public Dao<DBNode, Long> getNodeDAO() {
        if (nodeDAO == null) {
            try {
                nodeDAO = getDao(DBNode.class);
            } catch (SQLException e) {
                LogIt.e(LogIt.TRACEBOOK_TAG, "Could not create DAO object.");
            }
        }

        return nodeDAO;
    }

    /**
     * @return the pointslistDAO
     */
    public Dao<DBPointsList, Long> getPointslistDAO() {
        if (pointslistDAO == null) {
            try {
                pointslistDAO = getDao(DBPointsList.class);
            } catch (SQLException e) {
                LogIt.e(LogIt.TRACEBOOK_TAG, "Could not create DAO object.");
            }
        }

        return pointslistDAO;
    }

    /**
     * @return the tagDAO
     */
    public Dao<DBTag, Object> getTagDAO() {
        if (tagDAO == null) {
            try {
                tagDAO = getDao(DBTag.class);
            } catch (SQLException e) {
                LogIt.e(LogIt.TRACEBOOK_TAG, "Could not create DAO object.");
            }
        }

        return tagDAO;
    }

    /**
     * @return dao
     */
    public Dao<DBTrack, String> getTrackDAO() {
        if (trackDAO == null) {
            try {
                trackDAO = getDao(DBTrack.class);
            } catch (SQLException e) {
                LogIt.e(LogIt.TRACEBOOK_TAG,
                        "Could not create DAO object." + e.getMessage());
            }
        }

        return trackDAO;
    }

    @Override
    public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource) {
        try {
            TableUtils.createTable(connectionSource, DBTrack.class);
            TableUtils.createTable(connectionSource, DBMedia.class);
            TableUtils.createTable(connectionSource, DBNode.class);
            TableUtils.createTable(connectionSource, DBPointsList.class);
            TableUtils.createTable(connectionSource, DBTag.class);
            // TODO create tables here

        } catch (SQLException e) {
            LogIt.e(LogIt.TRACEBOOK_TAG, "Could not create table.");
        }

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource,
            int oldVersion, int newVersion) {
        try {
            TableUtils.dropTable(connectionSource, DBTrack.class, true);
            TableUtils.dropTable(connectionSource, DBMedia.class, true);
            TableUtils.dropTable(connectionSource, DBNode.class, true);
            TableUtils.dropTable(connectionSource, DBPointsList.class, true);
            TableUtils.dropTable(connectionSource, DBTag.class, true);
            // TODO drop tables here
            onCreate(db, connectionSource);
        } catch (SQLException e) {
            LogIt.e(LogIt.TRACEBOOK_TAG, "Could not drop table.");
            throw new RuntimeException(e);
        }
    }

    /**
     * @param instance
     *            the instance to set
     */
    public void setInstance() {
        DataOpenHelper.instance = this;
    }

}
