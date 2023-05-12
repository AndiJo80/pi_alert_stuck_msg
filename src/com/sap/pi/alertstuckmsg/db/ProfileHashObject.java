/**
 * Project: LocalDevelopment~pi.alertstuckmsg.ejb~cust.sap.com
 * File: ProfileHashObject.java
 * Type: ProfileHashObject
 * Author: Andreas Job
 * Creation Date: Apr 9, 2016
 */
package com.sap.pi.alertstuckmsg.db;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.sap.pi.alertstuckmsg.util.Hex;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;

/**
 * @author Andreas Job
 *
 */
public class ProfileHashObject implements Serializable
{
	private static final long serialVersionUID = 1550668378377025250L;
	private static final Location TRACE = Location.getLocation(ProfileHashObject.class);

	public enum ProfileAttributeName
	{
		FROM_SERVICE_NAME,
		FROM_PARTY_NAME,
		TO_SERVICE_NAME,
		TO_PARTY_NAME,
		ACTION_NAME,
		ACTION_TYPE,
		SCENARIO_IDENTIFIER,
		DIRECTION,
		DELIVERY_SEMANTICS,
		SERVER_NODE,
		INBOUND_CHANNEL,
		OUTBOUND_CHANNEL
	}

	private class ProfileHashObjectParser extends DefaultHandler
	{
		private String currentAttributeName = null;
		private String currentAttributeValue = null;
		private boolean inAttributeElement = false;
		private boolean inNameElement = false;
		private boolean inValueElement = false;

		private Properties data = null;

		public ProfileHashObjectParser()
		{
			data = new Properties();
		}

		public Properties getParsedData()
		{
			return data;
		}

		public void startDocument () throws SAXException
	    {
			data = new Properties();
			currentAttributeName = null;
			currentAttributeValue = null;
			inAttributeElement = false;
			inNameElement = false;
			inValueElement = false;
	    }

	    public void endDocument() throws SAXException
	    {
	    	currentAttributeName = null;
			currentAttributeValue = null;
			inAttributeElement = false;
			inNameElement = false;
			inValueElement = false;
	    }

		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
		{
			if ("Attribute".equals(localName))
			{
				if (inAttributeElement)
					throw new SAXException("Invalid XML Structure: Attribute element inside Attribute element");
				inAttributeElement = true;

				inNameElement = false;
				inValueElement = false;
				currentAttributeName = null;
				currentAttributeValue = null;
			}
			else if ("Name".equals(localName))
			{
				if (!inAttributeElement)
					throw new SAXException("Invalid XML Structure: Name not inside an Attribute element");
				inNameElement = true;
				currentAttributeName = null;
			}
			else if ("AttributeValue".equals(localName))
			{
				if (!inAttributeElement)
					throw new SAXException("Invalid XML Structure: Name not inside an Attribute element");
				inValueElement = true;
				currentAttributeValue = null;
			}
		}

		public void endElement(String uri, String localName, String qName) throws SAXException
		{
			if ("Attribute".equals(localName))
			{
				if (!inAttributeElement)
					throw new SAXException("Invalid XML Structure: Not inside Attribute element inside Attribute element");
				inAttributeElement = false;
				inNameElement = false;
				inValueElement = false;

				if (currentAttributeName != null)
				{
					currentAttributeName = currentAttributeName.trim();
					if (currentAttributeValue != null)
						currentAttributeValue = currentAttributeValue.trim();
					else
						currentAttributeValue = "";

					data.put(currentAttributeName, currentAttributeValue);
				}
				currentAttributeName = null;
				currentAttributeValue = null;
			}
			else if ("Name".equals(localName))
			{
				if (!inNameElement)
					throw new SAXException("Invalid XML Structure: Name not inside a Name element");
				inNameElement = false;
			}
			else if ("AttributeValue".equals(localName))
			{
				if (!inValueElement)
					throw new SAXException("Invalid XML Structure: Name not inside a AttributeValue element");
				inValueElement = false;
			}
		}

