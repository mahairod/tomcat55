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

/**
 * Utility class for extracting values from byte arrays.
 */
public class ByteUtil {

    private ByteUtil() {
        // Hide default constructor
    }


    public static boolean isBit7Set(byte input) {
        return (input & 0x80) > 0;
    }


    public static int get31Bits(byte[] input, int firstByte) {
        return ((input[firstByte] & 0x7F) << 24) + ((input[firstByte + 1] & 0xFF) << 16) +
                ((input[firstByte + 2] & 0xFF) << 8) + (input[firstByte + 3] & 0xFF);
    }


    public static void set31Bits(byte[] output, int firstByte, int value) {
        output[firstByte] = (byte) ((value & 0x7F000000) >> 24);
        output[firstByte + 1] = (byte) ((value & 0xFF0000) >> 16);
        output[firstByte + 2] = (byte) ((value & 0xFF00) >> 8);
        output[firstByte + 3] = (byte) (value & 0xFF);
    }


    public static int getOneByte(byte[] input, int pos) {
        return (input[pos] & 0xFF);
    }


    public static int getTwoBytes(byte[] input, int firstByte) {
        return ((input[firstByte] & 0xFF) << 8) +  (input[firstByte + 1] & 0xFF);
    }


    public static int getThreeBytes(byte[] input, int firstByte) {
        return ((input[firstByte] & 0xFF) << 16) + ((input[firstByte + 1] & 0xFF) << 8) +
                (input[firstByte + 2] & 0xFF);
    }


    public static void setThreeBytes(byte[] output, int firstByte, int value) {
        output[firstByte] = (byte) ((value & 0xFF0000) >> 16);
        output[firstByte + 1] = (byte) ((value & 0xFF00) >> 8);
        output[firstByte + 2] = (byte) (value & 0xFF);
    }


    public static long getFourBytes(byte[] input, int firstByte) {
        return ((long)(input[firstByte] & 0xFF) << 24) + ((input[firstByte + 1] & 0xFF) << 16) +
                ((input[firstByte + 2] & 0xFF) << 8) + (input[firstByte + 3] & 0xFF);
    }
}
