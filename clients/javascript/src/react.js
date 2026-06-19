import { useEffect, useState } from "react";

function useAsyncState(initialValue = null) {
  const [data, setData] = useState(initialValue);
  const [error, setError] = useState(null);
  const [isLoading, setIsLoading] = useState(false);
  return { data, setData, error, setError, isLoading, setIsLoading };
}

export function useMedia(client, mediaId, options = {}) {
  const state = useAsyncState(null);

  async function load(signal) {
    if (!mediaId) {
      state.setData(null);
      return;
    }
    state.setIsLoading(true);
    state.setError(null);
    try {
      const nextData = await client.getMedia(mediaId, { ...options, signal });
      state.setData(nextData);
    } catch (nextError) {
      if (nextError?.name !== "AbortError") {
        state.setError(nextError);
      }
    } finally {
      state.setIsLoading(false);
    }
  }

  useEffect(() => {
    const controller = new AbortController();
    load(controller.signal);
    return () => controller.abort();
  }, [client, mediaId]);

  return {
    data: state.data,
    error: state.error,
    isLoading: state.isLoading,
    refresh: () => {
      const controller = new AbortController();
      return load(controller.signal);
    }
  };
}

export function useMediaList(client, initialQuery = {}, options = {}) {
  const [query, setQuery] = useState(initialQuery);
  const state = useAsyncState({ items: [], page: 0, size: 0, totalElements: 0, totalPages: 0 });
  const queryKey = JSON.stringify(query);

  async function load(signal) {
    state.setIsLoading(true);
    state.setError(null);
    try {
      const nextData = await client.listMedia(query, { ...options, signal });
      state.setData(nextData);
    } catch (nextError) {
      if (nextError?.name !== "AbortError") {
        state.setError(nextError);
      }
    } finally {
      state.setIsLoading(false);
    }
  }

  useEffect(() => {
    const controller = new AbortController();
    load(controller.signal);
    return () => controller.abort();
  }, [client, queryKey]);

  return {
    data: state.data,
    error: state.error,
    isLoading: state.isLoading,
    query,
    setQuery,
    refresh: () => {
      const controller = new AbortController();
      return load(controller.signal);
    }
  };
}

function createMutationHook(runMutation) {
  return function useMutation(client) {
    const [isPending, setIsPending] = useState(false);
    const [error, setError] = useState(null);
    const [lastResult, setLastResult] = useState(null);

    async function mutate(...args) {
      setIsPending(true);
      setError(null);
      try {
        const result = await runMutation(client, ...args);
        setLastResult(result ?? null);
        return result;
      } catch (nextError) {
        setError(nextError);
        throw nextError;
      } finally {
        setIsPending(false);
      }
    }

    return {
      mutate,
      isPending,
      error,
      lastResult
    };
  };
}

export const useCreateMedia = createMutationHook((client, payload, requestOptions = {}) =>
  client.createMedia(payload, requestOptions)
);

export const useUpdateMedia = createMutationHook((client, mediaId, payload, requestOptions = {}) =>
  client.updateMedia(mediaId, payload, requestOptions)
);

export const useDeleteMedia = createMutationHook((client, mediaId, requestOptions = {}) =>
  client.deleteMedia(mediaId, requestOptions)
);