		public void characters(char ch[], int start, int length) throws SAXException
		{
			String value = new String(ch, start, length).trim();

	        if (value.length() == 0)
	            return; // ignore white space

	        if (inNameElement)
	        	currentAttributeName = value;
	        else if (inValueElement)
	        	currentAttributeValue = value;
		}
	};

	private byte[] hash = null;
	private byte[] metadata = null;

	private Properties data = null;

	public ProfileHashObject(byte[] hash, byte[] metadata)
	{
		this.hash = hash;
		this.metadata = metadata;
	}

	public byte[] getProfileHash()
	{
		return hash;
	}

	public String getProfileHashInHex()
	{
		return Hex.encode(hash);
	}

	public byte[] getMetadata()
	{
		return metadata;
	}

	public void setMetadata(byte[] metadata)
	{
		this.metadata = metadata;
	}

	private Properties parseMetadata() throws Exception
	{
		final String SIGNATURE = "parseMetadata()";
		TRACE.entering(SIGNATURE);

		Properties parsedData = null;
		if (metadata != null)
		{
			try
			{
				SAXParserFactory factory = SAXParserFactory.newInstance();
				factory.setNamespaceAware(true);
				SAXParser saxParser = factory.newSAXParser();
				ProfileHashObjectParser parser = new ProfileHashObjectParser();
				ByteArrayInputStream bais = new ByteArrayInputStream(metadata);
				saxParser.parse(bais, parser);

				parsedData = parser.getParsedData();
			}
			catch (ParserConfigurationException e)
			{
				TRACE.traceThrowableT(Severity.ERROR, SIGNATURE, "Error parsing metadata.", e);
				throw new Exception("Error parsing metadata for ProfileHashObject.", e);
			}
			catch (SAXException e)
			{
				TRACE.traceThrowableT(Severity.ERROR, SIGNATURE, "Error parsing metadata.", e);
				throw new Exception("Error parsing metadata for ProfileHashObject.", e);
			}
			catch (IOException e)
			{
				TRACE.traceThrowableT(Severity.ERROR, SIGNATURE, "Error parsing metadata.", e);
				throw new Exception("Error parsing metadata for ProfileHashObject.", e);
			}
			catch (Exception e)
			{
				TRACE.traceThrowableT(Severity.ERROR, SIGNATURE, "Error parsing metadata.", e);
				throw new Exception("Error parsing metadata for ProfileHashObject.", e);
			}
		}

		TRACE.exiting(SIGNATURE, parsedData);
		return parsedData;
	}

	public Properties getParsedMetadata()
	{
		final String SIGNATURE = "getParsedMetadata()";
		TRACE.entering(SIGNATURE);

		if (data == null && metadata != null)
			try
			{
				data = this.parseMetadata();
			}
			catch (Exception e)
			{
				TRACE.traceThrowableT(Severity.ERROR, SIGNATURE, "Error parsing metadata.", e);
			}

		TRACE.exiting(SIGNATURE, data);
		return data;
	}

	public String getAttributeValue(ProfileAttributeName attrib)
	{
		if (attrib == null)
			return null;
		Properties data = this.getParsedMetadata();
		String attributeValue = data.getProperty(attrib.toString());
		return attributeValue;
	}
	public void freeMemory()
	{
		data = null;
	}

	public int hashCode()
	{
		return this.toString().hashCode();
	}

	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(this.getClass().getName());
		sb.append(": hash=");
		sb.append((hash != null) ? Hex.encode(hash) : "<null>");
		sb.append("; metadata=");
		try
		{
			sb.append((metadata != null) ? new String(metadata, "UTF-8") : "<null>");
		}
		catch (UnsupportedEncodingException e)
		{
			sb.append("<unsupported encoding detected>");
		}
		return sb.toString();
	}

	public boolean equals(ProfileHashObject other)
	{
		if (other == null)
			return false;

		return Arrays.equals(hash, other.hash);
	}
}
// end class ProfileHashObject