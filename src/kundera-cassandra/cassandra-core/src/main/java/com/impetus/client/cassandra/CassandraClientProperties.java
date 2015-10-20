/**
 * Copyright 2012 Impetus Infotech.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.impetus.client.cassandra;

import java.util.Map;

import org.apache.cassandra.thrift.ConsistencyLevel;
import org.apache.commons.lang.StringUtils;

import com.impetus.client.cassandra.common.CassandraConstants;
import com.impetus.kundera.PersistenceProperties;
import com.impetus.kundera.client.Client;
import com.impetus.kundera.client.ClientPropertiesSetter;

/**
 * Cassandra implementation of {@link ClientPropertiesSetter}.
 *
 * @author amresh.singh
 */
class CassandraClientProperties
{
    
    /** The Constant TTL_VALUES. */
    private static final String TTL_VALUES = "ttl.values";

    /** The Constant TTL_PER_REQUEST. */
    private static final String TTL_PER_REQUEST = "ttl.per.request";

    /** The Constant TTL_PER_SESSION. */
    private static final String TTL_PER_SESSION = "ttl.per.session";

    /** The Constant CONSISTENCY_LEVEL. */
    private static final String CONSISTENCY_LEVEL = "consistency.level";

    /** The Constant CQL_VERSION. */
    private static final String CQL_VERSION = CassandraConstants.CQL_VERSION;

    /** The cassandra client base. */
    private CassandraClientBase cassandraClientBase;

    /**
     * Populate client properties.
     *
     * @param client the client
     * @param properties the properties
     */
    public void populateClientProperties(Client client, Map<String, Object> properties)
    {	
        this.cassandraClientBase = (CassandraClientBase) client;

        if (properties != null)
        {
            for (String key : properties.keySet())
            {

                Object value = properties.get(key);

                if (checkNull(key, value))
                {

                    if (key.equals(CONSISTENCY_LEVEL))
                    {
                        setConsistencylevel(value);
                    }
                    else if (key.equals(CQL_VERSION) && value instanceof String)
                    {
                        this.cassandraClientBase.setCqlVersion((String) value);
                    }
                    else if (key.equals(TTL_PER_SESSION))
                    {
                        setTTLPerSession(value);
                    }
                    else if (key.equals(TTL_PER_REQUEST))
                    {
                        setTTLPerRequest(value);
                    }
                    else if (key.equals(TTL_VALUES) && value instanceof Map)
                    {
                        this.cassandraClientBase.setTtlValues((Map) value);
                    }
                    else if (key.equals(PersistenceProperties.KUNDERA_BATCH_SIZE)
                            && !StringUtils.isBlank(value.toString()) && StringUtils.isNumeric(value.toString()))
                    {
                        this.cassandraClientBase.setBatchSize(value.toString());
                    }

                    // Add more properties as needed
                }
            }
        }
    }

    /**
     * set ttl per request.
     *
     * @param value the new TTL per request
     */
    private void setTTLPerRequest(Object value)
    {
        if (value instanceof String)
        {
            if (Boolean.valueOf((String) value).booleanValue())
            {
                this.cassandraClientBase.setTtlPerSession(false);
            }
            this.cassandraClientBase.setTtlPerRequest(Boolean.valueOf((String) value));

        }
        else if (value instanceof Boolean)
        {
            if (((Boolean) value).booleanValue())
            {
                this.cassandraClientBase.setTtlPerSession(false);
            }

            this.cassandraClientBase.setTtlPerRequest((Boolean) value);

        }

    }

    /**
     * set ttl per session.
     *
     * @param value the new TTL per session
     */
    private void setTTLPerSession(Object value)
    {
        if (value instanceof String)
        {
            if (Boolean.valueOf((String) value).booleanValue())
            {
                this.cassandraClientBase.setTtlPerRequest(false);
            }
            this.cassandraClientBase.setTtlPerSession(Boolean.valueOf((String) value));

        }
        else if (value instanceof Boolean)
        {
            if (((Boolean) value).booleanValue())
            {
                this.cassandraClientBase.setTtlPerRequest(false);
            }

            this.cassandraClientBase.setTtlPerSession((Boolean) value);

        }

    }

    /**
     * set consistency level.
     *
     * @param value the new consistencylevel
     */
    private void setConsistencylevel(Object value)
    {
        if (value instanceof String)
        {
            this.cassandraClientBase.setConsistencyLevel(ConsistencyLevel.valueOf((String) value));
        }
        else if (value instanceof ConsistencyLevel)
        {
            this.cassandraClientBase.setConsistencyLevel((ConsistencyLevel) value);
        }

    }

    /**
     * check key value map not null.
     *
     * @param key the key
     * @param value the value
     * @return true, if successful
     */
    private boolean checkNull(String key, Object value)
    {
        return key != null && value != null;
    }
}
