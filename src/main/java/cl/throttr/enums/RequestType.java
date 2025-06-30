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

package cl.throttr.enums;

/**
 * Request types
 */
public enum RequestType {
    /**
     * Insert
     */
    INSERT(0x01),

    /**
     * Query
     */
    QUERY(0x02),

    /**
     * Update
     */
    UPDATE(0x03),

    /**
     * Purge
     */
    PURGE(0x04),

    /**
     * Set
     */
    SET(0x05),

    /**
     * Get
     */
    GET(0x06),

    /**
     * List
     */
    LIST(0x07),

    /**
     * List
     */
    INFO(0x08),

    /**
     * Stat
     */
    STAT(0x09),

    /**
     * Stats
     */
    STATS(0x10),

    /**
     * Subscribe
     */
    SUBSCRIBE(0x11),

    /**
     * Unsubscribe
     */
    UNSUBSCRIBE(0x12),

    /**
     * Publish
     */
    PUBLISH(0x13),

    /**
     * Connections
     */
    CONNECTIONS(0x14),

    /**
     * Connection
     */
    CONNECTION(0x15),

    /**
     * Channels
     */
    CHANNELS(0x16),

    /**
     * Channel
     */
    CHANNEL(0x17),

    /**
     * WhoAmI
     */
    WHOAMI(0x18),

    /**
     * Event
     */
    EVENT(0x19);

    /**
     * Value
     */
    private final int value;

    /**
     * Constructor
     *
     * @param value
     */
    RequestType(int value) {
        this.value = value;
    }

    /**
     * Get value
     *
     * @return int
     */
    public int getValue() {
        return value;
    }
}
