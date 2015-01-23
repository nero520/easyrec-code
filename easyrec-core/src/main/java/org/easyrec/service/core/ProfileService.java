/**Copyright 2010 Research Studios Austria Forschungsgesellschaft mBH
 *
 * This file is part of easyrec.
 *
 * easyrec is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * easyrec is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with easyrec.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.easyrec.service.core;

import org.easyrec.model.core.ItemVO;
import org.easyrec.model.core.web.Item;
import org.easyrec.service.core.exception.FieldNotFoundException;
import org.easyrec.service.core.exception.MultipleProfileFieldsFoundException;
import org.w3c.dom.DOMException;
import org.xml.sax.SAXException;

import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;
import java.util.List;
import java.util.Set;

/**
 * The easyrec Profile system uses a sql columns in the database to save the profile as XML
 * This Service class loads them from the database using XPath to access the XML profiles.
 *
 * @author szavrel
 */
public interface ProfileService {

    /**
     * This function loads a profile XML string from the database
     *
     * @param tenantId   the tenantId of the profile
     * @param itemId     the itemId of the profile
     * @param itemTypeId the itemTypeId of the profile
     * @return a string with the XML profile for the given tenantId , itemId, itemTypeId combination
     */
    public String getProfile(Integer tenantId, Integer itemId, String itemTypeId);

    /**
     * This function loads a profile XML string from the database
     *
     * @param tenantId   the tenantId of the profile
     * @param itemId     the itemId of the profile
     * @param itemTypeId the itemTypeId of the profile
     * @return a string with the XML profile for the given tenantId , itemId, itemTypeId combination
     */
    public String getProfile(Integer tenantId, String itemId, Integer itemTypeId);

    /**
     * This function loads a profile XML string from the database
     *
     * @param tenantId   the tenantId of the profile
     * @param itemId     the itemId of the profile
     * @param itemTypeId the itemTypeId of the profile
     * @return a string with the XML profile for the given tenantId , itemId, itemTypeId combination
     */
    public String getProfile(Integer tenantId, Integer itemId, Integer itemTypeId);

    /**
     * This function loads a profile XML string from the database
     *
     * @param item The Item Object of the profile (holds itemId, itemTypeId and tenantId)
     * @return a string with the XML profile for the given item object
     */
    public String getProfile(Item item);

    /**
     * This function loads a profile XML string from the database
     *
     * @param item The Item Object of the profile (holds itemId, itemTypeId and tenantId)
     * @return a string with the XML profile for the given item object
     */
    public String getProfile(ItemVO<Integer, Integer> item);

    /**
     * This function loads a profile XML string from the database
     *
     * @param tenantId   the tenantId of the profile
     * @param itemId     the itemId of the profile
     * @param itemTypeId the itemTypeId of the profile
     * @return a string with the XML profile for the given tenantId , itemId, itemTypeId combination
     */
    public String getProfile(Integer tenantId, String itemId, String itemTypeId);

    /**
     * This function writes a profile as an XML string to the database
     *
     * @param tenantId   the tenantId of the profile
     * @param itemId     the itemId of the profile
     * @param itemTypeId the itemTypeId of the profile
     * @param profileXML the profile as an XML string
     * @return <code>true</code> if the operation succeeds <code>false</code> otherwise
     */
    public boolean storeProfile(Integer tenantId, Integer itemId, String itemTypeId, String profileXML);

    /**
     * This function writes a profile as an XML string to the database
     *
     * @param tenantId   the tenantId of the profile
     * @param itemId     the string itemId of the profile
     * @param itemType   the itemType of the profile
     * @param profileXML the profile as an XML string
     * @return <code>true</code> if the operation succeeds <code>false</code> otherwise
     */
    public boolean storeProfile(Integer tenantId, String itemId, String itemType, String profileXML);

    /**
     * This function deletes a profile of an item
     *
     * @param tenantId the tenantId of the profile's item
     * @param itemId   the string itemId of the profile's item
     * @param itemType the itemType of the profile's item
     * @return <code>true</code> if the operation succeeds <code>false</code> otherwise
     */
    public boolean deleteProfile(Integer tenantId, String itemId, String itemType);

    /**
     * This function loads the first result as string value from the profile
     * based on the provided xpath.
     *
     * @param tenantId       the tenantId of the profile
     * @param itemId         the itemId of the profile
     * @param itemTypeId     the itemTypeId of the profile
     * @param dimensionXPath the XPath string addressing the wanted value
     * @return string with the value on the given XPath location
     */
    public String getSimpleDimensionValue(Integer tenantId, Integer itemId, String itemTypeId, String dimensionXPath);

    /**
     * This function loads the first result as string value from the profile
     * based on the provided xpath.
     *
     * @param tenantId       the tenantId of the profile
     * @param itemId         the itemId of the profile
     * @param itemTypeId     the itemType of the profile
     * @param dimensionXPath the XPath string addressing the wanted value
     * @return string with the value on the given XPath location
     */
    public String getSimpleDimensionValue(Integer tenantId, String itemId, String itemTypeId, String dimensionXPath);

    /**
     * This function loads all results as List of string values from
     * the profile based on the provided xpath.
     *
     * @param tenantId       the tenantId of the profile
     * @param itemId         the itemId of the profile
     * @param itemType       the itemType of the profile
     * @param dimensionXPath the XPath of the value you want to load
     * @return the values of the given XPath
     */
    public Set<String> getMultiDimensionValue(Integer tenantId, Integer itemId, String itemType,
                                              String dimensionXPath);

