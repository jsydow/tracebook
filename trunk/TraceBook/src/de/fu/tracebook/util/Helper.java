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

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.mapsforge.android.maps.GeoPoint;
import org.mapsforge.android.maps.ItemizedOverlay;
import org.mapsforge.android.maps.OverlayItem;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import de.fu.tracebook.R;
import de.fu.tracebook.core.data.IDataPointsList;
import de.fu.tracebook.core.data.IDataTrack;
import de.fu.tracebook.core.data.StorageFactory;
import de.fu.tracebook.core.data.db.TagSearchResult;
import de.fu.tracebook.core.logger.ServiceConnector;
import de.fu.tracebook.gui.activity.MapsForgeActivity;

/**
 * General helper class to feature some useful functions.
 */
public final class Helper {

    private static Drawable defaultMarker;

    /**
     * Notification ID for the tracking notification.
     */
    static final int TRACKING_NOTIFY_ID = 1;

    /**
     * This Method show a alert dialog to save the Track and give a name for the
     * current track.
     * 
     * Shows a dialog box that saves the current track.
     * 
     * @param activity
     *            Activity in which the Dialog has to be display The activity
     *            that starts the dialog.
     */
    public static void alertStopTracking(final Activity activity) {

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        final EditText input = new EditText(activity);
        input.setHint(StorageFactory.getStorage().getTrack().getName());
        builder.setView(input);
        builder.setTitle(activity.getResources().getString(
                R.string.alert_newtrackActivity_saveSetTrack));
        builder.setMessage(
                activity.getResources().getString(R.string.alert_global_exit))
                .setCancelable(false)
                .setPositiveButton(
                        activity.getResources().getString(
                                R.string.alert_global_yes),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                // set track name
                                String value = input.getText().toString()
                                        .trim();
                                if (!value.equals("")) {
                                    StorageFactory.getStorage().getTrack()
                                            .setName(value);
                                }

                                // send notification toast for user
                                LogIt.popup(
                                        activity,
                                        activity.getResources()
                                                .getString(
                                                        R.string.alert_global_trackName)
                                                + " "
                                                + StorageFactory.getStorage()
                                                        .getTrack().getName());

                                // stop logging
                                try {
                                    ServiceConnector.getLoggerService()
                                            .stopTrack();
                                } catch (RemoteException e) {
                                    e.printStackTrace();
                                }

                                activity.finish();

                            }
                        })
                .setNegativeButton(
                        activity.getResources().getString(
                                R.string.alert_global_notSaveAndClose),
                        new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog,
                                    int which) {
                                dialog.cancel();

                            }
                        })
                .setNeutralButton(
                        activity.getResources().getString(
                                R.string.alert_global_no),
                        new DialogInterface.OnClickListener() {

                            public void onClick(final DialogInterface dialog,
                                    int which) {

                                (new AsyncTask<Void, Void, Void>() {

                                    @Override
                                    protected Void doInBackground(
                                            Void... params) {
                                        String trackname = StorageFactory
                                                .getStorage().getTrack()
                                                .getName();

                                        if (StorageFactory.getStorage()
                                                .getTrack().isNew()) {
                                            StorageFactory.getStorage()
                                                    .deleteTrack(trackname);
                                        }
                                        StorageFactory.getStorage()
                                                .unloadAllTracks();
                                        return null;
                                    }

                                    @Override
                                    protected void onPreExecute() {
                                        dialog.cancel();
                                        try {
                                            ServiceConnector.getLoggerService()
                                                    .pauseLogging();

                                        } catch (RemoteException e) {

                                            e.printStackTrace();
                                        }
                                    }
                                }).execute();

                                activity.finish();
                            }
                        });

        builder.show();

    }

    /**
     * This method check's the status of the visibility status bar.
     * 
     * @param activity
     *            context of the application
     * @return Returns visibility of the status bar which was checked/unchecked
     *         by the user at the preferences.
     */
    public static boolean checkStatusbarVisibility(Activity activity) {
        // Get the app's shared preferences
        SharedPreferences appPreferences = PreferenceManager
                .getDefaultSharedPreferences(activity);
        return appPreferences.getBoolean("check_visbilityStatusbar", false);
    }

    /**
     * Gets the current DataTrack for convenience.
     * 
     * @return the current DataTrack object
     */
    public static IDataTrack currentTrack() {
        return StorageFactory.getStorage().getTrack();
    }

    /**
     * This method cut the given String to a maximum character number.
     * 
     * @param toCut
     *            String to cut
     * @param maxChar
     *            Maximum character number to cut the given string
     * @return String which have only maxChar characters
     */
    public static String cutString(String toCut, int maxChar) {
        if (toCut != null && toCut.length() > maxChar)
            return toCut.trim().subSequence(0, maxChar) + "…";
        else
            return toCut;
    }

    /**
     * Gets a OverlayItem with default marker and no position added.
     * 
     * @param ctx
     *            The Activity context
     * @return a new OverlayItem
     */
    public static OverlayItem getOverlayItem(Context ctx) {
        if (defaultMarker == null)
            defaultMarker = ItemizedOverlay.boundCenterBottom(ctx
                    .getResources().getDrawable(R.drawable.card_marker_red));
        return new OverlayItem(null, null, null, defaultMarker);
    }

    /**
     * Creates a new OverlayItem.
     * 
     * @param pos
     *            position of the marker
     * @param marker
     *            id of the Graphics object to use
     * @param act
     *            context of the application
     * @return the new OverlayItem
     */
    public static OverlayItem getOverlayItem(GeoPoint pos, int marker,
            Activity act) {
        return getOverlayItem(pos, marker, act, false);
    }

    /**
     * Creates a new OverlayItem.
     * 
     * @param pos
     *            position of the marker
     * @param marker
     *            id of the Graphics object to use
     * @param act
     *            context of the application
     * @param center
     *            the center of the icon is at the given pos
     * @return the new OverlayItem
     */
    public static OverlayItem getOverlayItem(GeoPoint pos, int marker,
            Activity act, boolean center) {
        final OverlayItem oi = new OverlayItem(pos, null, null);
        Drawable icon = act.getResources().getDrawable(marker);
        if (center)
            oi.setMarker(ItemizedOverlay.boundCenter(icon));
        else
            oi.setMarker(ItemizedOverlay.boundCenterBottom(icon));

        return oi;
    }

    /**
     * Gets the current way in the current {@link IDataTrack}.
     * 
     * @return the current {@link IDataPointsList} ways
     */
    public static List<IDataPointsList> getWays() {
        if (StorageFactory.getStorage().getTrack() != null) {
            return StorageFactory.getStorage().getTrack().getWays();
        }
        return null;
    }

    /**
     * Generates an info box with the given information and returns a reference
     * to the object.
     * 
     * @param context
     *            Context to run this method in.
     * @param activity
     *            Activity to close, if necessary.
     * @param tag
     *            Tag to display information of.
     * @param buttonCaption
     *            Caption of the button in the dialog.
     * @param closeActivityAfterDialog
     *            Close given activity after the button has been clicked.
     * @return Reference to the generated dialog.
     */
    public static Dialog makeInfoDialog(final Context context,
            final Activity activity, final TagSearchResult tag,
            final String buttonCaption, final boolean closeActivityAfterDialog) {
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_searchinfo);
        dialog.setTitle(R.string.string_searchInfoDialog_title);
        dialog.setCancelable(true);

        ImageView img = (ImageView) dialog
                .findViewById(R.id.iv_searchInfoDialog_wikiImage);

        if (tag != null) {
            if (MapsForgeActivity.isOnline(activity)) {
                try {
                    URL url = new URL(tag.getImage());
                    InputStream is = (InputStream) url.getContent();
                    Drawable d = Drawable.createFromStream(is, "src");
                    img.setImageDrawable(d);
                } catch (MalformedURLException e) {
                    // TODO
                    img.setImageDrawable(context.getResources().getDrawable(
                            R.drawable.ic_noimage));
                } catch (IOException e) {
                    img.setImageDrawable(context.getResources().getDrawable(
                            R.drawable.ic_noimage));
                }
            } else {
                img.setImageDrawable(context.getResources().getDrawable(
                        R.drawable.ic_noimage));
            }

            TextView cat = (TextView) dialog
                    .findViewById(R.id.tv_searchInfoDialog_category);
            cat.setText(tag.getKey());

            TextView val = (TextView) dialog
                    .findViewById(R.id.tv_searchInfoDialog_value);
            val.setText(tag.getValue());

            TextView desc = (TextView) dialog
                    .findViewById(R.id.tv_searchInfoDialog_description);
            desc.setText(tag.getDescription());

            TextView wiki = (TextView) dialog
                    .findViewById(R.id.tv_searchInfoDialog_url);
            wiki.setText(tag.getLink());
        }

        Button button = (Button) dialog
                .findViewById(R.id.btn_searchInfoDialog_save);
        button.setText(buttonCaption);
        button.setOnClickListener(new OnClickListener() {
            public void onClick(View v1) {
                final Intent intent = new Intent();
                if (tag != null) {
                    intent.putExtra("DataNodeKey", tag.getKey());
                    intent.putExtra("DataNodeValue", tag.getValue());
                }
                activity.setResult(Activity.RESULT_OK, intent);
                dialog.cancel();
                if (closeActivityAfterDialog) {
                    activity.finish();
                }
            }
        });

        return dialog;
    }

    /**
     * 
     * This Method display a dialog box with all information about the activity
     * for the user. This help's for a better understanding of the functionality
     * of the active activity.
     * 
     * @param activity
     *            The activity in which the info dialog has to be displayed
     * @param title
     *            The title of the activity which has to be displayed in the
     *            dialog box.
     * @param desc
     *            The description of the activity which has to be displayed in
     *            the dialog box
     * 
     */
    public static void setActivityInfoDialog(Activity activity, String title,
            String desc) {
        final Dialog activityInfoDialog = new Dialog(activity);
        activityInfoDialog.setContentView(R.layout.dialog_activityinfo);
        activityInfoDialog.setTitle("Activity Informationen");
        activityInfoDialog.setCancelable(true);
        TextView dialogTitle = (TextView) activityInfoDialog
                .findViewById(R.id.tv_dialogactivityinfo_activityTitle);
        TextView dialogDesc = (TextView) activityInfoDialog
                .findViewById(R.id.tv_dialogactivityinfo_activityDescription);
        dialogTitle.setText(activity.getResources().getString(
                R.string.string_statusDialog_here)
                + ": " + title);
        dialogDesc.setText(activity.getResources().getString(
                R.string.string_global_descriptionTitle)
                + "\n" + desc);
        activityInfoDialog.show();
    }

    /**
     * This method check the global preferences. If the user checked the
     * position "show status bar" the method will activate the status bar with
     * much information for the given activity. If you want to show the status
     * bar in your activity please notice that you have to implement following
     * methods:
     * <p>
     * public void statusBarTitle (View v), public void statusBarDescription
     * (View v), public void statusBarPrefBtn(View v), public void
     * statusBarSearchBtn(View v) - if boolean searchBox = true
     * statusBarSearchfunc() - if boolean searchBox = true
     * 
     * @param activity
     *            The activity in the status bar to display.
     * @param activityTitle
     *            The title of the activity, that will displayed in the status
     *            bar.
     * @param activityDesc
     *            The description of the activity, that will displayed in the
     *            status bar.
     * @param layoutPosition
     *            The resource to inflate the menu at this position.
     * @param searchBox
     *            If a search window will appear. Notice - you have to implement
     *            the functionality of the EditText in your activity.
     */
    public static void setStatusBar(final Activity activity,
            String activityTitle, String activityDesc, int layoutPosition,
            boolean searchBox) {

        // inflate statusbar view
        LayoutInflater statusListInflater = (LayoutInflater) activity
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout statuslayoutHolder = (LinearLayout) activity
                .findViewById(layoutPosition);
        statusListInflater.inflate(R.layout.statusbar_global,
                statuslayoutHolder);

        // Set visibility of the search button in the status bar.
        if (!searchBox) {
            final ImageButton searchBtn = (ImageButton) activity
                    .findViewById(R.id.ib_statusbar_searchBtn);
            searchBtn.setVisibility(8);
            final ImageView seperator = (ImageView) activity
                    .findViewById(R.id.ib_statusbar_seperator);
            seperator.setVisibility(8);

        }

        Button title = (Button) activity
                .findViewById(R.id.btn_statusbar_activityTitle);
        Button desc = (Button) activity
                .findViewById(R.id.btn_statusbar_activityDescription);
        title.setText(activityTitle);
        desc.setText(cutString(activityDesc, 40));

    }

    /**
     * This method check the selected theme from the preferences menu and set
     * the theme for the activity.
     * 
     * @param activity
     *            context of the application
     */
    public static void setTheme(Activity activity) {
        SharedPreferences appPreferences = PreferenceManager
                .getDefaultSharedPreferences(activity);
        int theme = Integer.parseInt(appPreferences.getString(
                "lst_switchTheme", "1"));
        switch (theme) {
        case 1:
            activity.setTheme(android.R.style.Theme_Black);
            break;
        case 0:
            activity.setTheme(android.R.style.Theme_Light);
            break;
        case 2:
            activity.setTheme(R.style.TraceBookCarbon);
            break;
        case 3:
            activity.setTheme(R.style.TraceBookGreen);
            break;
        case 4:
            activity.setTheme(R.style.TraceBookStickyNode);
            break;
        default:
            activity.setTheme(android.R.style.Theme_Black);
        }
    }

    /**
     * 
     * This method starts the tracking notification for the user.
     * 
     * @param activity
     *            The activity from which the method called.
     * @param icon
     *            The icon id which will show in the notification bar.
     * @param cls
     *            The class which will be called for the intent, if the user
     *            click at the notification.
     * @param active
     *            If tracking active (true) or pause (false), change the
     *            notification text for the user.
     */
    public static void startUserNotification(Activity activity, int icon,
            Class<?> cls, boolean active) {
        CharSequence tickerText;
        CharSequence contentTitle;
        CharSequence contentText;

        if (active) {
            tickerText = activity.getResources().getString(
                    R.string.not_startActivity_tickerTextActive);
            contentTitle = activity.getResources().getString(
                    R.string.not_startActivity_contentTitleActive);
            contentText = activity.getResources().getString(
                    R.string.not_startActivity_contentTextActive);

        } else {
            tickerText = activity.getResources().getString(
                    R.string.not_startActivity_tickerTextPause);
            contentTitle = activity.getResources().getString(
                    R.string.not_startActivity_contentTitlePause);
            contentText = activity.getResources().getString(
                    R.string.not_startActivity_contentTextPause);
        }
        // User notification
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager mNotificationManager = (NotificationManager) activity
                .getSystemService(ns);

        long when = System.currentTimeMillis();

        Notification notification = new Notification(icon, tickerText, when);

        Context context = activity.getApplicationContext();

        Intent notificationIntent = new Intent(activity, cls);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(
                activity.getApplicationContext(), 0, notificationIntent, 0);

        notification.setLatestEventInfo(context, contentTitle, contentText,
                contentIntent);

        mNotificationManager.notify(TRACKING_NOTIFY_ID, notification);
    }

    /**
     * This method stops the tracking notification for the user.
     * 
     * @param activity
     *            The activity from which the method called.
     */
    public static void stopUserNotification(Activity activity) {
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager mNotificationManager = (NotificationManager) activity
                .getSystemService(ns);
        mNotificationManager.cancel(TRACKING_NOTIFY_ID);

    }

    private Helper() { // Do nothing
    }
}
