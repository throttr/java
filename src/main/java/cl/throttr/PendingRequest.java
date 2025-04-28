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
 * Pending request represents a request in the queue that is yet to be processed.
 *
 * @param buffer The byte array containing the request data to be sent
 * @param future The future object that will eventually hold the response
 */
record PendingRequest(byte[] buffer, CompletableFuture<Response> future) {

    /**
     * Checks equality between this PendingRequest and another object.
     *
     * @param o The object to compare with
     * @return boolean True if the objects are equal, false otherwise
     */
    @Override
    @javax.annotation.Generated("jacoco")
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PendingRequest(byte[] buffer1, CompletableFuture<Response> future1))) return false;
        return Arrays.equals(buffer, buffer1) && future.equals(future1);
    }

    /**
     * Generates a hash code for the PendingRequest.
     *
     * @return int The hash code value
     */
    @Override
    public int hashCode() {
        int result = Arrays.hashCode(buffer);
        result = 31 * result + future.hashCode();
        return result;
    }

    /**
     * Provides a string representation of the PendingRequest.
     *
     * @return String A string representation of the PendingRequest object
     */
    @Override
    public String toString() {
        return "PendingRequest{" +
                "buffer=" + Arrays.toString(buffer) +
                ", future=" + future +
                '}';
    }
}