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
package com.catination.server.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.xmpp.packet.Message.Type;

/** 
 * This class represents the basic user object.
 *
 *
 */
@Entity
@Table(name = "catination_messages")
public class Msg implements Serializable {

    private static final long serialVersionUID = 4733464888738356503L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "message", nullable = false, length = 128)
    private String message;

    @Column(name = "created_date", updatable = false)
    private Date createdDate = new Date();

    @Column(name = "type")
    private Type type;
    
    @Column(name = "user_from", length = 64)
    private String from;

    @Column(name = "user_to", length = 64)
    private String to;

    @Transient
    private boolean old;

    public Msg() {
    }

    public Msg(final String msg) {
        this.message = msg;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    
    public void setMessage(String msg) {
        this.message = msg;
    }

    public String getMessage() {
        return message;
    }

    public void setFrom(String from) {
        this.from = from;
    }
    
    public String getFrom() {
        return this.from;
    }
    
    /** 
     * If field type is Type.groupchat, field to is for group name.
     * Otherwise, for user name.
     */
    public void setTo(String to) {
        this.to = to;
    }
    
    /** 
     * If field type is Type.groupchat, field to is for group name.
     * Otherwise, for user name.
     */
    public String getTo() {
        return this.to;
    }
    
    public void setType(Type type) {
        this.type = type;
    }

    public Type getType() {
        return this.type;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public boolean isOld() {
        return old;
    }

    public void setOld(boolean old) {
        this.old = old;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Msg)) {
            return false;
        }

        final Msg obj = (Msg) o;
        if (from != null ? !from.equals(obj.from)
                : obj.from != null) {
            return false;
        }
        if (to != null ? !to.equals(obj.to)
                : obj.to != null) {
            return false;
        }
        if (message != null ? !message.equals(obj.message)
                : obj.message != null) {
            return false;
        }
        if (createdDate.getTime() != obj.createdDate.getTime()) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = 0;
        result = 29 * result + (from != null ? from.hashCode() : 0);
        result = 29 * result + (to != null ? to.hashCode() : 0);
        result = 29 * result + (message != null ? message.hashCode() : 0);
        result = 29 * result + (createdDate != null ? createdDate.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this,
                ToStringStyle.MULTI_LINE_STYLE);
    }

}
