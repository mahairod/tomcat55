/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.tomcat.websocket;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;

import javax.servlet.ServletInputStream;
import javax.websocket.CloseReason;
import javax.websocket.CloseReason.CloseCodes;
import javax.websocket.MessageHandler;
import javax.websocket.PongMessage;

import org.apache.tomcat.util.res.StringManager;

/**
 * Takes the ServletInputStream, processes the WebSocket frames it contains and
 * extracts the messages. WebSocket Pings received will be responded to
 * automatically without any action required by the application.
 */
public class WsFrame {

    private static final StringManager sm =
            StringManager.getManager(Constants.PACKAGE_NAME);

    // Connection level attributes
    private final ServletInputStream sis;
    private final WsSession wsSession;
    private final byte[] inputBuffer;

    // Attributes for control messages
    // Control messages can appear in the middle of other messages so need
    // separate attributes
    private final ByteBuffer controlBufferBinary = ByteBuffer.allocate(125);
    private final CharBuffer controlBufferText = CharBuffer.allocate(125);

    // Attributes of the current message
    private final ByteBuffer messageBufferBinary;
    private final CharBuffer messageBufferText;
    private final CharsetDecoder utf8DecoderControl = new Utf8Decoder().
            onMalformedInput(CodingErrorAction.REPORT).
            onUnmappableCharacter(CodingErrorAction.REPORT);
    private final CharsetDecoder utf8DecoderMessage = new Utf8Decoder().
            onMalformedInput(CodingErrorAction.REPORT).
            onUnmappableCharacter(CodingErrorAction.REPORT);
    private boolean continuationExpected = false;
    private boolean textMessage = false;

    // Attributes of the current frame
    private boolean fin = false;
    private int rsv = 0;
    private byte opCode = 0;
    private byte[] mask = new byte[4];
    private int maskIndex = 0;
    private long payloadLength = 0;
    private long payloadWritten = 0;

    // Attributes tracking state
    private State state = State.NEW_FRAME;
    private int readPos = 0;
    private int writePos = 0;

    public WsFrame(ServletInputStream sis, WsSession wsSession) {
        this.sis = sis;
        this.wsSession = wsSession;

        int readBufferSize =
                ServerContainerImpl.getServerContainer().getReadBufferSize();

        inputBuffer = new byte[readBufferSize];
        messageBufferBinary = ByteBuffer.allocate(readBufferSize);
        messageBufferText = CharBuffer.allocate(readBufferSize);
    }


    /**
     * Called when there is data in the ServletInputStream to process.
     */
    public void onDataAvailable() throws IOException {
        synchronized (sis) {
            while (sis.isReady()) {
                // Fill up the input buffer with as much data as we can
                int read = sis.read(inputBuffer, writePos,
                        inputBuffer.length - writePos);
                if (read == 0) {
                    return;
                }
                if (read == -1) {
                    throw new EOFException();
                }
                writePos += read;
                while (true) {
                    if (state == State.NEW_FRAME) {
                        if (!processInitialHeader()) {
                            break;
                        }
                    }
                    if (state == State.PARTIAL_HEADER) {
                        if (!processRemainingHeader()) {
                            break;
                        }
                    }
                    if (state == State.DATA) {
                        if (!processData()) {
                            break;
                        }
                    }
                }
            }
        }
    }


