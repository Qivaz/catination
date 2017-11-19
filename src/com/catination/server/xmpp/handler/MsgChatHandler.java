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

import java.util.Date;

import org.xmpp.packet.Message;
import org.xmpp.packet.Message.Type;

import com.catination.server.model.Msg;
import com.catination.server.service.MessageService;
import com.catination.server.service.ServiceLocator;
import com.catination.server.service.UserNotFoundException;
import com.catination.server.xmpp.UnauthorizedException;
import com.catination.server.xmpp.session.ClientSession;


/** 
 * This class is to handle the TYPE_IQ jabber:iq:register protocol.
 *
 *
 */
public class MsgChatHandler extends MsgHandler {

	private static final String TYPE = "chat";
	private MessageService messageService;


	/**
	 * Constructor.
	 */
	public MsgChatHandler() {
		messageService = ServiceLocator.getMessageService();
	}

	/**
	 * Handles the received Message packet.
	 * 
	 * @param packet the packet
	 * @return the response to send back
	 * @throws UnauthorizedException if the message is not authorized
	 */
	public Message handleMessage(Message packet) throws UnauthorizedException {
		log.info("packet="+packet);
		ClientSession session = sessionManager.getSession(packet.getFrom());
		String from = null;
		try {
			from = session.getUsername();
		} catch (UserNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String to = packet.getTo().toString();

		Msg msg = new Msg();
		msg.setMessage(packet.getBody());
		msg.setFrom(from);
		msg.setTo(to);
		msg.setType(Type.chat);
		log.debug("msg[chat]="+msg);
		messageService.saveMessage(msg);

		// Directly delivering to the destination users.
		messageService.deliverMessage(packet);

		return null;
	}

	/**
	 * Returns the namespace of the handler.
	 * 
	 * @return the namespace
	 */
	public String getType() {
		return TYPE;
	}

}
