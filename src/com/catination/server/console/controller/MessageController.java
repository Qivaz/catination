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
package com.catination.server.console.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

import com.catination.server.model.Msg;
import com.catination.server.service.MessageService;
import com.catination.server.service.ServiceLocator;

/** 
 * A controller class to process the msg related requests.  
 *
 *
 */
public class MessageController extends MultiActionController {

    private MessageService messageService;

    public MessageController() {
        messageService = ServiceLocator.getMessageService();
    }

    public ModelAndView list(HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        List<Msg> msgList = messageService.getMessages();
        ModelAndView mav = new ModelAndView();
        mav.addObject("msgList", msgList);
        mav.setViewName("msg/list");
        return mav;
    }

}
