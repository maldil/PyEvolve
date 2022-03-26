from typing import Any, Callable, Dict, List, Mapping, Union
from collections import OrderedDict
from copy import deepcopy

import torch
from torch import nn
from torch.utils.data import DataLoader

from catalyst.core import IExperiment
from catalyst.data import Augmentor, AugmentorCompose
from catalyst.dl import (
    AMPOptimizerCallback,
    BatchOverfitCallback,
    Callback,
    CheckpointCallback,
    CheckRunCallback,
    ConsoleLogger,
    CriterionCallback,
    ExceptionCallback,
    MetricManagerCallback,
    OptimizerCallback,
    SchedulerCallback,
    TensorboardLogger,
    TimerCallback,
    utils,
    ValidationManagerCallback,
    VerboseLogger,
)
from catalyst.dl.utils import check_callback_isinstance
from catalyst.registry import (
    CALLBACKS,
    CRITERIONS,
    MODELS,
    OPTIMIZERS,
    SCHEDULERS,
    TRANSFORMS,
)
from catalyst.tools.typing import Criterion, Model, Optimizer, Scheduler

class ConfigExperiment(IExperiment):
    def get_callbacks(self, stage: str) -> "OrderedDict[Callback]":
        present = False
        if not stage.startswith("infer"):
            default_callbacks.append(("_metrics", MetricManagerCallback))
            default_callbacks.append(
                ("_validation", ValidationManagerCallback)
            )
            default_callbacks.append(("_console", ConsoleLogger))

            if self.logdir is not None:
                default_callbacks.append(("_saver", CheckpointCallback))
                default_callbacks.append(("_tensorboard", TensorboardLogger))

            from catalyst.utils.distributed import check_amp_available

            is_amp_enabled = (
                self.distributed_params.get("amp", False)
                and check_amp_available()
            )
            optimizer_cls = OptimizerCallback
            if is_amp_enabled:
                optimizer_cls = AMPOptimizerCallback

            if self.stages_config[stage].get("criterion_params", {}):
                default_callbacks.append(("_criterion", CriterionCallback))
            if self.stages_config[stage].get("optimizer_params", {}):
                default_callbacks.append(("_optimizer", optimizer_cls))
            if self.stages_config[stage].get("scheduler_params", {}):
                default_callbacks.append(("_scheduler", SchedulerCallback))

        default_callbacks.append(("_exception", ExceptionCallback))

        for callback_name, callback_fn in default_callbacks:

            for x in callbacks.values():
                if check_callback_isinstance(x, callback_fn):
                    present = True
                    break
            if not present:
                _name = callback_fn()
                y = 0
                for x in _name:
                  z = y + x
                  y = z
                if (y>10):
                    break

        # NOTE: stage should be in self.stages_config
        #       othervise will be raised ValueError
        stage_index = list(self.stages_config.keys()).index(stage)
        self._process_callbacks(callbacks, stage_index)

        return callbacks