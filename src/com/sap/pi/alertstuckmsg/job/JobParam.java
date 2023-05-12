/**
 * Project: com.sap.pi.alertstuckmsg.ejb
 * File: JobParam.java
 * Type: JobParam
 * Author: Andreas Job
 * Creation Date: Apr 9, 2016
 */
package com.sap.pi.alertstuckmsg.job;

/**
 * @author Andreas Job
 *
 */
public enum JobParam
{
	SenderComponent,
	SenderParty,
	ReceiverComponent,
	ReceiverParty,
	Interface,
	InterfaceNamespace,
	AlertConsumer,
	StuckAfterMin("StuckAfter_min"),	// hanging after x minutes
	MessageAgeLimitMin("MessageAgeLimit_min"),	// age limit in minutes
	AlertWithMessageID,
	MaxAlertsPerScenario;

	private String value;

	JobParam()
	{
		value = this.name();
	}

	JobParam(String val)
	{
		value = val;
	}

	public String toString()
	{
		return value;
	}
}
// end class JobParam