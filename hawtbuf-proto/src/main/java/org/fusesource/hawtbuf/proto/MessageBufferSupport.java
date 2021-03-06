package org.fusesource.hawtbuf.proto;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import org.fusesource.hawtbuf.Buffer;
import org.fusesource.hawtbuf.BufferOutputStream;


final public class MessageBufferSupport {

    public static final String FORZEN_ERROR_MESSAGE = "Modification not allowed after object has been fozen.  Try modifying a copy of this object.";
    
    static public Buffer toUnframedBuffer(MessageBuffer message) {
        try {
            int size = message.serializedSizeUnframed();
            BufferOutputStream baos = new BufferOutputStream(size);
            CodedOutputStream output = new CodedOutputStream(baos);
            message.writeUnframed(output);
            Buffer rc = baos.toBuffer();
            assert rc.length == size : "Did not write as much data as expected.";
            return rc;
        } catch (IOException e) {
            throw new RuntimeException("Serializing to a byte array threw an IOException " + "(should never happen).", e);
        }
    }

    static public Buffer toFramedBuffer(MessageBuffer message) {
        try {
            int size = message.serializedSizeFramed();
            BufferOutputStream baos = new BufferOutputStream(size);
            CodedOutputStream output = new CodedOutputStream(baos);
            message.writeFramed(output);
            Buffer rc = baos.toBuffer();
            assert rc.length==size : "Did not write as much data as expected.";
            return rc;
        } catch (IOException e) {
            throw new RuntimeException("Serializing to a byte array threw an IOException " + "(should never happen).", e);
        }
    }
    
    public static void writeMessage(CodedOutputStream output, int tag, MessageBuffer message) throws IOException {
        output.writeTag(tag, WireFormat.WIRETYPE_LENGTH_DELIMITED);
        message.writeFramed(output);
    }

    public static int computeMessageSize(int tag, MessageBuffer message) {
        return CodedOutputStream.computeTagSize(tag) + message.serializedSizeFramed();
    }

    public static Buffer readFrame(java.io.InputStream input) throws IOException {
        int length = 0;
        try {
            length = readRawVarint32(input);
        } catch (InvalidProtocolBufferException e) {
            if( e.isEOF() ) {
                throw new EOFException();
            } else {
                throw e;
            }
        }

        byte[] data = new byte[length];
        int pos = 0;
        while (pos < length) {
            int r = input.read(data, pos, length - pos);
            if (r < 0) {
                throw new InvalidProtocolBufferException("Input stream ended before a full message frame could be read.");
            }
            pos += r;
        }
        return new Buffer(data);
    }
    
    /**
     * Read a raw Varint from the stream. If larger than 32 bits, discard the
     * upper bits.
     */
    static public int readRawVarint32(InputStream is) throws IOException {
        byte tmp = readRawByte(is);
        if (tmp >= 0) {
            return tmp;
        }
        int result = tmp & 0x7f;
        if ((tmp = readRawByte(is)) >= 0) {
            result |= tmp << 7;
        } else {
            result |= (tmp & 0x7f) << 7;
            if ((tmp = readRawByte(is)) >= 0) {
                result |= tmp << 14;
            } else {
                result |= (tmp & 0x7f) << 14;
                if ((tmp = readRawByte(is)) >= 0) {
                    result |= tmp << 21;
                } else {
                    result |= (tmp & 0x7f) << 21;
                    result |= (tmp = readRawByte(is)) << 28;
                    if (tmp < 0) {
                        // Discard upper 32 bits.
                        for (int i = 0; i < 5; i++) {
                            if (readRawByte(is) >= 0)
                                return result;
                        }
                        throw new InvalidProtocolBufferException("CodedInputStream encountered a malformed varint.");
                    }
                }
            }
        }
        return result;
    }
    
    static public byte readRawByte(InputStream is) throws IOException {
        int rc = is.read();
        if (rc == -1) {
            throw new InvalidProtocolBufferException("While parsing a protocol message, the input ended unexpectedly " + "in the middle of a field.  This could mean either than the " + "input has been truncated or that an embedded message "
                    + "misreported its own length.", true);
        }
        return (byte) rc;
    }
    
    static public <T> void addAll(Iterable<T> values, Collection<? super T> list) {
        if (values instanceof Collection) {
            @SuppressWarnings("unsafe")
            Collection<T> collection = (Collection<T>) values;
            list.addAll(collection);
        } else {
            for (T value : values) {
                list.add(value);
            }
        }
    }


}
