/*
 * Copyright 2013-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.openfeign.support;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.core.convert.ConversionFailedException;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.data.domain.Sort;

/**
 * This class provides support to serialize and deserialize spring {@link Sort} object.
 *
 * @author canbezmen
 */
public class SortJsonComponent {

	private static final Log LOG = LogFactory.getLog(SortJsonComponent.class);

	public static class SortSerializer extends JsonSerializer<Sort> {

		@Override
		public void serialize(Sort value, JsonGenerator gen,
			SerializerProvider serializers) throws IOException {
			gen.writeStartArray();
			value.iterator().forEachRemaining(v -> {
				try {
					gen.writeObject(v);
				}
				catch (IOException e) {
					String message = "Couldn't serialize Sort object for " + v;
					if (LOG.isErrorEnabled()) {
						LOG.error(message, e);
					}
					else {
						throw new ConversionFailedException(TypeDescriptor.valueOf(Sort.class),
							TypeDescriptor.valueOf(String.class), v, e);
					}
				}
			});
			gen.writeEndArray();
		}

		@Override
		public Class<Sort> handledType() {
			return Sort.class;
		}

	}

	public static class SortDeserializer extends JsonDeserializer<Sort> {

		@Override
		public Sort deserialize(JsonParser jsonParser,
			DeserializationContext deserializationContext) throws IOException {
			TreeNode treeNode = jsonParser.getCodec().readTree(jsonParser);
			if (treeNode.isArray()) {
				ArrayNode arrayNode = (ArrayNode) treeNode;
				List<Sort.Order> orders = new ArrayList<>();
				for (JsonNode jsonNode : arrayNode) {
					Sort.Order order = new Sort.Order(
						Sort.Direction.valueOf(jsonNode.get("direction").textValue()),
						jsonNode.get("property").textValue());
					orders.add(order);
				}
				return Sort.by(orders);
			}
			return null;
		}

		@Override
		public Class<Sort> handledType() {
			return Sort.class;
		}

	}

}
