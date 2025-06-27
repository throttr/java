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

import java.util.List;

public class ListResponse {
    private final boolean success;
    private final List<ListItem> items;

    public ListResponse(boolean success, List<ListItem> items) {
        this.success = success;
        this.items = items;
    }

    public boolean isSuccess() {
        return success;
    }

    public List<ListItem> getItems() {
        return items;
    }
}