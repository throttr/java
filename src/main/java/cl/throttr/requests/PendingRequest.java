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

package cl.throttr.requests;

import cl.throttr.enums.RequestType;

import java.util.concurrent.CompletableFuture;

/**
 * Pending request
 */
public class PendingRequest {
    private final byte[] buffer;
    private final CompletableFuture<Object> future;
    private final boolean expectFullResponse;
    private final RequestType requestType;

    public PendingRequest(byte[] buffer, CompletableFuture<Object> future, boolean expectFullResponse, RequestType requestType) {
        this.buffer = buffer;
        this.future = future;
        this.expectFullResponse = expectFullResponse;
        this.requestType = requestType;
    }

    public byte[] getBuffer() {
        return buffer;
    }

    public CompletableFuture<Object> getFuture() {
        return future;
    }

    public boolean isExpectFullResponse() {
        return expectFullResponse;
    }

    public RequestType getRequestType() {
        return requestType;
    }
}

