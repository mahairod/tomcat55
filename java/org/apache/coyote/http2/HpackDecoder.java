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
package org.apache.coyote.http2;

import java.nio.ByteBuffer;

import org.apache.tomcat.util.res.StringManager;

/**
 * A decoder for HPACK.
 */
public class HpackDecoder {

    protected static final StringManager sm = StringManager.getManager(HpackDecoder.class);

    private static final int DEFAULT_RING_BUFFER_SIZE = 10;

    /**
     * The object that receives the headers that are emitted from this decoder
     */
    private HeaderEmitter headerEmitter;

    /**
     * The header table
     */
    private Hpack.HeaderField[] headerTable;

    /**
     * The current HEAD position of the header table. We use a ring buffer type
     * construct as it would be silly to actually shuffle the items around in the
     * array.
     */
    private int firstSlotPosition = 0;

    /**
     * The current table size by index (aka the number of index positions that are filled up)
     */
    private int filledTableSlots = 0;

    /**
     * the current calculates memory size, as per the HPACK algorithm
     */
    private int currentMemorySize = 0;

    /**
     * The maximum allowed memory size
     */
    private int maxMemorySize;

    private final StringBuilder stringBuilder = new StringBuilder();

    HpackDecoder(int maxMemorySize) {
        this.maxMemorySize = maxMemorySize;
        headerTable = new Hpack.HeaderField[DEFAULT_RING_BUFFER_SIZE];
    }

    HpackDecoder() {
        this(Hpack.DEFAULT_TABLE_SIZE);
    }

    /**
     * Decodes the provided frame data. If this method leaves data in the buffer
     * then this buffer should be compacted so this data is preserved, unless
     * there is no more data in which case this should be considered a protocol error.
     *
     * @param buffer The buffer
     *
     * @throws HpackException If the packed data is not valid
     */
    void decode(ByteBuffer buffer) throws HpackException {
        while (buffer.hasRemaining()) {
            int originalPos = buffer.position();
            byte b = buffer.get();
            if ((b & 0b10000000) != 0) {
                //if the first bit is set it is an indexed header field
                buffer.position(buffer.position() - 1); //unget the byte
                int index = Hpack.decodeInteger(buffer, 7); //prefix is 7
                if (index == -1) {
                    buffer.position(originalPos);
                    return;
                } else if(index == 0) {
                    throw new HpackException(
                            sm.getString("hpackdecoder.zeroNotValidHeaderTableIndex"));
                }
                handleIndex(index);
            } else if ((b & 0b01000000) != 0) {
                //Literal Header Field with Incremental Indexing
                String headerName = readHeaderName(buffer, 6);
                if (headerName == null) {
                    buffer.position(originalPos);
                    return;
                }
                String headerValue = readHpackString(buffer);
                if (headerValue == null) {
                    buffer.position(originalPos);
                    return;
                }
                headerEmitter.emitHeader(headerName, headerValue);
                addEntryToHeaderTable(new Hpack.HeaderField(headerName, headerValue));
            } else if ((b & 0b11110000) == 0) {
                //Literal Header Field without Indexing
                String headerName = readHeaderName(buffer, 4);
                if (headerName == null) {
                    buffer.position(originalPos);
                    return;
                }
                String headerValue = readHpackString(buffer);
                if (headerValue == null) {
                    buffer.position(originalPos);
                    return;
                }
                headerEmitter.emitHeader(headerName, headerValue);
            } else if ((b & 0b11110000) == 0b00010000) {
                //Literal Header Field never indexed
                String headerName = readHeaderName(buffer, 4);
                if (headerName == null) {
                    buffer.position(originalPos);
                    return;
                }
                String headerValue = readHpackString(buffer);
                if (headerValue == null) {
                    buffer.position(originalPos);
                    return;
                }
                headerEmitter.emitHeader(headerName, headerValue);
            } else if ((b & 0b11100000) == 0b00100000) {
                //context update max table size change
                if (!handleMaxMemorySizeChange(buffer, originalPos)) {
                    return;
                }
            } else {
                throw new RuntimeException("Not yet implemented");
            }
        }
    }

    private boolean handleMaxMemorySizeChange(ByteBuffer buffer, int originalPos) throws HpackException {
        buffer.position(buffer.position() - 1); //unget the byte
        int size = Hpack.decodeInteger(buffer, 5);
        if (size == -1) {
            buffer.position(originalPos);
            return false;
        }
        maxMemorySize = size;
        if (currentMemorySize > maxMemorySize) {
            int newTableSlots = filledTableSlots;
            int tableLength = headerTable.length;
            int newSize = currentMemorySize;
            while (newSize > maxMemorySize) {
                int clearIndex = firstSlotPosition;
                firstSlotPosition++;
                if (firstSlotPosition == tableLength) {
                    firstSlotPosition = 0;
                }
                Hpack.HeaderField oldData = headerTable[clearIndex];
                headerTable[clearIndex] = null;
                newSize -= oldData.size;
                newTableSlots--;
            }
            this.filledTableSlots = newTableSlots;
            currentMemorySize = newSize;
        }
        return true;
    }

    private String readHeaderName(ByteBuffer buffer, int prefixLength) throws HpackException {
        buffer.position(buffer.position() - 1); //unget the byte
        int index = Hpack.decodeInteger(buffer, prefixLength);
        if (index == -1) {
            return null;
        } else if (index != 0) {
            return handleIndexedHeaderName(index);
        } else {
            return readHpackString(buffer);
        }
    }

