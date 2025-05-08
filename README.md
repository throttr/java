# Throttr SDK for Java

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

## 🛠️ Installation

Add the dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>cl.throttr</groupId>
    <artifactId>sdk</artifactId>
    <version>3.0.0</version>
</dependency>
```

Make sure to configure your Maven repositories to include GitHub Packages if needed.

## Basic Usage

```java
package your.source;

import java.util.concurrent.CompletableFuture;

import cl.throttr.Service;
import cl.throttr.requests.InsertRequest;
import cl.throttr.requests.QueryRequest;
import cl.throttr.requests.UpdateRequest;
import cl.throttr.requests.PurgeRequest;
import cl.throttr.enums.TTLType;
import cl.throttr.enums.AttributeType;
import cl.throttr.enums.ChangeType;
import cl.throttr.responses.FullResponse;
import cl.throttr.responses.SimpleResponse;

public class ExampleUsage {

    public static void main(String[] args) {
        Service service = new Service("127.0.0.1", 9000, 4); // Max connections = 4

        try {
            // Connect to Throttr
            service.connect();

            // Define a consumer and resource
            String key = "127.0.0.1:1234|GET /api/resource";

            // Insert quota
            CompletableFuture<Object> insertFuture = service.send(new InsertRequest(
                    5L, TTLType.Milliseconds, 3000L, key
            ));
            SimpleResponse insertResponse = (SimpleResponse) insertFuture.get();

            System.out.println("Allowed: " + insertResponse.success());

            // Update the quota
            CompletableFuture<Object> updateFuture = service.send(new UpdateRequest(
                    AttributeType.QUOTA, ChangeType.DECREASE, 1L, key
            ));
            SimpleResponse updateResponse = (SimpleResponse) updateFuture.get();
            System.out.println("Quota updated successfully: " + updateResponse.success());

            // Query the quota
            CompletableFuture<Object> queryFuture = service.send(new QueryRequest(
                    consumerId, resourceId
            ));
            FullResponse queryResponse = (FullResponse) queryFuture.get();

            System.out.println("Allowed after update: " + queryResponse.success());
            System.out.println("Remaining after update: " + queryResponse.quota());
            System.out.println("TTL type: " + queryResponse.ttlType());
            System.out.println("TTL after update: " + queryResponse.ttl());

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
- The package is defined to works with protocol 4.0.10 or greatest.

---

## License

Distributed under the [GNU Affero General Public License v3.0](./LICENSE).
