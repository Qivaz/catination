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

import java.util.List;

import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import com.catination.server.dao.RosterDao;
import com.catination.server.model.Roster;
import com.catination.server.service.UserNotFoundException;

/**
 * This class is the implementation of UserDAO using Spring's HibernateTemplate.
 * 
 *
 */
public class RosterDaoHibernate extends HibernateDaoSupport implements RosterDao {

	public Roster getRoster(Long id) {
		return (Roster) getHibernateTemplate().get(Roster.class, id);
	}

	public Roster saveRoster(Roster Roster) {
		getHibernateTemplate().saveOrUpdate(Roster);
		getHibernateTemplate().flush();
		return Roster;
	}
	
	public Roster updateRoster(Roster Roster) {
		return saveRoster(Roster);
	}

	public void removeRoster(Long id) {
		getHibernateTemplate().delete(getRoster(id));
	}

	public boolean exists(Long id) {
		Roster Roster = (Roster) getHibernateTemplate().get(Roster.class, id);
		return Roster != null;
	}

	@SuppressWarnings("unchecked")
	public Roster getRosterByUsername(String username) throws UserNotFoundException {
		List roster = getHibernateTemplate().find("from Roster where username=?",
				username);
		if (roster == null || roster.isEmpty()) {
			return null;
			//throw new UserNotFoundException("Roster of '" + username + "' not found");
		} else {
			return (Roster) roster.get(0);
		}
	}

}
