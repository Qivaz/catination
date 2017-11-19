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

import gnu.inet.encoding.StringprepException;

import org.dom4j.Attribute;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.QName;
import org.xmpp.packet.IQ;
import org.xmpp.packet.IQ.Type;
import org.xmpp.packet.JID;
import org.xmpp.packet.PacketError;

import com.catination.server.model.Roster;
import com.catination.server.model.User;
import com.catination.server.service.RosterService;
import com.catination.server.service.ServiceLocator;
import com.catination.server.service.UserExistsException;
import com.catination.server.service.UserNotFoundException;
import com.catination.server.service.UserService;
import com.catination.server.xmpp.UnauthenticatedException;
import com.catination.server.xmpp.UnauthorizedException;
import com.catination.server.xmpp.XmppServer;
import com.catination.server.xmpp.auth.AuthManager;
import com.catination.server.xmpp.auth.AuthToken;
import com.catination.server.xmpp.session.ClientSession;
import com.catination.server.xmpp.session.Session;

/** 
 * This class is to handle the TYPE_IQ jabber:iq:roster protocol.
 *
 *
 */
public class IQRosterHandler extends IQHandler {
    
    private static final String NAMESPACE = "jabber:iq:roster";
    
    private String serverName;
    private UserService userService;
    private RosterService rosterService;
    /**
     * Constructor.
     */
    public IQRosterHandler() {
    	userService = ServiceLocator.getUserService();
    	rosterService = ServiceLocator.getRosterService();
    	serverName = XmppServer.getInstance().getServerName();
    }

    /**
     * Handles the received IQ packet.
     * 
     * @param packet the packet
     * @return the response to send back
     * @throws UnauthorizedException if the user is not authorized
     */
    public IQ handleIQ(IQ packet) throws UnauthorizedException {
        // TODO
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
			/*
			 * Request:
			 * 
			 * <iq id="39Y2F-98" type="get"><query xmlns="jabber:iq:roster"></query></iq>
			 */
			
			/*
			 * Response:
			 * 
				<iq from='zhqh@catination.com/android'
					id='39Y2F-98'
					type='result'>
					<query xmlns='jabber:iq:roster'>
					<item jid='test2@catination.com' name='test1'/>
					<item jid='test1@catination.com' name='test2'/>
					</query>
				</iq>
			 * 
			 */
			if (session.getStatus() == Session.STATUS_AUTHENTICATED) {
				try {
					/*
					 * Roster format: username/name@group
					 * sperated with ;
					 */
					String user = packet.getFrom().getNode();
					Roster daoRoster = rosterService.getRosterByUsername(user);
					if (daoRoster != null) {
						Element query = DocumentHelper.createElement(QName.get("query", NAMESPACE));
						String friends = daoRoster.getFriends();
						String frs[] = friends.split(";");
						for (String fr:frs) {
							Element item = query.addElement("item");
							int pos = fr.indexOf("/");
							
							if (pos != -1) {
								String uid = fr.substring(0, pos);
								String name = fr.substring(pos+1, fr.indexOf("@"));
								item.addAttribute("jid", uid+"@"+serverName);
								item.addAttribute("name", name);
							}
						}
						reply = IQ.createResultIQ(packet);
						reply.setChildElement(query);
					} else {
						reply = IQ.createResultIQ(packet);
						reply.setChildElement(packet.getChildElement().createCopy());
						reply.setError(PacketError.Condition.item_not_found);
					}
				} catch (Exception ex) {
					// TODO Auto-generated catch block
					log.error(ex);
					reply = IQ.createResultIQ(packet);
					reply.setChildElement(packet.getChildElement().createCopy());
					if (ex instanceof UserNotFoundException) {
						reply.setError(PacketError.Condition.bad_request);
					}
				}
			}
		} else if (IQ.Type.set.equals(packet.getType())) {
			/*
			 * Request:
			 * 
			 * <iq id="39Y2F-99" type="set"><query xmlns="jabber:iq:roster"><item jid="test3@catination.com" name="test3"><group>Friends</group></item></query></iq>
			 */
			
			/*
			 * Response:
			 * 
			 * <iq to='zhqh@catination.com/android' type='result' id='39Y2F-99'/>
			 */
			try {
				if (session.getStatus() == Session.STATUS_AUTHENTICATED) {
					Element query = packet.getChildElement();
					Element item = query.element("item");
					
					//Attribute aJid = item.attribute("jid");
					//String jid = aJid.getValue();
					//Attribute aUsername = item.attribute("name");
					//String username = aUsername.getValue();
					String jid = item.attributeValue("jid");
					String friendUid = jid.substring(0, jid.indexOf("@"));
					String friendName = item.attributeValue("name");
					String subscr = item.attributeValue("subscription");
					boolean remove = false;
					remove = "remove".equals(subscr);
					String group = item.elementText("group");
					
					/* If friend to add does not exist */
					if (null == userService.getUserByUsername(friendUid)) {
						throw new UserNotFoundException();
					}
					
					/*
					 * Roster format: username/name@group
					 * sperated with ;
					 */
					String user = packet.getFrom().getNode();
					Roster daoRoster = rosterService.getRosterByUsername(user);
					if (daoRoster != null) {
						String daoFriends = daoRoster.getFriends();
						String daoGroup = daoRoster.getGroups();
						
						if (remove) {
							// Delete friend
							String target = friendUid+"/"+friendName+"@"+group;
							int pos = daoFriends.indexOf(target);
							if (pos != -1) {
								// Delete
								int len = target.length();
								String prefix = daoFriends.substring(0, pos);
								String suffix = daoFriends.substring(pos+len);
								daoFriends = prefix + suffix;
							} else {
								// Not found to delete
								log.error("Friend \""+target+"\" not found for " + packet.getFrom());
								reply = IQ.createResultIQ(packet);
								reply.setChildElement(packet.getChildElement().createCopy());
								reply.setError(PacketError.Condition.item_not_found);
								return reply;
							}
						} else {
							// Add friend
							if (daoGroup != null)
							if (!daoGroup.contains(group)) {
								// Create group
								daoGroup = daoGroup + ";"+group;
								daoRoster.setGroup(daoGroup);
							}
							
							if (daoFriends != null)
							if (!daoFriends.contains(friendUid+"/")) {
								// Add friend
								daoFriends = daoFriends + ";"+friendUid+"/"+friendName+"@"+group;
								daoRoster.setFriends(daoFriends);
							} else {
								// Change group??
							}
						}
					} else {
						daoRoster = new Roster();
						daoRoster.setUsername(user);
						daoRoster.setGroup(group);
						daoRoster.setFriends(friendUid+"/"+friendName+"@"+group);
					}
					
					rosterService.updateRoster(daoRoster);

					reply = IQ.createResultIQ(packet);
					//reply.setChildElement(packet.getChildElement().createCopy());
					reply.setType(Type.result);
					//reply.setChildElement("query", this.getNamespace());
				}
			} catch (Exception ex) {
				log.error(ex);
				reply = IQ.createResultIQ(packet);
				//reply.setChildElement(packet.getChildElement().createCopy());
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
