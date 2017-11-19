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
import org.xmpp.packet.HeartBeatMessage;
import org.xmpp.packet.IQ;
import org.xmpp.packet.Packet;
import org.xmpp.packet.PacketError;

import com.catination.server.xmpp.UnauthorizedException;
import com.catination.server.xmpp.router.PacketDeliverer;
import com.catination.server.xmpp.session.ClientSession;
import com.catination.server.xmpp.session.SessionManager;

/** 
 * This is an abstract class to handle routed HB packets.
 *
 *
 */
public class HBHandler {

	protected final Log log = LogFactory.getLog(getClass());

	protected SessionManager sessionManager;

	private static final String NAMESPACE = "heartbeat";
	/**
	 * Constructor.
	 */
	public HBHandler() {
		sessionManager = SessionManager.getInstance();
	}

	/**
	 * Processes the received IQ packet.
	 * 
	 * @param packet the packet
	 */
	public void process(Packet packet) {
		HeartBeatMessage hb = (HeartBeatMessage) packet;
		try {
			ClientSession cs = sessionManager.getSession(hb.getFrom());
			ClientSession.Location loc = null;
			if (hb.isCdma()) {
				loc = new ClientSession.Location(true, hb.getSid(), hb.getNid());
				log.info("ClientSession.Location was updated! [cdma:"+loc.sid+", "+loc.nid+"]");
			} else if (hb.isGsm()) {
				loc = new ClientSession.Location(false, hb.getLac(), hb.getCid());
				log.info("ClientSession.Location was updated! [gsm:"+loc.lac+", "+loc.cid+"]");
			} else {
				loc = new ClientSession.Location(hb.getLon(), hb.getLat());
				log.info("ClientSession.Location was updated! [loc:"+loc.lon+", "+loc.lat+"]");
			}
			cs.setLocation(loc);
		} catch (Exception e) {
			log.error("Internal server error", e);
		}
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
