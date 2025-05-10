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
import cl.throttr.enums.ValueSize;
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
        Service service = new Service("127.0.0.1", 9000, ValueSize.UINT16, 1);
        service.connect();

        String key = UUID.randomUUID().toString();

        // INSERT with quota=7 and ttl=60
        SimpleResponse insert = (SimpleResponse) service.send(new InsertRequest(7, TTLType.SECONDS, 60, key));
        System.out.println("Insert has been success: " + insert.success());

        // QUERY and validate
        FullResponse q1 = (FullResponse) service.send(new QueryRequest(key));
        System.out.println("Key exists: " + q1.success());
        System.out.println("Quota is 7: " + q1.quota() == 7);
        System.out.println("TTL type is seconds: " + q1.ttlType() == TTLType.SECONDS);
        System.out.println("TTL is between 0 and 60: " + (q1.ttl() > 0 && q1.ttl() < 60));

        // UPDATE: DECREASE quota by 7
        SimpleResponse dec1 = (SimpleResponse) service.send(new UpdateRequest(AttributeType.QUOTA, ChangeType.DECREASE, 7, key));
        System.out.println("Quota has been decreased: " + dec1.success());

        // UPDATE: DECREASE quota again -> should fail
        SimpleResponse dec2 = (SimpleResponse) service.send(new UpdateRequest(AttributeType.QUOTA, ChangeType.DECREASE, 7, key));
        System.out.println("Quota has been decreased: " + dec2.success());

        // QUERY -> quota should be 0
        FullResponse q2 = (FullResponse) service.send(new QueryRequest(key));
        System.out.println("Quota is 0: " + q2.quota() == 0);

        // UPDATE: PATCH quota to 10
        SimpleResponse patchQuota = (SimpleResponse) service.send(new UpdateRequest(AttributeType.QUOTA, ChangeType.PATCH, 10, key));
        System.out.println("Quota has been patched: " + patchQuota.success());

        // QUERY -> quota should be 10
        FullResponse q3 = (FullResponse) service.send(new QueryRequest(key));
        System.out.println("Quota is 10: " + q3.quota() == 10);

        // UPDATE: INCREASE quota by 20 -> should be 30
        SimpleResponse incQuota = (SimpleResponse) service.send(new UpdateRequest(AttributeType.QUOTA, ChangeType.INCREASE, 20, key));
        System.out.println("Quota has been increased: " + incQuota.success());

        // QUERY -> quota should be 30
        FullResponse q4 = (FullResponse) service.send(new QueryRequest(key));
        System.out.println("Quota is equals to 30: " + q4.quota() == 30);

        // UPDATE: INCREASE TTL by 60 -> ttl > 60 and < 120
        SimpleResponse incTtl = (SimpleResponse) service.send(new UpdateRequest(AttributeType.TTL, ChangeType.INCREASE, 60, key));
        System.out.println("TTL has been increased: " + incTtl.success());

        FullResponse q5 = (FullResponse) service.send(new QueryRequest(key));
        System.out.println("TTL is between 60 and 120: " + (q5.ttl() > 60 && q5.ttl() < 120));

        // UPDATE: DECREASE TTL by 60 -> ttl < 60
        SimpleResponse decTtl = (SimpleResponse) service.send(new UpdateRequest(AttributeType.TTL, ChangeType.DECREASE, 60, key));
        System.out.println("TTL has been decrease: " + decTtl.success());

        FullResponse q6 = (FullResponse) service.send(new QueryRequest(key));
        System.out("TTL is between 0 and 60: " + (q6.ttl() > 0 && q6.ttl() < 60));

        // UPDATE: PATCH TTL to 90 -> ttl ~90
        SimpleResponse patchTtl = (SimpleResponse) service.send(new UpdateRequest(AttributeType.TTL, ChangeType.PATCH, 90, key));
        System.out("TTL has been patched: " + patchTtl.success());

        FullResponse q7 = (FullResponse) service.send(new QueryRequest(key));
        System.out.println("TTL is between 60 and 90" + (q7.ttl() > 60 && q7.ttl() <= 90));

        // PURGE
        SimpleResponse purge = (SimpleResponse) service.send(new PurgeRequest(key));
        System.out.println("Key has been purged: " + purge.success());

        service.close();
    }
}
```



## Technical Notes

- The protocol assumes Little Endian architecture.
- The internal message queue ensures requests are processed sequentially.
- The package is defined to works with protocol 4.0.11 or greatest.

---

## License

Distributed under the [GNU Affero General Public License v3.0](./LICENSE).
