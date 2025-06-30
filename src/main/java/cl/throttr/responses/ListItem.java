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

package cl.throttr.responses;

public class ListItem {
    private String key;
    public final int keyLength;
    public final int keyType;
    public final int ttlType;
    public final long expiresAt;
    public final long bytesUsed;

    public ListItem(String key, int keyLength, int keyType, int ttlType, long expiresAt, long bytesUsed) {
        this.key = key;
        this.keyLength = keyLength;
        this.keyType = keyType;
        this.ttlType = ttlType;
        this.expiresAt = expiresAt;
        this.bytesUsed = bytesUsed;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}