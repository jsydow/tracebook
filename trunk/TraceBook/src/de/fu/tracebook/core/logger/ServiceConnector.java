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

package de.fu.tracebook.core.logger;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import de.fu.tracebook.util.LogIt;

/**
 * This class provides methods for controlling the {@link WaypointLogService}.
 */
public final class ServiceConnector {
    private static Activity activity = null;
    private static volatile LoggerServiceConnection conn = null;
    private static final String LOG_TAG = "LOGSERVICECLIENT";
    private static boolean started = false;

    /**
     * @return get a reference to the loggerService
     */
    public static ILoggerService getLoggerService() {
        if (conn != null)
            return conn.getLoggerService();
        return null;
    }

    /**
     * Bind the logger service to this activity.
     */
    public static synchronized void initService() {
        if (conn == null) {
            conn = new LoggerServiceConnection();
            Intent intent = new Intent();
            intent.setClassName(activity.getPackageName(),
                    WaypointLogService.class.getName());
            activity.bindService(intent, conn, Context.BIND_AUTO_CREATE);
            LogIt.d("bindService()");
        } else
            LogIt.d("Cannot bind - service already bound");
    }

    /**
     * Release the logger service.
     */
    public static synchronized void releaseService() {
        if (conn != null) {
            activity.unbindService(conn);
            conn = null;
            LogIt.d("unbindService()");
        } else
            LogIt.d("Cannot unbind - service not bound");
    }

    /**
     * Start the logging service (collect GPS data).
     * 
     * @param act
     *            reference to an activity for which the service should be
     *            binded
     */
    public static void startService(Activity act) {
        activity = act;
        if (started) {
            LogIt.d("Service already started");
        } else {
            Intent intent = new Intent();
            intent.setClassName(activity.getPackageName(),
                    WaypointLogService.class.getName());
            activity.startService(intent);
            LogIt.d("startService()");
            started = true;
        }
    }

    /**
     * Stop logging service (stop collecting GPS data).
     */
    public static void stopService() {
        if (!started) {
            LogIt.d("Service not yet started");
        } else {
            Intent intent = new Intent();
            intent.setClassName(activity.getPackageName(),
                    WaypointLogService.class.getName());
            activity.stopService(intent);
            LogIt.d("stopService()");
        }
    }

    private ServiceConnector() {
        // Empty constructor. Does nothing.
    }
}
