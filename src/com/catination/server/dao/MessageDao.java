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
package com.catination.server.dao;

import java.util.Date;
import java.util.List;

import com.catination.server.model.Msg;
import com.catination.server.service.MessageNotFoundException;

/** 
 * User DAO (Data Access Object) interface. 
 *
 *
 */
public interface MessageDao {

    public Msg getMessage(Long id);

    public Msg saveMessage(Msg msg);

    public void removeMessage(Long id);

    public boolean exists(Long id);
    
    public List<Msg> getMessages();
    
    public List<Msg> getMessagesFromCreatedDate(Date createDate);

    public List<Msg> getMessagesByUser(String username) throws MessageNotFoundException;

    public List<Msg> getMessagesByKeyword(String keyword) throws MessageNotFoundException;

    public List<Msg> getMessagesByTo(String username) throws MessageNotFoundException;

    public List<Msg> getMessagesByFrom(String username)	throws MessageNotFoundException;

}
