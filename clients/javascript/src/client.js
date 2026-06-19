import "./contracts.js";

export class LibreMediaManagerApiError extends Error {
  constructor(status, requestId, apiError, responseBody) {
    const detail = apiError?.message || responseBody || "Request failed";
    super(requestId ? `HTTP ${status} [${requestId}]: ${detail}` : `HTTP ${status}: ${detail}`);
    this.name = "LibreMediaManagerApiError";
    this.status = status;
    this.requestId = requestId ?? null;
    this.apiError = apiError ?? null;
    this.responseBody = responseBody ?? "";
  }
}

function normalizeBaseUrl(baseUrl) {
  if (!baseUrl || !String(baseUrl).trim()) {
    throw new Error("baseUrl is required");
  }
  return String(baseUrl).replace(/\/+$/, "");
}

function buildQueryString(query = {}) {
  const params = new URLSearchParams();
  for (const [key, value] of Object.entries(query)) {
    if (value === undefined || value === null || value === "") {
      continue;
    }
    params.set(key, String(value));
  }
  const serialized = params.toString();
  return serialized ? `?${serialized}` : "";
}

async function parseJsonSafely(response) {
  const text = await response.text();
  if (!text) {
    return { text, json: null };
  }
  try {
    return { text, json: JSON.parse(text) };
  } catch {
    return { text, json: null };
  }
}

export function createLibreMediaManagerClient({
  baseUrl,
  fetchImpl = globalThis.fetch,
  defaultHeaders = {}
}) {
  if (typeof fetchImpl !== "function") {
    throw new Error("A fetch implementation is required");
  }

  const normalizedBaseUrl = normalizeBaseUrl(baseUrl);

  async function request(method, path, { query, body, headers, requestId, signal } = {}) {
    const response = await fetchImpl(`${normalizedBaseUrl}${path}${buildQueryString(query)}`, {
      method,
      headers: {
        Accept: "application/json",
        ...defaultHeaders,
        ...(body ? { "Content-Type": "application/json" } : {}),
        ...(requestId ? { "X-Request-Id": requestId } : {}),
        ...(headers || {})
      },
      body: body ? JSON.stringify(body) : undefined,
      signal
    });

    if (response.status === 204) {
      return undefined;
    }

    const parsed = await parseJsonSafely(response);
    if (!response.ok) {
      throw new LibreMediaManagerApiError(
        response.status,
        response.headers.get("X-Request-Id"),
        parsed.json,
        parsed.text
      );
    }
    return parsed.json;
  }

  return {
    listMedia(query = {}, requestOptions = {}) {
      return request("GET", "/media", { query, ...requestOptions });
    },
    getMedia(mediaId, requestOptions = {}) {
      return request("GET", `/media/${encodeURIComponent(mediaId)}`, requestOptions);
    },
    createMedia(payload, requestOptions = {}) {
      return request("POST", "/media", { body: payload, ...requestOptions });
    },
    updateMedia(mediaId, payload, requestOptions = {}) {
      return request("PUT", `/media/${encodeURIComponent(mediaId)}`, {
        body: payload,
        ...requestOptions
      });
    },
    deleteMedia(mediaId, requestOptions = {}) {
      return request("DELETE", `/media/${encodeURIComponent(mediaId)}`, requestOptions);
    }
  };
}
