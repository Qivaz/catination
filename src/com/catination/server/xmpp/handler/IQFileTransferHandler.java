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

import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.QName;
import org.xmpp.packet.IQ;
import org.xmpp.packet.JID;
import org.xmpp.packet.PacketError;

import com.catination.server.xmpp.UnauthorizedException;
import com.catination.server.xmpp.auth.AuthManager;
import com.catination.server.xmpp.filetransfer.FileTransferManager;
import com.catination.server.xmpp.session.ClientSession;

/** 
 * This class is to handle the TYPE_IQ jabber:iq:auth protocol.
 *
 *
 */
public class IQFileTransferHandler extends IQHandler {

	private static final String NAMESPACE = FileTransferManager.NAMESPACE_BYTESTREAMS;

	private Element probeResponse;

	/**
	 * Constructor.
	 */
	public IQFileTransferHandler() {
		probeResponse = DocumentHelper.createElement(QName.get("query",
				NAMESPACE));
		probeResponse.addElement("username");
		if (AuthManager.isPlainSupported()) {
			probeResponse.addElement("password");
		}
		if (AuthManager.isDigestSupported()) {
			probeResponse.addElement("digest");
		}
		probeResponse.addElement("resource");
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
		
		ClientSession session = sessionManager.getSession(packet.getFrom());
		if (session == null) {
			log.error("Session not found for key " + packet.getFrom());
			reply = IQ.createResultIQ(packet);
			reply.setChildElement(packet.getChildElement().createCopy());
			reply.setError(PacketError.Condition.internal_server_error);
			return reply;
		}
		
		if (handleIQ(packet, session)) {
			return null;
		}
		
		reply = IQ.createResultIQ(packet);
		reply.setChildElement(packet.getChildElement().createCopy());
		reply.setError(PacketError.Condition.feature_not_implemented);
		return reply;
	}
	
    public boolean handleIQ(IQ packet, ClientSession router) throws UnauthorizedException {
        Element childElement = packet.getChildElement();
        String namespace = null;

        // ignore errors
        if (packet.getType() == IQ.Type.error) {
            return true;
        }
        if (childElement != null) {
            namespace = childElement.getNamespaceURI();
        }

        log.info(namespace);
        if ("http://jabber.org/protocol/disco#info".equals(namespace)) {
            //IQ reply = XMPPServer.getInstance().getIQDiscoInfoHandler().handleIQ(packet);
            //router.route(reply);
            return true;
        }
        else if ("http://jabber.org/protocol/disco#items".equals(namespace)) {
            // a component
            //IQ reply = XMPPServer.getInstance().getIQDiscoItemsHandler().handleIQ(packet);
            //router.route(reply);
            return true;
        }
        else if (FileTransferManager.NAMESPACE_BYTESTREAMS.equals(namespace)) {
            if (packet.getType() == IQ.Type.get) {
            	log.info("namespace:"+FileTransferManager.NAMESPACE_BYTESTREAMS+", IQ.Type.get");
                IQ reply = IQ.createResultIQ(packet);
                Element newChild = reply.setChildElement("query",
                        FileTransferManager.NAMESPACE_BYTESTREAMS);
                Element response = newChild.addElement("streamhost");
                response.addAttribute("jid", packet.getFrom().toFullJID());
                response.addAttribute("host", proxyIP);
                response.addAttribute("port", proxyPort);
                router.process(reply);
                return true;
            }
            else if (packet.getType() == IQ.Type.set && childElement != null) {
            	log.info("namespace:"+FileTransferManager.NAMESPACE_BYTESTREAMS+", IQ.Type.set");
                String sid = childElement.attributeValue("sid");
                JID from = packet.getFrom();
                JID to = new JID(childElement.elementTextTrim("activate"));

                IQ reply = IQ.createResultIQ(packet);
                try {
                    //connectionManager.activate(from, to, sid);
                }
                catch (IllegalArgumentException ie) {
                    log.error("Error activating connection", ie);
                    reply.setType(IQ.Type.error);
                    reply.setError(new PacketError(PacketError.Condition.not_allowed));
                }

                router.process(reply);
                return true;
            }
        }
        return false;
    }
    
    private String proxyIP = "109.131.18.203";
    private String proxyPort = "1818";
    
    public void setProxy(String ip, String port) {
    	proxyIP = ip;
    	proxyPort = port;
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
