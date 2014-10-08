/*
 * Copyright 2013 Florian Müller & Jay Brown
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 * This code is based on the Apache Chemistry OpenCMIS FileShare project
 * <http://chemistry.apache.org/java/developing/repositories/dev-repositories-fileshare.html>.
 *
 * It is part of a training exercise and not intended for production use!
 *
 */
package org.example.cmis.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.data.PropertyData;
import org.apache.chemistry.opencmis.commons.data.PropertyDateTime;
import org.apache.chemistry.opencmis.commons.data.PropertyId;
import org.apache.chemistry.opencmis.commons.data.PropertyString;
import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisStorageException;
import org.apache.chemistry.opencmis.commons.impl.json.JSONObject;
import org.apache.chemistry.opencmis.commons.impl.json.parser.JSONParseException;
import org.apache.chemistry.opencmis.commons.impl.json.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class FileBridgeUtils {

    private static final Logger LOG = LoggerFactory
            .getLogger(FileBridgeUtils.class);

    private FileBridgeUtils() {
    }

    /**
     * Returns the boolean value of the given value or the default value if the
     * given value is <code>null</code>.
     */
    public static boolean getBooleanParameter(Boolean value, boolean def) {
        if (value == null) {
            return def;
        }

        return value.booleanValue();
    }

    /**
     * Converts milliseconds into a {@link GregorianCalendar} object, setting
     * the timezone to GMT and cutting milliseconds off.
     */
    public static GregorianCalendar millisToCalendar(long millis) {
        GregorianCalendar result = new GregorianCalendar();
        result.setTimeZone(TimeZone.getTimeZone("GMT"));
        result.setTimeInMillis((long) (Math.ceil((double) millis / 1000) * 1000));

        return result;
    }

    /**
     * Splits a filter statement into a collection of properties. If
     * <code>filter</code> is <code>null</code>, empty or one of the properties
     * is '*' , an empty collection will be returned.
     */
    public static Set<String> splitFilter(String filter) {
        if (filter == null) {
            return null;
        }

        if (filter.trim().length() == 0) {
            return null;
        }

        Set<String> result = new HashSet<String>();
        for (String s : filter.split(",")) {
            s = s.trim();
            if (s.equals("*")) {
                return null;
            } else if (s.length() > 0) {
                result.add(s);
            }
        }

        // set a few base properties
        // query name == id (for base type properties)
        result.add(PropertyIds.OBJECT_ID);
        result.add(PropertyIds.OBJECT_TYPE_ID);
        result.add(PropertyIds.BASE_TYPE_ID);

        return result;
    }

    /**
     * Gets the type id from a set of properties.
     */
    public static String getObjectTypeId(Properties properties) {
        PropertyData<?> typeProperty = properties.getProperties().get(
                PropertyIds.OBJECT_TYPE_ID);
        if (!(typeProperty instanceof PropertyId)) {
            throw new CmisInvalidArgumentException("Type Id must be set!");
        }

        String typeId = ((PropertyId) typeProperty).getFirstValue();
        if (typeId == null) {
            throw new CmisInvalidArgumentException("Type Id must be set!");
        }

        return typeId;
    }

    /**
     * Returns the first value of an id property.
     */
    public static String getIdProperty(Properties properties, String name) {
        PropertyData<?> property = properties.getProperties().get(name);
        if (!(property instanceof PropertyId)) {
            return null;
        }

        return ((PropertyId) property).getFirstValue();
    }

    /**
     * Returns the first value of a string property.
     */
    public static String getStringProperty(Properties properties, String name) {
        PropertyData<?> property = properties.getProperties().get(name);
        if (!(property instanceof PropertyString)) {
            return null;
        }

        return ((PropertyString) property).getFirstValue();
    }

    /**
     * Returns the first value of a datetime property.
     */
    public static GregorianCalendar getDateTimeProperty(Properties properties,
            String name) {
        PropertyData<?> property = properties.getProperties().get(name);
        if (!(property instanceof PropertyDateTime)) {
            return null;
        }

        return ((PropertyDateTime) property).getFirstValue();
    }

    // +JLL
    /**
     * Read file content
     * @param is
     * @return
     * @throws IOException
     */
    private static String readAllLines(InputStreamReader is) throws IOException {
        BufferedReader br = null;
        StringBuilder stb = new StringBuilder();
        try {
            String sCurrentLine;
            br = new BufferedReader(is);
            while ((sCurrentLine = br.readLine()) != null) {
                stb.append(sCurrentLine);
            }
        } finally {
            try {
                if (br != null)
                    br.close();
            } catch (IOException ex) {
                LOG.warn("When closing readAllLines stream ", ex);
                return null;
            }
        }
        return stb.toString();
    }

    /**
     * Writes the metadata to disc.
     */
    public static void writeMetadata(File newFile, Properties properties) {
        PrintWriter out = null;
        try {
            out = new PrintWriter(newFile);
            String absolutePath = newFile.getAbsolutePath();
            StringBuilder stb = new StringBuilder();
            Map<String, Object> metadata = new LinkedHashMap<String, Object>();

            if (allMetadata.containsKey(absolutePath)) {
                // load old properties
                metadata = allMetadata.get(absolutePath);
            }

            // update with new one
            for (PropertyData<?> propertyData : properties.getPropertyList()) {
                metadata.put(propertyData.getId(), propertyData.getFirstValue());
            }

            stb.append("{");
            stb.append("\r\n");
            for (Map.Entry<String, Object> entry : metadata.entrySet()) {
                stb.append(String.format("\"" + entry.getKey() + "\":\""
                        + entry.getValue() + "\""));
                stb.append("\r\n");
            }
            // remove last comma
            String json = stb.toString().substring(0, stb.length() - 2) + "\r\n}";
            out.print(json);
            out.flush();
            allMetadata.put(absolutePath, metadata);
            LOG.debug("Adding metadata from cache : {} / {}", absolutePath,
                    metadata);
        } catch (IOException e) {
            throw new CmisStorageException("Could not write metadata: "
                    + e.getMessage(), e);
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    /**
     * Delete metadata file from disc
     */
    public static void deleteMetadata(File metadataFile) {
        // delete metadata file
        if (metadataFile.exists()) {

            String absolutePath = metadataFile.getAbsolutePath();
            LOG.debug("Removing metadata from cache : {} / {}", absolutePath,
                    allMetadata.get(absolutePath));
            allMetadata.remove(absolutePath);

            if (!metadataFile.delete()) {
                throw new CmisStorageException("Metadata deletion failed!");
            }
        }
    }

    /**
     * Read the metadata from disc.
     */
    public static Map<String, Object> readMapMetadata(File metadataFile) {
        Map<String, Object> result = new HashMap<String, Object>();
        if (!metadataFile.exists()) {
            return result;
        }
        String absolutePath = metadataFile.getAbsolutePath();
        if (allMetadata.containsKey(absolutePath)) {
            result = allMetadata.get(absolutePath);
            LOG.debug("Retrieving metadata from cache : {} / {}", absolutePath,
                    result);
            return result;
        }
        String metadata = "";
        try {
            metadata = readAllLines(new FileReader(metadataFile));
        } catch (IOException e) {
            LOG.warn("When filtering with metadata", e);
            return result;
        }
        JSONObject JsonMetadata = null;
        try {
            JsonMetadata = (JSONObject) new JSONParser().parse(metadata);
            result = JsonMetadata;
        } catch (JSONParseException e) {
            LOG.warn("When parsing JSON metadata content", e);
            return result;
        }

        allMetadata.put(absolutePath, result);
        LOG.debug("Adding metadata into cache : {} / {}", absolutePath, result);

        return result;
    }

    static Map<String, Map<String, Object>> allMetadata = new LinkedHashMap<String, Map<String, Object>>();

    // _JLL
}
