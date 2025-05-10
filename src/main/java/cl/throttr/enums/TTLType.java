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
 * TTL types
 */
public enum TTLType {
    /**
     * Nanoseconds
     */
    NANOSECONDS(1),

    /**
     * Microseconds
     */
    MICROSECONDS(2),

    /**
     * Milliseconds
     */
    MILLISECONDS(3),

    /**
     * Seconds
     */
    SECONDS(4),

    /**
     * Minutes
     */
    MINUTES(5),

    /**
     * Hours
     */
    HOURS(6);

    /**
     * Value
     */
    private final int value;

    /**
     * Constructor
     *
     * @param value
     */
    TTLType(int value) {
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

    /**
     * From byte
     *
     * @param raw
     * @return TTLType
     */
    public static TTLType fromByte(byte raw) {
        int val = raw & 0xFF;
        for (TTLType type : TTLType.values()) {
            if (type.getValue() == val) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid TTLType value: " + val);
    }
}
