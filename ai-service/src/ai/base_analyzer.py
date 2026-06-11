from abc import ABC, abstractmethod


class BaseAnalyzer(ABC):
    def __init__(self, device: str = "cpu"):
        self.device = device

    @abstractmethod
    def predict(self, payload: dict) -> tuple[str, float]:
        pass
