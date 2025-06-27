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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class InfoResponse {
    public final boolean success;
    public final long timestamp;
    public final long total_requests;
    public final long total_requests_per_minute;
    public final long total_insert_requests;
    public final long total_insert_requests_per_minute;
    public final long total_query_requests;
    public final long total_query_requests_per_minute;
    public final long total_update_requests;
    public final long total_update_requests_per_minute;
    public final long total_purge_requests;
    public final long total_purge_requests_per_minute;
    public final long total_get_requests;
    public final long total_get_requests_per_minute;
    public final long total_set_requests;
    public final long total_set_requests_per_minute;
    public final long total_list_requests;
    public final long total_list_requests_per_minute;
    public final long total_info_requests;
    public final long total_info_requests_per_minute;
    public final long total_stats_requests;
    public final long total_stats_requests_per_minute;
    public final long total_stat_requests;
    public final long total_stat_requests_per_minute;
    public final long total_subscribe_requests;
    public final long total_subscribe_requests_per_minute;
    public final long total_unsubscribe_requests;
    public final long total_unsubscribe_requests_per_minute;
    public final long total_publish_requests;
    public final long total_publish_requests_per_minute;
    public final long total_channel_requests;
    public final long total_channel_requests_per_minute;
    public final long total_channels_requests;
    public final long total_channels_requests_per_minute;
    public final long total_whoami_requests;
    public final long total_whoami_requests_per_minute;
    public final long total_connection_requests;
    public final long total_connection_requests_per_minute;
    public final long total_connections_requests;
    public final long total_connections_requests_per_minute;
    public final long total_read_bytes;
    public final long total_read_bytes_per_minute;
    public final long total_write_bytes;
    public final long total_write_bytes_per_minute;
    public final long total_keys;
    public final long total_counters;
    public final long total_buffers;
    public final long total_allocated_bytes_on_counters;
    public final long total_allocated_bytes_on_buffers;
    public final long total_subscriptions;
    public final long total_channels;
    public final long startup_timestamp;
    public final long total_connections;
    public final String version;

    public InfoResponse(
            boolean success,
            long[] v,
            String version
    ) {
        this.success = success;
        this.timestamp = v[0];
        this.total_requests = v[1];
        this.total_requests_per_minute = v[2];
        this.total_insert_requests = v[3];
        this.total_insert_requests_per_minute = v[4];
        this.total_query_requests = v[5];
        this.total_query_requests_per_minute = v[6];
        this.total_update_requests = v[7];
        this.total_update_requests_per_minute = v[8];
        this.total_purge_requests = v[9];
        this.total_purge_requests_per_minute = v[10];
        this.total_get_requests = v[11];
        this.total_get_requests_per_minute = v[12];
        this.total_set_requests = v[13];
        this.total_set_requests_per_minute = v[14];
        this.total_list_requests = v[15];
        this.total_list_requests_per_minute = v[16];
        this.total_info_requests = v[17];
        this.total_info_requests_per_minute = v[18];
        this.total_stats_requests = v[19];
        this.total_stats_requests_per_minute = v[20];
        this.total_stat_requests = v[21];
        this.total_stat_requests_per_minute = v[22];
        this.total_subscribe_requests = v[23];
        this.total_subscribe_requests_per_minute = v[24];
        this.total_unsubscribe_requests = v[25];
        this.total_unsubscribe_requests_per_minute = v[26];
        this.total_publish_requests = v[27];
        this.total_publish_requests_per_minute = v[28];
        this.total_channel_requests = v[29];
        this.total_channel_requests_per_minute = v[30];
        this.total_channels_requests = v[31];
        this.total_channels_requests_per_minute = v[32];
        this.total_whoami_requests = v[33];
        this.total_whoami_requests_per_minute = v[34];
        this.total_connection_requests = v[35];
        this.total_connection_requests_per_minute = v[36];
        this.total_connections_requests = v[37];
        this.total_connections_requests_per_minute = v[38];
        this.total_read_bytes = v[39];
        this.total_read_bytes_per_minute = v[40];
        this.total_write_bytes = v[41];
        this.total_write_bytes_per_minute = v[42];
        this.total_keys = v[43];
        this.total_counters = v[44];
        this.total_buffers = v[45];
        this.total_allocated_bytes_on_counters = v[46];
        this.total_allocated_bytes_on_buffers = v[47];
        this.total_subscriptions = v[48];
        this.total_channels = v[49];
        this.startup_timestamp = v[50];
        this.total_connections = v[51];
        this.version = version;
    }

    public static InfoResponse fromBytes(byte[] full) {
        if (full.length != 433) {
            throw new IllegalArgumentException("Expected 433 bytes, got " + full.length);
        }

        boolean success = full[0] == 0x01;
        ByteBuffer bb = ByteBuffer.wrap(full, 1, 432).order(ByteOrder.LITTLE_ENDIAN);
        long[] values = new long[52];
        for (int i = 0; i < 52; i++) {
            values[i] = bb.getLong();
        }

        byte[] versionBytes = new byte[16];
        bb.get(versionBytes);
        String version = new String(versionBytes).replaceAll("\u0000+$", "");

        return new InfoResponse(success, values, version);
    }
}