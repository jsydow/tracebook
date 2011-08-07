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

interface ILoggerService {
	/**
	 * Starts a new {@link de.fu.tracebook.core.data.DataTrack DataTrack}. 
	 */
	void startTrack(); 
	
	/**
	* @return id of the track
	* 
	* Stops logging of the current track and disabes GPS.
	* In one_shot mode, a last point will be recorded after calling
	* this function, in continuous mode logging will stop instantly.
	*/
	int stopTrack();
	
	/**
	* Adds a new point of interest, returning its id. The location of
	* the point of interest will be updated the next time a GPS fix is
	* available. If no continuous way is recorded, GPS is disabled
	* after obtaining the fix.
	* 
	* This function will start GPS. If currently no track is recorded but
	* on_way is true, the point will be added regardless.
	* 
	* @param on_way Whether the POI is on the current track.
	* @return id of the new POI, -1 if creating of the POI has failed.
	*/
	long createPOI(boolean on_way);
	
	/**
	 * Starts a new area.
	 * If oneShotMode is true, points are only added when calling beginArea()
	 * again, otherwise all GPS fixes will be added to the way automatically.
	 * 
	 *  @param oneShotMode	Start a way in oneShotMode or add a point to the
	 *  					way when one has already been started.
	 *  
	 *  @return The ID of the new area
	 */
	long beginArea();
	
	/**
	 * Starts a new way.
	 * If oneShotMode is true, points are only added when calling beginWay()
	 * again, otherwise all GPS fixes will be added to the way automatically.
	 * 
	 *  @param oneShotMode 	Start a way in oneShotMode or add a Point to the
	 *  					way when there is already a way started.
	 *  
	 *  @return The ID of the new way
	 */
	long beginWay();
	
	/**
	* Stops the current way or area.
	*/
	long endWay();
	
	
	/**
	* @return true if currently a way is recorded.
	*/
	boolean isWayLogging();
	
	/**
	* @return true if area logging is in progress.
	*/
	boolean isAreaLogging();
	
	/**
	 * Stops GPS logging.
	 */
	void pauseLogging();
	
	/**
	 * Resumes GPS logging.
	 */
	void resumeLogging();
	
	/**
	 * Returns the state of GPS logging.
	 * @return true if GPS logging is running.
	 */
	boolean isLogging();
	
	/**
	 * Returns true if there is already one GPS coordinate.
	 */
	boolean hasFix();	
	
	/**
	 * Returns the latitude the last known position.
	 */
	double getLatitude();
	
	/**
	 * Returns the longitude the last known position.
	 */
	double getLongitude();
}