    /**
     * This function loads all results as List of string values from
     * the profile based on the provided xpath.
     *
     * @param tenantId       the tenantId of the profile
     * @param itemId         the itemId of the profile
     * @param itemType       the itemTypeId of the profile
     * @param dimensionXPath the XPath of the value you want to load
     * @return the values of the given XPath
     */
    public Set<String> getMultiDimensionValue(Integer tenantId, String itemId, String itemType,
                                              String dimensionXPath);

    /**
     * This function loads all results as List of string values from
     * the profile based on the provided XPath. In contrast to
     * <code>getMultiDimensionValue</code> it also throws the XPath
     * and DOM relevant Exceptions.
     *
     * @param tenantId       the tenantId of the profile
     * @param itemId         the itemId of the profile
     * @param itemType       the itemTypeId of the profile
     * @param dimensionXPath the XPath of the value you want to load
     * @return the values of the given XPath
     */
    public Set<String> loadProfileField(Integer tenantId, String itemId, String itemType,
                                        String dimensionXPath)
            throws XPathExpressionException, SAXException, DOMException;

    /**
     * This function updates or inserts a item's ( based on tenantId, itemId, itemtypeId) XML Profile
     * at the specified XPath with the specified value.
     *
     * @param tenantId       the tenantId of the profile
     * @param itemId         the itemId of the profile
     * @param itemTypeId     the itemTypeId of the profile
     * @param dimensionXPath the XPath of the value you want to update or insert
     * @param value          the value you want to insert or update into the profile
     * @return <code>true</code> if the operation succeeds <code>false</code> otherwise
     */
    public boolean insertOrUpdateSimpleDimension(Integer tenantId, Integer itemId, String itemTypeId,
                                                 String dimensionXPath, String value);

    /**
     * This function updates or inserts a value into an item's
     * (based on tenantId, itemId, itemtypeId) XML Profile
     * at the specified XPath.
     *
     * @param tenantId       the tenantId of the profile
     * @param itemId         the itemId of the profile
     * @param itemTypeId     the itemTypeId of the profile
     * @param dimensionXPath the XPath of the value you want to update or insert
     * @param value          the value you want to insert or update into the profile
     * @return <code>true</code> if the operation succeeds <code>false</code> otherwise
     */
    public boolean insertOrUpdateSimpleDimension(Integer tenantId, String itemId, String itemTypeId,
                                                 String dimensionXPath, String value);

    /**
     * This function inserts a value into an item's ( based on tenantId, itemId, itemTypeId)
     * XML Profile at the specified XPath.
     *
     * @param tenantId       the tenantId of the profile
     * @param itemId         the itemId of the profile
     * @param itemTypeId     the itemTypeId of the profile
     * @param dimensionXPath the XPath of the value you want to update or insert
     * @param value          the value you want to insert or update into the profile
     * @return <code>true</code> if the operation succeeds <code>false</code> otherwise
     */
    public boolean storeProfileField(Integer tenantId, String itemId, String itemTypeId,
                                     String dimensionXPath, String value)
            throws XPathExpressionException, TransformerException, SAXException,
            DOMException, MultipleProfileFieldsFoundException;

    /**
     * This function updates a item's ( based on tenantId, itemId, itemType) XML Profile
     * at the specified XPath with the specified values.
     *
     * @param tenantId       the tenantId of the profile
     * @param itemId         the itemId of the profile
     * @param itemType       the itemType of the profile
     * @param dimensionXPath the XPath of the value you want to update or insert
     * @param values         the value you want to insert or update into the profile
     * @return <code>true</code> if the operation succeeds <code>false</code> otherwise
     */
    public boolean insertOrUpdateMultiDimension(Integer tenantId, Integer itemId, String itemType, String dimensionXPath,
                                                List<String> values);

    /**
     * This function updates a item's ( based on tenantId, itemId, itemType) XML Profile
     * at the specified XPath with the specified values.
     *
     * @param tenantId       the tenantId of the profile
     * @param itemId         the itemId of the profile
     * @param itemType       the itemType of the profile
     * @param dimensionXPath the XPath of the value you want to update or insert
     * @param values         the value you want to insert or update into the profile
     * @return <code>true</code> if the operation succeeds <code>false</code> otherwise
     */
    public boolean insertOrUpdateMultiDimension(Integer tenantId, String itemId, String itemType, String dimensionXPath,
                                                List<String> values);

    /**
     * This function deletes the nodes defined by <code>deleteXPath</code>.
     *
     * @param tenantId    the tenantId of the profile
     * @param itemId      the itemId of the profile
     * @param itemType    the itemType of the profile
     * @param deleteXPath the XPath to the nodes which will be deleted
     * @return returns <code>true</code> if the operation succeeded and
     *         <code>false</code> otherwise
     */
    public boolean deleteProfileField(Integer tenantId, String itemId, String itemType, String deleteXPath)
            throws XPathExpressionException, TransformerException, SAXException, DOMException, FieldNotFoundException;

    /**
     * This function loads all Item's based on the given profile values
     * defined by the dimensionXPath and value parameter.         *
     *
     * @param tenantId       the tenantId of the profile
     * @param itemType       the itemType name of the profile
     * @param dimensionXPath the XPath of the profile field you want to use to filter your result set
     * @param value          the desired value of your itemVO within the dimensionXPath
     * @return A list of ItemVo Objects matching the above conditions
     */
    public List<ItemVO<Integer, Integer>> getItemsByDimensionValue(Integer tenantId, String itemType,
                                                                   String dimensionXPath, String value);

    /**
     * This function will load all Item's with a specific itemType
     *
     * @param tenantId the tenantId of the profile
     * @param itemType the itemType name of the profile
     * @param count    the maximum result set size
     * @return A list of ItemVo Objects of the requested itemType
     */
    public List<ItemVO<Integer, Integer>> getItemsByItemType(Integer tenantId, String itemType, int count);

}
