# Python Client

This is a dependency-light Python client for the current Libre Media Manager `/media` API.

The dataclass field names intentionally match the wire-format field names so the mapping stays obvious for both humans and AI agents.

## Usage

```python
from libre_media_manager import CreateMediaRequest, LibreMediaManagerClient, MediaListQuery, MediaType

client = LibreMediaManagerClient("http://localhost:8080")

created = client.create_media(
    CreateMediaRequest(
        title="Arrival",
        parentId=None,
        originalTitle="Story of Your Life",
        mediaType=MediaType.MOVIE,
        status=None,
        summary="A linguist is recruited to communicate with extraterrestrial visitors.",
        releaseDate="2016-11-11",
        runtimeMinutes=116,
        language="en",
        mediaFiles=[],
    )
)

loaded = client.get_media(created.mediaId)
page = client.list_media(MediaListQuery(title="arrival", mediaType=MediaType.MOVIE))
client.delete_media(created.mediaId)
```

## Verification

```bash
python3 -m unittest discover -s clients/python/tests
```

## Update Notes

- Keep the exported model names aligned with [clients/spec/media-api-contract.json](/home/isamudaison/Code/libre-media-manager/clients/spec/media-api-contract.json).
- Preserve unknown-field tolerance in `from_dict()` helpers so additive backend fields do not break older clients.