    /**
     * @return <code>true</code> if sufficient data was present to process all
     *         of the initial header
     */
    private boolean processInitialHeader() throws IOException {
        // Need at least two bytes of data to do this
        if (writePos - readPos < 2) {
            return false;
        }
        int b = inputBuffer[readPos++];
        fin = (b & 0x80) > 0;
        rsv = (b & 0x70) >>> 4;
        if (rsv != 0) {
            // TODO Extensions may use rsv bits
            throw new WsIOException(new CloseReason(
                    CloseCodes.PROTOCOL_ERROR,
                    sm.getString("wsFrame.wrongRsv", Integer.valueOf(rsv))));
        }
        opCode = (byte) (b & 0x0F);
        if (isControl()) {
            if (!fin) {
                throw new WsIOException(new CloseReason(
                        CloseCodes.PROTOCOL_ERROR,
                        sm.getString("wsFrame.controlFragmented")));
            }
            if (opCode != Constants.OPCODE_PING &&
                    opCode != Constants.OPCODE_PONG &&
                    opCode != Constants.OPCODE_CLOSE) {
                throw new WsIOException(new CloseReason(
                        CloseCodes.PROTOCOL_ERROR,
                        sm.getString("wsFrame.invalidOpCode",
                                Integer.valueOf(opCode))));
            }
        } else {
            if (continuationExpected) {
                if (opCode != Constants.OPCODE_CONTINUATION) {
                    throw new WsIOException(new CloseReason(
                            CloseCodes.PROTOCOL_ERROR,
                            sm.getString("wsFrame.noContinuation")));
                }
            } else {
                if (opCode == Constants.OPCODE_BINARY) {
                    textMessage = false;
                } else if (opCode == Constants.OPCODE_TEXT) {
                    textMessage = true;
                } else {
                    throw new WsIOException(new CloseReason(
                            CloseCodes.PROTOCOL_ERROR,
                            sm.getString("wsFrame.invalidOpCode",
                                    Integer.valueOf(opCode))));
                }
            }
            continuationExpected = !fin;
        }
        b = inputBuffer[readPos++];
        // Client data must be masked
        if ((b & 0x80) == 0) {
            throw new WsIOException(new CloseReason(
                    CloseCodes.PROTOCOL_ERROR,
                    sm.getString("wsFrame.notMasked")));
        }
        payloadLength = b & 0x7F;
        state = State.PARTIAL_HEADER;
        return true;
    }


    /**
     * @return <code>true</code> if sufficient data was present to complete the
     *         processing of the header
     */
    private boolean processRemainingHeader() throws IOException {
        // Ignore the 2 bytes already read. 4 for the mask
        int headerLength = 4;
        // Add additional bytes depending on length
        if (payloadLength == 126) {
            headerLength += 2;
        } else if (payloadLength == 127) {
            headerLength += 8;
        }
        if (writePos - readPos < headerLength) {
            return false;
        }
        // Calculate new payload length if necessary
        if (payloadLength == 126) {
            payloadLength = byteArrayToLong(inputBuffer, readPos, 2);
            readPos += 2;
        } else if (payloadLength == 127) {
            payloadLength = byteArrayToLong(inputBuffer, readPos, 8);
            readPos += 8;
        }
        if (isControl()) {
            if (payloadLength > 125) {
                throw new WsIOException(new CloseReason(
                        CloseCodes.PROTOCOL_ERROR,
                        sm.getString("wsFrame.controlPayloadTooBig",
                                Long.valueOf(payloadLength))));
            }
            if (!fin) {
                throw new WsIOException(new CloseReason(
                        CloseCodes.PROTOCOL_ERROR,
                        sm.getString("wsFrame.controlNoFin")));
            }
        }
        System.arraycopy(inputBuffer, readPos, mask, 0, 4);
        readPos += 4;
        state = State.DATA;
        return true;
    }


    private boolean processData() throws IOException {
        checkRoomPayload();
        if (isControl()) {
            return processDataControl();
        } else if (textMessage) {
            return processDataText();
        } else {
            return processDataBinary();
        }
    }


    private boolean processDataControl() throws IOException {
        if (!appendPayloadToMessage(controlBufferBinary)) {
            return false;
        }
        controlBufferBinary.flip();
        if (opCode == Constants.OPCODE_CLOSE) {
            String reason = null;
            int code = CloseCodes.NORMAL_CLOSURE.getCode();
            if (controlBufferBinary.remaining() == 1) {
                controlBufferBinary.clear();
                // Payload must be zero or greater than 2
                throw new WsIOException(new CloseReason(
                        CloseCodes.PROTOCOL_ERROR,
                        sm.getString("wsFrame.oneByteCloseCode")));
            }
            if (controlBufferBinary.remaining() > 1) {
                code = controlBufferBinary.getShort();
                if (controlBufferBinary.remaining() > 0) {
                    CoderResult cr = utf8DecoderControl.decode(
                            controlBufferBinary, controlBufferText, true);
                    if (cr.isError()) {
                        controlBufferBinary.clear();
                        controlBufferText.clear();
                        throw new WsIOException(new CloseReason(
                                CloseCodes.PROTOCOL_ERROR,
                                sm.getString("wsFrame.invalidUtf8Close")));
                    }
                    // There will be no overflow as the output buffer is big
                    // enough. There will be no underflow as all the data is
                    // passed to the decoder in a single call.
                    reason = controlBufferText.toString();
                }
            }
            wsSession.onClose(
                    new CloseReason(Util.getCloseCode(code), reason));
        } else if (opCode == Constants.OPCODE_PING) {
            wsSession.getRemote().sendPong(controlBufferBinary);
        } else if (opCode == Constants.OPCODE_PONG) {
            MessageHandler.Basic<PongMessage> mhPong =
                    wsSession.getPongMessageHandler();
            if (mhPong != null) {
                mhPong.onMessage(new WsPongMessage(controlBufferBinary));
            }
        } else {
            // Should have caught this earlier but just in case...
            controlBufferBinary.clear();
            throw new WsIOException(new CloseReason(
                    CloseCodes.PROTOCOL_ERROR,
                    sm.getString("wsFrame.invalidOpCode",
                            Integer.valueOf(opCode))));
        }
        controlBufferBinary.clear();
        newFrame();
        return true;
    }


