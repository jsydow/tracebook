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

package de.fu.tracebook.core.logger;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;

/**
 * Stub for RPC communication with the logger Service.
 */
public class LoggerServiceConnection implements ServiceConnection {

    /**
     * Reference for loggerService.
     */
    ILoggerService loggerService = null;

    /**
     * @return get a reference to the loggerService
     */
    public ILoggerService getLoggerService() {
        return loggerService;
    }

    public void onServiceConnected(ComponentName className, IBinder boundService) {
        loggerService = ILoggerService.Stub.asInterface(boundService);
        // LogIt.d(LOG_TAG, "onServiceConnected");
    }

    public void onServiceDisconnected(ComponentName className) {
        loggerService = null;
        // LogIt.d(LOG_TAG, "onServiceDisconnected");
    }
}
