/**
 * Project: LocalDevelopment~pi.alertstuckmsg.ejb~cust.sap.com
 * File: AlertInfo.java
 * Type: AlertInfo
 * Author: Andreas Job
 * Creation Date: Apr 9, 2016
 */
package com.sap.pi.alertstuckmsg.job;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.json.JSONException;
import org.json.JSONObject;

import com.sap.tc.logging.Location;


/**
 * @author Andreas Job
 *
 */
public class AlertInfo
{
	private static final Location TRACE = Location.getLocation(AlertInfo.class);

	public final static String SCENARIO_ID = "ScenarioId" ;
	//public final static String SCENARIO_NAME = "ScenarioName" ;
	public final static String RULE_ID = "RuleId" ;
	public final static String SEVERITY = "Severity";
	public final static String TIMESTAMP = "Timestamp" ;
	public final static String MESSAGE_ID = "MsgId" ;
	public final static String ERROR_TEXT = "ErrText" ;
	public final static String ERROR_CODE = "ErrCode" ;
	public final static String ERROR_CATEGORY = "ErrCat" ;
	public final static String ERROR_LABEL = "ErrLabel";
	public final static String FROM_PARTY = "FromParty" ;
	public final static String FROM_SERVICE = "FromService" ;
	public final static String TO_PARTY = "ToParty" ;
	public final static String TO_SERVICE = "ToService" ;
	public final static String INTERFACE = "Interface" ;
	public final static String NAMESPACE = "Namespace" ;
	public final static String COMPONENT = "Component" ;

	private String timestamp;
	private String senderService;
	private String senderParty;
	private String receiverService;
	private String receiverParty;
	private String interfaceName;
	private String interfaceNamespace;
	private String scenarioID;

	private String messageID;

	private String severity = "MEDIUM";
	private String errorCategory;
	private String errorLabel;
	private String errorCode;
	private String errorText;
	private String alertRuleID;

	public AlertInfo(String senderService, String senderParty, String receiverService, String receiverParty, String interfaceName, String interfaceNamespace, String scenarioID, String messageID, String errorText, String errorCategory, String errorLabel, String errorCode, String alertRuleID)
	{
		this.senderService = senderService;
		this.senderParty = senderParty;
		this.receiverService = receiverService;
		this.receiverParty = receiverParty;
		this.interfaceName = interfaceName;
		this.interfaceNamespace = interfaceNamespace;
		this.scenarioID = scenarioID;
		this.messageID = messageID;
		this.errorText = errorText;

		this.severity = "MEDIUM";
		this.errorCategory = errorCategory;
		this.errorLabel = errorLabel;
		this.errorCode = errorCode;
		this.alertRuleID = (alertRuleID != null) ? alertRuleID : "AlertStuckMessagesJob";

		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		timestamp = dateFormat.format(new Date(System.currentTimeMillis()));
	}

	public String getSenderService()
	{
		return senderService;
	}

	public String getSenderParty()
	{
		return senderParty;
	}

	public String getReceiverService()
	{
		return receiverService;
	}

	public String getReceiverParty()
	{
		return receiverParty;
	}

	public String getInterfaceName()
	{
		return interfaceName;
	}

	public String getInterfaceNamespace()
	{
		return interfaceNamespace;
	}

	public String getScenarioID()
	{
		return scenarioID;
	}

	public String getMessageID()
	{
		return messageID;
	}

	public String getSeverity()
	{
		return severity;
	}

	public String getErrorCategory()
	{
		return errorCategory;
	}

	public String getErrorLabel()
	{
		return errorLabel;
	}

	public String getErrorCode()
	{
		return errorCode;
	}

	public String getErrorText()
	{
		return errorText;
	}

	public String getAlertRuleID()
	{
		return alertRuleID;
	}

	public void setMessageID(String messageID)
	{
		this.messageID = messageID;
	}

	public void setErrorText(String errorText)
	{
		this.errorText = errorText;
	}

	public String renderToJSON() throws JSONException
	{
		final String SIGNATURE = "renderToJSON()";
		TRACE.entering(SIGNATURE);

		JSONObject jsonObject = new JSONObject();

		jsonObject.put(SCENARIO_ID, this.scenarioID);
		//if (scenarioName != null)
			//jsonObject.put(SCENARIO_NAME, this.scenarioName);
		jsonObject.put(RULE_ID, this.alertRuleID);
		jsonObject.put(SEVERITY, this.severity);
		jsonObject.put(TIMESTAMP, this.timestamp);
		jsonObject.put(COMPONENT, "Adapter Engine");
		jsonObject.put(ERROR_LABEL, this.errorLabel);
		if (messageID != null)
			jsonObject.put(MESSAGE_ID, this.messageID);
		jsonObject.put(ERROR_TEXT, this.errorText);
		jsonObject.put(ERROR_CODE, this.errorCode);
		jsonObject.put(ERROR_CATEGORY, this.errorCategory);
		//jsonObject.put(ADAPTER_TYPE, this.adapterType);
		jsonObject.put(FROM_PARTY, this.senderParty);
		jsonObject.put(FROM_SERVICE, this.senderService);
		jsonObject.put(TO_PARTY, this.receiverParty);
		jsonObject.put(TO_SERVICE, this.receiverService);
		jsonObject.put(INTERFACE, this.interfaceName);
		jsonObject.put(NAMESPACE, this.interfaceNamespace);
		//jsonObject.put(MONITORING_URL, monitoringUrl);

		//jsonObject.put(CHANNEL_NAME,this.channel);
		//jsonObject.put(CHANNEL_PARTY,this.channelParty);
		//jsonObject.put(CHANNEL_SERVICE,this.channelService);
		//jsonObject.put(ADAPTER_NAMESPACE,this.adapterNamespace);

		/*if (this.errorParameterNames != null) {
			JSONObject params = new JSONObject();
			for (int i=0;i<this.errorParameterNames.length;i++) {
				params.accumulate(this.errorParameterNames[i], this.errorParameterValues[i]);
			}
			jsonObject.put(ERROR_PARAMETERS, params);
		}

		if (this.udsAttributeNames != null) {
			JSONObject attributes = new JSONObject();
			for (int i=0;i<this.udsAttributeNames.length;i++) {
				attributes.accumulate(this.udsAttributeNames[i], this.udsAttributeValues[i]);
			}
			jsonObject.put(UDS_ATTRIBUTES, attributes);
		}*/

		String jsonString = jsonObject.toString(4);
		TRACE.exiting(SIGNATURE, jsonString);
		return jsonString;
	}

}
// end class AlertInfo