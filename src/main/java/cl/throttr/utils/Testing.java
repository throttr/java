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

package cl.throttr.utils;

import cl.throttr.enums.ValueSize;

/**
 * Testing
 */
public class Testing {
    /**
     * Constructor
     */
    private Testing() {
    }

    /**
     * Get value size
     *
     * @return ValueSize
     */
    public static ValueSize getValueSizeFromEnv() {
        String size = System.getenv("THROTTR_SIZE");

        if (size == null) {
            size = "uint16"; // default
        }

        return switch (size) {
            case "uint8" -> ValueSize.UINT8;
            case "uint16" -> ValueSize.UINT16;
            case "uint32" -> ValueSize.UINT32;
            case "uint64" -> ValueSize.UINT64;
            default -> throw new IllegalArgumentException("Unsupported THROTTR_SIZE: " + size);
        };
    }
}
