/**
 * Project: LocalDevelopment~pi.alertstuckmsg.ejb~cust.sap.com
 * File: MessageOverviewData.java
 * Type: MessageOverviewData
 * Author: Andreas Job
 * Creation Date: Apr 9, 2016
 */
package com.sap.pi.alertstuckmsg.job;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import com.sap.pi.alertstuckmsg.db.ProfileCacheObject;
import com.sap.pi.alertstuckmsg.db.ProfileHashObject;
import com.sap.pi.alertstuckmsg.util.Utils;

/**
 * @author Andreas Job
 *
 */
public class MessageOverviewData
{
	private String senderService;
	private String senderParty;
	private String receiverService;
	private String receiverParty;
	private String interfaceName;
	private String interfaceNamespace;
	private String scenarioID;

	private Set<String> status = null;
	private long counter = 0;
	private Set<String> messageIDs = null;

	/*public MessageOverviewData(String senderService, String senderParty, String receiverService, String receiverParty, String interfaceName, String interfaceNamespace, String scenarioID)
	{
		this.senderService = senderService;
		this.senderParty = senderParty;
		this.receiverService = receiverService;
		this.receiverParty = receiverParty;
		this.interfaceName = interfaceName;
		this.interfaceNamespace = interfaceNamespace;
		this.scenarioID = scenarioID;
	}

	public MessageOverviewData(String senderService, String senderParty, String receiverService, String receiverParty, String interfaceName, String interfaceNamespace, String scenarioID, Set<String> status, long counter, Set<String> messageIDs)
	{
		this.senderService = senderService;
		this.senderParty = senderParty;
		this.receiverService = receiverService;
		this.receiverParty = receiverParty;
		this.interfaceName = interfaceName;
		this.interfaceNamespace = interfaceNamespace;
		this.scenarioID = scenarioID;
		this.status = status;
		this.counter = counter;
		this.messageIDs = messageIDs;
	}*/

	public MessageOverviewData(ProfileHashObject profHashObject)
	{
		Properties data = profHashObject.getParsedMetadata();

		senderService = (String)data.get(ProfileHashObject.ProfileAttributeName.FROM_SERVICE_NAME.toString());
		senderParty = (String)data.get(ProfileHashObject.ProfileAttributeName.FROM_PARTY_NAME.toString());
		receiverService = (String)data.get(ProfileHashObject.ProfileAttributeName.TO_SERVICE_NAME.toString());
		receiverParty = (String)data.get(ProfileHashObject.ProfileAttributeName.TO_PARTY_NAME.toString());
		interfaceName = (String)data.get(ProfileHashObject.ProfileAttributeName.ACTION_NAME.toString());
		interfaceNamespace = (String)data.get(ProfileHashObject.ProfileAttributeName.ACTION_TYPE.toString());
		scenarioID = (String)data.get(ProfileHashObject.ProfileAttributeName.SCENARIO_IDENTIFIER.toString());
	}

	public MessageOverviewData(ProfileHashObject profHashObject, ProfileCacheObject profileCacheObject)
	{
		Properties data = profHashObject.getParsedMetadata();

		senderService = (String)data.get(ProfileHashObject.ProfileAttributeName.FROM_SERVICE_NAME.toString());
		senderParty = (String)data.get(ProfileHashObject.ProfileAttributeName.FROM_PARTY_NAME.toString());
		receiverService = (String)data.get(ProfileHashObject.ProfileAttributeName.TO_SERVICE_NAME.toString());
		receiverParty = (String)data.get(ProfileHashObject.ProfileAttributeName.TO_PARTY_NAME.toString());
		interfaceName = (String)data.get(ProfileHashObject.ProfileAttributeName.ACTION_NAME.toString());
		interfaceNamespace = (String)data.get(ProfileHashObject.ProfileAttributeName.ACTION_TYPE.toString());
		scenarioID = (String)data.get(ProfileHashObject.ProfileAttributeName.SCENARIO_IDENTIFIER.toString());

		this.counter = profileCacheObject.getCounter();
		this.addStatus(profileCacheObject.getStatus());
		this.addMessageID(profileCacheObject.getMsgID());
	}

	public boolean merge(MessageOverviewData other)
	{
		if (!this.equals(other))
			return false;

		Set<String> otherStatusList = other.getStatus();
		if (otherStatusList != null)
			for (String status : otherStatusList)
				this.addStatus(status);

		this.counter += other.counter;

		Set<String> otherMessageIDs = other.getMessageIDs();
		if (otherMessageIDs != null)
			for (String msgID : otherMessageIDs)
				this.addMessageID(msgID);

		return true;
	}

	public String getSenderService()
	{
		return senderService;
	}

	public void setSenderService(String senderService)
	{
		this.senderService = senderService;
	}

	public String getSenderParty()
	{
		return senderParty;
	}

	public void setSenderParty(String senderParty)
	{
		this.senderParty = senderParty;
	}

	public String getReceiverService()
	{
		return receiverService;
	}

	public void setReceiverService(String receiverService)
	{
		this.receiverService = receiverService;
	}

	public String getReceiverParty()
	{
		return receiverParty;
	}

