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
package com.catination.server.xmpp.push;

import java.util.Collection;
import java.util.Iterator;
import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.QName;
import org.xmpp.packet.IQ;
import org.xmpp.packet.JID;
import org.xmpp.packet.Message;
import org.xmpp.packet.Message.Type;

import com.catination.server.xmpp.group.Group;
import com.catination.server.xmpp.group.GroupManager;
import com.catination.server.xmpp.session.ClientSession;
import com.catination.server.xmpp.session.SessionManager;

/** 
 * This class is to manage sending the notifcations to the users.  
 *
 *
 */
public class MessageManager {

	private static final String NOTIFICATION_NAMESPACE = "message";

	private final Log log = LogFactory.getLog(getClass());

	private SessionManager sessionManager;
	
	private GroupManager groupManager;

	/**
	 * Constructor.
	 */
	public MessageManager() {
		// Should not create SessionManager here in current thread
		//sessionManager = SessionManager.getInstance();
	}

	/**
	 * Broadcasts a newly created message to all connected users.
	 * 
	 * @param to the destination user
	 * @param from the original user
	 * @param type chat or groupchat
	 * @param body the message details
	 * @param thread the current chat for the user
	 */
	public void sendBroadcast(String to, String from, Type type,
			String body, String thread) {
		log.debug("sendBroadcast(), to="+to+", from="+from+", type="
					+type+", body="+body+", thread="+thread);
		sessionManager = SessionManager.getInstance();
		Message message = createMessage(to, from, type, body, thread);
		for (ClientSession session : sessionManager.getSessions()) {
			if (session.getPresence().isAvailable()) {
				session.deliver(message);
			}
		}
	}

	/**
	 * Sends a newly created message to the user in specific group.
	 * 
	 * @param to the destination group
	 * @param from the original user
	 * @param type chat or groupchat
	 * @param body the message details
	 * @param thread the current chat for the user
	 */
	public void sendMessage(String to, String from, Type type,
			String body, String thread) {
		if (type == Type.groupchat) {
			sendMessageToGroup(to, from, type, body, thread);
		} else { //type == Type.chat || Type.file
			sendMessageToUser(to, from, type, body, thread); 
		}
	}
	
	/**
	 * Sends a original message to the user in specific group.
	 * 
	 * @param msg the original message
	 */
	public void sendMessage(Message msg) {
		sessionManager = SessionManager.getInstance();
		ClientSession session = sessionManager.getSession(msg.getTo());
		log.info("sendMessageToUser(), session="+session);
		if (session != null) {
			if (session.getPresence().isAvailable()) {
				session.deliver(msg);
			} else {
				log.error("sendMessageToUser(), '"+msg.getTo()+"' not present (1)");
			}
		} else {
			log.error("sendMessageToUser(), '"+msg.getTo()+"' not present (2)");
		}
	}
	
	/**
	 * Sends a newly created message to the user in specific group.
	 * 
	 * @param to the destination group
	 * @param from the original user
	 * @param type chat or groupchat
	 * @param body the message details
	 * @param thread the current chat for the user
	 */
	public void sendMessageToGroup(String toGroup, String from, Type type,
			String body, String thread) {
		log.debug("sendMessageToGroup(), toGroup="+toGroup+", from="+from+", type="
					+type+", body="+body+", thread="+thread);
		sessionManager = SessionManager.getInstance();
		groupManager = GroupManager.getInstance();
		Group group = groupManager.getGroup(toGroup);
		Collection<Object> users = group.getUsers();
		Iterator iter = users.iterator();
		
		String fromUid = from.substring(0, from.indexOf('@'));
		log.debug("sendMessageToGroup(), users.size()="+users.size()+", fromUid="+fromUid);
		while (iter.hasNext()) {
			String user = (String)iter.next();
			log.debug("sendMessageToGroup(), user="+user);
			//type = Type.chat; // Temporarily.
			
			if (!user.equals(fromUid)) { // Not send to self
				sendMessageToUser(user, from, type, body, thread);
			}
		}
	}
	
	/**
	 * Sends a newly created message to the specific user.
	 * 
	 * @param to the destination user
	 * @param from the original user
	 * @param type chat or groupchat
	 * @param body the message details
	 * @param thread the current chat for the user
	 */
	public void sendMessageToUser(String to, String from, Type type,
			String body, String thread) {
		if (to.indexOf('@') != -1) {
			to = to.substring(0, to.indexOf('@'));
		}
		log.debug("sendMessageToUser(), to="+to+", from="+from+", type="
					+type+", body="+body+", thread="+thread);
		sessionManager = SessionManager.getInstance();
		Message message = createMessage(to, from, type, body, thread);
		ClientSession session = sessionManager.getSession(to);
		log.info("sendMessageToUser(), session="+session);
		if (session != null) {
			if (session.getPresence().isAvailable()) {
				session.deliver(message);
			} else {
				log.error("sendMessageToUser(), '"+to+"' not present (1)");
			}
		} else {
			log.error("sendMessageToUser(), '"+to+"' not present (2)");
		}
	}
	
	/**
	 * Sends a created message to the specific user.
	 * 
	 * @param message the packed message to be sent
	 */
	public void sendMessageToUser(Message message) {
		log.debug("sendMessageToUser(), message="+message);
		sessionManager = SessionManager.getInstance();
		ClientSession session = sessionManager.getSession(message.getTo());
		log.info("sendMessageToUser(), session="+session);
		if (session != null) {
			if (session.getPresence().isAvailable()) {
				session.deliver(message);
			} else {
				log.info("sendMessageToUser(), '"+message.getTo()+"' not present");
			}
		}
	}

	/**
	 * Creates a new message and returns it.
	 */
	private Message createMessage(String to, String from, Type type,
			String body, String thread) {
		Random random = new Random();
		String id = Integer.toHexString(random.nextInt());
		// String id = String.valueOf(System.currentTimeMillis());

		Message message = new Message();
		message.setID(id);
		message.setTo(to);
		message.setFrom(from);
		message.setType(type);
		message.setBody(body);
		message.setThread(thread);

		log.info("createMessage(), message="+message);
		return message;
	}
}
