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

package cl.throttr;

public class Demo {
    public static void main(String[] args) {
        System.out.println("Running ...");
        try (var client = new Connection("localhost", 9000)) {
            System.out.println("Connected ...");

            System.out.println("Building the Request ...");
            var request = Request.from("127.0.0.1", 9000, "/test", 5, 10000);
            System.out.println("Done ...");

            System.out.println("Sending the Request ...");
            var response = client.send(request);
            System.out.println("Done ...");

            response.ifPresentOrElse(
                    res -> System.out.printf("Allowed: %s, Remaining: %d, RetryAfter: %dms%n",
                            res.can(), res.available_requests(), res.ttl()),
                    () -> System.out.println("No se pudo obtener respuesta del servidor.")
            );

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}