package cl.throttr.requests;

import cl.throttr.enums.ValueSize;

public class Serializer {
    public static byte[] invoke(Object request, ValueSize size) {
        return switch (request) {
            case InsertRequest insert -> insert.toBytes(size);
            case QueryRequest query -> query.toBytes();
            case UpdateRequest update -> update.toBytes(size);
            case PurgeRequest purge -> purge.toBytes();
            case SetRequest set -> set.toBytes(size);
            case GetRequest get -> get.toBytes();
            case ListRequest list -> list.toBytes();
            case InfoRequest info -> info.toBytes();
            case StatRequest stat -> stat.toBytes();
            case StatsRequest stats -> stats.toBytes();
            case SubscribeRequest subscribe -> subscribe.toBytes();
            case UnsubscribeRequest unsubscribe -> unsubscribe.toBytes();
            case PublishRequest publish -> publish.toBytes(size);
            case ConnectionsRequest connections -> connections.toBytes();
            case ConnectionRequest connection -> connection.toBytes();
            case ChannelsRequest channels -> channels.toBytes();
            case ChannelRequest channel -> channel.toBytes();
            case WhoAmiRequest whoami -> whoami.toBytes();
            case null, default -> throw new IllegalArgumentException("Unsupported request type");
        };
    }
}
