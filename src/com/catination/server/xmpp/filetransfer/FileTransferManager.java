package com.catination.server.xmpp.filetransfer;


public interface FileTransferManager {
    /**
     * The Stream Initiation, SI, namespace.
     */
    static final String NAMESPACE_SI = "http://jabber.org/protocol/si";

    /**
     * Namespace for the file transfer profile of Stream Initiation.
     */
    static final String NAMESPACE_SI_FILETRANSFER =
            "http://jabber.org/protocol/si/profile/file-transfer";

    /**
     * Bytestreams namespace
     */
    static final String NAMESPACE_BYTESTREAMS = "http://jabber.org/protocol/bytestreams";

}