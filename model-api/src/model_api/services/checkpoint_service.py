from typing import Protocol


class CheckpointService(Protocol):
    def load_checkpoint(self):
        ...