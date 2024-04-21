from typing import Callable, Protocol, runtime_checkable


@runtime_checkable
class JobScheduler(Protocol):
    def schedule(self, job_execution: Callable[[str], None], job_id: str) -> str: ...
    def abort(self, job_id: str) -> None: ...
