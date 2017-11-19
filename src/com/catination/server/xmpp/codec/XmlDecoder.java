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
package com.catination.server.xmpp.codec;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.jivesoftware.openfire.nio.XMLLightweightParser;

import com.catination.server.xmpp.net.XmppIoHandler;

/** 
 * Decoder class that parses ByteBuffers and generates XML stanzas.
 *
 *
 */
public class XmlDecoder extends CumulativeProtocolDecoder {

    private final Log log = LogFactory.getLog(XmlDecoder.class);

    @Override
    public boolean doDecode(IoSession session, IoBuffer in,
            ProtocolDecoderOutput out) {
        log.debug("<XML>doDecode()...");

        XMLLightweightParser parser = (XMLLightweightParser) session
                .getAttribute(XmppIoHandler.XML_PARSER);
        
        try {
			parser.read(in);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			log.debug("<XML>doDecode(), "+e);
			log.debug("<XML>doDecode(), parser="+parser);
			log.debug("<XML>doDecode(), in="+in);
			return false;
		}

        log.debug("<XML>doDecode(), parser.areThereMsgs()="+parser.areThereMsgs());
        if (parser.areThereMsgs()) {
        	log.debug("<XML>doDecode(), out:"+out.toString());
            for (String stanza : parser.getMsgs()) {
                out.write(stanza);
                log.debug("<XML>doDecode(), out.write(\""+stanza+"\")");
            }
        }
        return !in.hasRemaining();
    }

}