    private String readHpackString(ByteBuffer buffer) throws HpackException {
        if (!buffer.hasRemaining()) {
            return null;
        }
        byte data = buffer.get(buffer.position());

        int length = Hpack.decodeInteger(buffer, 7);
        if (buffer.remaining() < length) {
            return null;
        }
        boolean huffman = (data & 0b10000000) != 0;
        if (huffman) {
            return readHuffmanString(length, buffer);
        }
        for (int i = 0; i < length; ++i) {
            stringBuilder.append((char) buffer.get());
        }
        String ret = stringBuilder.toString();
        stringBuilder.setLength(0);
        return ret;
    }

    private String readHuffmanString(int length, ByteBuffer buffer) throws HpackException {
        HPackHuffman.decode(buffer, length, stringBuilder);
        String ret = stringBuilder.toString();
        stringBuilder.setLength(0);
        return ret;
    }

    private String handleIndexedHeaderName(int index) throws HpackException {
        if (index <= Hpack.STATIC_TABLE_LENGTH) {
            return Hpack.STATIC_TABLE[index].name;
        } else {
            if (index >= Hpack.STATIC_TABLE_LENGTH + filledTableSlots) {
                throw new HpackException();
            }
            int adjustedIndex = getRealIndex(index - Hpack.STATIC_TABLE_LENGTH);
            Hpack.HeaderField res = headerTable[adjustedIndex];
            if (res == null) {
                throw new HpackException();
            }
            return res.name;
        }
    }

    /**
     * Handle an indexed header representation
     *
     * @param index The index
     * @throws HpackException
     */
    private void handleIndex(int index) throws HpackException {
        if (index <= Hpack.STATIC_TABLE_LENGTH) {
            addStaticTableEntry(index);
        } else {
            int adjustedIndex = getRealIndex(index - Hpack.STATIC_TABLE_LENGTH);
            Hpack.HeaderField headerField = headerTable[adjustedIndex];
            headerEmitter.emitHeader(headerField.name, headerField.value);
        }
    }

    /**
     * because we use a ring buffer type construct, and don't actually shuffle
     * items in the array, we need to figure out the real index to use.
     * <p/>
     * package private for unit tests
     *
     * @param index The index from the hpack
     * @return the real index into the array
     */
    int getRealIndex(int index) {
        //the index is one based, but our table is zero based, hence -1
        //also because of our ring buffer setup the indexes are reversed
        //index = 1 is at position firstSlotPosition + filledSlots
        return (firstSlotPosition + (filledTableSlots - index)) % headerTable.length;
    }

    private void addStaticTableEntry(int index) throws HpackException {
        //adds an entry from the static table.
        //this must be an entry with a value as far as I can determine
        Hpack.HeaderField entry = Hpack.STATIC_TABLE[index];
        if (entry.value == null) {
            throw new HpackException();
        }
        headerEmitter.emitHeader(entry.name, entry.value);
    }

    private void addEntryToHeaderTable(Hpack.HeaderField entry) {
        if (entry.size > maxMemorySize) {
            //it is to big to fit, so we just completely clear the table.
            while (filledTableSlots > 0) {
                headerTable[firstSlotPosition] = null;
                firstSlotPosition++;
                if (firstSlotPosition == headerTable.length) {
                    firstSlotPosition = 0;
                }
                filledTableSlots--;
            }
            currentMemorySize = 0;
            return;
        }
        resizeIfRequired();
        int newTableSlots = filledTableSlots + 1;
        int tableLength = headerTable.length;
        int index = (firstSlotPosition + filledTableSlots) % tableLength;
        headerTable[index] = entry;
        int newSize = currentMemorySize + entry.size;
        while (newSize > maxMemorySize) {
            int clearIndex = firstSlotPosition;
            firstSlotPosition++;
            if (firstSlotPosition == tableLength) {
                firstSlotPosition = 0;
            }
            Hpack.HeaderField oldData = headerTable[clearIndex];
            headerTable[clearIndex] = null;
            newSize -= oldData.size;
            newTableSlots--;
        }
        this.filledTableSlots = newTableSlots;
        currentMemorySize = newSize;
    }

    private void resizeIfRequired() {
        if(filledTableSlots == headerTable.length) {
            Hpack.HeaderField[] newArray = new Hpack.HeaderField[headerTable.length + 10]; //we only grow slowly
            for(int i = 0; i < headerTable.length; ++i) {
                newArray[i] = headerTable[(firstSlotPosition + i) % headerTable.length];
            }
            firstSlotPosition = 0;
            headerTable = newArray;
        }
    }


    /**
     * Interface implemented by the intended recipient of the headers.
     */
    interface HeaderEmitter {
        /**
         * Pass a single header to the recipient.
         *
         * @param name  Header name
         * @param value Header value
         */
        void emitHeader(String name, String value);

        /**
         * Are the headers pass to the recipient so far valid? The decoder needs
         * to process all the headers to maintain state even if there is a
         * problem. In addition, it is easy for the the intended recipient to
         * track if the complete set of headers is valid since to do that state
         * needs to be maintained between the parsing of the initial headers and
         * the parsing of any trailer headers. The recipient is the best place
         * to maintain that state.
         *
         * @throws StreamException If the headers received to date are not valid
         */
        void validateHeaders() throws StreamException;
    }


    HeaderEmitter getHeaderEmitter() {
        return headerEmitter;
    }

    void setHeaderEmitter(HeaderEmitter headerEmitter) {
        this.headerEmitter = headerEmitter;
    }

    //package private fields for unit tests

    int getFirstSlotPosition() {
        return firstSlotPosition;
    }

    Hpack.HeaderField[] getHeaderTable() {
        return headerTable;
    }

    int getFilledTableSlots() {
        return filledTableSlots;
    }

    int getCurrentMemorySize() {
        return currentMemorySize;
    }

    int getMaxMemorySize() {
        return maxMemorySize;
    }
}
