from typing import Dict, Type

from ai.base_analyzer import BaseAnalyzer


class ModelFactory:
    _registry: Dict[str, Type[BaseAnalyzer]] = {}

    @classmethod
    def register(cls, name: str):
        def wrapper(wrapped_class: Type[BaseAnalyzer]):
            cls._registry[name] = wrapped_class
            return wrapped_class
        return wrapper

    @classmethod
    def get_analyzer(cls, name: str, device: str = "cpu") -> BaseAnalyzer:
        if name not in cls._registry:
            raise ValueError(f"Model '{name}' not found in registry. Available models: {list(cls._registry.keys())}")
        
        analyzer_class = cls._registry[name]
        return analyzer_class(device=device)
