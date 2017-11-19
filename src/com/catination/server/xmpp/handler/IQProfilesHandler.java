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
package com.catination.server.xmpp.handler;

import gnu.inet.encoding.Stringprep;
import gnu.inet.encoding.StringprepException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.QName;
import org.xmpp.packet.IQ;
import org.xmpp.packet.JID;
import org.xmpp.packet.PacketError;

import com.catination.server.model.User;
import com.catination.server.service.ServiceLocator;
import com.catination.server.service.UserExistsException;
import com.catination.server.service.UserNotFoundException;
import com.catination.server.service.UserService;
import com.catination.server.xmpp.UnauthenticatedException;
import com.catination.server.xmpp.UnauthorizedException;
import com.catination.server.xmpp.auth.AuthManager;
import com.catination.server.xmpp.auth.AuthToken;
import com.catination.server.xmpp.session.ClientSession;
import com.catination.server.xmpp.session.Session;

/** 
 * This class is to handle the TYPE_IQ jabber:iq:register protocol.
 *
 *
 */
public class IQProfilesHandler extends IQHandler {
	private static final String NAMESPACE = "jabber:iq:profiles";

	private UserService userService;

	private Element probeResponse;

	/**
	 * Constructor.
	 */
	public IQProfilesHandler() {
		userService = ServiceLocator.getUserService();
		probeResponse = DocumentHelper.createElement(QName.get("query",
				NAMESPACE));
		probeResponse.addElement("username");
		probeResponse.addElement("password");
		probeResponse.addElement("newpwd");
		probeResponse.addElement("email");
		probeResponse.addElement("name");
		probeResponse.addElement("portrait");
		probeResponse.addElement("relation");
		probeResponse.addElement("group");
	}

	/**
	 * Handles the received IQ packet.
	 * 
	 * @param packet the packet
	 * @return the response to send back
	 * @throws UnauthorizedException if the user is not authorized
	 */
	public IQ handleIQ(IQ packet) throws UnauthorizedException {
		IQ reply = null;
		
		log.debug("handleIQ(), packet="+packet.toXML());

		ClientSession session = sessionManager.getSession(packet.getFrom());
		if (session == null) {
			log.error("Session not found for key " + packet.getFrom());
			reply = IQ.createResultIQ(packet);
			reply.setChildElement(packet.getChildElement().createCopy());
			reply.setError(PacketError.Condition.internal_server_error);
			return reply;
		}

		if (IQ.Type.get.equals(packet.getType())) {
			reply = IQ.createResultIQ(packet);
			if (session.getStatus() == Session.STATUS_AUTHENTICATED) {
				// TODO
			} else {
				reply.setTo((JID) null);
				reply.setChildElement(probeResponse.createCopy());
			}
		} else if (IQ.Type.set.equals(packet.getType())) {
			try {
				Element query = packet.getChildElement();
				if (query.element("remove") != null) {
					if (session.getStatus() == Session.STATUS_AUTHENTICATED) {
						// TODO
					} else {
						throw new UnauthorizedException();
					}
				} else {
					String username = query.elementText("username");
					String password = query.elementText("password");
					String newpwd = query.elementText("newpwd");
					String email = query.elementText("email");
					String name = query.elementText("name");
					String portrait = query.elementText("portrait");
					String relation = query.elementText("relation");
					String group = query.elementText("group");
					
					String digest = null;
					if (query.element("digest") != null) {
						digest = query.elementText("digest").toLowerCase();
					}
					
					//if (newpwd == null) {
					//	String newdigest = null;
					//	if (query.element("newdigest") != null) {
					//		newdigest = query.elementText("newdigest").toLowerCase();
					//	}
					//}

					if (email != null && email.matches("\\s*")) {
						email = null;
					} else if (email != null && !email.matches(".*@.*\\..*")) {
						email = null;
					}
					if (name != null && name.matches("\\s*")) {
						name = null;
					}
					
					// Verify that username and password are correct
					AuthToken token = null;
					if (password != null && AuthManager.isPlainSupported()) {
						token = AuthManager.authenticate(username, password);
					} else if (digest != null && AuthManager.isDigestSupported()) {
						token = AuthManager.authenticate(username, session
								.getStreamID().toString(), digest);
					}
					log.debug("handleIQ(), token="+token);
					if (token == null) {
						throw new UnauthenticatedException();
					}

					User user;
					if (session.getStatus() == Session.STATUS_AUTHENTICATED) {
						user = userService.getUserByUsername(session.getUsername());
					} else {
						user = new User();
					}
					user.setUsername(username);
					user.setPassword(newpwd);
					user.setEmail(email);
					user.setName(name);
					user.setPortrait(portrait);
					user.setRelation(relation);
					user.setGroup(group);
					userService.updateUser(user);

					reply = IQ.createResultIQ(packet);
				}
			} catch (Exception ex) {
				log.error(ex);
				reply = IQ.createResultIQ(packet);
				reply.setChildElement(packet.getChildElement().createCopy());
				if (ex instanceof UserExistsException) {
					reply.setError(PacketError.Condition.conflict);
				} else if (ex instanceof UserNotFoundException) {
					reply.setError(PacketError.Condition.bad_request);
				} else if (ex instanceof StringprepException) {
					reply.setError(PacketError.Condition.jid_malformed);
				} else if (ex instanceof IllegalArgumentException) {
					reply.setError(PacketError.Condition.not_acceptable);
				} else {
					reply.setError(PacketError.Condition.internal_server_error);
				}
			}
		}

		// Send the response directly to the session
		if (reply != null) {
			session.process(reply);
		}
		return null;
	}

	/**
	 * Returns the namespace of the handler.
	 * 
	 * @return the namespace
	 */
	public String getNamespace() {
		return NAMESPACE;
	}

}
