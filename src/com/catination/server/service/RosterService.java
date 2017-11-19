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

import javax.persistence.EntityExistsException;

import org.springframework.dao.DataIntegrityViolationException;

import com.catination.server.dao.RosterDao;
import com.catination.server.model.Roster;
import com.catination.server.model.User;

/** 
 * Business service interface for the user management.
 *
 *
 */
public interface RosterService {

    public void setRosterDao(RosterDao rosterDao);

    public Roster getRoster(long rosterId);

    public Roster saveRoster(Roster roster) throws UserExistsException;

    public Roster updateRoster(Roster roster);
    
    public Roster getRosterByUsername(String username) throws UserNotFoundException;

    public void removeRoster(Long rosterId);
}
