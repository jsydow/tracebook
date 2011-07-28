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

package de.fu.tracebook.core.overlays;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.mapsforge.android.maps.ArrayItemizedOverlay;
import org.mapsforge.android.maps.ArrayWayOverlay;
import org.mapsforge.android.maps.GeoPoint;
import org.mapsforge.android.maps.MapView;
import org.mapsforge.android.maps.OverlayItem;
import org.mapsforge.android.maps.OverlayWay;
import org.mapsforge.android.maps.Projection;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import de.fu.tracebook.R;
import de.fu.tracebook.core.data.IDataNode;
import de.fu.tracebook.core.data.IDataPointsList;
import de.fu.tracebook.core.data.StorageFactory;
import de.fu.tracebook.gui.activity.AddPointActivity;
import de.fu.tracebook.gui.activity.MapsForgeActivity;
import de.fu.tracebook.util.Helper;
import de.fu.tracebook.util.LogIt;
import de.fu.tracebook.util.Pair;
import de.fu.tracebook.util.PointInPolygon;
import de.fu.tracebook.util.PointLineDistance;

/**
 * Class wrapping the ·{@link ArrayWayOverlay} to get some methods out of
 * {@link MapsForgeActivity}.
 */
public class DataPointsListArrayRouteOverlay extends ArrayWayOverlay {

    private class DefaultListener implements DialogInterface.OnClickListener {
        private final CharSequence[] items = {
                context.getResources().getString(
                        R.string.cm_PointsListOverlay_tag),
                context.getResources().getString(
                        R.string.cm_PointsListOverlay_delete) };

        private IDataPointsList way = null;

        public DefaultListener() {
            // do nothing, just to eliminate warnings.
        }

        public CharSequence[] getItems() {
            return items;
        }

        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
            case 0: // edit

                final Intent intent = new Intent(context,
                        AddPointActivity.class);
                intent.putExtra("WayId", way.getId());
                context.startActivity(intent);
                break;

            case 1: // delete
                StorageFactory.getStorage().getTrack().deleteWay(way.getId());
                StorageFactory.getStorage().getOverlayManager().removeWay(way);
                requestRedraw();

