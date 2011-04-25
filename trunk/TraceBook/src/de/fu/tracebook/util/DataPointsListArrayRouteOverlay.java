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

import java.util.ArrayList;
import java.util.List;

import org.mapsforge.android.maps.ArrayItemizedOverlay;
import org.mapsforge.android.maps.ArrayWayOverlay;
import org.mapsforge.android.maps.OverlayWay;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Paint;
import de.fu.tracebook.R;
import de.fu.tracebook.core.data.DataNode;
import de.fu.tracebook.core.data.DataPointsList;
import de.fu.tracebook.gui.activity.MapsForgeActivity;

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
     * Adds a {@link DataPointsList} to the overlay.
     * 
     * @param way
     *            The DataPointList representing the way
     * @param editing
     *            weather the way should be given the 'currently edited' color
     */
    public void addWay(DataPointsList way, boolean editing) {
        if (way.getNodes().size() == 0) // skip empty ways
            return;
        color(way, editing);
        this.addWay(way.getOverlayRoute());

        if (showWaypoints)
            addWaypoints(way);
    }

    /**
     * Add a list of ways to the Overlay.
     * 
     * @param ways
     *            a list of the {@link DataPointsList}s, adding all of them to
     *            the overlay
     */
    public void addWays(List<DataPointsList> ways) {
        for (DataPointsList l : ways) {
            l.updateOverlayRoute();
            addWay(l, false);
        }
    }

    /**
     * Sets a color for the {@link OverlayWay} in the {@link DataPointsList}.
     * 
     * @param way
     *            The way to be colored
     * @param editing
     *            weather the way should be marked as currently edited
     */
    public void color(DataPointsList way, boolean editing) {
        Pair<Paint, Paint> col = getColor(editing, way.isArea());
        way.getOverlayRoute().setPaint(col.first, col.second);
    }

    /**
     * Creates a new OverlayItem for n if it has none yet.
     * 
     * @param n
     *            DataNode that should get a marker
     */
    public void putWaypoint(DataNode n) {
        if (n.getOverlayItem() == null)
            n.setOverlayItem(Helper.getOverlayItem(n.toGeoPoint(),
                    R.drawable.card_dot_blue, context, true));
        if (showWaypoints)
            pointsOverlay.addItem(n.getOverlayItem());
    }

    /**
     * Enable/disable the drawing of way point markers.
     */
    public void toggleWaypoints() {
        showWaypoints = !showWaypoints;

        for (DataPointsList dpl : Helper.getWays())
            if (showWaypoints)
                addWaypoints(dpl);
            else
                removeWaypoints(dpl);
    }

    private void addWaypoints(DataPointsList way) {
        for (DataNode n : way.getNodes())
            putWaypoint(n);
    }

    private void removeWaypoints(DataPointsList way) {
        for (DataNode n : way.getNodes())
            pointsOverlay.removeItem(n.getOverlayItem());
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
