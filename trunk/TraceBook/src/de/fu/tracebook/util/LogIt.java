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

package de.fu.tracebook.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import de.fu.tracebook.core.data.NewStorage;

/**
 * This a general logging class and should be used for all logging action. It
 * can make pop-up toasts that are visible to the user and logs that create a
 * log file or log to logcat. There is a log level which is an integer from 1 to
 * 5. 5 is the highest level and is equivalent to error in logcat. 1 is
 * equivalent to verbose in logcat.
 */
public final class LogIt {

    /**
     * Log using the Android logging method.
     */
    public static final int LOGMETHOD_ANDROID = 1;
    /**
     * Log to file.
     */
    public static final int LOGMETHOD_FILE = 2;

    /**
     * Maximum value for a logging level.
     */
    public static final int MAX_LOG_LEVEL = 5;

    /**
     * Minimum value for a logging level.
     */
    public static final int MIN_LOG_LEVEL = 1;

    /**
     * The tag for logging.
     */
    public static final String TRACEBOOK_TAG = "TraceBook";

    private static final String LOG_PREFIX = "TraceBook";

    private static int maxLogLevel = 1000;

    private static int method = LOGMETHOD_ANDROID;

    private static int minLogLevel = -1000;

    /**
     * Logs message with debug priority.
     * 
     * @param message
     *            The message to be logged
     */
    public static synchronized void d(String message) {
        log(TRACEBOOK_TAG, message, 2);
    }

    /**
     * Logs message with error priority.
     * 
     * @param message
     *            The message to be logged
     */
    public static synchronized void e(String message) {
        log(TRACEBOOK_TAG, message, 5);
    }

    /**
     * Log a message.
     * 
     * @param prefix
     *            The prefix specifying the the origin of the log message.
     * @param message
     *            The actual message.
     * @param logLevel
     *            The importance of the log message. 0-5
     */
    public static synchronized void log(String prefix, String message,
            int logLevel) {
        if ((logLevel <= maxLogLevel) || (logLevel <= minLogLevel)) {
            switch (method) {
            case LOGMETHOD_FILE:
                File logFile = new File(NewStorage.getTraceBookDirPath()
                        + File.separator + "log.txt");
                try {
                    FileWriter fw = new FileWriter(logFile);
                    fw.append(prefix + ": " + message);
                    fw.close();
                } catch (IOException e) {
                    LogIt.e("Logging error: Could not log to file!");
                }
                break;
            case LOGMETHOD_ANDROID:
                switch (logLevel) {
                case 1:
                    Log.v(LOG_PREFIX, prefix + ": " + message);
                    break;
                case 2:
                    Log.d(LOG_PREFIX, prefix + ": " + message);
                    break;
                case 3:
                    Log.i(LOG_PREFIX, prefix + ": " + message);
                    break;
                case 4:
                    Log.w(LOG_PREFIX, prefix + ": " + message);
                    break;
                case 5:
                    Log.e(LOG_PREFIX, prefix + ": " + message);
                    break;
                default:
                    break;
                }
                break;
            default:
                break;
            }
        }
    }

    /**
     * Shows a toast with a given message.
     * 
     * @param app
     *            The activity that shows the toast.
     * @param msg
     *            The message to display.
     */
    public static void popup(Context app, String msg) {
        Toast.makeText(app.getApplicationContext(), msg, Toast.LENGTH_LONG)
                .show();
    }

    public static void printStackTrace() {
        for (StackTraceElement e : Thread.currentThread().getStackTrace()) {
            d(e.toString());
        }
    }

    /**
     * Sets the method of logging. Use the constants above.
     * 
     * @param newMethod
     *            the new logging method.
     */
    public static synchronized void setLogMethod(int newMethod) {
        method = newMethod;
    }

    /**
     * If you want to log only less important messages set the maximum log level
     * lower.
     * 
     * @param logLevel
     *            the new maximum logging level.
     */
    public static synchronized void setMaxLogLevel(int logLevel) {
        if (logLevel > MAX_LOG_LEVEL)
            maxLogLevel = MAX_LOG_LEVEL;
        else
            maxLogLevel = logLevel;
    }

    /**
     * If you want to log only more important messages set minimum log level
     * higher.
     * 
     * @param logLevel
     *            The new minimum logging level.
     */
    public static synchronized void setMinLogLevel(int logLevel) {
        if (logLevel < MIN_LOG_LEVEL)
            minLogLevel = MIN_LOG_LEVEL;
        else
            minLogLevel = logLevel;
    }

    /**
     * Logs message with error priority.
     * 
     * @param message
     *            The message to be logged
     */
    public static synchronized void w(String message) {
        log(TRACEBOOK_TAG, message, 4);
    }

    private LogIt() {
        // Empty constructor. Does nothing.
    }
}
