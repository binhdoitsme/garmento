from concurrent.futures import Executor, Future
from dataclasses import dataclass, field
from typing import Callable

from injector import inject

from ..services.job_scheduler import JobScheduler


@inject
class BackgroundJobScheduler(JobScheduler):
    def __init__(self,executor: Executor) -> None:
        self.executor = executor
        self.tasks = dict[str, Future]()

    def schedule(self, job_execution: Callable[[str], None], job_id: str) -> str:
        if job_id in self.tasks:
            return job_id

        task = self.executor.submit(job_execution, job_id)
        task.add_done_callback(lambda _: self.tasks.pop(job_id))
        self.tasks[job_id] = task
        return job_id

    def abort(self, job_id: str) -> None:
        if job_id not in self.tasks:
            return
        self.tasks[job_id].cancel()

    def __del__(self):
        self.executor.shutdown(wait=False)
        print("SHUTDOWN COMPLETED")