	public void setReceiverParty(String receiverParty)
	{
		this.receiverParty = receiverParty;
	}

	public String getInterfaceName()
	{
		return interfaceName;
	}

	public void setInterfaceName(String interfaceName)
	{
		this.interfaceName = interfaceName;
	}

	public String getInterfaceNamespace()
	{
		return interfaceNamespace;
	}

	public void setInterfaceNamespace(String interfaceNamespace)
	{
		this.interfaceNamespace = interfaceNamespace;
	}

	public String getScenarioID()
	{
		return scenarioID;
	}

	public void setScenarioID(String scenarioID)
	{
		this.scenarioID = scenarioID;
	}

	public Set<String> getStatus()
	{
		return status;
	}

	public void addStatus(String status)
	{
		if (status == null)
			return;
		if (this.status == null)
			this.status = new HashSet<String>(2);

		if (!this.status.contains(status))
			this.status.add(status);
	}

	public long getCounter()
	{
		return counter;
	}

	public void setCounter(long counter)
	{
		this.counter = counter;
	}

	public Set<String> getMessageIDs()
	{
		return messageIDs;
	}

	public void setMessageIDs(Set<String> messageIDs)
	{
		this.messageIDs = messageIDs;
	}

	public void addMessageID(String messageID)
	{
		if (messageID == null)
			return;
		if (this.messageIDs == null)
			this.messageIDs = new HashSet<String>(2);

		if (!messageIDs.contains(messageID))
			this.messageIDs.add(messageID);
	}

	public boolean equals(Object other)
	{
		if (other == null)
			return false;
		if (this == other)
			return true;

		if (other instanceof MessageOverviewData)
		{
			MessageOverviewData otherData = (MessageOverviewData) other;

			if ((senderService == null && otherData.senderService != null) || (senderService != null && otherData.senderService == null)) return false;
			if (!Utils.asString(senderService).equals(otherData.senderService)) return false;

			if ((senderParty == null && otherData.senderParty != null) || (senderParty != null && otherData.senderParty == null)) return false;
			if (!Utils.asString(senderParty).equals(otherData.senderParty)) return false;

			if ((receiverService == null && otherData.receiverService != null) || (receiverService != null && otherData.receiverService == null)) return false;
			if (!Utils.asString(receiverService).equals(otherData.receiverService)) return false;

			if ((receiverParty == null && otherData.receiverParty != null) || (receiverParty != null && otherData.receiverParty == null)) return false;
			if (!Utils.asString(receiverParty).equals(otherData.receiverParty)) return false;

			if ((interfaceName == null && otherData.interfaceName != null) || (interfaceName != null && otherData.interfaceName == null)) return false;
			if (!Utils.asString(interfaceName).equals(otherData.interfaceName)) return false;

			if ((interfaceNamespace == null && otherData.interfaceNamespace != null) || (interfaceNamespace != null && otherData.interfaceNamespace == null)) return false;
			if (!Utils.asString(interfaceNamespace).equals(otherData.interfaceNamespace)) return false;

			if ((scenarioID == null && otherData.scenarioID != null) || (scenarioID != null && otherData.scenarioID == null)) return false;
			if (!Utils.asString(scenarioID).equals(otherData.scenarioID)) return false;

			return true;
		}
		return false;
	}

	public int hashCode()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("senderService=");
		sb.append(Utils.asString(senderService));
		sb.append(" senderParty=");
		sb.append(Utils.asString(senderParty));
		sb.append(" receiverService=");
		sb.append(Utils.asString(receiverService));
		sb.append(" receiverParty=");
		sb.append(Utils.asString(receiverParty));
		sb.append(" interfaceName=");
		sb.append(Utils.asString(interfaceName));
		sb.append(" interfaceNamespace=");
		sb.append(Utils.asString(interfaceNamespace));
		sb.append(" scenarioID=");
		sb.append(Utils.asString(scenarioID));

		return sb.toString().hashCode();
	}

	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(this.getClass().getName() + ":\n");
		sb.append("  senderService=");
		sb.append(Utils.asString(senderService));
		sb.append('\n');
		sb.append("  senderParty=");
		sb.append(Utils.asString(senderParty));
		sb.append('\n');
		sb.append("  receiverService=");
		sb.append(Utils.asString(receiverService));
		sb.append('\n');
		sb.append("  receiverParty=");
		sb.append(Utils.asString(receiverParty));
		sb.append('\n');
		sb.append("  interfaceName=");
		sb.append(Utils.asString(interfaceName));
		sb.append('\n');
		sb.append("  interfaceNamespace=");
		sb.append(Utils.asString(interfaceNamespace));
		sb.append('\n');
		sb.append("  scenarioID=");
		sb.append(Utils.asString(scenarioID));
		sb.append('\n');
		sb.append("  counter=");
		sb.append(counter);
		sb.append('\n');
		sb.append("  status=");
		sb.append(Utils.asString(status));
		sb.append('\n');
		sb.append("  messageIDs=");
		sb.append(Utils.asString(messageIDs));

		return sb.toString();
	}
}
// end class MessageOverviewData