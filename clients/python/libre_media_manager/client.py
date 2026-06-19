from __future__ import annotations

import json
import urllib.error
import urllib.parse
import urllib.request
from typing import Any

from .errors import ApiError, ApiException
from .models import CreateMediaRequest, Media, MediaListQuery, MediaPage, UpdateMediaRequest


class LibreMediaManagerClient:
    def __init__(self, base_url: str, default_headers: dict[str, str] | None = None) -> None:
        if not base_url or not base_url.strip():
            raise ValueError("base_url is required")
        self.base_url = base_url.rstrip("/")
        self.default_headers = dict(default_headers or {})

    def list_media(self, query: MediaListQuery | None = None) -> MediaPage:
        payload = self._request("GET", "/media", query=(query or MediaListQuery()).to_query_params())
        return MediaPage.from_dict(payload)

    def get_media(self, media_id: str) -> Media:
        payload = self._request("GET", f"/media/{urllib.parse.quote(media_id, safe='')}")
        return Media.from_dict(payload)

    def create_media(self, request: CreateMediaRequest) -> Media:
        payload = self._request("POST", "/media", json_body=request.to_dict())
        return Media.from_dict(payload)

    def update_media(self, media_id: str, request: UpdateMediaRequest) -> Media:
        payload = self._request(
            "PUT",
            f"/media/{urllib.parse.quote(media_id, safe='')}",
            json_body=request.to_dict(),
        )
        return Media.from_dict(payload)

    def delete_media(self, media_id: str) -> None:
        self._request("DELETE", f"/media/{urllib.parse.quote(media_id, safe='')}")

    def _request(
        self,
        method: str,
        path: str,
        query: dict[str, str] | None = None,
        json_body: dict[str, Any] | None = None,
    ) -> dict[str, Any]:
        url = f"{self.base_url}{path}"
        if query:
            url = f"{url}?{urllib.parse.urlencode(query)}"

        headers = {"Accept": "application/json", **self.default_headers}
        data = None
        if json_body is not None:
            headers["Content-Type"] = "application/json"
            data = json.dumps(json_body).encode("utf-8")

        request = urllib.request.Request(url, data=data, headers=headers, method=method)
        try:
            with urllib.request.urlopen(request, timeout=5) as response:
                body = response.read().decode("utf-8")
                if response.status == 204 or not body:
                    return {}
                return json.loads(body)
        except urllib.error.HTTPError as exc:
            body = exc.read().decode("utf-8")
            api_error = None
            if body:
                try:
                    payload = json.loads(body)
                except json.JSONDecodeError:
                    payload = None
                if isinstance(payload, dict):
                    api_error = ApiError.from_dict(payload)
            raise ApiException(
                exc.code,
                exc.headers.get("X-Request-Id"),
                api_error,
                body,
            ) from exc

