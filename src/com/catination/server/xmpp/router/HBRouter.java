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
import org.xmpp.packet.HeartBeatMessage;
import org.xmpp.packet.IQ;
import org.xmpp.packet.JID;
import org.xmpp.packet.PacketError;

import com.catination.server.xmpp.handler.HBHandler;
import com.catination.server.xmpp.session.ClientSession;
import com.catination.server.xmpp.session.Session;
import com.catination.server.xmpp.session.SessionManager;

/** 
 * This class is to route IQ packets to their corresponding handler.
 *
 *
 */
public class HBRouter {

	private final Log log = LogFactory.getLog(getClass());

	private SessionManager sessionManager;

	private List<HBHandler> hbHandlers = new ArrayList<HBHandler>();

	private Map<String, HBHandler> namespace2Handlers = new ConcurrentHashMap<String, HBHandler>();

	/**
	 * Constucts a packet router registering new IQ handlers.
	 */
	public HBRouter() {
		sessionManager = SessionManager.getInstance();
		hbHandlers.add(new HBHandler());
	}

	/**
	 * Routes the IQ packet based on its namespace.
	 * 
	 * @param packet the packet to route
	 */
	public void route(HeartBeatMessage packet) {
		if (packet == null) {
			throw new NullPointerException();
		}
		JID sender = packet.getFrom();
		ClientSession session = sessionManager.getSession(sender);

		handle(packet);
	}

	private void handle(HeartBeatMessage packet) {
		HBHandler handler = hbHandlers.get(0);
		if (handler == null) {
			log.error("handler = " + handler);
		} else {
			handler.process(packet);
		}
	}

}
