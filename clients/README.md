# Client Libraries

This directory contains the first client-library slice for Libre Media Manager.

Current goals:

- provide a thin, typed wrapper around the shipped `/media` endpoints
- keep the public model names aligned across Java, JavaScript, and Python
- make additive API changes easy to carry through every client
- give AI agents one place to look for the current contract and update workflow

## Layout

- [spec/README.md](/home/isamudaison/Code/libre-media-manager/clients/spec/README.md): shared contract and maintenance notes
- [java/README.md](/home/isamudaison/Code/libre-media-manager/clients/java/README.md): Java client
- [javascript/README.md](/home/isamudaison/Code/libre-media-manager/clients/javascript/README.md): fetch-based JavaScript client plus React hooks
- [python/README.md](/home/isamudaison/Code/libre-media-manager/clients/python/README.md): Python client

## Contract Source Of Truth

The current hand-maintained contract snapshot lives in [spec/media-api-contract.json](/home/isamudaison/Code/libre-media-manager/clients/spec/media-api-contract.json).

That file is intentionally AI-friendly:

- endpoint names are stable
- request and response model names match the client libraries
- enums are centralized
- error-envelope semantics are explicit

The clients are also designed to be forward-compatible with additive server changes:

- Java ignores unknown JSON fields when reading responses
- Python filters unknown keys during dataclass hydration
- JavaScript returns raw JSON objects and only relies on known fields

## Update Workflow

When the backend adds a new endpoint, object, or field:

1. Update the backend implementation, tests, and OpenAPI annotations first.
2. Refresh [spec/media-api-contract.json](/home/isamudaison/Code/libre-media-manager/clients/spec/media-api-contract.json).
3. If local HTTP access is available, export a fresh `/v3/api-docs` snapshot with [spec/export_openapi.py](/home/isamudaison/Code/libre-media-manager/clients/spec/export_openapi.py).
4. Add or extend the matching model and method in each client library.
5. Update the examples in each client README.
6. Run the client-specific verification commands documented below.

## Verification

- Java: `./mvnw -f clients/java/pom.xml test`
- Python: `python3 -m unittest discover -s clients/python/tests`
- JavaScript: static review only in this environment because Node.js is not installed
