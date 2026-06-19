# Java Client

This is a small Java 17+ client for the current Libre Media Manager `/media` API.

## Design Goals

- no framework lock-in
- thin wrapper over HTTP and JSON
- stable model names that match the shared contract
- forward-compatible response parsing through unknown-field tolerance

## Usage

```java
import net.creft.lmm.client.CreateMediaRequest;
import net.creft.lmm.client.LibreMediaManagerClient;
import net.creft.lmm.client.Media;
import net.creft.lmm.client.MediaListRequest;
import net.creft.lmm.client.MediaType;

LibreMediaManagerClient client = new LibreMediaManagerClient("http://localhost:8080");

Media created = client.createMedia(new CreateMediaRequest(
        "Arrival",
        null,
        "Story of Your Life",
        MediaType.MOVIE,
        null,
        "A linguist is recruited to communicate with extraterrestrial visitors.",
        "2016-11-11",
        116,
        "en",
        null
));

Media loaded = client.getMedia(created.mediaId());
var page = client.listMedia(MediaListRequest.builder().title("arrival").build());
client.deleteMedia(created.mediaId());
```

## Verification

```bash
./mvnw -f clients/java/pom.xml test
```

## Update Notes

- Add new request/response records under `src/main/java/net/creft/lmm/client/`.
- Keep names aligned with [clients/spec/media-api-contract.json](/home/isamudaison/Code/libre-media-manager/clients/spec/media-api-contract.json).
- Keep response records tolerant of unknown JSON fields.
