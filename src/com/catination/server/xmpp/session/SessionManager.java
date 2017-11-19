/*
 * Copyright (C) 2010 Moduad Co., Ltd.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package com.catination.server.xmpp.session;

import java.net.UnknownHostException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xmpp.packet.JID;

import com.catination.server.xmpp.XmppServer;
import com.catination.server.xmpp.net.Connection;
import com.catination.server.xmpp.net.ConnectionCloseListener;

/** 
 * This class manages the sessions connected to the server.
 *
 *
 */
public class SessionManager {

	private static final Log log = LogFactory.getLog(SessionManager.class);

	private static final String RESOURCE_NAME = "SC";

	private static SessionManager instance;

	private String serverName;

	private Map<String, ClientSession> preAuthSessions = new ConcurrentHashMap<String, ClientSession>();

	private Map<String, ClientSession> clientSessions = new ConcurrentHashMap<String, ClientSession>();

	private final AtomicInteger connectionsCounter = new AtomicInteger(0);

	private ClientSessionListener clientSessionListener = new ClientSessionListener();

	private SessionManager() {
		serverName = XmppServer.getInstance().getServerName();
	}

	/**
	 * Returns the singleton instance of SessionManager.
	 * 
	 * @return the instance
	 */
	public static SessionManager getInstance() {
		if (instance == null) {
			synchronized (SessionManager.class) {
				instance = new SessionManager();
			}
		}
		return instance;
	}

	/**
	 * Creates a new ClientSession and returns it.
	 *  
	 * @param conn the connection
	 * @return a newly created session
	 * @throws UnknownHostException 
	 */
	public ClientSession createClientSession(Connection conn) throws UnknownHostException  {
		if (serverName == null) {
			throw new IllegalStateException("Server not initialized");
		}

		Random random = new Random();
		String streamId = Integer.toHexString(random.nextInt());

		ClientSession session = new ClientSession(serverName, conn, streamId/*conn.getHostAddress()*/);
		conn.init(session);
		conn.registerCloseListener(clientSessionListener);

		// Add to pre-authenticated sessions
		preAuthSessions.put(session.getAddress().getResource(), session);
		//preAuthSessions.put(session.getHostAddress(), session);

		// Increment the counter of user sessions
		connectionsCounter.incrementAndGet();

		log.debug("ClientSession created.");
		return session;
	}

	/**
	 * Adds a new session that has been authenticated. 
	 *  
	 * @param session the session
	 * @throws UnknownHostException 
	 * @ 
	 */
	public void addSession(ClientSession session) throws UnknownHostException   {
		log.debug("addSession(), session="+session);
		preAuthSessions.remove(session.getStreamID().toString());
		//preAuthSessions.remove(session.getHostAddress());
		clientSessions.put(session.getAddress().getNode(), session);
	}

	/**
	 * Returns the session associated with the username.
	 * 
	 * @param username the username of the client address
	 * @return the session associated with the username
	 */
	public ClientSession getSession(String username) {
		return getSession(new JID(username, serverName, null, true));
		//return getSession(new JID(username, serverName, RESOURCE_NAME, true));
		//return getSession(new JID(username, serverName, RESOURCE_NAME, false));
	}

	/**
	 * Returns the session associated with the JID.
	 * 
	 * @param from the client address
	 * @return the session associated with the JID
	 */
	public ClientSession getSession(JID from) {
		log.debug("getSession(), jid="+from);
		if (from == null || serverName == null
				|| !serverName.equals(from.getDomain())) {
			log.debug("getSession(), serverName="+serverName);
			return null;
		}
		// Check pre-authenticated sessions
		if (from.getNode() == null) {
			ClientSession session = preAuthSessions.get(from.getResource());
			if (session != null) {
				log.debug("getSession(), session="+session);
				return session;
			}
		}
		
		if (from.getNode() == null) {
			log.debug("getSession(), from.getNode()="+from.getNode());
			return null;
		}
		return clientSessions.get(from.getNode());
	}

	/**
	 * Returns a list that contains all authenticated client sessions.
	 * 
	 * @return a list that contains all client sessions
	 */
	public Collection<ClientSession> getSessions() {
		return clientSessions.values();
	}

	/**
	 * Removes a client session.
	 * 
	 * @param session the session to be removed
	 * @return true if the session was successfully removed 
	 * @throws UnknownHostException 
	 */
	public boolean removeSession(ClientSession session) throws UnknownHostException  {
		if (session == null || serverName == null) {
			return false;
		}
		
		log.debug("removeSession(), session="+session);
		JID fullJID = session.getAddress();

		// Remove the session from list
		boolean preAuthRemoved = (preAuthSessions.remove(fullJID.getResource()) != null);
		//boolean preAuthRemoved = (preAuthSessions.remove(session.getHostAddress()) != null);
		boolean clientRemoved = clientSessions.remove(fullJID.getNode()) != null;

		// Decrement the counter of user sessions
		if (clientRemoved || preAuthRemoved) {
			connectionsCounter.decrementAndGet();
			return true;
		}
		return false;
	}

	/**
	 * Closes the all sessions. 
	 */
	public void closeAllSessions() {
		try {
			// Send the close stream header to all connections
			Set<ClientSession> sessions = new HashSet<ClientSession>();
			sessions.addAll(preAuthSessions.values());
			sessions.addAll(clientSessions.values());

			for (ClientSession session : sessions) {
				try {
					session.getConnection().systemShutdown();
				} catch (Throwable t) {
				}
			}
		} catch (Exception e) {
		}
	}

	/**
	 * A listner to handle a session that has been closed.
	 */
	private class ClientSessionListener implements ConnectionCloseListener {

		public void onConnectionClose(Object handback) {
			try {
				ClientSession session = (ClientSession) handback;
				removeSession(session);
			} catch (Exception e) {
				log.error("Could not close socket", e);
			}
		}
	}

}
