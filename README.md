# Throttr Java SDK

<p align="center">
<a href="https://github.com/throttr/java/actions/workflows/build.yml"><img src="https://github.com/throttr/throttr/actions/workflows/build.yml/badge.svg" alt="Build"></a>
<a href="https://codecov.io/gh/throttr/java"><img src="https://codecov.io/gh/throttr/java/graph/badge.svg?token=java" alt="Coverage"></a>
<a href="https://sonarcloud.io/project/overview?id=throttr_java"><img src="https://sonarcloud.io/api/project_badges/measure?project=throttr_java&metric=alert_status" alt="Quality Gate"></a>
</p>

<p align="center">
<a href="https://sonarcloud.io/project/overview?id=throttr_java"><img src="https://sonarcloud.io/api/project_badges/measure?project=throttr_java&metric=bugs" alt="Bugs"></a>
<a href="https://sonarcloud.io/project/overview?id=throttr_java"><img src="https://sonarcloud.io/api/project_badges/measure?project=throttr_java&metric=vulnerabilities" alt="Vulnerabilities"></a>
<a href="https://sonarcloud.io/project/overview?id=throttr_java"><img src="https://sonarcloud.io/api/project_badges/measure?project=throttr_java&metric=code_smells" alt="Code Smells"></a>
<a href="https://sonarcloud.io/project/overview?id=throttr_java"><img src="https://sonarcloud.io/api/project_badges/measure?project=throttr_java&metric=duplicated_lines_density" alt="Duplicated Lines"></a>
<a href="https://sonarcloud.io/project/overview?id=throttr_java"><img src="https://sonarcloud.io/api/project_badges/measure?project=throttr_java&metric=sqale_index" alt="Technical Debt"></a>
</p>

<p align="center">
<a href="https://sonarcloud.io/project/overview?id=throttr_java"><img src="https://sonarcloud.io/api/project_badges/measure?project=throttr_java&metric=reliability_rating" alt="Reliability"></a>
<a href="https://sonarcloud.io/project/overview?id=throttr_java"><img src="https://sonarcloud.io/api/project_badges/measure?project=throttr_java&metric=security_rating" alt="Security"></a>
<a href="https://sonarcloud.io/project/overview?id=throttr_java"><img src="https://sonarcloud.io/api/project_badges/measure?project=throttr_throttr&metric=sqale_rating" alt="Maintainability"></a>
</p>

Java client for communicating with a Throttr server over TCP.

The SDK enables sending traffic control requests efficiently, without HTTP, respecting the server's native binary protocol.

## Installation

Add the dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>cl.throttr</groupId>
    <artifactId>sdk</artifactId>
    <version>2.0.0</version>
</dependency>
```

Make sure to configure your Maven repositories to include GitHub Packages if needed.

## Basic Usage

```java
package your.source;

import java.util.concurrent.CompletableFuture;

import cl.throttr.Service;
import cl.throttr.InsertRequest;
import cl.throttr.QueryRequest;
import cl.throttr.UpdateRequest;
import cl.throttr.PurgeRequest;
import cl.throttr.TTLType;
import cl.throttr.AttributeType;
import cl.throttr.ChangeType;
import cl.throttr.FullResponse;
import cl.throttr.SimpleResponse;

public class ExampleUsage {

    public static void main(String[] args) {
        Service service = new Service("127.0.0.1", 9000, 4); // Max connections = 4

        try {
            // Connect to Throttr
            service.connect();

            // Define a consumer and resource
            String consumerId = "127.0.0.1:1234";
            String resourceId = "GET /api/resource";

            // Insert quota
            CompletableFuture<Object> insertFuture = service.send(new InsertRequest(
                    5L, 0L, TTLType.Milliseconds, 3000L, consumerId, resourceId
            ));
            FullResponse insertResponse = (FullResponse) insertFuture.get();

            System.out.println("Allowed: " + insertResponse.allowed());
            System.out.println("Remaining: " + insertResponse.quotaRemaining());
            System.out.println("TTL type: " + insertResponse.ttlType());
            System.out.println("TTL remaining: " + insertResponse.ttlRemaining());

            // Update the quota
            CompletableFuture<Object> updateFuture = service.send(new UpdateRequest(
                    AttributeType.QUOTA, ChangeType.DECREASE, 1L, consumerId, resourceId
            ));
            SimpleResponse updateResponse = (SimpleResponse) updateFuture.get();
            System.out.println("Quota updated successfully: " + updateResponse.success());

            // Query the quota
            CompletableFuture<Object> queryFuture = service.send(new QueryRequest(
                    consumerId, resourceId
            ));
            FullResponse queryResponse = (FullResponse) queryFuture.get();

            System.out.println("Allowed after update: " + queryResponse.allowed());
            System.out.println("Remaining after update: " + queryResponse.quotaRemaining());
            System.out.println("TTL type: " + queryResponse.ttlType());
            System.out.println("TTL after update: " + queryResponse.ttlRemaining());

            // Optionally, purge the quota
            CompletableFuture<Object> purgeFuture = service.send(new PurgeRequest(
                    consumerId, resourceId
            ));
            SimpleResponse purgeResponse = (SimpleResponse) purgeFuture.get();
            System.out.println("Purge success: " + purgeResponse.success());

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Disconnect once done
            service.close();
        }
    }
}
```



## Technical Notes

- The protocol assumes Little Endian architecture.
- The internal message queue ensures requests are processed sequentially.
- The package is defined to works with protocol 2.0.0 or greatest.

---

## License

Distributed under the [GNU Affero General Public License v3.0](./LICENSE).
