/**
 * Copyright (C) 2010-2013 Axel Morgner, structr <structr@structr.org>
 *
 * This file is part of structr <http://structr.org>.
 *
 * structr is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * structr is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with structr.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.structr.core.notion;

import org.structr.core.property.PropertyKey;

/**
 * Combines a {@link PropertySerializationStrategy} and an {@link IdDeserializationStrategy}
 * to read/write a relationship entity.
 *
 * @author Christian Morgner
 */
public class RelationshipNotion extends Notion {

	private PropertyKey propertyKey = null;

	public RelationshipNotion(PropertyKey propertyKey) {

		super(
			new PropertySerializationStrategy(propertyKey),
			new IdDeserializationStrategy()
		);

		this.propertyKey = propertyKey;
	}
	
	public RelationshipNotion(PropertyKey propertyKey, boolean createIfNotExisting) {

		super(
			new PropertySerializationStrategy(propertyKey),
			new IdDeserializationStrategy(propertyKey, createIfNotExisting)
		);
		
		this.propertyKey = propertyKey;
	}
        
	@Override
	public PropertyKey getPrimaryPropertyKey() {
		return propertyKey;
	}
}
