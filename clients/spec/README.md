# Shared Client Spec

This folder exists so every client library can evolve from the same contract language.

## Files

- [media-api-contract.json](/home/isamudaison/Code/libre-media-manager/clients/spec/media-api-contract.json): current machine-readable contract snapshot for the `/media` endpoints
- [export_openapi.py](/home/isamudaison/Code/libre-media-manager/clients/spec/export_openapi.py): helper for writing a local `/v3/api-docs` snapshot when the backend is running

## Why This Exists

The project is still early, and full code generation would be premature right now. Instead, this folder gives us:

- one place to document endpoint and model names
- one place to record shared enums and error semantics
- a stable artifact that AI agents can inspect before editing multiple clients

## Maintenance Rules

- Prefer additive changes to the contract snapshot so old clients remain understandable.
- Keep model names the same across languages unless the language has a very strong convention against it.
- When the server adds response fields, keep old client readers tolerant of unknown keys.
- When the server adds request fields, add them to the shared contract first, then wire them into each client.
