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

import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.QName;
import org.xmpp.packet.IQ;

import com.catination.server.xmpp.session.ClientSession;
import com.catination.server.xmpp.session.SessionManager;

/** 
 * This class is to manage sending the notifcations to the users.  
 *
 *
 */
public class GroupMessageManager {

    private static final String NOTIFICATION_NAMESPACE = "jabber:iq:message";

    private final Log log = LogFactory.getLog(getClass());

    private SessionManager sessionManager;

    /**
     * Constructor.
     */
    public GroupMessageManager() {
        sessionManager = SessionManager.getInstance();
    }

    /**
     * Broadcasts a newly created message message to all connected users.
     * 
     * @param apiKey the API key
     * @param title the title
     * @param message the message details
     * @param uri the uri
     */
    public void sendBroadcast(String apiKey, String title, String message,
            String uri) {
        log.debug("sendBroadcast()...");
        IQ messageIQ = createNotificationIQ(apiKey, title, message, uri);
        for (ClientSession session : sessionManager.getSessions()) {
            if (session.getPresence().isAvailable()) {
                messageIQ.setTo(session.getAddress());
                session.deliver(messageIQ);
            }
        }
    }

    /**
     * Sends a newly created message message to the specific user.
     * 
     * @param apiKey the API key
     * @param title the title
     * @param message the message details
     * @param uri the uri
     */
    public void sendNotifcationToUser(String apiKey, String username,
            String title, String message, String uri) {
        log.debug("sendNotifcationToUser()...");
        IQ messageIQ = createNotificationIQ(apiKey, title, message, uri);
        ClientSession session = sessionManager.getSession(username);
    	log.info("sendNotifcationToUser(), session="+session);
        if (session != null) {
            if (session.getPresence().isAvailable()) {
                messageIQ.setTo(session.getAddress());
                session.deliver(messageIQ);
            } else {
            	log.info("sendNotifcationToUser(), "+username+" not present");
            }
        }
    }

    /**
     * Creates a new message IQ and returns it.
     */
    private IQ createNotificationIQ(String apiKey, String title,
            String content, String uri) {
    	log.info("createNotificationIQ(), NOTIFICATION_NAMESPACE="+NOTIFICATION_NAMESPACE);
        Random random = new Random();
        String id = Integer.toHexString(random.nextInt());
        // String id = String.valueOf(System.currentTimeMillis());

        Element message = DocumentHelper.createElement(QName.get(
                "message", NOTIFICATION_NAMESPACE));
        message.addElement("id").setText(id);
        message.addElement("apiKey").setText(apiKey);
        message.addElement("title").setText(title);
        message.addElement("content").setText(content);
        message.addElement("uri").setText(uri);

        IQ iq = new IQ();
        iq.setType(IQ.Type.set);
        iq.setChildElement(message);

        return iq;
    }
}
