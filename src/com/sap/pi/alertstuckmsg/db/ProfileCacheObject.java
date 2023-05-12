/**
 * Project: LocalDevelopment~pi.alertstuckmsg.ejb~cust.sap.com
 * File: ProfileCacheObject.java
 * Type: ProfileCacheObject
 * Author: Andreas Job
 * Creation Date: Apr 9, 2016
 */
package com.sap.pi.alertstuckmsg.db;

import java.io.Serializable;

import com.sap.pi.alertstuckmsg.util.Hex;

/**
 * @author Andreas Job
 *
 */
public class ProfileCacheObject implements Serializable
{
	private static final long serialVersionUID = -7018707400505254226L;

	private String msgID = null;
	private String direction = null;
	private String status = null;
	//private Timestamp sentRecvTime = null;
	//private Timestamp transDelvTime = null;
	private byte[] hash = null;
	private long counter;

	public ProfileCacheObject(String status, byte[] hash, long counter)
	{
		this.status = status;
		this.hash = hash;
		this.counter = counter;
	}

	public ProfileCacheObject(String msgID, String direction, String status, byte[] hash)
	{
		this.msgID = msgID;
		this.direction = direction;
		this.status = status;
		this.hash = hash;
		counter = 1;
	}

	public String getMsgID()
	{
		return msgID;
	}

	public void setMsgID(String msgID)
	{
		this.msgID = msgID;
	}

	public String getDirection()
	{
		return direction;
	}

	public void setDirection(String direction)
	{
		this.direction = direction;
	}

	public String getStatus()
	{
		return status;
	}

	public void setStatus(String status)
	{
		this.status = status;
	}

	/*public Timestamp getSentRecvTime()
	{
		return sentRecvTime;
	}

	public void setSentRecvTime(Timestamp sentRecvTime)
	{
		this.sentRecvTime = sentRecvTime;
	}

	public Timestamp getTransDelvTime()
	{
		return transDelvTime;
	}

	public void setTransDelvTime(Timestamp transDelvTime)
	{
		this.transDelvTime = transDelvTime;
	}*/

	public byte[] getHash()
	{
		return hash;
	}

	public String getHashInHex()
	{
		return Hex.encode(hash);
	}

	public void setHash(byte[] hash)
	{
		this.hash = hash;
	}

	public void setCounter(long counter)
	{
		this.counter = counter;
	}

	public long getCounter()
	{
		return counter;
	}

	public String toString()
	{
		return this.getClass().getName() + ": hash=" + Hex.encode(hash) + "; msgID=" + msgID + "; direction=" + direction + "; status=" + status + "; counter=" + counter;
	}
}
// end class ProfileCacheObject