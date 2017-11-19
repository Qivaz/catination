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
package com.catination.server.dao.hibernate;

import java.util.Date;
import java.util.List;

import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import com.catination.server.dao.MessageDao;
import com.catination.server.model.Msg;
import com.catination.server.service.MessageNotFoundException;

/**
 * This class is the implementation of UserDAO using Spring's HibernateTemplate.
 * 
 *
 */
public class MessageDaoHibernate extends HibernateDaoSupport implements MessageDao {

	public Msg getMessage(Long id) {
		// TODO Auto-generated method stub
		return (Msg) getHibernateTemplate().get(Msg.class, id);
	}

	public Msg saveMessage(Msg msg) {
		// TODO Auto-generated method stub
		//getHibernateTemplate().
		getHibernateTemplate().saveOrUpdate(msg);
		getHibernateTemplate().flush();
		return msg;
	}

	public void removeMessage(Long id) {
		// TODO Auto-generated method stub
		getHibernateTemplate().delete(getMessage(id));
	}
	
	public boolean exists(Long id) {
		// TODO Auto-generated method stub
		Msg msg = (Msg) getHibernateTemplate().get(Msg.class, id);
		return msg != null;
	}
	
	@SuppressWarnings("unchecked")
	public List<Msg> getMessages() {
		// TODO Auto-generated method stub
		return getHibernateTemplate().find(
				"from Msg m order by m.createdDate desc");
	}

	@SuppressWarnings("unchecked")
	public List<Msg> getMessagesFromCreatedDate(Date createDate) {
		// TODO Auto-generated method stub
		return getHibernateTemplate()
				.find("from Msg m where m.createdDate >= ? order by m.createdDate desc",
						createDate);
	}

	@SuppressWarnings("unchecked")
	public List<Msg> getMessagesByUser(String username)
			throws MessageNotFoundException {
		// TODO Auto-generated method stub
		@SuppressWarnings("rawtypes")
		List messages = getHibernateTemplate().find("from Msg where user_from=?||user_to=?",
				username);
		if (messages == null || messages.isEmpty()) {
			throw new MessageNotFoundException("Message of '" + username + "' not found");
		} else {
			return messages;
		}
	}
	
	@SuppressWarnings("unchecked")
	public List<Msg> getMessagesByFrom(String username)
			throws MessageNotFoundException {
		// TODO Auto-generated method stub
		@SuppressWarnings("rawtypes")
		List messages = getHibernateTemplate().find("from Msg where user_from=?",
				username);
		if (messages == null || messages.isEmpty()) {
			throw new MessageNotFoundException("Message of '" + username + "' not found");
		} else {
			return messages;
		}
	}
	
	@SuppressWarnings("unchecked")
	public List<Msg> getMessagesByTo(String username)
			throws MessageNotFoundException {
		// TODO Auto-generated method stub
		@SuppressWarnings("rawtypes")
		List messages = getHibernateTemplate().find("from Msg where user_to=?",
				username);
		if (messages == null || messages.isEmpty()) {
			throw new MessageNotFoundException("Message of '" + username + "' not found");
		} else {
			return messages;
		}
	}

	public List<Msg> getMessagesByKeyword(String keyword)
			throws MessageNotFoundException {
		// TODO Auto-generated method stub
		return null;
	}
	
	// @SuppressWarnings("unchecked")
	// public User findUserByUsername(String username) {
	// List users = getHibernateTemplate().find("from User where username=?",
	// username);
	// return (users == null || users.isEmpty()) ? null : (User) users.get(0);
	// }

}
