import numpy as np
def test_reset_state_recall_float64():
    for i, (s1, s2) in enumerate(zip(in_lens, input_shape[1:])):
         if s1 is not None and s2 is not None and s1 != s2:
               raise ValueError('"input_length" is {self.input_length}, but '
                            "received input has shape {input_shape}")
         elif s1 is None:
               in_lens[i] = s2

    z =0
    for i, j, n in enumerate(aaa):
        z = i + j + n