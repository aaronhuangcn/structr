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


package org.structr.core.agent;

import org.structr.core.graph.StructrTransaction;
import org.structr.core.graph.RelationshipFactory;
import org.structr.core.graph.GraphDatabaseCommand;
import org.structr.core.graph.NodeFactory;
import org.structr.core.graph.TransactionCommand;
import java.util.Iterator;
import org.neo4j.graphdb.GraphDatabaseService;

import org.structr.common.SecurityContext;
import org.structr.common.error.FrameworkException;
import org.structr.core.Services;

//~--- JDK imports ------------------------------------------------------------

import java.util.logging.Level;
import java.util.logging.Logger;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.index.Index;
import org.neo4j.tooling.GlobalGraphOperations;
import org.structr.common.PropertyView;
import org.structr.core.EntityContext;
import org.structr.core.GraphObject;
import org.structr.core.entity.AbstractNode;
import org.structr.core.entity.AbstractRelationship;
import org.structr.core.graph.NodeService;
import org.structr.core.property.PropertyKey;

//~--- classes ----------------------------------------------------------------

/**
 * The agent class that acts on {@link RebuildIndexTask}, rebuilding the Lucene
 * index in structr.
 * 
 * @author Axel Morgner
 */
public class RebuildIndexAgent extends Agent {

	private static final Logger logger = Logger.getLogger(RebuildIndexAgent.class.getName());

	//~--- constructors ---------------------------------------------------

	public RebuildIndexAgent() {
		setName("RebuildIndexAgent");
	}

	//~--- methods --------------------------------------------------------

	@Override
	public ReturnValue processTask(Task task) throws FrameworkException {

		if (task instanceof RebuildIndexTask) {

			long t0 = System.currentTimeMillis();

			logger.log(Level.INFO, "Starting rebuilding index ...");

			long nodes = rebuildNodeIndex();
			long t1    = System.currentTimeMillis();

			logger.log(Level.INFO, "Re-indexing nodes finished, {0} nodes processed in {1} s", new Object[] { nodes, (t1 - t0) / 1000 });

			long rels = rebuildRelationshipIndex();
			long t2   = System.currentTimeMillis();

			logger.log(Level.INFO, "Re-indexing relationships finished, {0} relationships processed in {1} s", new Object[] { rels, (t2 - t1) / 1000 });

		}

		return (ReturnValue.Success);
	}

	private long rebuildNodeIndex() throws FrameworkException {

		final SecurityContext securityContext = SecurityContext.getSuperUserInstance();
		final GraphDatabaseService graphDb    = Services.command(securityContext, GraphDatabaseCommand.class).execute();
		final NodeFactory nodeFactory         = new NodeFactory(securityContext);

		final Iterable<Node> allNodes         = GlobalGraphOperations.at(graphDb).getAllNodes();
		final NodeService nodeService         = Services.getService(NodeService.class);
		long nodeCount                        = 0L;
	
		logger.log(Level.INFO, "Start indexing of nodes.");
		
		final Iterator<Node> nodeIterator = allNodes.iterator();
		while (nodeIterator.hasNext()) {

			nodeCount += Services.command(securityContext, TransactionCommand.class).execute(new StructrTransaction<Integer>(false) {

				@Override
				public Integer execute() throws FrameworkException {

					int count = 0;

					while (nodeIterator.hasNext()) {

						try {
							Node dbNode = nodeIterator.next();

							if (dbNode.hasProperty(GraphObject.uuid.dbName())) {

								nodeFactory.instantiate(dbNode).updateInIndex();

								// restart transaction after 2000 iterations
								if (++count == 2000) {
									break;
								}
							}
							
						} catch(Throwable t) {}
					}

					return count;
				}

			});

			logger.log(Level.INFO, "Indexed {0} nodes ...", nodeCount);
		}

		logger.log(Level.INFO, "Done");

		return nodeCount;
	}

	private long rebuildRelationshipIndex() throws FrameworkException {

		final SecurityContext securityContext         = SecurityContext.getSuperUserInstance();
		final GraphDatabaseService graphDb            = Services.command(securityContext, GraphDatabaseCommand.class).execute();
		final RelationshipFactory relFactory          = new RelationshipFactory(securityContext);

		final Iterable<Relationship> allRelationships = GlobalGraphOperations.at(graphDb).getAllRelationships();
		final NodeService nodeService                 = Services.getService(NodeService.class);
		long relationshipCount                        = 0L;
	
		logger.log(Level.INFO, "Start indexing of relationships.");
		
		final Iterator<Relationship> relationshipIterator = allRelationships.iterator();
		while (relationshipIterator.hasNext()) {

			relationshipCount += Services.command(securityContext, TransactionCommand.class).execute(new StructrTransaction<Integer>(false) {

				@Override
				public Integer execute() throws FrameworkException {

					int count = 0;

					while (relationshipIterator.hasNext()) {

						try {
							Relationship dbRelationship = relationshipIterator.next();

							if (dbRelationship.hasProperty(GraphObject.uuid.dbName())) {

								relFactory.instantiate(dbRelationship).updateInIndex();

								// restart transaction after 2000 iterations
								if (++count == 2000) {
									break;
								}
							}
							
						} catch(Throwable t) {}
					}

					return count;
				}

			});

			logger.log(Level.INFO, "Indexed {0} relationships ...", relationshipCount);
		}

		logger.log(Level.INFO, "Done");

		return relationshipCount;
	}

	//~--- get methods ----------------------------------------------------

	@Override
	public Class getSupportedTaskType() {
		return (RebuildIndexTask.class);
	}
}
