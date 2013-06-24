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

package org.structr.core.entity;

import org.structr.common.error.EmptyPropertyToken;
import org.structr.common.error.ErrorBuffer;
import org.structr.common.error.NullPropertyToken;
import org.structr.core.property.Property;

/**
 * A node with validation. This is a convenience class that avoids having to
 * implement the validation methods for all types separately.
 * 
 * @author Christian Morgner
 */
public abstract class ValidatedNode extends AbstractNode {
	
	protected boolean nonEmpty(Property<String> property, ErrorBuffer errorBuffer) {
		
		String value  = getProperty(property);
		boolean valid = true;
		
		if (value == null) {
			
			errorBuffer.add(getClass().getSimpleName(), new NullPropertyToken(property));
			valid = false;
			
		} else if (value.isEmpty()) {
			
			errorBuffer.add(getClass().getSimpleName(), new EmptyPropertyToken(property));
			valid = false;
		}

		return valid;
	}
	
	protected boolean nonNull(Property property, ErrorBuffer errorBuffer) {
		
		Object value  = getProperty(property);
		boolean valid = true;
		
		if (value == null) {
			
			errorBuffer.add(getClass().getSimpleName(), new EmptyPropertyToken(property));
			valid = false;
		}

		return valid;
	}
}
