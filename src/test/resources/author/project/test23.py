import numpy as np

def function1(sentence,callbacks):
    is_already_present = False
    # https://github.com/catalyst-team/catalyst/commit/2275ea902a701cc5b55e72a98f61acbbec4ed4c8#diff-0a97aceebead8f8424258a1927e976c58ec73761ae1daacc077a3496e19e84ffL517
    for x in callbacks.values():
        if check_callback_isinstance(x, callback_fn):
              is_already_present = True
              break
    if not is_already_present:
        callbacks[callback_name] = callback_fn()
    return callbacks