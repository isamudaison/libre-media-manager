# JavaScript + React Client

This package provides:

- a fetch-based JavaScript client for the current `/media` endpoints
- small React hooks that wrap the client for data loading and mutations

## Usage

```js
import { createLibreMediaManagerClient, useMedia, useMediaList } from "./src/index.js";

const client = createLibreMediaManagerClient({
  baseUrl: "http://localhost:8080"
});

const page = await client.listMedia({ title: "arrival", mediaType: "MOVIE" });
const media = await client.getMedia("media-id");
```

React usage:

```js
import { createLibreMediaManagerClient, useMediaList } from "./src/index.js";

const client = createLibreMediaManagerClient({ baseUrl: "http://localhost:8080" });

function MediaListScreen() {
  const { data, error, isLoading, query, setQuery, refresh } = useMediaList(client, {
    title: "arrival",
    mediaType: "MOVIE"
  });

  return null;
}
```

## Notes

- `fetch` must be available in the runtime or supplied explicitly with `fetchImpl`.
- The hooks expect a stable `client` instance created outside the render path.
- Node.js is not installed in this environment, so this package was added with static review rather than runtime execution here.
