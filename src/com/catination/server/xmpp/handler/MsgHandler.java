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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xmpp.packet.Message;
import org.xmpp.packet.Packet;
import org.xmpp.packet.PacketError;

import com.catination.server.xmpp.UnauthorizedException;
import com.catination.server.xmpp.router.PacketDeliverer;
import com.catination.server.xmpp.session.SessionManager;

/** 
 * This is an abstract class to handle routed Message packets.
 *
 *
 */
public abstract class MsgHandler {

    protected final Log log = LogFactory.getLog(getClass());

    protected SessionManager sessionManager;

    /**
     * Constructor.
     */
    public MsgHandler() {
        sessionManager = SessionManager.getInstance();
        /*log.debug("sessionManager="+sessionManager);
        log.debug("Thread.currentThread()="+Thread.currentThread());
        try {
    		throw new Exception();
    	} catch (Exception e) {
    		e.printStackTrace();
    	}*/
    }

    /**
     * Processes the received Message packet.
     * 
     * @param packet the packet
     */
    public void process(Message packet) {
        Message message = (Message) packet;
        try {
            Message reply = handleMessage(message);
            if (reply != null) {
                PacketDeliverer.deliver(reply);
            }
        } catch (UnauthorizedException e) {
            /*if (message != null) {
                try {
                    Message response = Message.createResultIQ(message);
                    response.setChildElement(message.getChildElement().createCopy());
                    response.setError(PacketError.Condition.not_authorized);
                    sessionManager.getSession(message.getFrom()).process(response);
                } catch (Exception de) {
                    log.error("Internal server error", de);
                    sessionManager.getSession(message.getFrom()).close();
                }
            }*/
        } catch (Exception e) {
            log.error("Internal server error", e);
            /*try {
                Message response = message..createResultIQ(message);
                response.setChildElement(message.getChildElement().createCopy());
                response.setError(PacketError.Condition.internal_server_error);
                sessionManager.getSession(message.getFrom()).process(response);
            } catch (Exception ex) {
                // Ignore
            }*/
        }
    }

    /**
     * Handles the received Message packet.
     * 
     * @param packet the packet
     * @return the response to send back
     * @throws UnauthorizedException if the user is not authorized
     */
    public abstract Message handleMessage(Message packet) throws UnauthorizedException;

    /**
     * Returns the type of the handler.
     * 
     * @return the type
     */
    public abstract String getType();

}
