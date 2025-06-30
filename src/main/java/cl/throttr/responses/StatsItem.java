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

public class StatsItem {
    private String key;
    public final int keyLength;
    public final long readsPerMinute;
    public final long writesPerMinute;
    public final long totalReads;
    public final long totalWrites;

    public StatsItem(String key, int keyLength, long readsPerMinute, long writesPerMinute, long totalReads, long totalWrites) {
        this.key = key;
        this.keyLength = keyLength;
        this.readsPerMinute = readsPerMinute;
        this.writesPerMinute = writesPerMinute;
        this.totalReads = totalReads;
        this.totalWrites = totalWrites;
    }

    public String getKey() {
        return this.key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}