    @SuppressWarnings("unchecked")
    private void sendMessageText(boolean last) {
        MessageHandler mh = wsSession.getTextMessageHandler();
        if (mh != null) {
            if (mh instanceof MessageHandler.Async<?>) {
                ((MessageHandler.Async<String>) mh).onMessage(
                        messageBufferText.toString(), last);
            } else {
                ((MessageHandler.Basic<String>) mh).onMessage(
                        messageBufferText.toString());
            }
            messageBufferText.clear();
        }
    }


    private boolean processDataText() throws IOException {
        // Copy the available data to the buffer
        while (!appendPayloadToMessage(messageBufferBinary)) {
            // Frame not complete - we ran out of something
            // Convert bytes to UTF-8
            messageBufferBinary.flip();
            while (true) {
                CoderResult cr = utf8DecoderMessage.decode(
                        messageBufferBinary, messageBufferText, false);
                if (cr.isError()) {
                    throw new WsIOException(new CloseReason(
                            CloseCodes.NOT_CONSISTENT,
                            sm.getString("wsFrame.invalidUtf8")));
                } else if (cr.isOverflow()) {
                    // Ran out of space in text buffer - flush it
                    if (usePartial()) {
                        messageBufferText.flip();
                        sendMessageText(false);
                        messageBufferText.clear();
                    } else {
                        throw new WsIOException(new CloseReason(
                                CloseCodes.TOO_BIG,
                                sm.getString("wsFrame.textMessageTooBig")));
                    }
                } else if (cr.isUnderflow()) {
                    // Need more input
                    // Compact what we have to create as much space as possible
                    messageBufferBinary.compact();

                    // What did we run out of?
                    if (readPos == writePos) {
                        // Ran out of input data - get some more
                        return false;
                    } else {
                        // Ran out of message buffer - exit inner loop and
                        // refill
                        break;
                    }
                }
            }
        }

        messageBufferBinary.flip();
        boolean last = false;
        // Frame is fully received
        // Convert bytes to UTF-8
        while (true) {
            CoderResult cr = utf8DecoderMessage.decode(messageBufferBinary,
                    messageBufferText, last);
            if (cr.isError()) {
                throw new WsIOException(new CloseReason(
                        CloseCodes.NOT_CONSISTENT,
                        sm.getString("wsFrame.invalidUtf8")));
            } else if (cr.isOverflow()) {
                // Ran out of space in text buffer - flush it
                if (usePartial()) {
                    messageBufferText.flip();
                    sendMessageText(false);
                    messageBufferText.clear();
                } else {
                    throw new WsIOException(new CloseReason(
                            CloseCodes.TOO_BIG,
                            sm.getString("wsFrame.textMessageTooBig")));
                }
            } else if (cr.isUnderflow() & !last) {
                // End of frame and possible message as well.

                if (continuationExpected) {
                    messageBufferBinary.compact();
                    newFrame();
                    // Process next frame
                    return true;
                } else {
                    // Make sure coder has flushed all output
                    last = true;
                }
            } else {
                // End of message
                messageBufferText.flip();
                sendMessageText(true);

                newMessage();
                return true;
            }
        }
    }


