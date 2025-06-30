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
    public final long totalRequests;
    public final long totalRequestsPerMinute;
    public final long totalInsertRequests;
    public final long totalInsertRequestsPerMinute;
    public final long totalQueryRequests;
    public final long totalQueryRequestsPerMinute;
    public final long totalUpdateRequests;
    public final long totalUpdateRequestsPerMinute;
    public final long totalPurgeRequests;
    public final long totalPurgeRequestsPerMinute;
    public final long totalGetRequests;
    public final long totalGetRequestsPerMinute;
    public final long totalSetRequests;
    public final long totalSetRequestsPerMinute;
    public final long totalListRequests;
    public final long totalListRequestsPerMinute;
    public final long totalInfoRequests;
    public final long totalInfoRequestsPerMinute;
    public final long totalStatsRequests;
    public final long totalStatsRequestsPerMinute;
    public final long totalStatRequests;
    public final long totalStatRequestsPerMinute;
    public final long totalSubscribeRequests;
    public final long totalSubscribeRequestsPerMinute;
    public final long totalUnsubscribeRequests;
    public final long totalUnsubscribeRequestsPerMinute;
    public final long totalPublishRequests;
    public final long totalPublishRequestsPerMinute;
    public final long totalChannelRequests;
    public final long totalChannelRequestsPerMinute;
    public final long totalChannelsRequests;
    public final long totalChannelsRequestsPerMinute;
    public final long totalWhoamiRequests;
    public final long totalWhoamiRequestsPerMinute;
    public final long totalConnectionRequests;
    public final long totalConnectionRequestsPerMinute;
    public final long totalConnectionsRequests;
    public final long totalConnectionsRequestsPerMinute;
    public final long totalReadBytes;
    public final long totalReadBytesPerMinute;
    public final long totalWriteBytes;
    public final long totalWriteBytesPerMinute;
    public final long totalKeys;
    public final long totalCounters;
    public final long totalBuffers;
    public final long totalAllocatedBytesOnCounters;
    public final long totalAllocatedBytesOnBuffers;
    public final long totalSubscriptions;
    public final long totalChannels;
    public final long startupTimestamp;
    public final long totalConnections;
    public final String version;

    public InfoResponse(
            boolean success,
            long[] v,
            String version
    ) {
        this.success = success;
        this.timestamp = v[0];
        this.totalRequests = v[1];
        this.totalRequestsPerMinute = v[2];
        this.totalInsertRequests = v[3];
        this.totalInsertRequestsPerMinute = v[4];
        this.totalQueryRequests = v[5];
        this.totalQueryRequestsPerMinute = v[6];
        this.totalUpdateRequests = v[7];
        this.totalUpdateRequestsPerMinute = v[8];
        this.totalPurgeRequests = v[9];
        this.totalPurgeRequestsPerMinute = v[10];
        this.totalGetRequests = v[11];
        this.totalGetRequestsPerMinute = v[12];
        this.totalSetRequests = v[13];
        this.totalSetRequestsPerMinute = v[14];
        this.totalListRequests = v[15];
        this.totalListRequestsPerMinute = v[16];
        this.totalInfoRequests = v[17];
        this.totalInfoRequestsPerMinute = v[18];
        this.totalStatsRequests = v[19];
        this.totalStatsRequestsPerMinute = v[20];
        this.totalStatRequests = v[21];
        this.totalStatRequestsPerMinute = v[22];
        this.totalSubscribeRequests = v[23];
        this.totalSubscribeRequestsPerMinute = v[24];
        this.totalUnsubscribeRequests = v[25];
        this.totalUnsubscribeRequestsPerMinute = v[26];
        this.totalPublishRequests = v[27];
        this.totalPublishRequestsPerMinute = v[28];
        this.totalChannelRequests = v[29];
        this.totalChannelRequestsPerMinute = v[30];
        this.totalChannelsRequests = v[31];
        this.totalChannelsRequestsPerMinute = v[32];
        this.totalWhoamiRequests = v[33];
        this.totalWhoamiRequestsPerMinute = v[34];
        this.totalConnectionRequests = v[35];
        this.totalConnectionRequestsPerMinute = v[36];
        this.totalConnectionsRequests = v[37];
        this.totalConnectionsRequestsPerMinute = v[38];
        this.totalReadBytes = v[39];
        this.totalReadBytesPerMinute = v[40];
        this.totalWriteBytes = v[41];
        this.totalWriteBytesPerMinute = v[42];
        this.totalKeys = v[43];
        this.totalCounters = v[44];
        this.totalBuffers = v[45];
        this.totalAllocatedBytesOnCounters = v[46];
        this.totalAllocatedBytesOnBuffers = v[47];
        this.totalSubscriptions = v[48];
        this.totalChannels = v[49];
        this.startupTimestamp = v[50];
        this.totalConnections = v[51];
        this.version = version;
    }

    public static InfoResponse fromBytes(byte[] full) {
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
