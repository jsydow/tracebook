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
import java.util.List;

import org.mapsforge.android.maps.ArrayItemizedOverlay;
import org.mapsforge.android.maps.ArrayWayOverlay;
import org.mapsforge.android.maps.OverlayItem;
import org.mapsforge.android.maps.OverlayWay;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Paint;
import de.fu.tracebook.R;
import de.fu.tracebook.core.data.IDataNode;
import de.fu.tracebook.core.data.IDataPointsList;
import de.fu.tracebook.core.data.StorageFactory;
import de.fu.tracebook.gui.activity.MapsForgeActivity;
import de.fu.tracebook.util.Helper;
import de.fu.tracebook.util.Pair;

/**
 * Class wrapping the ·{@link ArrayWayOverlay} to get some methods out of
 * {@link MapsForgeActivity}.
 */
public class DataPointsListArrayRouteOverlay extends ArrayWayOverlay {
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

    private Activity context;

    private ArrayItemizedOverlay pointsOverlay;

    /**
     * List of possible colors for ways and areas the first color in the list is
     * always used for the current way.
     */
    private List<Pair<Paint, Paint>> wayColors;

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
        if (way.getNodes().size() == 0) // skip empty ways
            return;
        color(way, editing);
        this.addWay(StorageFactory.getStorage().getOverlayManager()
                .getOverlayRoute(way));

        if (showWaypoints)
            addWaypoints(way);
    }

    /**
     * Add a list of ways to the Overlay.
     * 
     * @param list
     *            a list of the {@link IDataPointsList}s, adding all of them to
     *            the overlay
     */
    public void addWays(List<IDataPointsList> list) {
        for (IDataPointsList l : list) {
            StorageFactory.getStorage().getOverlayManager()
                    .updateOverlayRoute(l, null);
            addWay(l, false);
        }
    }

    /**
     * Sets a color for the {@link OverlayWay} in the {@link IDataPointsList}.
     * 
     * @param way
     *            The way to be colored
     * @param editing
     *            weather the way should be marked as currently edited
     */
    public void color(IDataPointsList way, boolean editing) {
        Pair<Paint, Paint> col = getColor(editing, way.isArea());
        StorageFactory.getStorage().getOverlayManager().getOverlayRoute(way)
                .setPaint(col.first, col.second);
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

        for (IDataPointsList dpl : Helper.getWays())
            if (showWaypoints)
                addWaypoints(dpl);
            else
                removeWaypoints(dpl);
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