/*
 * Copyright Siemens AG, 2013-2015. Part of the SW360 Portal Project.
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License Version 2.0 as published by the
 * Free Software Foundation with classpath exception.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License version 2.0 for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program (please see the COPYING file); if not, write to the Free
 * Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 */
package com.siemens.sw360.portal.common;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.apache.log4j.Logger;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.apache.thrift.TSerializer;
import org.apache.thrift.protocol.TSimpleJSONProtocol;

import java.io.IOException;

/**
 *
 * @author cedric.bodet@tngtech.com
 */
public class ThriftJsonSerializer {


    private static final Logger log = Logger.getLogger(ThriftJsonSerializer.class);

    ObjectMapper mapper;

    public ThriftJsonSerializer() {
        // Create a module with the proper serializer for Thrift classes
        SimpleModule module = new SimpleModule();
        module.addSerializer(TBase.class, new TBaseSerializer());

        // Create the object mapper and register the module
        mapper = new ObjectMapper();
        mapper.registerModule(module);
    }

    public String toJson(Object object) throws IOException {
        return mapper.writeValueAsString(object);
    }

    /**
     * Helper class to serialize Thrift object within Jackson while using the Thrift serializer
     */
    static class TBaseSerializer extends JsonSerializer<TBase> {

        TSerializer serializer;

        public TBaseSerializer() {
            // Initialize the serializer with the standard protocol
            serializer = new TSerializer(new TSimpleJSONProtocol.Factory());
        }

        @Override
        public void serialize(TBase tBase, JsonGenerator generator, SerializerProvider provider) throws IOException {
            String json = null;
            try {
                json = serializer.toString(tBase);
            } catch (TException e) {
                log.error("Error serializing Thrift object.", e);
            }
            // Write the raw value that was generated by Thrift.
            generator.writeRawValue(json);
        }
    }
}