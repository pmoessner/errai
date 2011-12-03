/*
 * Copyright 2011 JBoss, by Red Hat, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.marshalling.client.marshallers;

import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import org.jboss.errai.common.client.protocols.SerializationParts;
import org.jboss.errai.marshalling.client.api.annotations.ClientMarshaller;
import org.jboss.errai.marshalling.client.api.Marshaller;
import org.jboss.errai.marshalling.client.api.MarshallingSession;
import org.jboss.errai.marshalling.client.api.annotations.ImplementationAliases;
import org.jboss.errai.marshalling.client.util.MarshallUtil;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
@ClientMarshaller
@ImplementationAliases({AbstractMap.class, HashMap.class, LinkedHashMap.class})
public class MapMarshaller implements Marshaller<JSONValue, Map> {
  @Override
  public Class<Map> getTypeHandled() {
    return Map.class;
  }

  @Override
  public String getEncodingType() {
    return "json";
  }

  @Override
  public Map demarshall(JSONValue o, MarshallingSession ctx) {
    JSONObject jsonObject = o.isObject();
    if (jsonObject == null) return null;

    Map<Object, Object> map = new HashMap<Object, Object>();
    Marshaller<Object, Object> cachedKeyMarshaller = null;
    Marshaller<Object, Object> cachedValueMarshaller = null;

    Object demarshalledKey, demarshalledValue;
    for (String key : jsonObject.keySet()) {
      if (key.startsWith(SerializationParts.EMBEDDED_JSON)) {
        JSONValue val = JSONParser.parseStrict(key.substring(SerializationParts.EMBEDDED_JSON.length()));
        demarshalledKey = ctx.getMarshallerForType(ctx.determineTypeFor(null, val)).demarshall(val, ctx);
      }
      else {
        demarshalledKey = key;
      }

      JSONValue v = jsonObject.get(key);
      demarshalledValue = ctx.getMarshallerForType(ctx.determineTypeFor(null, v)).demarshall(v, ctx);

      map.put(demarshalledKey, demarshalledValue);
    }
    return map;
  }

  @Override
  public String marshall(Map o, MarshallingSession ctx) {
    if (o == null) {
      return "null";
    }
    StringBuilder buf = new StringBuilder("{");

    Object key, val;
    int i = 0;
    for (Map.Entry<Object, Object> entry : ((Map<Object, Object>) o).entrySet()) {
      if (i++ > 0) {
        buf.append(",");
      }
      key = entry.getKey();
      val = entry.getValue();
      if (key instanceof String) {
        buf.append("\"" + key + "\"");
      }
      else if (key != null) {
        buf.append(("\"" + SerializationParts.EMBEDDED_JSON))
                .append(MarshallUtil.jsonStringEscape(ctx.marshall(key)))
                .append("\"");
      }

      buf.append(":");

      if (val == null) {
        buf.append("null");
      }
      else {
        buf.append(ctx.marshall(val));
      }
    }

    return buf.append("}").toString();
  }

  @Override
  public boolean handles(JSONValue o) {
    return o.isArray() != null;
  }
}
