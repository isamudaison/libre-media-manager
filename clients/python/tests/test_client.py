from __future__ import annotations

import json
import sys
import unittest
import urllib.error
from io import BytesIO
from pathlib import Path
from unittest.mock import patch

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))

from libre_media_manager import (
    ApiException,
    LibreMediaManagerClient,
    MediaListQuery,
    MediaType,
    UpdateMediaRequest,
)


class FakeResponse:
    def __init__(self, status: int, body: str, headers: dict[str, str] | None = None) -> None:
        self.status = status
        self._body = body.encode("utf-8")
        self.headers = headers or {}

    def read(self) -> bytes:
        return self._body

    def __enter__(self) -> "FakeResponse":
        return self

    def __exit__(self, exc_type, exc, tb) -> None:
        return None


class LibreMediaManagerClientTest(unittest.TestCase):
    def setUp(self) -> None:
        self.client = LibreMediaManagerClient("http://127.0.0.1:8080")

    def test_list_media_parses_page(self) -> None:
        def fake_urlopen(request, timeout=5):
            self.assertEqual(
                "http://127.0.0.1:8080/media?title=arrival&mediaType=MOVIE&page=1&size=5",
                request.full_url,
            )
            self.assertEqual("GET", request.get_method())
            return FakeResponse(
                200,
                json.dumps(
                    {
                        "items": [
                            {
                                "mediaId": "media-1",
                                "title": "Arrival",
                                "mediaType": "MOVIE",
                                "status": "ACTIVE",
                                "mediaFiles": [
                                    {
                                        "mediaFileId": "file-1",
                                        "location": "/srv/media/arrival.mkv",
                                        "primaryFile": True,
                                        "version": 0,
                                        "futureField": "ignored",
                                    }
                                ],
                                "futureField": "ignored",
                            }
                        ],
                        "page": 1,
                        "size": 5,
                        "totalElements": 1,
                        "totalPages": 1,
                    }
                ),
                {"Content-Type": "application/json"},
            )

        with patch("urllib.request.urlopen", side_effect=fake_urlopen):
            page = self.client.list_media(MediaListQuery(title="arrival", mediaType=MediaType.MOVIE, page=1, size=5))

        self.assertEqual("media-1", page.items[0].mediaId)
        self.assertEqual("file-1", page.items[0].mediaFiles[0].mediaFileId)

    def test_update_media_conflict_raises_api_exception(self) -> None:
        def fake_urlopen(request, timeout=5):
            self.assertEqual("http://127.0.0.1:8080/media/media-1", request.full_url)
            self.assertEqual("PUT", request.get_method())
            raise urllib.error.HTTPError(
                request.full_url,
                409,
                "Conflict",
                {
                    "Content-Type": "application/json",
                    "X-Request-Id": "req-42",
                },
                BytesIO(
                    json.dumps(
                        {
                            "error": "Conflict",
                            "message": "Media with id 'media-1' has version 4 but the request expected version 3",
                            "fieldErrors": {
                                "version": "version does not match the current media state"
                            },
                        }
                    ).encode("utf-8")
                ),
            )

        with patch("urllib.request.urlopen", side_effect=fake_urlopen):
            with self.assertRaises(ApiException) as context:
                self.client.update_media(
                    "media-1",
                    UpdateMediaRequest(
                        title="Arrival",
                        version=3,
                        mediaType=MediaType.MOVIE,
                    ),
                )

        self.assertEqual(409, context.exception.status_code)
        self.assertEqual("req-42", context.exception.request_id)
        self.assertIn("version", context.exception.api_error.fieldErrors)
