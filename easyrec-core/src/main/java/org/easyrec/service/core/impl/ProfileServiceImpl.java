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
package org.easyrec.service.core.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.easyrec.model.core.ItemVO;
import org.easyrec.model.core.web.Item;
import org.easyrec.service.core.ProfileService;
import org.easyrec.service.core.exception.FieldNotFoundException;
import org.easyrec.service.core.exception.MultipleProfileFieldsFoundException;
import org.easyrec.service.domain.TypeMappingService;
import org.easyrec.store.dao.IDMappingDAO;
import org.easyrec.store.dao.core.ProfileDAO;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.validation.SchemaFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * @author szavrel
 */
public class ProfileServiceImpl implements ProfileService {

    private ProfileDAO profileDAO;
    private IDMappingDAO idMappingDAO;
    private TypeMappingService typeMappingService;
    private SchemaFactory sf;
    private DocumentBuilderFactory dbf;

    private Transformer trans;

    // logging
    private final Log logger = LogFactory.getLog(this.getClass());

    public ProfileServiceImpl(ProfileDAO profileDAO, IDMappingDAO idMappingDAO, TypeMappingService typeMappingService) {
        this(profileDAO, null, idMappingDAO, typeMappingService);
    }


    public ProfileServiceImpl(ProfileDAO profileDAO,
                              String docBuilderFactory, IDMappingDAO idMappingDAO, TypeMappingService typeMappingService) {

        this.profileDAO = profileDAO;
        this.idMappingDAO = idMappingDAO;
        this.typeMappingService = typeMappingService;
        if (docBuilderFactory != null)
            System.setProperty("javax.xml.parsers.DocumentBuilderFactory", docBuilderFactory);
        dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        if (logger.isDebugEnabled()) {
            logger.debug("DocumentBuilderFactory: " + dbf.getClass().getName());
            ClassLoader cl = Thread.currentThread().getContextClassLoader().getSystemClassLoader();
            URL url = cl.getResource("org/apache/xerces/jaxp/DocumentBuilderFactoryImpl.class");
            logger.debug("Parser loaded from: " + url);
        }

        TransformerFactory tf = TransformerFactory.newInstance();
        try {
            trans = tf.newTransformer();
            trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        } catch (Exception e) {

        }
    }

    public boolean storeProfile(Integer tenantId, Integer itemId, String itemTypeId, String profileXML) {
        return profileDAO.storeProfile(tenantId, itemId,
                typeMappingService.getIdOfItemType(tenantId, itemTypeId), profileXML) != 0;
    }

    public boolean storeProfile(Integer tenantId, String itemId, String itemType, String profileXML) {
        return profileDAO.storeProfile(tenantId, idMappingDAO.lookup(itemId),
                typeMappingService.getIdOfItemType(tenantId, itemType), profileXML) != 0;
    }

    public boolean deleteProfile(Integer tenantId, String itemId, String itemType) {
        return profileDAO.deleteProfile(tenantId, idMappingDAO.lookup(itemId),
                typeMappingService.getIdOfItemType(tenantId, itemType));
    }

    public String getProfile(Integer tenantId, String itemId, Integer itemTypeId) {
        Integer mappedItemId = idMappingDAO.lookup(itemId);
        return profileDAO.getProfile(tenantId, mappedItemId, itemTypeId);
    }

    public String getProfile(Integer tenantId, Integer itemId, Integer itemTypeId) {
        return profileDAO.getProfile(tenantId, itemId, itemTypeId);
    }

    public String getProfile(Integer tenantId, Integer itemId, String itemTypeId) {
        return profileDAO.getProfile(tenantId, itemId, typeMappingService.getIdOfItemType(tenantId, itemTypeId));
    }

    public String getProfile(Item item) {
        return getProfile(item.getTenantId(), item.getItemId(), item.getItemType());
    }

    public String getProfile(ItemVO<Integer, Integer> item) {
        return getProfile(item.getTenant(), item.getItem(), item.getType());
    }

    public String getProfile(Integer tenantId, String itemId, String itemTypeId) {
        Integer mappedItemId = idMappingDAO.lookup(itemId);
        return getProfile(tenantId, mappedItemId, itemTypeId);
    }

