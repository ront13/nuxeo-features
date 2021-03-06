/*
 * Copyright (c) 2006-2012 Nuxeo SA (http://nuxeo.com/) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Nuxeo
 */
package org.nuxeo.ecm.automation.client.jaxrs.spi.marshallers;

import java.io.Serializable;
import java.util.Map;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.nuxeo.ecm.automation.client.jaxrs.spi.JsonMarshaller;
import org.nuxeo.ecm.automation.client.model.RecordSet;

/**
 * Manage JSON Decoding of RecordSet object returned by QueryAndFetch
 * 
 * @author <a href="mailto:tdelprat@nuxeo.com">Tiry</a>
 * @since 5.7
 */
public class RecordSetMarshaller implements JsonMarshaller<RecordSet> {

    @Override
    public String getType() {
        return "recordset";
    }

    @Override
    public Class<RecordSet> getJavaType() {
        return RecordSet.class;
    }

    @Override
    public RecordSet read(JsonParser jp) throws Exception {
        jp.nextToken();
        String key = jp.getCurrentName();
        if ("isPaginable".equals(key)) {
            jp.nextToken();
            boolean isPaginable = jp.getBooleanValue();
            if (isPaginable) {
                jp.nextToken();
                return readPaginableRecordSet(jp);
            }
        }
        return readRecord(jp);
    }

    protected RecordSet readPaginableRecordSet(JsonParser jp) throws Exception {
        RecordSet record = new RecordSet();
        JsonToken tok = jp.getCurrentToken();
        while (tok != JsonToken.END_OBJECT) {
            String key = jp.getCurrentName();
            jp.nextToken();
            if ("pageSize".equals(key)) {
                record.setPageSize(jp.getIntValue());
            } else if ("pageCount".equals(key)) {
                record.setPageCount(jp.getIntValue());
            } else if ("pageIndex".equals(key)) {
                record.setPageIndex(jp.getIntValue());
            } else if ("entries".equals(key)) {
                readRecordEntries(jp, record);
            }
            tok = jp.nextToken();
        }
        return record;
    }

    protected RecordSet readRecord(JsonParser jp) throws Exception {
        RecordSet record = new RecordSet();
        JsonToken tok = jp.nextToken();
        while (tok != JsonToken.END_ARRAY) {
            String key = jp.getCurrentName();
            if ("entries".equals(key)) {
                readRecordEntries(jp, record);
                return record;
            }
            tok = jp.nextToken();
        }
        return record;
    }

    protected void readRecordEntries(JsonParser jp, RecordSet record)
            throws Exception {
        JsonToken tok = jp.nextToken();
        while (tok != JsonToken.END_ARRAY) {
            Map<String, Serializable> entry = jp.readValueAs(Map.class);
            record.add(entry);
            tok = jp.nextToken();
        }
    }

    @Override
    public void write(JsonGenerator jg, RecordSet value) throws Exception {
    }

}
