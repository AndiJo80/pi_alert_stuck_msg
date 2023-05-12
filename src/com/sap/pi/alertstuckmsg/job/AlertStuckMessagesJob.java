/**
 * Project: LocalDevelopment~pi.alertstuckmsg.ejb~cust.sap.com
 * File: AlertStuckMessagesJob.java
 * Type: AlertStuckMessagesJob
 * Author: Andreas Job
 * Creation Date: Apr 9, 2016
 */
package com.sap.pi.alertstuckmsg.job;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RunAs;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.naming.InitialContext;

import com.sap.scheduler.api.Scheduler;
import com.sap.scheduler.api.SchedulerTask;
import com.sap.scheduler.runtime.JobContext;
import com.sap.scheduler.runtime.JobParameter;
import com.sap.scheduler.runtime.mdb.MDBJobImplementation;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;

/**
 * Message-Driven Bean implementation class for: AlertStuckMessagesJob
 *
 */
@MessageDriven(
	activationConfig = {@ActivationConfigProperty(propertyName = "messageSelector", propertyValue = "JobDefinition=\'AlertStuckMessagesJob\' AND ApplicationName=\'cust.sap.com/pi.alertstuckmsg\'"),
						@ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue") },
	mappedName = "JobQueue")
@DeclareRoles("admin")
@RunAs(value="admin") // these does not work, so ejb-jar.xml is introduced too!
public class AlertStuckMessagesJob extends MDBJobImplementation
{
	private static final long serialVersionUID = -8534850441139234002L;
	private static final Location TRACE = Location.getLocation(AlertStuckMessagesJob.class);

	@Override
	public void onJob(JobContext jobContext) throws Exception
	{
		final String SIGNATURE = "onJob(JobContext)";
		TRACE.entering(SIGNATURE, new Object[] { jobContext });
		long jobStartTime = System.currentTimeMillis();
		java.util.logging.Logger jobLog = null;
		try
		{
			jobLog = jobContext.getLogger();

			Properties jobParameter = readJobParameter(jobContext);

			TRACE.debugT(SIGNATURE, "Begin of job execution at " + new Timestamp(jobStartTime).toString());
			jobLog.entering(this.getClass().getName(), SIGNATURE, new Object[] { new Date(jobStartTime), jobParameter});

			/* The alerts will later contain a "RuleID" which is filled with the task name of this job */
			Scheduler scheduler = lookupScheduler();
			SchedulerTask jobTask = (scheduler != null) ? scheduler.getTask(jobContext.getJob().getSchedulerTaskId()) : null;
			String jobTaskName = (jobTask != null) ? jobTask.getName() : null;
			jobTaskName = (jobTaskName != null) ? jobTaskName : "AlertStuckMessagesJob";
			/* retrieve some more of the job parameters */
			boolean retrieveMessagekeys = "true".equalsIgnoreCase(jobParameter.getProperty(JobParam.AlertWithMessageID.toString()));
			long stuckAfterMinutes = Long.valueOf(jobParameter.getProperty(JobParam.StuckAfterMin.toString()));
			long ageLimit = Long.valueOf(jobParameter.getProperty(JobParam.MessageAgeLimitMin.toString()));
			long maxAlertsPerScenario = Long.valueOf(jobParameter.getProperty(JobParam.MaxAlertsPerScenario.toString()));

			Filter scenariosFilter = new Filter(jobParameter.getProperty(JobParam.SenderComponent.toString()),
											   jobParameter.getProperty(JobParam.SenderParty.toString()),
											   jobParameter.getProperty(JobParam.ReceiverComponent.toString()),
											   jobParameter.getProperty(JobParam.ReceiverParty.toString()),
											   jobParameter.getProperty(JobParam.Interface.toString()),
											   jobParameter.getProperty(JobParam.InterfaceNamespace.toString()));

			/* retrieve info about stuck messages. data is based on the Message Overview data */
			LocalMsgOverviewDataProvider localMsgOvProvider = new LocalMsgOverviewDataProvider();
			List<MessageOverviewData> stuckMessages = localMsgOvProvider.getStuckMessages(new Timestamp(jobStartTime - ageLimit*60L*1000L),
																						  new Timestamp(jobStartTime - stuckAfterMinutes*60L*1000L),
																						  retrieveMessagekeys,
																						  scenariosFilter,
																						  ((maxAlertsPerScenario > 0) ? maxAlertsPerScenario + 1 : 0)); // 0 for unlimited
			jobLog.info("Number of scenarios with stuck messages: " + ((stuckMessages != null) ? stuckMessages.size() : "<null>"));
			if (stuckMessages != null && !stuckMessages.isEmpty() && !jobContext.getJob().getCancelRequest())
			{
				String consumerName = jobParameter.getProperty(JobParam.AlertConsumer.toString());
				if (consumerName == null || consumerName.trim().isEmpty())
					consumerName = "ALERT-TO-MAIL";
				TRACE.debugT(SIGNATURE, "Consumer name: " + consumerName);

				JMSAlertSender jmsSender = new JMSAlertSender(); // send alerts to target consumer via a JMS connection
				long alertsSent = 0;
				try
				{
					jmsSender.openConnection(consumerName);
					for (MessageOverviewData messageOverviewData : stuckMessages)
					{
						long alertsForScenario = 0;
						AlertInfo alert = new AlertInfo(messageOverviewData.getSenderService(),
														messageOverviewData.getSenderParty(),
														messageOverviewData.getReceiverService(),
														messageOverviewData.getReceiverParty(),
														messageOverviewData.getInterfaceName(),
														messageOverviewData.getInterfaceNamespace(),
														messageOverviewData.getScenarioID(),
														null,
														messageOverviewData.getCounter() + " message(s) stuck in status " + messageOverviewData.getStatus() + " for more than " + stuckAfterMinutes + " minutes",
														"MESSAGING_SYSTEM", //errorCategory
														"1001", //errorLabel
														"MESSAGE_STUCK", //errorCode
														jobTaskName); // alert rule ID

						Set<String> messageIDs = messageOverviewData.getMessageIDs();
						if (retrieveMessagekeys && messageIDs != null && !messageIDs.isEmpty())
						{
							TRACE.debugT(SIGNATURE, "Sending Alerts for " + messageIDs.size() + " message IDs...");
							for (String messageID : messageIDs) // send many alerts - one for each message ID
							{
								alert.setMessageID(messageID);
								alert.setErrorText("Message " + messageID + " stuck for more than " + stuckAfterMinutes + " minutes");

								jmsSender.send(consumerName, alert);
								alertsSent++;
								alertsForScenario++;
								if ((maxAlertsPerScenario > 0) && (alertsForScenario >= maxAlertsPerScenario))
								{
									TRACE.debugT(SIGNATURE, "Limit for number of alerts reached. Breaking loop. Number: " + alertsForScenario);
									break;
								}

								if (jobContext.getJob().getCancelRequest())
								{
									TRACE.debugT(SIGNATURE, "Cancel request was triggered for this job. Breaking loop.");
									break;
								}
							}
						}
						else
						{
							TRACE.debugT(SIGNATURE, "Sending Alert without message IDs...");
							jmsSender.send(consumerName, alert);
							alertsSent++;
							alertsForScenario++;
						}

						if (jobContext.getJob().getCancelRequest())
						{
							TRACE.debugT(SIGNATURE, "Cancel request was triggered for this job. Breaking loop.");
							break;
						}
					}
				}
				finally
				{
					jmsSender.closeConnection();
					jobLog.info("Number of alerts sent to JMS queue: " + alertsSent + "; JMS queue name: " + jmsSender.getQueueName());
				}
			}

			long jobEndTime = System.currentTimeMillis();
			TRACE.debugT(SIGNATURE, "End of job execution at " + new Timestamp(jobEndTime).toString() + ". Job duration: " + (jobEndTime - jobStartTime) + "ms" );
			jobContext.setReturnCode((short) 0);
			jobLog.info("Successfully completed task at " + new Date(System.currentTimeMillis()));
			jobLog.exiting(this.getClass().getName(), "onJob");
		}
		catch (Throwable e)
		{
			TRACE.traceThrowableT(Severity.ERROR, SIGNATURE, "An exception occurred.", e);
			jobContext.setReturnCode((short)-1);
			jobContext.jobFailed();
			if (jobLog != null)
				jobLog.severe(e.toString());
		}
		finally
		{
			TRACE.exiting(SIGNATURE);
		}
	}

	private static Properties readJobParameter(JobContext jobContext)
	{
		final String SIGNATURE = "readJobParameter(JobContext)";
		TRACE.entering(SIGNATURE);

		Properties result = new Properties();

		JobParameter parameter = jobContext.getJobParameter(JobParam.SenderComponent.toString());
		String parameterValueString = (parameter != null && parameter.getStringValue() != null) ? parameter.getStringValue().trim() : "";
		result.setProperty(JobParam.SenderComponent.toString(), parameterValueString);

		parameter = jobContext.getJobParameter(JobParam.SenderParty.toString());
		parameterValueString = (parameter != null && parameter.getStringValue() != null) ? parameter.getStringValue().trim() : "";
		result.setProperty(JobParam.SenderParty.toString(), parameterValueString);

		parameter = jobContext.getJobParameter(JobParam.ReceiverComponent.toString());
		parameterValueString = (parameter != null && parameter.getStringValue() != null) ? parameter.getStringValue().trim() : "";
		result.setProperty(JobParam.ReceiverComponent.toString(), parameterValueString);

		parameter = jobContext.getJobParameter(JobParam.ReceiverParty.toString());
		parameterValueString = (parameter != null && parameter.getStringValue() != null) ? parameter.getStringValue().trim() : "";
		result.setProperty(JobParam.ReceiverParty.toString(), parameterValueString);

		parameter = jobContext.getJobParameter(JobParam.Interface.toString());
		parameterValueString = (parameter != null && parameter.getStringValue() != null) ? parameter.getStringValue().trim() : "";
		result.setProperty(JobParam.Interface.toString(), parameterValueString);

		parameter = jobContext.getJobParameter(JobParam.InterfaceNamespace.toString());
		parameterValueString = (parameter != null && parameter.getStringValue() != null) ? parameter.getStringValue().trim() : "";
		result.setProperty(JobParam.InterfaceNamespace.toString(), parameterValueString);

		parameter = jobContext.getJobParameter(JobParam.AlertConsumer.toString());
		parameterValueString = (parameter != null && parameter.getStringValue() != null) ? parameter.getStringValue().trim() : null;
		if (parameterValueString != null)
			result.setProperty(JobParam.AlertConsumer.toString(), parameterValueString);

		parameter = jobContext.getJobParameter(JobParam.StuckAfterMin.toString());
		Integer parameterValueInt = (parameter != null) ? parameter.getIntegerValue() : null;
		if ((parameterValueInt == null)||(parameterValueInt < 5))
			parameterValueInt = 5;
		int paramStuckAfterMin = parameterValueInt;
		result.setProperty(JobParam.StuckAfterMin.toString(), parameterValueInt.toString());

		parameter = jobContext.getJobParameter(JobParam.MessageAgeLimitMin.toString());
		parameterValueInt = (parameter != null) ? parameter.getIntegerValue() : null;
		if ((parameterValueInt == null)||(parameterValueInt <= paramStuckAfterMin))
			parameterValueInt = paramStuckAfterMin + 1;
		result.setProperty(JobParam.MessageAgeLimitMin.toString(), parameterValueInt.toString());

		parameter = jobContext.getJobParameter(JobParam.AlertWithMessageID.toString());
		Boolean parameterValueBool = (parameter != null && parameter.getBooleanValue() != null) ? parameter.getBooleanValue() : Boolean.FALSE;
		result.setProperty(JobParam.AlertWithMessageID.toString(), parameterValueBool.toString());

		parameter = jobContext.getJobParameter(JobParam.MaxAlertsPerScenario.toString());
		parameterValueInt = (parameter != null) ? parameter.getIntegerValue() : null;
		if (parameterValueInt == null)
			parameterValueInt = 0;
		result.setProperty(JobParam.MaxAlertsPerScenario.toString(), parameterValueInt.toString());

		TRACE.exiting(SIGNATURE);
		return result;
	}

	private static Scheduler lookupScheduler()
	{
		final String SIGNATURE = "lookupScheduler()";
		TRACE.entering(SIGNATURE, new Object[] {});

		Scheduler scheduler = null;
		try
		{
			InitialContext ctx = new InitialContext();
			scheduler = (Scheduler) ctx.lookup("scheduler");
		}
		catch (Exception e)
		{
			TRACE.traceThrowableT(Severity.WARNING, SIGNATURE, "Can't find the scheduler service. Check the service was successfully started and the JNDI entry '/scheduler' exists.", e);
		}
		TRACE.exiting(SIGNATURE);
		return scheduler;
	}
}
