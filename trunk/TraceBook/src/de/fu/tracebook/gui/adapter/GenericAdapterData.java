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

package de.fu.tracebook.gui.adapter;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * The GenericAdapterData helps you to define content for currently tree types
 * of different views.
 * <ul>
 * <li>1. TextView
 * <li>2. ImageView
 * <li>3. Button.
 * </ul>
 * One instance of a GenericAdapterData represents the date for one item in the
 * list view. Where one item can hold one or more views. You can associate views
 * with String tags via the GenericItemDesciption class.
 * 
 * This class provides you easy access to fill your views with the right data.
 */
public class GenericAdapterData {

    private interface GenericItem {
        void fillItem(View view, int id);
    }

    /**
     * The class representation to handle Button data.
     */
    static class ButtonItem implements GenericItem {

        /**
         * Reference to an onClickListener which will be called when an onClick
         * event occurs.
         */
        View.OnClickListener onClickListener;

        /**
         * @param onClickListener
         *            reference to a onClickListener which will be called then a
         *            onClick event occurs
         */
        public ButtonItem(View.OnClickListener onClickListener) {
            this.onClickListener = onClickListener;
        }

        public void fillItem(View view, int id) {
            Button button = (Button) view.findViewById(id);
            button.setOnClickListener(onClickListener);
        }

        @Override
        public String toString() {
            return "Button";
        }

    }

    /**
     * The class representation to handle ImageView data.
     */
    static class ImageItem implements GenericItem {

        /**
         * The resource id to an image.
         */
        int imageId;

        /**
         * @param imageId
         *            resource id of an image to set
         */
        public ImageItem(int imageId) {
            this.imageId = imageId;
        }

        public void fillItem(View view, int id) {
            ImageView imageView = (ImageView) view.findViewById(id);
            imageView.setImageResource(imageId);
            imageView.invalidate();
        }

        @Override
        public String toString() {
            return "Image " + imageId;
        }

    }

    /**
     * An enumeration to define the three different types of views in a item.
     */
    enum ItemTypes {
        /**
         * Button.
         */
        ItemType_Button,
        /**
         * ImageView.
         */
        ItemType_Image,

        /**
         * TextView.
         */
        ItemType_Text,
    }

    /**
     * The class representation to handle TextView data.
     */
    static class TextItem implements GenericItem {

        /**
         * Reference to a textView to which this item is connect with.
         */
        private TextView textView;

        /**
         * Reference to a string which will be used to fill a text view.
         */
        String text;

        /**
         * @param text
         *            String to set the text
         */
        public TextItem(String text) {
            this.text = text;
        }

        public void fillItem(View view, int id) {
            textView = (TextView) view.findViewById(id);
            textView.setText(text);
        }

        /**
         * @return returns the current text of the TextView which is connect
         *         with this item.
         */
        public String getCurrentText() {
            return text;
        }

        @Override
        public String toString() {
            return text;
        }

    }

    private Object additional;

    /**
     * Reference to a description object. See @GenericItemDescription
     */
    GenericItemDescription description;

    /**
     * Map items to string tags.
     */
    Map<String, GenericItem> items = new HashMap<String, GenericItem>();

    /**
     * @param desc
     *            reference to a GenericItemDescription object which will handle
     *            the mapping of tags to view objects.
     */
    public GenericAdapterData(GenericItemDescription desc) {
        description = desc;
    }

    /**
     * @param view
     *            not used
     */
    public void fillView(View view) {
        Iterator<Entry<String, GenericItem>> iterator = items.entrySet()
                .iterator();

        while (iterator.hasNext()) {
            Map.Entry<String, GenericItem> pairs = iterator.next();
            pairs.getValue().fillItem(view,
                    description.getResourceId(pairs.getKey()));
        }

    }

    /**
     * Retrieves the additional data stored in this data.
     * 
     * @return The data stored which may be null.
     */
    public Object getAdditional() {
        return additional;
    }

    /**
     * @param tag
     *            Tag which is associated with the TextView
     * @return return the text for a given TextView
     */
    public String getText(String tag) {
        TextItem textItem = (TextItem) items.get(tag);
        return textItem.getCurrentText();

    }

    /**
     * Store a any object in this data. This can be the Object represented by
     * this data.
     * 
     * @param obj
     *            The object to store.
     */
    public void setAdditional(Object obj) {
        additional = obj;
    }

    /**
     * @param tag
     *            tag which is associated with a given Button.
     * @param onClickListener
     *            reference to a onClickListener which will be called when the
     *            given button is clicked.
     */
    public void setButtonCallback(String tag,
            View.OnClickListener onClickListener) {
        items.put(tag, new ButtonItem(onClickListener));

    }

    /**
     * @param tag
     *            tag which is associated with a given ImageView.
     * @param image
     *            resource id of an image
     */
    public void setImage(String tag, int image) {
        items.put(tag, new ImageItem(image));
    }

    /**
     * @param tag
     *            tag which is associated with an given TextView.
     * @param text
     *            text to set.
     * 
     */
    public void setText(String tag, String text) {
        items.put(tag, new TextItem(text));
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        if (description.getNameTag() != null) {
            GenericItem item = items.get(description.getNameTag());
            if (item != null) {
                return item.toString();
            } else {
                return super.toString();
            }
        } else {
            return super.toString();
        }
    }

}
