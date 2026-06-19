from __future__ import annotations

from dataclasses import dataclass, field


@dataclass(slots=True)
class ApiError:
    error: str
    message: str
    fieldErrors: dict[str, str] = field(default_factory=dict)

    @classmethod
    def from_dict(cls, payload: dict[str, object]) -> "ApiError":
        field_errors = payload.get("fieldErrors")
        return cls(
            error=str(payload.get("error", "")),
            message=str(payload.get("message", "")),
            fieldErrors=dict(field_errors) if isinstance(field_errors, dict) else {},
        )


class ApiException(RuntimeError):
    def __init__(
        self,
        status_code: int,
        request_id: str | None,
        api_error: ApiError | None,
        response_body: str,
    ) -> None:
        self.status_code = status_code
        self.request_id = request_id
        self.api_error = api_error
        self.response_body = response_body
        detail = api_error.message if api_error and api_error.message else response_body
        if request_id:
            super().__init__(f"HTTP {status_code} [{request_id}]: {detail}")
        else:
            super().__init__(f"HTTP {status_code}: {detail}")
