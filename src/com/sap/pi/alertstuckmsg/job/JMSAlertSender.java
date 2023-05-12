/**
 * Project: LocalDevelopment~pi.alertstuckmsg.ejb~cust.sap.com
 * File: JMSAlertSender.java
 * Type: JMSAlertSender
 * Author: Andreas Job
 * Creation Date: Apr 9, 2016
 */
package com.sap.pi.alertstuckmsg.job;

import java.util.StringTokenizer;

import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;

/**
 * @author Andreas Job
 *
 */
public class JMSAlertSender
{
	private static final Location TRACE = Location.getLocation(JMSAlertSender.class);

	private static final String VIRTUAL_PROVIDER_PATH = "jmsqueues/alertingVP/";
	private static final String ALERT_CONSUMER_QUEUES_RELATIVE_PATH = "jms/queue/xi/monitoring/alert/";

	InitialContext context;
	QueueConnectionFactory factory;
	QueueConnection connection;
	QueueSession session;
	Queue queue;
	QueueSender sender;
	String consumer = null;

	public void openConnection(String consumer) throws Exception
	{
		final String SIGNATURE = "openConnection(String)";
		TRACE.entering(SIGNATURE, new Object[] { consumer });
		this.consumer = consumer;

		try
		{
			context = new InitialContext();

			factory = (QueueConnectionFactory) context.lookup("jmsfactory/alertingVP/AlertingConsumerConnectionFactory");

			connection = factory.createQueueConnection();
			session = connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);

			try
			{
				queue = (Queue) context.lookup(VIRTUAL_PROVIDER_PATH + ALERT_CONSUMER_QUEUES_RELATIVE_PATH + consumer);

			}
			catch (javax.naming.NamingException e)
			{
				StringTokenizer tokenizer = new StringTokenizer(VIRTUAL_PROVIDER_PATH + ALERT_CONSUMER_QUEUES_RELATIVE_PATH, "/");
				Context subcontext = context;
				String nextToken = null;
				while (tokenizer.hasMoreTokens())
				{
					nextToken = tokenizer.nextToken();
					if (!nextToken.trim().equals(""))
					{
						try
						{
							subcontext = subcontext.createSubcontext(nextToken);
						}
						catch (NamingException exc)
						{
							subcontext = (Context) subcontext.lookup(nextToken);
						}
					}
				}
				queue = session.createQueue(ALERT_CONSUMER_QUEUES_RELATIVE_PATH + consumer); // Alerting.VirtualProviderAdmin (or Administrator) role is required
				context.bind(VIRTUAL_PROVIDER_PATH + ALERT_CONSUMER_QUEUES_RELATIVE_PATH + consumer, queue);
			}

			sender = session.createSender(queue); // Alerting.AlertProducer (or Administrator) role is required

			connection.start();
		}
		catch (Exception e)
		{
			TRACE.traceThrowableT(Severity.ERROR, SIGNATURE, consumer, e);
			throw e;
		}
		TRACE.exiting(SIGNATURE);
	}

	public void closeConnection()
	{
		final String SIGNATURE = "closeConnection()";
		TRACE.entering(SIGNATURE);
		try
		{
			if (sender != null)
				sender.close();
		}
		catch (Exception ex)
		{
			TRACE.catching(SIGNATURE, ex);
		}
		try
		{
			if (session != null)
				session.close();
		}
		catch (Exception ex)
		{
			TRACE.catching(SIGNATURE, ex);
		}
		try
		{
			if (connection != null)
				connection.close();
		}
		catch (Exception ex)
		{
			TRACE.catching(SIGNATURE, ex);
		}
		try
		{
			if (context != null)
				context.close();
		}
		catch (Exception ex)
		{
			TRACE.catching(SIGNATURE, ex);
		}
		TRACE.exiting(SIGNATURE);
	}

	public String getQueueName()
	{
		return ALERT_CONSUMER_QUEUES_RELATIVE_PATH + consumer;
	}

	public void send(String consumer, AlertInfo alert) throws JMSException
	{
		final String SIGNATURE = "send(String, AlertInfo)";
		TRACE.entering(SIGNATURE);
		try
		{
			TextMessage msg = session.createTextMessage();
			msg.setText(alert.renderToJSON());
			if (alert.getSeverity() != null)
				msg.setStringProperty(AlertInfo.SEVERITY, alert.getSeverity());
			if (alert.getAlertRuleID() != null)
				msg.setStringProperty(AlertInfo.RULE_ID, alert.getAlertRuleID());
			if (alert.getScenarioID() != null)
				msg.setStringProperty(AlertInfo.SCENARIO_ID, alert.getScenarioID());
			if (alert.getErrorLabel() != null)
				try { msg.setIntProperty(AlertInfo.ERROR_LABEL, Integer.valueOf(alert.getErrorLabel())); } catch (Exception e) { TRACE.debugT(SIGNATURE, "Not an integer for error label"); }
			TRACE.debugT(SIGNATURE, "Sending JMS message...");
			sender.send(msg);
		}
		catch (JMSException e)
		{
			TRACE.traceThrowableT(Severity.ERROR, SIGNATURE, consumer, e);
			throw e;
		}
		TRACE.exiting(SIGNATURE);
	}
}
// end class JMSAlertSender