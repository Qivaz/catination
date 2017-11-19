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
package com.catination.server.xmpp.router;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;
import org.xmpp.packet.IQ;
import org.xmpp.packet.JID;
import org.xmpp.packet.Message;
import org.xmpp.packet.Message.Type;
import org.xmpp.packet.PacketError;

import com.catination.server.xmpp.handler.IQAuthHandler;
import com.catination.server.xmpp.handler.IQRegisterHandler;
import com.catination.server.xmpp.handler.IQRosterHandler;
import com.catination.server.xmpp.handler.MsgChatHandler;
import com.catination.server.xmpp.handler.MsgFileHandler;
import com.catination.server.xmpp.handler.MsgGroupChatHandler;
import com.catination.server.xmpp.handler.MsgHandler;
import com.catination.server.xmpp.session.ClientSession;
import com.catination.server.xmpp.session.Session;
import com.catination.server.xmpp.session.SessionManager;

/** 
 * This class is to route Message packets to their corresponding handler.
 *
 *
 */
public class MsgRouter {
	private final Log log = LogFactory.getLog(getClass());
	private SessionManager sessionManager;

	private List<MsgHandler> msgHandlers = new ArrayList<MsgHandler>();
	private Map<String, MsgHandler> type2Handlers = new ConcurrentHashMap<String, MsgHandler>();

	/**
	 * Constucts a packet router.
	 */
	public MsgRouter() {
		sessionManager = SessionManager.getInstance();

		msgHandlers.add(new MsgChatHandler());
		msgHandlers.add(new MsgGroupChatHandler());
		msgHandlers.add(new MsgFileHandler());
	}

	/**
	 * Routes the Message packet.
	 * 
	 * @param packet the packet to route
	 */
	public void route(Message packet) {
		if (packet == null) {
			throw new NullPointerException();
		}
		JID sender = packet.getFrom();
		ClientSession session = sessionManager.getSession(sender);

		log.debug("route(), packet.type="+packet.getType());
		if (session != null
				&& session.getStatus() == Session.STATUS_AUTHENTICATED
				&& ((Type.chat == packet.getType())
						|| (Type.groupchat == packet.getType())
						|| (Type.file == packet.getType()))) {
			handle(packet);
		} else {
			log.debug("route(), failed! session="+session);
		}
	}

	private void handle(Message packet) {
		try {
			String type = packet.getType().toString();
			/*if (Type.chat == packet.getType()) {
				type = "chat";
			} else if (Type.groupchat == packet.getType()) {
				type = "groupchat";
			}*/

			log.info("handle(), type="+type);
			if (type == null || type.isEmpty()) {
				log.equals("Unknown packet: " + packet);
			} else {
				MsgHandler handler = getHandler(type);
				if (handler == null) {
					sendErrorPacket(null,
							PacketError.Condition.service_unavailable);
				} else {
					handler.process(packet);
				}
			}

		} catch (Exception e) {
			log.error("Could not route packet", e);
		}
	}

	/**
	 * Senda the error packet to the original sender
	 */
	private void sendErrorPacket(IQ originalPacket,
			PacketError.Condition condition) {
	}

	/**
	 * Adds a new MsgHandler to the list of registered handler.
	 * 
	 * @param handler the MsgHandler
	 */
	public void addHandler(MsgHandler handler) {
		if (msgHandlers.contains(handler)) {
			throw new IllegalArgumentException(
					"MsgHandler already provided by the server");
		}
		log.info("addHandler(), type="+handler.getType());
		type2Handlers.put(handler.getType(), handler);
	}

	/**
	 * Removes an MsgHandler from the list of registered handler.
	 * 
	 * @param handler the MsgHandler
	 */
	public void removeHandler(MsgHandler handler) {
		if (msgHandlers.contains(handler)) {
			throw new IllegalArgumentException(
					"Cannot remove an MsgHandler provided by the server");
		}
		type2Handlers.remove(handler.getType());
	}

	/**
	 * Returns an MsgHandler with the given type.
	 */
	private MsgHandler getHandler(String type) {
		log.info("getHandler(), type="+type);
		MsgHandler handler = type2Handlers.get(type);
		if (handler == null) {
			for (MsgHandler handlerCandidate : msgHandlers) {
				if (type.equalsIgnoreCase(handlerCandidate.getType())) {
					handler = handlerCandidate;
					type2Handlers.put(type, handler);
					break;
				}
			}
		}
		return handler;
	}
}
