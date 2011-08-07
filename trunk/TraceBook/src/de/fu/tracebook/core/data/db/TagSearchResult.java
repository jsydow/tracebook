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

package de.fu.tracebook.core.data.db;

/**
 * This class holds all information that are needed for the tag search.
 * 
 */
public class TagSearchResult {
    private String description;
    private String image;
    private String key;
    private String language;
    private String link;
    private String name;
    private String value;
    private String valueType;

    /**
     * @param key
     *            The key part of the tag.
     * @param value
     *            The value part of the tag.
     * @param name
     *            The name of the text in the language specified by language.
     * @param description
     *            The description of the tag.
     * @param link
     *            The link to the
     *            http://wiki.openstreetmap.org/wiki/Map_Features article
     * @param image
     *            The resource id of the image which can be used for.
     * @param valueType
     *            The type of the value which can be number, string etc.
     * @param language
     *            The language in which the tag is described.
     */
    TagSearchResult(String key, String value, String name, String description,
            String link, String image, String valueType, String language) {
        this.key = key;
        this.value = value;
        this.name = name;
        this.description = description;
        this.link = link;
        this.image = image;
        this.valueType = valueType;
        this.language = language;
    }

    /**
     * Getter method.
     * 
     * @return The description of the map feature.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Getter method.
     * 
     * @return The image URL.
     */
    public String getImage() {
        return image;
    }

    /**
     * Getter method.
     * 
     * @return The key field of the tag.
     */
    public String getKey() {
        return key;
    }

    /**
     * Getter method.
     * 
     * @return The language.
     */
    public String getLanguage() {
        return language;
    }

    /**
     * Getter method.
     * 
     * @return The URL to Wiki page of the map feature.
     */
    public String getLink() {
        return link;
    }

    /**
     * Getter method.
     * 
     * @return The displayed name of the tag.
     */
    public String getName() {
        return name;
    }

    /**
     * Getter method.
     * 
     * @return The value field of the tag.
     */
    public String getValue() {
        return value;
    }

    /**
     * Getter method.
     * 
     * @return The type of the value field of the tag.
     */
    public String getValueType() {
        return valueType;
    }
}
