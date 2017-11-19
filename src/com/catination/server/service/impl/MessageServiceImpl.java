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
package com.catination.server.service.impl;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityExistsException;

//import com.catination.server.service.MessageExistsException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.xmpp.packet.Message;

import com.catination.server.dao.MessageDao;
import com.catination.server.model.Msg;
import com.catination.server.service.MessageNotFoundException;
import com.catination.server.service.MessageService;
import com.catination.server.xmpp.push.MessageManager;

/** 
 * This class is the implementation of MessageService.
 *
 *
 */
public class MessageServiceImpl implements MessageService {

	protected final Log log = LogFactory.getLog(getClass());

	private MessageDao messageDao;
	
	private MessageManager messageMgr = new MessageManager();

	public void setMessageDao(MessageDao messageDao) {
		this.messageDao = messageDao;
	}

	public Msg getMessage(String messageId) {
		return messageDao.getMessage(new Long(messageId));
	}

	public List<Msg> getMessages() {
		return messageDao.getMessages();
	}

	public List<Msg> getMessagesFromCreatedDate(Date createDate) {
		return messageDao.getMessagesFromCreatedDate(createDate);
	}

	public Msg saveMessage(Msg message) {
		log.debug("saveMessage(), " + message);
		try {
			return messageDao.saveMessage(message);
		} catch (DataIntegrityViolationException e) {
			e.printStackTrace();
			log.warn(e.getMessage());
		} catch (EntityExistsException e) { // needed for JPA
			e.printStackTrace();
			log.warn(e.getMessage());
		}
		return message;
	}

	public Message deliverMessage(Message message) {
		log.debug("deliverMessage(), " + message);
		
		deliverMessage(message, true);
		return message;
	}
	
	public Message deliverMessage(Message message, boolean reassemble) {
		log.debug("deliverMessage(), " + message + ", reassemble="+reassemble);
		
		if (reassemble) {
			messageMgr.sendMessage(message.getTo().toString(), message.getFrom().toString(), message.getType(), message.getBody(), message.getThread());
		} else {
			messageMgr.sendMessage(message);
		}
		return message;
	}

	public List<Msg> getMessageByUser(String username) throws MessageNotFoundException {
		return messageDao.getMessagesByUser(username);
	}

	public void removeMessage(Long messageId) {
		log.debug("removing message: " + messageId);
		messageDao.removeMessage(messageId);
	}

}