                break;
            default:
                break;
            }
        }

        public void setWay(IDataPointsList selectedWay) {
            way = selectedWay;
        }
    }

    /**
     * Generates a pair of paint objects with the same color, but different
     * levels of transparency.
     */
    private static Pair<Paint, Paint> getPaintPair(int color, boolean area) {
        Paint paintOutline = new Paint();
        paintOutline.setAntiAlias(true);
        paintOutline.setStyle(Paint.Style.STROKE);
        paintOutline.setStrokeWidth(4);
        paintOutline.setStrokeCap(Paint.Cap.BUTT);
        paintOutline.setStrokeJoin(Paint.Join.ROUND);
        paintOutline.setColor(color);
        paintOutline.setAlpha(96);

        Paint paintFill = new Paint(paintOutline);
        if (area)
            paintFill.setStyle(Paint.Style.FILL);
        paintFill.setAlpha(160);

        return new Pair<Paint, Paint>(paintFill, paintOutline);
    }

    private List<Pair<Paint, Paint>> areaColors;
    private int colorID = 0;

    private ArrayItemizedOverlay pointsOverlay;

    /**
     * List of possible colors for ways and areas the first color in the list is
     * always used for the current way.
     */
    private List<Pair<Paint, Paint>> wayColors;

    /**
     * The activity that uses this overlay. Probably the MapsforgeActivity
     */
    Activity context;

    /**
     * Show a knob for every way point.
     */
    boolean showWaypoints = false;

    /**
     * Sets overlays and generates color array.
     * 
     * @param context
     *            a reference to the MapActivity
     * @param pointsOverlay
     *            a reference to the pointsOverlay that also is bound to the
     *            MapActivity
     * 
     */
    public DataPointsListArrayRouteOverlay(Activity context,
            ArrayItemizedOverlay pointsOverlay) {

        super(null, null);
        // super(getPaintPair(Color.rgb(0, 255, 0), false).first, getPaintPair(
        // Color.rgb(0, 255, 0), false).second);

        this.pointsOverlay = pointsOverlay;
        this.context = context;

        // create paint list
        wayColors = new ArrayList<Pair<Paint, Paint>>();
        wayColors.add(getPaintPair(Color.rgb(0, 255, 0), false));
        wayColors.add(getPaintPair(Color.rgb(0, 0, 230), false));
        wayColors.add(getPaintPair(Color.rgb(0, 0, 200), false));
        wayColors.add(getPaintPair(Color.rgb(0, 0, 170), false));

        areaColors = new ArrayList<Pair<Paint, Paint>>();
        areaColors.add(getPaintPair(Color.rgb(0, 255, 0), true));
        areaColors.add(getPaintPair(Color.rgb(230, 0, 0), true));
        areaColors.add(getPaintPair(Color.rgb(200, 0, 0), true));
        areaColors.add(getPaintPair(Color.rgb(170, 0, 0), true));
    }

    /**
     * Adds a {@link IDataPointsList} to the overlay.
     * 
     * @param way
     *            The DataPointList representing the way
     * @param editing
     *            weather the way should be given the 'currently edited' color
     */
    public void addWay(IDataPointsList way, boolean editing) {
        if (way.getNodes().size() == 0) { // skip empty ways
            return;
        }

        OverlayWay w = StorageFactory.getStorage().getOverlayManager()
                .getOverlayRoute(way);
        w.setWayData(new GeoPoint[][] { way.toGeoPointArray(null) });
        color(way, w, editing);
        this.addWay(w);

        if (showWaypoints) {
            addWaypoints(way);
        }
    }

    /**
     * Add a list of ways to the Overlay.
     * 
     * @param list
     *            a list of the {@link IDataPointsList}s, adding all of them to
     *            the overlay
     */
    public void addWays(List<IDataPointsList> list) {
        IDataPointsList currWay = StorageFactory.getStorage().getTrack()
                .getCurrentWay();
        LogIt.w("Addways " + list.size());
        for (IDataPointsList l : list) {
            LogIt.w("way has nodes: "
                    + Arrays.toString(l.toGeoPointArray(null)));
            StorageFactory.getStorage().getOverlayManager()
                    .updateOverlayRoute(l, null);
            boolean editing = l.equals(currWay);
            addWay(l, editing);
        }
    }

    /**
     * Sets a color for the {@link OverlayWay} in the {@link IDataPointsList}.
     * 
     * @param way
     *            The way to be colored
     * @param overlay
     *            The overlay of the Way way.
     * @param editing
     *            weather the way should be marked as currently edited
     */
    public void color(IDataPointsList way, OverlayWay overlay, boolean editing) {
        Pair<Paint, Paint> col = getColor(editing, way.isArea());
        overlay.setPaint(col.first, col.second);
        // overlay.setPaint(null, null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.mapsforge.android.maps.Overlay#onTap(org.mapsforge.android.maps.GeoPoint
     * , org.mapsforge.android.maps.MapView)
     */
    @Override
    public boolean onTap(GeoPoint p, MapView mapView) {

        // TODO color selected ways areas

        IDataPointsList selectedWay = null;
        Projection proj = mapView.getProjection();
        Point point = proj.toPoint(p, null, mapView.getZoomLevel());

        int size = this.size();
        for (int i = 0; i < size; ++i) {
            OverlayWay way = this.createWay(i);

            GeoPoint[] data = way.getWayData()[0];
            Point[] points = new Point[data.length];

            for (int j = 0; j < data.length; ++j) {
                points[j] = proj.toPoint(data[j], null, mapView.getZoomLevel());
            }

            IDataPointsList dataway = StorageFactory.getStorage()
                    .getOverlayManager().getPointsList(way);

            if (points.length > 1) {
                if (points[0] == points[points.length - 1]) {
                    // Area
                    if (PointInPolygon.isPointInPolygon(point, points)) {
                        selectedWay = dataway;
                        break;
                    }
                } else {
                    // Way
                    boolean selected = false;

                    Point a = null;
                    for (Point b : points) {
                        if (a != null) {
                            double factor = 12; // TODO
                            double threshold = factor * factor;

                            if (PointLineDistance.sqDistancePointLine(point, a,
                                    b) < threshold) {
                                selectedWay = dataway;
                                selected = true;
                                break;
                            }
                        }
                        a = b;
                    }

                    if (selected) {
                        break;
                    }
                }
            }
        }

        DefaultListener contextMenueListener = new DefaultListener();

        LogIt.d("DataPointsList.onTap()");

        if (selectedWay != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this.context);
            builder.setTitle("id: " + selectedWay.getId());
            contextMenueListener.setWay(selectedWay);
            builder.setItems(contextMenueListener.getItems(),
                    contextMenueListener);

            builder.show();
            return true;
        }

        return false;
    }

    /**
     * Creates a new OverlayItem for n if it has none yet.
     * 
     * @param n
     *            DataNode that should get a marker
     */
    public void putWaypoint(IDataNode n) {
        OverlayItem item;
        if (StorageFactory.getStorage().getOverlayManager().getOverlayItem(n) == null) {
            item = Helper.getOverlayItem(n.getCoordinates(),
                    R.drawable.card_dot_blue, context, true);
            StorageFactory.getStorage().getOverlayManager()
                    .setOverlayItem(item, n);
        } else {
            item = StorageFactory.getStorage().getOverlayManager()
                    .getOverlayItem(n);
        }
        if (showWaypoints) {
            pointsOverlay.addItem(item);
        }
    }

    /**
     * Enable/disable the drawing of way point markers.
     */
    public void toggleWaypoints() {
        showWaypoints = !showWaypoints;

        for (IDataPointsList dpl : Helper.getWays()) {
            if (showWaypoints) {
                addWaypoints(dpl);
            } else {
                removeWaypoints(dpl);
            }
        }
    }

    private void addWaypoints(IDataPointsList way) {
        for (IDataNode n : way.getNodes())
            putWaypoint(n);
    }

    private void removeWaypoints(IDataPointsList way) {
        for (IDataNode n : way.getNodes())
            pointsOverlay.removeItem(StorageFactory.getStorage()
                    .getOverlayManager().getOverlayItem(n));
    }

    /**
     * Gets a color from the rotating color array.
     * 
     * @param editing
     *            true if the track is currently edited and the color pair for
     *            the current way should be used
     * @param area
     *            true if the color should be used for an area
     * 
     * @return a {@link Pair} of {@link Paint} where the first element is the
     *         FillPaint and the second one the OutlinePaint
     */
    Pair<Paint, Paint> getColor(boolean editing, boolean area) {
        List<Pair<Paint, Paint>> colors = area ? areaColors : wayColors;
        // the first color is used for the current track, so rotate over the
        // remaining array fields.
        colorID = editing ? 0 : colorID % (colors.size() - 1) + 1;
        return colors.get(colorID);
    }

}
