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
package org.structr.core.property;

import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.apache.lucene.search.BooleanClause;
import org.structr.common.SecurityContext;
import org.structr.common.error.FrameworkException;
import org.structr.core.GraphObject;
import org.structr.core.PropertyValidator;
import org.structr.core.converter.PropertyConverter;
import org.structr.core.graph.NodeService;
import org.structr.core.graph.search.SearchAttribute;

/**
 * Base interface for typed property keys.
 *
 * @author Christian Morgner
 */
public interface PropertyKey<T> {

	/**
	 * Return the JSON name of this property.
	 * 
	 * @return 
	 */
	public String jsonName();
	
	/**
	 * Returns the database name of this property.
	 * 
	 * @return 
	 */
	public String dbName();
	
	/**
	 * Sets the name of this property in the JSON context. This
	 * is the key under which the property will be found in the
	 * JSON input/output.
	 * 
	 * @param jsonName 
	 */
	public void jsonName(String jsonName);

	/**
	 * Sets the name of this property in the database context. This
	 * is the key under which the property will be stored in the
	 * database.
	 * 
	 * @param dbName
	 */
	public void dbName(String dbName);
	
	/**
	 * Use this method to mark a property for indexing. This
	 * method registers the property in both the keyword and
	 * the fulltext index. To select the appropriate index
	 * for yourself, use the other indexed() methods.
	 * 
	 * @return the Property to satisfy the builder pattern
	 */
	public Property<T> indexed();
	
	/**
	 * Use this method to mark a property for indexing 
	 * in the given index.
	 * 
	 * @return the Property to satisfy the builder pattern
	 */
	public Property<T> indexed(NodeService.NodeIndex nodeIndex);
	
	/**
	 * Use this method to mark a property for indexing 
	 * in the given index.
	 * 
	 * @return the Property to satisfy the builder pattern
	 */
	public Property<T> indexed(NodeService.RelationshipIndex relIndex);
	
	/**
	 * Use this method to indicate that a property key can change its value
	 * without setProperty() being called directly on it. This method causes
	 * the given property to be indexed at the end of a transaction instead
	 * of immediately on setProperty(). This method registers the property
	 * in both the keyword and the fulltext index. To select the appropriate
	 * index for yourself, use the other indexed() methods.
	 * 
	 * @return the Property to satisfy the builder pattern
	 */
	public Property<T> passivelyIndexed();
	
	/**
	 * Use this method to indicate that a property key can change its value
	 * without setProperty() being called directly on it. This method causes
	 * the given property to be indexed at the end of a transaction instead
	 * of immediately on setProperty().
	 * 
	 * @return the Property to satisfy the builder pattern
	 */
	public Property<T> passivelyIndexed(NodeService.NodeIndex nodeIndex);
	
	/**
	 * Use this method to indicate that a property key can change its value
	 * without setProperty() being called directly on it. This method causes
	 * the given property to be indexed at the end of a transaction instead
	 * of immediately on setProperty().
	 * 
	 * @return the Property to satisfy the builder pattern
	 */
	public Property<T> passivelyIndexed(NodeService.RelationshipIndex relIndex);
	
	public Property<T> indexedWhenEmpty();
	
	
	/**
	 * Returns the desired type name that will be used in the error message if a
	 * wrong type was provided.
	 */
	public String typeName();
	
	/**
	 * Returns the type of the related property this property key references, or
	 * null if this is not a relationship property.
	 * 
	 * @return 
	 */
	public Class relatedType();
	
	/**
	 * Returns the default value for this property.
	 * 
	 * @return 
	 */
	public T defaultValue();
	
	public PropertyConverter<T, ?> databaseConverter(SecurityContext securityContext);
	public PropertyConverter<T, ?> databaseConverter(SecurityContext securityContext, GraphObject entity);
	public PropertyConverter<?, T> inputConverter(SecurityContext securityContext);

	public void addValidator(PropertyValidator<T> validator);
	public List<PropertyValidator<T>> getValidators();
	public boolean requiresSynchronization();
	public String getSynchronizationKey();
	
	public void setDeclaringClass(Class<? extends GraphObject> declaringClass);
	public Class<? extends GraphObject> getDeclaringClass();

	public T getProperty(SecurityContext securityContext, GraphObject obj, boolean applyConverter);
	public void setProperty(SecurityContext securityContext, GraphObject obj, T value) throws FrameworkException;

	public void registrationCallback(Class entityType);
	
	/**
	 * Indicates whether this property is an unvalidated property or not.
	 * If a transaction contains only modifications AND those modifications
	 * affect unvalidated properties only, structr will NOT call
	 * afterModification callbacks. This can be used to avoid endless
	 * loops after a transaction. Just mark the property key that causes
	 * the loop as unvalidated.
	 * 
	 * @return whether this property is unvalidated
	 */
	public boolean isUnvalidated();
	
	/**
	 * Indicates whether this property is read-only. Read-only properties
	 * will throw a FrameworkException with error code 422 when the value
	 * is modified.
	 * 
	 * @return 
	 */
	public boolean isReadOnly();

	/**
	 * Indicates whether this property is write-once. Write-once properties
	 * will throw a FrameworkException with error code 422 when the value
	 * is modified after it has been initially set.
	 * 
	 * @return 
	 */
	public boolean isWriteOnce();

	/**
	 * Indicates whether this property is indexed, i.e. searchable using
	 * REST queries.
	 * 
	 * @return 
	 */
	public boolean isIndexed();

	/**
	 * Indicates whether this property is indexed. The difference to the
	 * above method is, that the value for indexing will be obtained at
	 * the end of the transaction, so you can use this method to achieve
	 * indexing (and searchability) of properties that are never directly
	 * set using setProperty.
	 * 
	 * @return 
	 */
	public boolean isPassivelyIndexed();
	
	/**
	 * Indicates whether this property is searchable.
	 * @return 
	 */
	public boolean isSearchable();

	/**
	 * Indicates whether this property is searchable with an empty value.
	 * This behaviour is achieved by storing a special value for empty
	 * fields which can then later be found again.
	 * 
	 * @return 
	 */
	public boolean isIndexedWhenEmpty();

	/**
	 * Indicates whether this property represents a collection or a single
	 * value in the JSON output.
	 * 
	 * @return 
	 */
	public boolean isCollection();

	/**
	 * Returns the (lucene) sort type of this property.
	 * @return 
	 */
	public Integer getSortType();

	public void index(GraphObject entity, Object value);
	
	public SearchAttribute getSearchAttribute(SecurityContext securityContext, BooleanClause.Occur occur, T searchValue, boolean exactMatch);
	public List<SearchAttribute> extractSearchableAttribute(SecurityContext securityContext, HttpServletRequest request, boolean looseSearch) throws FrameworkException;
	public T extractSearchableAttribute(SecurityContext securityContext, String requestParameter) throws FrameworkException;
}
