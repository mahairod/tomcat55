/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.tomcat.util.http.fileupload.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.tomcat.util.http.fileupload.InvalidFileNameException;

/**
 * Utility class for working with streams.
 *
 * @version $Id$
 */
public final class Streams {

    /**
     * Private constructor, to prevent instantiation.
     * This class has only static methods.
     */
    private Streams() {
        // Does nothing
    }

    /**
     * Default buffer size for use in
     * {@link #copy(InputStream, OutputStream, boolean)}.
     */
    private static final int DEFAULT_BUFFER_SIZE = 8192;

    /**
     * Copies the contents of the given {@link InputStream}
     * to the given {@link OutputStream}. Shortcut for
     * <pre>
     *   copy(pInputStream, pOutputStream, new byte[8192]);
     * </pre>
     *
     * @param pInputStream The input stream, which is being read.
     * It is guaranteed, that {@link InputStream#close()} is called
     * on the stream.
     * @param pOutputStream The output stream, to which data should
     * be written. May be null, in which case the input streams
     * contents are simply discarded.
     * @param pClose True guarantees, that {@link OutputStream#close()}
     * is called on the stream. False indicates, that only
     * {@link OutputStream#flush()} should be called finally.
     *
     * @return Number of bytes, which have been copied.
     * @throws IOException An I/O error occurred.
     */
    public static long copy(InputStream pInputStream,
            OutputStream pOutputStream, boolean pClose)
            throws IOException {
        return copy(pInputStream, pOutputStream, pClose,
                new byte[DEFAULT_BUFFER_SIZE]);
    }

    /**
     * Copies the contents of the given {@link InputStream}
     * to the given {@link OutputStream}.
     *
     * @param pIn The input stream, which is being read.
     *   It is guaranteed, that {@link InputStream#close()} is called
     *   on the stream.
     * @param pOut The output stream, to which data should
     *   be written. May be null, in which case the input streams
     *   contents are simply discarded.
     * @param pClose True guarantees, that {@link OutputStream#close()}
     *   is called on the stream. False indicates, that only
     *   {@link OutputStream#flush()} should be called finally.
     * @param pBuffer Temporary buffer, which is to be used for
     *   copying data.
     * @return Number of bytes, which have been copied.
     * @throws IOException An I/O error occurred.
     */
    public static long copy(InputStream pIn,
            OutputStream pOut, boolean pClose,
            byte[] pBuffer)
    throws IOException {
        OutputStream out = pOut;
        InputStream in = pIn;
        try {
            long total = 0;
            for (;;) {
                int res = in.read(pBuffer);
                if (res == -1) {
                    break;
                }
                if (res > 0) {
                    total += res;
                    if (out != null) {
                        out.write(pBuffer, 0, res);
                    }
                }
            }
            if (out != null) {
                if (pClose) {
                    out.close();
                } else {
                    out.flush();
                }
                out = null;
            }
            in.close();
            in = null;
            return total;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ioe) {
                    /* Ignore me */
                }
            }
            if (pClose  &&  out != null) {
                try {
                    out.close();
                } catch (IOException ioe) {
                    /* Ignore me */
                }
            }
        }
    }

    /**
     * This convenience method allows to read a
     * {@link org.apache.tomcat.util.http.fileupload.FileItemStream}'s
     * content into a string. The platform's default character encoding
     * is used for converting bytes into characters.
     *
     * @param pStream The input stream to read.
     * @see #asString(InputStream, String)
     * @return The streams contents, as a string.
     * @throws IOException An I/O error occurred.
     */
    public static String asString(InputStream pStream) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        copy(pStream, baos, true);
        return baos.toString();
    }

    /**
     * This convenience method allows to read a
     * {@link org.apache.tomcat.util.http.fileupload.FileItemStream}'s
     * content into a string, using the given character encoding.
     *
     * @param pStream The input stream to read.
     * @param pEncoding The character encoding, typically "UTF-8".
     * @see #asString(InputStream)
     * @return The streams contents, as a string.
     * @throws IOException An I/O error occurred.
     */
    public static String asString(InputStream pStream, String pEncoding)
            throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        copy(pStream, baos, true);
        return baos.toString(pEncoding);
    }

    /**
     * Checks, whether the given file name is valid in the sense,
     * that it doesn't contain any NUL characters. If the file name
     * is valid, it will be returned without any modifications. Otherwise,
     * an {@link InvalidFileNameException} is raised.
     *
     * @param pFileName The file name to check
     * @return Unmodified file name, if valid.
     * @throws InvalidFileNameException The file name was found to be invalid.
     */
    public static String checkFileName(String pFileName) {
        if (pFileName != null  &&  pFileName.indexOf('\u0000') != -1) {
            // pFileName.replace("\u0000", "\\0")
            final StringBuilder sb = new StringBuilder();
            for (int i = 0;  i < pFileName.length();  i++) {
                char c = pFileName.charAt(i);
                switch (c) {
                    case 0:
                        sb.append("\\0");
                        break;
                    default:
                        sb.append(c);
                        break;
                }
            }
            throw new InvalidFileNameException(pFileName,
                    "Invalid file name: " + sb);
        }
        return pFileName;
    }

}
