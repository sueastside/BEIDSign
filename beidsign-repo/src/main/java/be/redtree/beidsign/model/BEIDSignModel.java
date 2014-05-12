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

package be.redtree.beidsign.model;

import org.alfresco.service.namespace.QName;

public interface BEIDSignModel 
{
	// namespace
	static final String BEID_MODEL_1_0_URI = "http://beidsign.redtree.be/model/signature/1.0";
	
	// signature image type
	static final QName TYPE_SIGNATUREIMAGE = QName.createQName(BEID_MODEL_1_0_URI, "signatureImage");
	static final QName PROP_SIGNATUREJSON = QName.createQName(BEID_MODEL_1_0_URI, "signaureJson");

	// signer aspect and properties
	static final QName ASPECT_SIGNER = QName.createQName(BEID_MODEL_1_0_URI, "signer");
	static final QName ASSOC_SIGNERSIGNATUREIMAGE = QName.createQName(BEID_MODEL_1_0_URI, "signerSignatureImage");
}
