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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataIntegrityViolationException;

import com.catination.server.dao.RosterDao;
import com.catination.server.dao.RosterDao;
import com.catination.server.model.Roster;
import com.catination.server.model.User;
import com.catination.server.service.RosterService;
import com.catination.server.service.UserExistsException;
import com.catination.server.service.UserNotFoundException;
import com.catination.server.service.UserService;

/** 
 * This class is the implementation of UserService.
 *
 *
 */
public class RosterServiceImpl implements RosterService {

    protected final Log log = LogFactory.getLog(getClass());

    private RosterDao rosterDao;

    public void setRosterDao(RosterDao rosterDao) {
        this.rosterDao = rosterDao;
    }

    public Roster getRoster(long rosterId) {
        return rosterDao.getRoster(new Long(rosterId));
    }

    public Roster saveRoster(Roster roster) throws UserExistsException {
        try {
            return rosterDao.saveRoster(roster);
        } catch (DataIntegrityViolationException e) {
            e.printStackTrace();
            log.warn(e.getMessage());
            throw new UserExistsException("Roster of '" + roster.getUsername()
                    + "' already exists!");
        } catch (EntityExistsException e) { // needed for JPA
            e.printStackTrace();
            log.warn(e.getMessage());
            throw new UserExistsException("Roster of '" + roster.getUsername()
                    + "' already exists!");
        }
    }

    public Roster updateRoster(Roster roster) {
        try {
            return rosterDao.updateRoster(roster);
        } catch (DataIntegrityViolationException e) {
            e.printStackTrace();
            log.warn(e.getMessage());
        } catch (EntityExistsException e) { // needed for JPA
            e.printStackTrace();
            log.warn(e.getMessage());
        }
		return roster;
    }
    
    public Roster getRosterByUsername(String username) throws UserNotFoundException {
        return (Roster) rosterDao.getRosterByUsername(username);
    }

    public void removeRoster(Long rosterId) {
        log.debug("removing roster of: " + rosterId);
        rosterDao.removeRoster(rosterId);
    }

}