    private boolean processDataBinary() {
        // Copy the available data to the buffer
        while (!appendPayloadToMessage(messageBufferBinary)) {
            // Frame not complete - what did we run out of?
            if (readPos == writePos) {
                // Ran out of input data - get some more
                return false;
            } else {
                // Ran out of message buffer - flush it
                messageBufferBinary.flip();
                sendMessageBinary(false);
                messageBufferBinary.clear();
            }
        }

        // Frame is fully received
        if (continuationExpected) {
            // More data for this message expected
            newFrame();
        } else {
            // Message is complete - send it
            messageBufferBinary.flip();
            sendMessageBinary(true);
            messageBufferBinary.clear();
            newMessage();
        }

        return true;
    }


    @SuppressWarnings("unchecked")
    private void sendMessageBinary(boolean last) {
        MessageHandler mh = wsSession.getBinaryMessageHandler();
        if (mh != null) {
            if (mh instanceof MessageHandler.Async<?>) {
                ((MessageHandler.Async<ByteBuffer>) mh).onMessage(
                        messageBufferBinary, last);
            } else {
                ((MessageHandler.Basic<ByteBuffer>) mh).onMessage(
                        messageBufferBinary);
            }
        }
    }

    private void newMessage() {
        messageBufferBinary.clear();
        messageBufferText.clear();
        utf8DecoderMessage.reset();
        continuationExpected = false;
        newFrame();
    }


    private void newFrame() {
        if (readPos == writePos) {
            readPos = 0;
            writePos = 0;
        }

        maskIndex = 0;
        payloadWritten = 0;
        state = State.NEW_FRAME;

        // These get reset in processInitialHeader()
        // fin, rsv, opCode, payloadLength, mask

        checkRoomHeaders();
    }


    private void checkRoomHeaders() {
        // Is the start of the current frame too near the end of the input
        // buffer?
        if (inputBuffer.length - readPos < 131) {
            // Limit based on a control frame with a full payload
            makeRoom();
        }
    }


    private void checkRoomPayload() throws IOException {
        if (inputBuffer.length - readPos - payloadLength + payloadWritten < 0) {
            if (isControl()) {
                makeRoom();
                return;
            }
            if (!usePartial() && (inputBuffer.length < payloadLength)) {
                // TODO i18n - buffer too small
                CloseReason cr = new CloseReason(CloseCodes.TOO_BIG,
                        "Buffer size: [" + inputBuffer.length +
                        "], payload size: [" + payloadLength + "]");
                wsSession.close(cr);
                wsSession.onClose(cr);
                throw new IOException(cr.getReasonPhrase());
            }
            makeRoom();
        }
    }


    private void makeRoom() {
        System.arraycopy(inputBuffer, readPos, inputBuffer, 0,
                writePos - readPos);
        writePos = writePos - readPos;
        readPos = 0;
    }


    private boolean usePartial() {
        if (isControl()) {
            return false;
        } else if (textMessage) {
            MessageHandler mh = wsSession.getTextMessageHandler();
            if (mh != null) {
                return mh instanceof MessageHandler.Async<?>;
            }
            return false;
        } else {
            // Must be binary
            MessageHandler mh = wsSession.getBinaryMessageHandler();
            if (mh != null) {
                return mh instanceof MessageHandler.Async<?>;
            }
            return false;
        }
    }


    private boolean appendPayloadToMessage(ByteBuffer dest) {
        while (payloadWritten < payloadLength && readPos < writePos &&
                dest.hasRemaining()) {
            byte b = (byte) ((inputBuffer[readPos] ^ mask[maskIndex]) & 0xFF);
            maskIndex++;
            if (maskIndex == 4) {
                maskIndex = 0;
            }
            readPos++;
            payloadWritten++;
            dest.put(b);
        }
        return (payloadWritten == payloadLength);
    }


    protected static long byteArrayToLong(byte[] b, int start, int len)
            throws IOException {
        if (len > 8) {
            throw new IOException(sm.getString("wsFrame.byteToLongFail",
                    Long.valueOf(len)));
        }
        int shift = 0;
        long result = 0;
        for (int i = start + len - 1; i >= start; i--) {
            result = result + ((b[i] & 0xFF) << shift);
            shift += 8;
        }
        return result;
    }


    private boolean isControl() {
        return (opCode & 0x08) > 0;
    }


    private static enum State {
        NEW_FRAME, PARTIAL_HEADER, DATA
    }
}
