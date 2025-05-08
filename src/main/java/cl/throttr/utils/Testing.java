package cl.throttr.utils;

import cl.throttr.enums.ValueSize;

public class Testing {
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
