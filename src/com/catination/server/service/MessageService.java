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
package com.catination.server.service;

import java.util.Date;
import java.util.List;

import org.xmpp.packet.Message;

import com.catination.server.model.Msg;

/** 
 * Business service interface for the user management.
 *
 *
 */
public interface MessageService {

	public Msg getMessage(String msgId);

	public List<Msg> getMessages();

	public List<Msg> getMessagesFromCreatedDate(Date createDate);

	public Msg saveMessage(Msg msg);

	public Message deliverMessage(Message message);
	
	public Message deliverMessage(Message message, boolean reassemble);

	public List<Msg> getMessageByUser(String username) throws MessageNotFoundException;

	public void removeMessage(Long msgId);

}
