/*******************************************************************************
 * * Copyright 2012 Impetus Infotech.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 ******************************************************************************/
package com.impetus.client.cassandra.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.cassandra.db.marshal.CounterColumnType;
import org.apache.cassandra.locator.SimpleStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.impetus.client.cassandra.common.CassandraConstants;
import com.impetus.kundera.configure.AbstractPropertyReader;
import com.impetus.kundera.configure.ClientProperties;
import com.impetus.kundera.configure.ClientProperties.DataStore;
import com.impetus.kundera.configure.ClientProperties.DataStore.Connection.Server;
import com.impetus.kundera.configure.ClientProperties.DataStore.Schema;
import com.impetus.kundera.configure.ClientProperties.DataStore.Schema.Table;
import com.impetus.kundera.configure.PropertyReader;
import com.impetus.kundera.metadata.model.PersistenceUnitMetadata;

/**
 * Cassandra Property Reader reads cassandra properties from property file
 * {kundera-cassandra.properties} and put it into cassandra schema metadata.
 * 
 * @author kuldeep.mishra
 * 
 */
public class CassandraPropertyReader extends AbstractPropertyReader implements PropertyReader
{
    /** The log instance. */
    private Logger logger = LoggerFactory.getLogger(CassandraPropertyReader.class);

    /** csmd instance of CassandraSchemaMetadata */

    public static CassandraSchemaMetadata csmd;

    private static final String dataStoreName = "cassandra";

    public CassandraPropertyReader(Map externalProperties, final PersistenceUnitMetadata puMetadata)
    {
        super(externalProperties, puMetadata);
        csmd = new CassandraSchemaMetadata();
    }

    public void onXml(ClientProperties cp)
    {	
    	logger.info("Client properties loaded from property file : " + cp);
        if (cp != null)
        {
            csmd.setClientProperties(cp);
        }
    }

    /**
     * Cassandra schema metadata holds metadata information.
     * 
     * @author kuldeep.mishra
     * 
     */
    public class CassandraSchemaMetadata extends AbstractSchemaMetadata
    {

        /**
         * @return the replication_factor
         */
        public String getReplication_factor(String schemaName)
        {
            Schema schema = getSchema(schemaName);
            String replication = "1";
            if (schema != null && schema.getSchemaProperties() != null && !schema.getSchemaProperties().isEmpty())
            {
                replication = schema.getSchemaProperties().getProperty(CassandraConstants.REPLICATION_FACTOR);
            }

            if (logger.isInfoEnabled())
            {
                logger.info("Returning replication factor value {}", replication);
            }
            return replication;
        }

        /**
         * @return the placement_strategy
         */
        public String getPlacement_strategy(String schemaName)
        {
            Schema schema = getSchema(schemaName);
            String placementStrategy = SimpleStrategy.class.getName();
            if (schema != null && schema.getSchemaProperties() != null && !schema.getSchemaProperties().isEmpty())
            {
                placementStrategy = schema.getSchemaProperties().getProperty(CassandraConstants.PLACEMENT_STRATEGY);
            }

            if (logger.isInfoEnabled())
            {
                logger.info("Returning placement strategy value {}", placementStrategy);
            }
            return placementStrategy;
        }

        public boolean isCounterColumn(String schemaName, String cfName)
        {
            Table table = getColumnFamily(schemaName, cfName);
            if (table != null)
            {
                return table.getProperties().getProperty(CassandraConstants.DEFAULT_VALIDATION_CLASS)
                        .equalsIgnoreCase(CounterColumnType.class.getSimpleName()) ? true : false;
            }
            return false;
        }

        public boolean isInvertedIndexingEnabled(String schemaName)
        {
            DataStore dataStore = getDataStore(dataStoreName);
            boolean result = false;
            if (schemaName != null && dataStore != null && dataStore.getSchemas() != null)
            {
                for (Schema schema : dataStore.getSchemas())
                {
                    if (schema != null && schemaName.equals(schema.getName()) && schema.getSchemaProperties() != null
                            && schema.getSchemaProperties() != null)
                    {
                        result = Boolean.parseBoolean((String) schema.getSchemaProperties().get(
                                CassandraConstants.INVERTED_INDEXING_ENABLED));
                        break;
                    }
                }

            }
            return result;
        }

        public String getCqlVersion()
        {
            DataStore dataStore = getDataStore(dataStoreName);

            if (dataStore != null)
            {
                Properties properties = dataStore.getConnection() != null ? dataStore.getConnection().getProperties()
                        : null;

                if (properties != null)
                {
                    String cqlVersion = properties.getProperty(CassandraConstants.CQL_VERSION);
                    if (cqlVersion != null)
                    {
                        if (cqlVersion.equalsIgnoreCase(CassandraConstants.CQL_VERSION_3_0)
                                || cqlVersion.equalsIgnoreCase(CassandraConstants.CQL_VERSION_2_0))
                        {
                            return cqlVersion;
                        }
                        else
                        {
                            logger.warn("Invalid {} cql version provided, valid are {},{}.", cqlVersion,
                                    CassandraConstants.CQL_VERSION_2_0, CassandraConstants.CQL_VERSION_3_0);
                        }
                    }
                }
            }
            return CassandraConstants.CQL_VERSION_2_0;
        }

        public Schema getSchema(String schemaName)
        {
            DataStore dataStore = getDataStore(dataStoreName);

            if (dataStore != null)
            {
                List<Schema> schemas = dataStore.getSchemas();
                if (schemas != null && !schemas.isEmpty())
                {
                    for (Schema s : schemas)
                    {
                        if (s != null && s.getName() != null && s.getName().equalsIgnoreCase(schemaName))
                        {
                            return s;
                        }
                    }
                }
            }
            return null;
        }

        public Table getColumnFamily(String schemaName, String cfName)
        {
            Schema schema = getSchema(schemaName);
            if (schema != null)
            {
                if (schema.getTables() != null)
                {
                    for (Table table : schema.getTables())
                    {
                        if (table != null && table.getName() != null && table.getName().equalsIgnoreCase(cfName))
                        {
                            return table;
                        }
                    }
                    if (logger.isWarnEnabled())
                    {
                        logger.warn("No column family schema found, returning null.");
                    }
                }
            }
            return null;
        }

        public Properties getConnectionProperties()
        {
            DataStore ds = getDataStore(dataStoreName);
            Properties properties = new Properties();
            if (ds != null)
            {
                if (ds.getConnection() != null)
                {
                    properties = ds.getConnection().getProperties();
                    return properties != null ? properties : new Properties();
                }
                if (logger.isWarnEnabled())
                {
                    logger.warn("No connection properties found, returning none.");
                }
            }
            return properties;
        }

        public List<Server> getConnectionServers()
        {
            DataStore ds = getDataStore(dataStoreName);
            List<Server> servers = new ArrayList<Server>();
            if (ds != null && ds.getConnection() != null)
            {
                servers = ds.getConnection().getServers();
                return servers;
            }
            return servers;
        }

        public Properties getDatastoreProperties()
        {
            DataStore ds = getDataStore(dataStoreName);
            Properties properties = new Properties();
            if (ds != null && ds.getProperties() != null)
            {
                properties.putAll(ds.getProperties());
            }
            return properties;
        }
    }
}
