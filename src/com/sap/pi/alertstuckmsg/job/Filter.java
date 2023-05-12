/**
 * Project: LocalDevelopment~pi.alertstuckmsg.ejb~cust.sap.com
 * File: Filter.java
 * Type: Filter
 * Author: Andreas Job
 * Creation Date: Apr 9, 2016
 */
package com.sap.pi.alertstuckmsg.job;

import com.sap.pi.alertstuckmsg.db.ProfileHashObject;
import com.sap.pi.alertstuckmsg.db.ProfileHashObject.ProfileAttributeName;
import com.sap.pi.alertstuckmsg.util.Utils;

/**
 * @author Andreas Job
 *
 */
public class Filter
{
	private String senderServiceFilter;
	private String senderPartyFilter;
	private String receiverServiceFilter;
	private String receiverPartyFilter;
	private String interfaceNameFilter;
	private String interfaceNamespaceFilter;

	public Filter(String senderServiceFilter, String senderPartyFilter, String receiverServiceFilter, String receiverPartyFilter, String interfaceNameFilter, String interfaceNamespaceFilter)
	{
		this.senderServiceFilter = senderServiceFilter;
		this.senderPartyFilter = senderPartyFilter;
		this.receiverServiceFilter = receiverServiceFilter;
		this.receiverPartyFilter = receiverPartyFilter;
		this.interfaceNameFilter = interfaceNameFilter;
		this.interfaceNamespaceFilter = interfaceNamespaceFilter;
	}

	public boolean matchesFilter(ProfileHashObject profileHashObject)
	{
		return this.matchesFilter(profileHashObject.getAttributeValue(ProfileAttributeName.FROM_SERVICE_NAME),
								  profileHashObject.getAttributeValue(ProfileAttributeName.FROM_PARTY_NAME),
								  profileHashObject.getAttributeValue(ProfileAttributeName.TO_SERVICE_NAME),
								  profileHashObject.getAttributeValue(ProfileAttributeName.TO_PARTY_NAME),
								  profileHashObject.getAttributeValue(ProfileAttributeName.ACTION_NAME),
								  profileHashObject.getAttributeValue(ProfileAttributeName.ACTION_TYPE));
	}

	public boolean matchesFilter(String senderService, String senderParty, String receiverService, String receiverParty, String interfaceName, String interfaceNamespace)
	{
		if (!matchesFilter(Utils.asString(senderServiceFilter), Utils.asString(senderService))) return false;
		if (!matchesFilter(Utils.asString(senderPartyFilter), Utils.asString(senderParty))) return false;
		if (!matchesFilter(Utils.asString(receiverServiceFilter), Utils.asString(receiverService))) return false;
		if (!matchesFilter(Utils.asString(receiverPartyFilter), Utils.asString(receiverParty))) return false;
		if (!matchesFilter(Utils.asString(interfaceNameFilter), Utils.asString(interfaceName))) return false;
		if (!matchesFilter(Utils.asString(interfaceNamespaceFilter), Utils.asString(interfaceNamespace))) return false;
		return true;
	}

	private static boolean matchesFilter(String filter, String text)
	{
		if ((filter == null) || (text == null)) return false;
		if (filter.equals("*")) return true;
		if (filter.isEmpty())
			return text.isEmpty();
		else if (text.isEmpty())
			return false;

		if (filter.startsWith("*") && filter.endsWith("*"))
		{
			filter = filter.substring(1, filter.length() - 1);
			return text.contains(filter);
		}
		else if (filter.startsWith("*"))
		{
			filter = filter.substring(1, filter.length());
			return text.endsWith(filter);
		}
		else if (filter.endsWith("*"))
		{
			filter = filter.substring(0, filter.length() - 1);
			return text.startsWith(filter);
		}
		else
			return filter.equals(text);
	}

	public String toString()
	{
		return Filter.class.getName() + ": senderService=" + senderServiceFilter + "; senderParty=" + senderPartyFilter + "; receiverService=" + receiverServiceFilter + "; receiverParty=" + receiverPartyFilter + "; interfaceName=" + interfaceNameFilter + "; interfaceNamespace=" + interfaceNamespaceFilter;
	}
}
// end class Filter