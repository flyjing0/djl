/*
 * Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file except in compliance
 * with the License. A copy of the License is located at
 *
 * http://aws.amazon.com/apache2.0/
 *
 * or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package software.amazon.ai.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
import software.amazon.ai.ndarray.types.DataType;

/** A class containing utility methods. */
public final class Utils {

    public static final boolean DEBUG = true;

    private Utils() {}

    /**
     * Returns the index of the first occurrence of the specified element in {@code array}, or -1 if
     * this list does not contain the element.
     *
     * @param array input array
     * @param value element to search for
     * @param <T> array type
     * @return the index of the first occurrence of the specified element in {@code array}, or -1 if
     *     this list does not contain the element
     */
    public static <T> int indexOf(T[] array, T value) {
        if (array != null) {
            for (int i = 0; i < array.length; ++i) {
                if (value.equals(array[i])) {
                    return i;
                }
            }
        }

        return -1;
    }

    /**
     * Returns @{code true} if the {@code array} contains the specified element.
     *
     * @param array input array
     * @param value element whose presence in {@code array} is to be tested
     * @param <T> array type
     * @return @{code true} if this list contains the specified element
     */
    public static <T> boolean contains(T[] array, T value) {
        return indexOf(array, value) >= 0;
    }

    /**
     * Adds padding chars to specified StringBuilder.
     *
     * @param sb the StringBuilder to append
     * @param c padding char
     * @param count number characters to be added
     */
    public static void pad(StringBuilder sb, char c, int count) {
        for (int i = 0; i < count; ++i) {
            sb.append(c);
        }
    }

    /**
     * Deletes entire directory and ignore all errors.
     *
     * @param dir directory to be removed
     */
    public static void deleteQuietly(Path dir) {
        try {
            Files.walk(dir)
                    .sorted(Comparator.reverseOrder())
                    .forEach(
                            path -> {
                                try {
                                    Files.deleteIfExists(path);
                                } catch (IOException ignore) {
                                    // ignore
                                }
                            });
        } catch (IOException ignore) {
            // ignore
        }
    }

    /**
     * Reads all lines from a file.
     *
     * @param file the file to be read
     * @return all lines in the file
     * @throws IOException if read file failed
     */
    public static List<String> readLines(Path file) throws IOException {
        if (Files.notExists(file)) {
            return Collections.emptyList();
        }
        try (InputStream is = Files.newInputStream(file)) {
            return readLines(is);
        }
    }

    /**
     * Reads all lines from specified InputStream.
     *
     * @param is the InputStream to read
     * @return all lines from the input
     */
    public static List<String> readLines(InputStream is) {
        List<String> list = new ArrayList<>();
        try (Scanner scanner =
                new Scanner(is, StandardCharsets.UTF_8.name()).useDelimiter("\\n|\\r\\n")) {
            while (scanner.hasNext()) {
                list.add(scanner.next());
            }
        }
        return list;
    }

    /**
     * Returns a formatted CharSequence from input ByteBuffer.
     *
     * @param buf buffer to read from
     * @param dataType DataType to be used for formatting
     * @return formatted CharSequence
     */
    public static CharSequence toCharSequence(ByteBuffer buf, DataType dataType) {
        StringBuilder sb = new StringBuilder();
        while (buf.hasRemaining()) {
            int padding;
            String value;
            switch (dataType) {
                case FLOAT32:
                    value = String.format("%.7e", buf.getFloat());
                    padding = 14 - value.length();
                    break;
                case FLOAT64:
                    value = String.format("%.7e", buf.getDouble());
                    padding = 15 - value.length();
                    break;
                case INT8:
                    value = String.valueOf(buf.get());
                    padding = 4 - value.length();
                    break;
                case UINT8:
                    value = String.format("0x%02X", buf.get());
                    padding = 0;
                    break;
                case INT32:
                    value = String.valueOf(buf.getInt());
                    padding = 11 - value.length();
                    break;
                case INT64:
                    value = String.format("0x%016X", buf.getLong());
                    padding = 18 - value.length();
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported DataType: " + dataType);
            }
            Utils.pad(sb, ' ', padding);
            sb.append(value);
            if (buf.hasRemaining()) {
                sb.append(", ");
            }
        }
        return sb;
    }

    /**
     * Convert a List of Number to float array.
     *
     * @param list the list to be converted
     * @return float array
     */
    public static float[] toFloatArray(List<? extends Number> list) {
        float[] ret = new float[list.size()];
        int idx = 0;
        for (Number n : list) {
            ret[idx++] = n.floatValue();
        }
        return ret;
    }
}
