/**
 * Project: LocalDevelopment~pi.alertstuckmsg.ejb~cust.sap.com
 * File: Utils.java
 * Type: Utils
 * Author: Andreas Job
 * Creation Date: Apr 9, 2016
 */
package com.sap.pi.alertstuckmsg.util;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.naming.Context;
import javax.naming.InitialContext;

import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;

/**
 * @author d047487
 *
 */
public class Utils
{
	private static final Location TRACE = Location.getLocation(Utils.class);

	/*public static String getSystemHostName()
	{
		Properties sysProperties =  System.getProperties();
		String dbhost = sysProperties.getProperty("j2ee.dbhost"); //for e.g.inld50047204a

		if (dbhost == null)
			dbhost = "localhost";

		return dbhost;
	}*/

	public static String getSystemHostName()
	{
		final String SIGNATURE = "getSystemHostName()";
		TRACE.entering(SIGNATURE);

		String instanceHost = null;
		try
		{
			Context ctx = new InitialContext();
			MBeanServer server = (MBeanServer) ctx.lookup("jmx");
			Set<ObjectName> result = server.queryNames(new ObjectName(":cimclass=SAP_ITSAMJ2eeNode,SAP_J2EEClusterNode=\"\",*"), null);
			Iterator<ObjectName> iterator = result.iterator();
			ObjectName cluster = null;
			if (iterator.hasNext())
			{
				cluster = (ObjectName) iterator.next();
			}
			if (cluster != null)
			{
				String instanceNameFull = cluster.getKeyProperty("SAP_ITSAMJ2eeInstance.Name");
				if (instanceNameFull != null)
					instanceHost = instanceNameFull.substring(instanceNameFull.lastIndexOf(".") + 1);
			}
		}
		catch (Exception e)
		{
			TRACE.traceThrowableT(Severity.ERROR, e.getMessage(), e);
		}

		TRACE.exiting(SIGNATURE, instanceHost);
		return instanceHost;
	}

	public static String getSystemHttpPort()
	{
		final String SIGNATURE = "getSystemHttpPort()";
		TRACE.entering(SIGNATURE);

		String instancePort = null;
		try
		{
			Context ctx = new InitialContext();
			MBeanServer server = (MBeanServer) ctx.lookup("jmx");
			Set<ObjectName> result = server.queryNames(new ObjectName(":cimclass=SAP_ITSAMJ2eeNode,SAP_J2EEClusterNode=\"\",*"), null);
			Iterator<ObjectName> iterator = result.iterator();
			ObjectName cluster = null;
			if (iterator.hasNext())
			{
				cluster = (ObjectName) iterator.next();
			}
			if (cluster != null)
			{
				ObjectName[] instanceNodeGroup = (ObjectName[]) server.getAttribute(cluster, "SAP_ITSAMJ2eeInstanceJ2eeNodeGroupComponent");
				if (instanceNodeGroup != null && instanceNodeGroup.length > 0)
				{
					ObjectName[] instanceNodeGroupPort = (ObjectName[]) server.getAttribute(instanceNodeGroup[0], "SAP_ITSAMJ2eeInstanceHostedServicePortPartComponent");
					if (instanceNodeGroupPort != null && instanceNodeGroupPort.length > 0)
					{
						for (ObjectName nodePort : instanceNodeGroupPort)
						{
							Hashtable<String, String> keyProperties = nodePort.getKeyPropertyList();
							String portName = null;
							String portNumber = null;
							for (String key : keyProperties.keySet())
							{
								if ("SAP_ITSAMJ2eeIPServicePort.Name".equals(key))
									portName = (String) keyProperties.get(key);
								if ("SAP_ITSAMJ2eeIPServicePort.PortNumber".equals(key))
									portNumber = (String) keyProperties.get(key);
							}
							if ("http".equals(portName))
							{
								instancePort = portNumber;
								break;
							}
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			TRACE.traceThrowableT(Severity.ERROR, e.getMessage(), e);
		}

		TRACE.exiting(SIGNATURE, instancePort);
		return instancePort;
	}

	public static String getSystemUrl()
	{
		final String SIGNATURE = "getSystemUrl()";
		TRACE.entering(SIGNATURE);

		String instanceHost = null;
		String instancePort = null;
		try
		{
			Context ctx = new InitialContext();
			MBeanServer server = (MBeanServer) ctx.lookup("jmx");
			Set<ObjectName> result = server.queryNames(new ObjectName(":cimclass=SAP_ITSAMJ2eeNode,SAP_J2EEClusterNode=\"\",*"), null);
			Iterator<ObjectName> iterator = result.iterator();
			ObjectName cluster = null;
			if (iterator.hasNext())
			{
				cluster = (ObjectName) iterator.next();
			}
			if (cluster != null)
			{
				String instanceNameFull = cluster.getKeyProperty("SAP_ITSAMJ2eeInstance.Name");
				if (instanceNameFull != null)
				{
					instanceHost = instanceNameFull.substring(instanceNameFull.lastIndexOf(".") + 1);

					ObjectName[] instanceNodeGroup = (ObjectName[]) server.getAttribute(cluster, "SAP_ITSAMJ2eeInstanceJ2eeNodeGroupComponent");
					if (instanceNodeGroup != null && instanceNodeGroup.length > 0)
					{
						ObjectName[] instanceNodeGroupPort = (ObjectName[]) server.getAttribute(instanceNodeGroup[0], "SAP_ITSAMJ2eeInstanceHostedServicePortPartComponent");
						if (instanceNodeGroupPort != null && instanceNodeGroupPort.length > 0)
						{
							for (ObjectName nodePort : instanceNodeGroupPort)
							{
								Hashtable<String, String> keyProperties = nodePort.getKeyPropertyList();
								String portName = null;
								String portNumber = null;
								for (String key : keyProperties.keySet())
								{
									if ("SAP_ITSAMJ2eeIPServicePort.Name".equals(key))
										portName = (String) keyProperties.get(key);
									if ("SAP_ITSAMJ2eeIPServicePort.PortNumber".equals(key))
										portNumber = (String) keyProperties.get(key);
								}
								if ("http".equals(portName))
								{
									instancePort = portNumber;
									break;
								}
							}
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			TRACE.traceThrowableT(Severity.ERROR, e.getMessage(), e);
		}

		String url = "http://" + instanceHost + ":" + instancePort;
		TRACE.exiting(SIGNATURE, url);
		return url;
	}

	/**
     * Returns a string representation of an object. If the given object is null,
     * the empty string is returned.
     */
	public static String asString(Object obj)
	{
		return (obj == null ? "" : obj.toString());
	}

	/**
	 * Returns a string representation of an object. If the given object is null, is either represented by "null" if
	 * <code>spellNull == true</code>, or by the empty string "" otherwise.
	 */
	public static String asString(Object obj, boolean spellNull)
	{
		return (obj == null ? (spellNull ? "null" : "") : obj.toString());
	}

	public static String asTrimmedString(Object obj)
	{
		return asString(obj).trim();
	}

	public static String asTrimmedString(Object obj, boolean spellNull)
	{
		return asString(obj, spellNull).trim();
	}
}
// end class Utils