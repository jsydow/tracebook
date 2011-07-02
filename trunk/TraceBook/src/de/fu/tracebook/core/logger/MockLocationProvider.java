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

import java.util.Random;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;

/**
 * A mock location provider. For testing only.
 */
public class MockLocationProvider {

    private static MockLocationProvider instance;
    private final static String PROVIDER_NAME = LocationManager.GPS_PROVIDER;

    /**
     * Singleton implementation.
     * 
     * @param ctx
     *            The context.
     * @return This.
     */
    public static MockLocationProvider getInstance(WaypointLogService ctx) {
        if (instance == null) {
            instance = new MockLocationProvider(ctx);
        }

        return instance;
    }

    private double lat = 52.4559497304728;
    private LocationManager lm;
    private double lon = 13.2975200387581;

    private Random rnd = new Random();

    private MockLocationProvider(WaypointLogService ctx) {
        lm = (LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE);
        try {
            lm.removeTestProvider(PROVIDER_NAME);
        } catch (IllegalArgumentException e) {
            // to nothing
        }
        try {
            lm.addTestProvider(PROVIDER_NAME, false, false, false, false,
                    false, false, false, 0, 5);
        } catch (IllegalArgumentException e) {
            // to nothing
        }
        lm.setTestProviderEnabled(PROVIDER_NAME, true);
    }

    /**
     * Generate new location.
     */
    public void newLoc() {
        lat += (rnd.nextDouble() - 0.5) * 0.001;
        lon += (rnd.nextDouble() - 0.5) * 0.001;

        Location l = new Location(PROVIDER_NAME);
        l.setLatitude(lat);
        l.setLongitude(lon);
        l.setTime(System.currentTimeMillis());
        lm.setTestProviderLocation(PROVIDER_NAME, l);
    }

}
