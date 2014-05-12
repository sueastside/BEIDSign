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

package be.redtree.beidsign.action.executer;

import java.util.List;

import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;

public class PDFSignActionExecuter extends ActionExecuterAbstractBase 
{
	public static final String	PARAM_LOCATION          = "location";
    public static final String	PARAM_GEOLOCATION	   	= "geolocation";
    public static final String	PARAM_REASON            = "reason";
    public static final String	PARAM_VISIBLE           = "visible";
    public static final String	PARAM_SIGNATURE_JSON	= "signature-json";
    public static final String	PARAM_POSITION		    = "position";
    public static final String	PARAM_GRAPHIC			= "graphic";
    
    public static final String[] PARAMS = {PARAM_LOCATION, PARAM_GEOLOCATION, PARAM_REASON, 
    											PARAM_VISIBLE, PARAM_SIGNATURE_JSON, PARAM_POSITION, 
    											PARAM_GRAPHIC};

	@Override
	protected void executeImpl(Action ruleAction, NodeRef actionedUponNodeRef) 
	{
		/*
		 This is how the component looks like in Share.
		 Notice the submissionUrl, this is the AppletContextWebscript
		 which actually processes the parameters in this action.
		 [beidsign-share/src/main/amp/config/alfresco/web-extension/site-data/template-instances/beidsign-document.xml]
		 <component>
			<region-id>signature-form</region-id>
			<sub-components>
				<sub-component id="default">
					<url>/components/form</url>
					<properties>
						<submitType>json</submitType>
						<showCaption>true</showCaption>
						<formUI>true</formUI>
						<showCancelButton>true</showCancelButton>
						<showResetButton>true</showResetButton>
						<itemKind>action</itemKind>
						<itemId>beid.pdf-sign-action</itemId>
						<destination>{nodeRef}</destination>
						<redirect>/share/page/beidsign-document2?nodeRef={nodeRef}</redirect>
						<submissionUrl>/beid/appletcontext?nodeRef={nodeRef}</submissionUrl>
						<mode>create</mode>
					</properties>
				</sub-component>
			</sub-components>
		</component>
		 */
	}



	@Override
	protected void addParameterDefinitions(List<ParameterDefinition> paramList) 
	{
		paramList.add(new ParameterDefinitionImpl(PARAM_LOCATION, DataTypeDefinition.TEXT, false, getParamDisplayLabel(PARAM_LOCATION)));
        paramList.add(new ParameterDefinitionImpl(PARAM_GEOLOCATION, DataTypeDefinition.TEXT, false, getParamDisplayLabel(PARAM_GEOLOCATION)));
        paramList.add(new ParameterDefinitionImpl(PARAM_REASON, DataTypeDefinition.TEXT, false, getParamDisplayLabel(PARAM_REASON)));

        paramList.add(new ParameterDefinitionImpl(PARAM_SIGNATURE_JSON, DataTypeDefinition.TEXT, false, getParamDisplayLabel(PARAM_SIGNATURE_JSON)));
        paramList.add(new ParameterDefinitionImpl(PARAM_VISIBLE, DataTypeDefinition.BOOLEAN, false, getParamDisplayLabel(PARAM_VISIBLE)));
        paramList.add(new ParameterDefinitionImpl(PARAM_GRAPHIC, DataTypeDefinition.BOOLEAN, false, getParamDisplayLabel(PARAM_GRAPHIC)));
        paramList.add(new ParameterDefinitionImpl(PARAM_POSITION, DataTypeDefinition.TEXT, false, getParamDisplayLabel(PARAM_POSITION), false, "beidsign.position"));
	}
}
