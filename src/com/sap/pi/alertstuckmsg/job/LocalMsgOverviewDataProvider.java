/**
 * Project: LocalDevelopment~pi.alertstuckmsg.ejb~cust.sap.com
 * File: LocalMsgOverviewDataProvider.java
 * Type: LocalMsgOverviewDataProvider
 * Author: Andreas Job
 * Creation Date: Apr 9, 2016
 */
package com.sap.pi.alertstuckmsg.job;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import com.sap.pi.alertstuckmsg.db.ProfileCacheObject;
import com.sap.pi.alertstuckmsg.db.ProfileHashObject;
import com.sap.pi.alertstuckmsg.util.Hex;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;

/**
 * @author Andreas Job
 *
 */
public class LocalMsgOverviewDataProvider
{

	private static final Location TRACE = Location.getLocation(LocalMsgOverviewDataProvider.class);


	public LocalMsgOverviewDataProvider()
	{

	}

	private Connection getConnection() throws Exception
	{
		Context ctx = new InitialContext();
		DataSource dataSource = (DataSource) ctx.lookup("jdbc/SAP/BC_XI_AF");
		Connection conn = dataSource.getConnection();

		return conn;
	}

	/**
	 * Retrieve blocked messages from Message Overview data (tables
	 * @param selectionBeginTime  The earliest sent time of the messages to consider. Message selection starts with this timestamp. Older messages are not considered anymore.
	 * @param latestStatusUpdateTime Last status change of the message. Message must be stuck/hanging for longer than that time.
	 * @param retrieveMessageKeys Retrieve individual message ids for each stuck message, if this is set to true. Otherwise we will only return a counter for each message scenario.
	 * @param scenariosFilter
	 * @param maxMessageKeysPerScenario Max number of message IDs for each scenario. Numbers smaller than 1 means unlimited results. Only valid if parameter retrieveMessageKeys is true.
	 * @return List of stuck/hanging messages.
	 */
	public List<MessageOverviewData> getStuckMessages(Timestamp selectionBeginTime, Timestamp latestStatusUpdateTime, boolean retrieveMessageKeys, Filter scenariosFilter, long maxMessageKeysPerScenario)
    {
    	final String SIGNATURE = "getStuckMessages(Timestamp, Timestamp, Boolean)";
		TRACE.entering(SIGNATURE);

		Connection conn = null;
		PreparedStatement stm = null;
		ResultSet resultSet = null;
		ArrayList<ProfileCacheObject> profileCacheObjects = null; // data from table XI_AF_PROF_HASH
		HashMap<String, ProfileHashObject> profileHashObjects = null; // data from table XI_AF_PROF_HASH
		try
		{
			conn = this.getConnection();

			/* -------------------- select the XI_AF_PROF_CACHE table -------------------- */
			String query;
			if (retrieveMessageKeys)
				query = "SELECT MSG_ID, DIRECTION, STATUS, HASH " +
						"FROM XI_AF_PROF_CACHE " +
						"WHERE (SENT_RECV_TIME >= ?) AND (TRANS_DELV_TIME < ?) ";
			else
				query = "SELECT STATUS, HASH, COUNT(*) AS CNT FROM XI_AF_PROF_CACHE " +
   						"WHERE (SENT_RECV_TIME >= ?) AND (TRANS_DELV_TIME < ?) " +
   						"GROUP BY STATUS, HASH";
			stm = conn.prepareStatement(query);

			stm.setTimestamp(1, selectionBeginTime);
			stm.setTimestamp(2, latestStatusUpdateTime);

			resultSet = stm.executeQuery(); // execute SQL query
			profileCacheObjects = new ArrayList<ProfileCacheObject>();
			HashMap<String, Long> msgIDsPerScenario = new HashMap<String, Long>();
			while (resultSet.next())
			{
				ProfileCacheObject profileCacheObject;
				if (retrieveMessageKeys)
				{
					profileCacheObject = new ProfileCacheObject(resultSet.getString("MSG_ID"), resultSet.getString("DIRECTION"), resultSet.getString("STATUS"), resultSet.getBytes("HASH"));

					if (maxMessageKeysPerScenario > 0) // check if we already reached the limit for number of message ids -> 0 means unlimited
					{
						Long msgIDsForScenario = msgIDsPerScenario.get(profileCacheObject.getHashInHex());
						msgIDsForScenario = (msgIDsForScenario == null) ? 0L : msgIDsForScenario;

						if (msgIDsForScenario >= maxMessageKeysPerScenario)
							profileCacheObject = null; // already too many message ids -> drop the current entry and message id
						else
							msgIDsPerScenario.put(profileCacheObject.getHashInHex(), msgIDsForScenario + 1L); // increase counter by 1 since we have another message id
					}
				}
				else // don't retrieve message ids
					profileCacheObject = new ProfileCacheObject(resultSet.getString("STATUS"), resultSet.getBytes("HASH"), resultSet.getInt( "CNT"));

				if (profileCacheObject != null)
					profileCacheObjects.add(profileCacheObject);
			}
			resultSet.close();
			resultSet = null;
			stm.close();
			stm = null;

			/* -------------------- select the profile hashes in table XI_AF_PROF_HASH -------------------- */
			profileHashObjects = new HashMap<String, ProfileHashObject>();
			if (!profileCacheObjects.isEmpty())
			{
				query = "SELECT METADATA FROM XI_AF_PROF_HASH WHERE HASH = ?";
				stm = conn.prepareStatement(query);

				HashSet<String> excludedHashes = new HashSet<String>(); // hash values that were excluded because they don't match the input search filter
				for (ProfileCacheObject profileCacheObject : profileCacheObjects)
				{
					byte[] hash = profileCacheObject.getHash();
					String hashInHex = Hex.encode(hash);
					if (!profileHashObjects.containsKey(hashInHex) && !excludedHashes.contains(hashInHex))
					{
						stm.setBytes(1, hash);

						resultSet = stm.executeQuery(); // execute SQL query
						if (resultSet.next())
						{
							byte[] metadata = resultSet.getBytes("METADATA");
							ProfileHashObject profileHashObject = new ProfileHashObject(hash, metadata);

							if (scenariosFilter.matchesFilter(profileHashObject)) // only keep those messages that match the input search filter from the job parameters
								profileHashObjects.put(hashInHex, profileHashObject);
							else
								excludedHashes.add(hashInHex);
						}
						resultSet.close();
						resultSet = null;
					}
				}
				stm.close();
				stm = null;
			}
		}
		catch (Exception e)
		{
			TRACE.traceThrowableT(Severity.ERROR, SIGNATURE, "Exception while retrieving information about stuck messages.", e);
		}
		finally
		{
			if (resultSet != null)
				try { resultSet.close(); } catch (SQLException e) { TRACE.catching( SIGNATURE, e ); TRACE.traceThrowableT(Severity.DEBUG, SIGNATURE, "Error closing resultset", e); }
			if (stm != null)
				try { stm.close(); } catch (SQLException e) { TRACE.catching( SIGNATURE, e ); TRACE.traceThrowableT(Severity.DEBUG, SIGNATURE, "Error closing statement", e); }
			if (conn != null)
				try { conn.close(); } catch (SQLException e) { TRACE.catching( SIGNATURE, e ); TRACE.traceThrowableT(Severity.DEBUG, SIGNATURE, "Error closing connection", e); }
		}

		/* -------------------- build the result data by merging the ProfileCacheObjects with the same profile hash and summing up their counters -------------------- */
		HashMap<MessageOverviewData, MessageOverviewData> resultStuckMessages = new HashMap<MessageOverviewData, MessageOverviewData>();
		if (profileHashObjects != null && profileCacheObjects != null && !profileHashObjects.isEmpty())
		{
			for (ProfileCacheObject profileCacheObject : profileCacheObjects)
			{
				byte[] hash = profileCacheObject.getHash();
				ProfileHashObject profileHashObject = profileHashObjects.get(Hex.encode(hash));

				if (profileHashObject != null)
				{
					MessageOverviewData newMsgOvData = new MessageOverviewData(profileHashObject, profileCacheObject);
					MessageOverviewData oldMsgOvData = resultStuckMessages.get(newMsgOvData);
					if (oldMsgOvData != null)
						oldMsgOvData.merge(newMsgOvData);
					else
						resultStuckMessages.put(newMsgOvData, newMsgOvData);
				}
			}
		}

        TRACE.exiting(SIGNATURE);
        return new ArrayList<MessageOverviewData>(resultStuckMessages.keySet());
    }

}
// end class LocalMsgOverviewDataProvider