    public Set<String> getMultiDimensionValue(Integer tenantId, Integer itemId, String itemType,
                                              String dimensionXPath) {
        return profileDAO.getMultiDimensionValue(tenantId, itemId,
                typeMappingService.getIdOfItemType(tenantId, itemType), dimensionXPath);
    }

    public Set<String> getMultiDimensionValue(Integer tenantId, String itemId, String itemType,
                                              String dimensionXPath) {
        return profileDAO.getMultiDimensionValue(tenantId, idMappingDAO.lookup(itemId),
                typeMappingService.getIdOfItemType(tenantId, itemType), dimensionXPath);
    }

    public Set<String> loadProfileField(Integer tenantId, String itemId, String itemType,
                                        String dimensionXPath)
            throws XPathExpressionException, SAXException {

        Set<String> result = new HashSet<String>();

        try {
            int itemIntID = idMappingDAO.lookup(itemId);

            XPathFactory xpf = XPathFactory.newInstance();

            Document doc = getProfileXMLDocument(tenantId, itemIntID, itemType);

            XPath xp = xpf.newXPath();
            NodeList nodeList = (NodeList) xp.evaluate(dimensionXPath, doc, XPathConstants.NODESET);

            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
//                result.add(node.getTextContent());
                result.add(node.getNodeValue());
            }

        } catch (Exception e) {
            logger.error("Error loading profile field: " + e.getMessage());
            e.printStackTrace();

            if (e instanceof SAXException)
                throw (SAXException) e;
            if (e instanceof XPathExpressionException)
                throw (XPathExpressionException) e;
            if (e instanceof DOMException)
                throw (DOMException) e;
            if (e instanceof IllegalArgumentException)
                throw (IllegalArgumentException) e;

            return null;
        }
        return result;
    }

    public String getSimpleDimensionValue(Integer tenantId, Integer itemId, String itemTypeId, String dimensionXPath) {
        return profileDAO
                .getSimpleDimensionValue(tenantId, itemId, typeMappingService.getIdOfItemType(tenantId, itemTypeId),
                        dimensionXPath);
    }

    public String getSimpleDimensionValue(Integer tenantId, String itemId, String itemTypeId, String dimensionXPath) {
        return profileDAO.getSimpleDimensionValue(
                tenantId,
                idMappingDAO.lookup(itemId),
                typeMappingService.getIdOfItemType(tenantId, itemTypeId),
                dimensionXPath);
    }

    public boolean insertOrUpdateMultiDimension(Integer tenantId, Integer itemId, String itemType, String dimensionXPath,
                                                List<String> values) {

        XPathFactory xpf = XPathFactory.newInstance();

        try {
            // load and parse the profile
            Document doc = getProfileXMLDocument(tenantId, itemId, itemType);
            // check if the element exists
            Node node = null;
            Node parent = null;
            XPath xp = xpf.newXPath();
            for (Iterator<String> it = values.iterator(); it.hasNext(); ) {
                String value = it.next();
                // look if value already exists
                node = (Node) xp.evaluate(dimensionXPath + "[text()='" + value + "']", doc, XPathConstants.NODE);
                // if value exists, value can be discarded
                if (node != null) {
                    // optimization: if a node was found, store the parent; later no new XPath evaluation is necessary
                    parent = node.getParentNode();
                    it.remove();
                }
            }
            if (values.isEmpty()) return true; // nothing left to do
            String parentPath = dimensionXPath.substring(0, dimensionXPath.lastIndexOf("/"));
            parent = (Node) xp.evaluate(parentPath, doc, XPathConstants.NODE);
            // find path to parent
            if (parent == null) {
                String tmpPath = parentPath;
                while (parent == null) {
                    tmpPath = parentPath.substring(0, tmpPath.lastIndexOf("/"));
                    parent = (Node) xp.evaluate(tmpPath, doc, XPathConstants.NODE);
                }
                parent = insertElement(doc, parent, parentPath.substring(tmpPath.length()), null);
            }
            String tag = dimensionXPath.substring(parentPath.length() + 1);
            for (String value : values) {
                Element el = doc.createElement(tag);
                el.setNodeValue(value);
                //el.setTextContent(value);
                parent.appendChild(el);
            }

            StringWriter writer = new StringWriter();
            Result result = new StreamResult(writer);
            trans.transform(new DOMSource(doc), result);
            writer.close();
            String xml = writer.toString();
            logger.debug(xml);
            storeProfile(tenantId, itemId, itemType, xml);

        } catch (Exception e) {
            logger.error("Error inserting Multi Dimension: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean insertOrUpdateMultiDimension(Integer tenantId, String itemId,
                                                String itemType, String dimensionXPath,
                                                List<String> values) {
        return insertOrUpdateMultiDimension(tenantId, idMappingDAO.lookup(itemId), itemType, dimensionXPath, values);
    }


    public boolean insertOrUpdateSimpleDimension(Integer tenantId, Integer itemId, String itemTypeId,
                                                 String dimensionXPath, String value) {

        XPathFactory xpf = XPathFactory.newInstance();
        try {
            // load and parse the profile
            Document doc = getProfileXMLDocument(tenantId, itemId, itemTypeId);
            // check if the element exists
            XPath xp = xpf.newXPath();
            Node node = (Node) xp.evaluate(dimensionXPath, doc, XPathConstants.NODE);
            // if the element exists, just update the value
            if (node != null) {
                // if value doesn't change, there is no need to alter the profile and write it to database
//                if (value.equals(node.getTextContent())) return true;
//                node.setTextContent(value);
            	if(value.equals(node.getNodeValue())) return true;
            	node.setNodeValue(value);
            } else { // if the element cannot be found, insert it at the position given in the dimensionXPath
                // follow the XPath from bottom to top until you find the first existing path element
                String tmpPath = dimensionXPath;
                while (node == null) {
                    tmpPath = dimensionXPath.substring(0, tmpPath.lastIndexOf("/"));
                    node = (Node) xp.evaluate(tmpPath, doc, XPathConstants.NODE);
                }
                // found the correct node to insert or ended at Document root, hence insert
                insertElement(doc, node, dimensionXPath.substring(tmpPath.length()/*, dimensionXPath.length()*/),
                        value);
            }

            StringWriter writer = new StringWriter();
            Result result = new StreamResult(writer);
            trans.transform(new DOMSource(doc), result);
            writer.close();
            String xml = writer.toString();
            logger.debug(xml);
            storeProfile(tenantId, itemId, itemTypeId, xml);

        } catch (Exception e) {
            logger.error("Error inserting Simple Dimension: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean insertOrUpdateSimpleDimension(Integer tenantId, String itemId, String itemTypeId,
                                                 String dimensionXPath, String value) {
        return insertOrUpdateSimpleDimension(tenantId, idMappingDAO.lookup(itemId), itemTypeId,
                dimensionXPath, value);
    }

    public synchronized boolean storeProfileField(Integer tenantId, String itemId, String itemTypeId,
                                     String dimensionXPath, String value)
            throws XPathExpressionException, TransformerException, SAXException,
            DOMException, MultipleProfileFieldsFoundException {

        try {
            int itemIntID = idMappingDAO.lookup(itemId);

            XPathFactory xpf = XPathFactory.newInstance();

            // load and parse the profile
            Document doc = getProfileXMLDocument(tenantId, itemIntID, itemTypeId);

            // follow the XPath from bottom to top until you find the first existing path element
            XPath xp = xpf.newXPath();
            String tmpPath = dimensionXPath;
            NodeList nodeList = (NodeList) xp.evaluate(tmpPath, doc, XPathConstants.NODESET);
            if (nodeList.getLength() > 1)
                throw new MultipleProfileFieldsFoundException(nodeList.getLength() + " nodes found.");

            Node node = null;
            if (nodeList.getLength() == 1)
//                nodeList.item(0).setTextContent(value);
            	nodeList.item(0).setNodeValue(value);
            else {
                while (node == null) {
                    tmpPath = dimensionXPath.substring(0, tmpPath.lastIndexOf("/"));
                    if ("".equals(tmpPath))
                        tmpPath = "/";
                    node = (Node) xp.evaluate(tmpPath, doc, XPathConstants.NODE);
                }
                insertElement(doc, node,
                        dimensionXPath.substring(tmpPath.length()), value);
            }

            StringWriter writer = new StringWriter();
            Result result = new StreamResult(writer);
            trans.transform(new DOMSource(doc), result);
            writer.close();
            String xml = writer.toString();
            logger.debug(xml);
            storeProfile(tenantId, itemId, itemTypeId, xml);
        } catch (Exception e) {
            logger.error("Error inserting Simple Dimension: " + e.getMessage());
            e.printStackTrace();

            if (e instanceof SAXException)
                throw (SAXException) e;
            if (e instanceof TransformerException)
                throw (TransformerException) e;
            if (e instanceof XPathExpressionException)
                throw (XPathExpressionException) e;
            if (e instanceof DOMException)
                throw (DOMException) e;
            if (e instanceof MultipleProfileFieldsFoundException)
                throw (MultipleProfileFieldsFoundException) e;
            if (e instanceof IllegalArgumentException)
                throw (IllegalArgumentException) e;

            return false;
        }
        return true;
    }


    public boolean deleteProfileField(Integer tenantId, String itemId, String itemType, String deleteXPath)
            throws XPathExpressionException, TransformerException, SAXException, FieldNotFoundException {

        XPathFactory xpf = XPathFactory.newInstance();
        try {
            // load and parse the profile
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new InputSource(new StringReader(
                    getProfile(tenantId, itemId, itemType))));

            // check if the element exists
            XPath xp = xpf.newXPath();
            NodeList nodeList = (NodeList) xp.evaluate(deleteXPath, doc, XPathConstants.NODESET);

            if (nodeList.getLength() == 0)
                throw new FieldNotFoundException("Field does not exist in this profile!");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                node.getParentNode().removeChild(node);
            }

            StringWriter writer = new StringWriter();
            Result result = new StreamResult(writer);
            trans.transform(new DOMSource(doc), result);
            writer.close();
            String xml = writer.toString();
            logger.debug(xml);
            storeProfile(tenantId, itemId, itemType, xml);

            return true;

        } catch (Exception e) {
            logger.error("Error deleting field: " + e.getMessage());
            e.printStackTrace();

            if (e instanceof SAXException)
                throw (SAXException) e;
            if (e instanceof TransformerException)
                throw (TransformerException) e;
            if (e instanceof XPathExpressionException)
                throw (XPathExpressionException) e;
            if (e instanceof DOMException)
                throw (DOMException) e;
            if (e instanceof FieldNotFoundException)
                throw (FieldNotFoundException) e;
            if (e instanceof IllegalArgumentException)
                throw (IllegalArgumentException) e;

            return false;
        }
    }

    public List<ItemVO<Integer, Integer>> getItemsByDimensionValue(Integer tenantId, String itemType,
                                                                   String dimensionXPath, String value) {
        return profileDAO.getItemsByDimensionValue(tenantId, typeMappingService.getIdOfItemType(tenantId, itemType),
                dimensionXPath, value);
    }

    public List<ItemVO<Integer, Integer>> getItemsByItemType(Integer tenantId, String itemType, int count) {
        return profileDAO.getItemsByItemType(tenantId, typeMappingService.getIdOfItemType(tenantId, itemType), count);
    }

    /**
     * Inserts a new element and value into an XML Document at the position given in xPathExpression
     * relative to the Node given in startNode.
     *
     * @param doc             the Document in which the Element is inserted
     * @param startNode       the Node in the Document used as start point for the XPath Expression
     * @param xPathExpression the XPath from the startNode to the new Element
     * @param value           the value of the new Element
     */
    private Node insertElement(Document doc, Node startNode, String xPathExpression, String value) {

        if (!"".equals(xPathExpression)) {
            String[] xPathTokens = xPathExpression.split("/");
            for (String tag : xPathTokens) {
                if (!"".equals(tag)) {
                    Element el = doc.createElement(tag);
                    startNode.appendChild(el);
                    startNode = startNode.getLastChild();
                }
            }
//            if (value != null) startNode.setTextContent(value);
            if (value != null) startNode.setNodeValue(value);
        }
        return startNode;
    }

    private Document getProfileXMLDocument(Integer tenantId, Integer itemId, String itemTypeId)
            throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilder db = dbf.newDocumentBuilder();
        String profile = getProfile(tenantId, itemId, itemTypeId);
        if (profile == null || profile.equals(""))
            return db.newDocument();
        else
            return db.parse(new InputSource(new StringReader(profile)));
    }

}
