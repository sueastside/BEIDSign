/*
 * Copyright (C) 2014  Jelle Hellemans
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package be.redtree.beidsign.service;

import java.util.List;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class BeidSignService
{
	private ServiceRegistry 	serviceRegistry;

	public NodeRef getUserSignatureNode(String person, QName assoc)
	{
		NodeRef personNode = serviceRegistry.getPersonService().getPerson(person);
		return getUserSignatureNode(personNode, assoc);
	}

	public NodeRef getUserSignatureNode(NodeRef person, QName assoc)
    {
		NodeRef node = null;
		List<ChildAssociationRef> nodes = serviceRegistry.getNodeService()
				.getChildAssocs(
						person, 
						assoc, 
						null, 
						Integer.MAX_VALUE, 
						false);

		if(nodes != null && nodes.size() > 0)
		{
			node = nodes.get(0).getChildRef();
		}

		return node;
    }
	
	public void setServiceRegistry(ServiceRegistry serviceRegistry)
	{
		this.serviceRegistry = serviceRegistry;
	}
}
