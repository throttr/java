// Copyright (C) 2025 Ian Torres
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU Affero General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU Affero General Public License for more details.
//
// You should have received a copy of the GNU Affero General Public License
// along with this program. If not, see <https://www.gnu.org/licenses/>.

package cl.throttr;

import java.util.concurrent.CompletableFuture;
import java.util.Arrays;

/**
 * Pending Request
 *
 * @param buffer Buffer
 * @param future Future
 */
record PendingRequest(byte[] buffer, CompletableFuture<Response> future) {
    /**
     * Constructor
     *
     * @param buffer Buffer
     * @param future Promise
     */
    PendingRequest {
    }

    /**
     * Buffer
     *
     * @return byte[]
     */
    @Override
    public byte[] buffer() {
        return buffer;
    }

    /**
     * Future
     *
     * @return CompletableFuture<Response>
     */
    @Override
    public CompletableFuture<Response> future() {
        return future;
    }

    /**
     * Equals
     *
     * @param o Object
     * @return boolean
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PendingRequest(byte[] buffer1, CompletableFuture<Response> future1))) return false;
        return Arrays.equals(buffer, buffer1) &&
                future.equals(future1);
    }

    /**
     * Hash code
     *
     * @return int
     */
    @Override
    public int hashCode() {
        int result = Arrays.hashCode(buffer);
        result = 31 * result + future.hashCode();
        return result;
    }

    /**
     * To string
     *
     * @return String
     */
    @Override
    public String toString() {
        return "PendingRequest{" +
                "buffer=" + Arrays.toString(buffer) +
                ", future=" + future +
                '}';
    }
}