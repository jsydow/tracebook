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

package de.fu.tracebook.gui.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

/**
 * The GenericAdapter class helps us it reduce the number of customizable
 * ArrayAdapter classes. You can use it for all of you custom ListView layouts.
 * It needs a layoutInflater and a reference to a list of GenericAdapterData.
 * Each element in the list describes one element in a list view.
 */
public class GenericAdapter extends ArrayAdapter<GenericAdapterData> {

    /**
     * The layout id where the view elements are stored for on custom item in
     * the list view.
     */
    int layoutId;

    /**
     * A reference to a layoutInflater which will be used to inflate the custom
     * view for an item in the list view.
     */
    LayoutInflater layoutInflater;

    /**
     * The standard constructor for an ArrayAdapter plus a reference to a
     * LayoutInflater.
     * 
     * @param context
     *            The context of the ListView.
     * @param resource
     *            The resource ID for a layout file containing a layout to use
     *            when instantiating views.
     * @param listViewId
     *            The id of the ListView.
     * @param objects
     *            List of GenericAdapterData which will be used to fill the
     *            list.
     */
    public GenericAdapter(Context context, int resource, int listViewId,
            List<GenericAdapterData> objects) {
        super(context, resource, listViewId, objects);
        layoutId = resource;
        layoutInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View inflatedView = convertView;

        if (inflatedView == null) {
            inflatedView = layoutInflater.inflate(layoutId, parent, false);
        }

        if (position >= super.getCount()) {
            return null;
        }
        GenericAdapterData data = super.getItem(position);
        data.fillView(inflatedView);

        return inflatedView;
    }
}
