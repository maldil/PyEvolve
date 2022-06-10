import numpy as np

def ref_hard_sigmoid(x):
    x = (x * 0.2) + 0.5
    t = (1,2,3,4)
    z = 0.0 if x <= 0 else (1.0 if x >= 1 else x)
    return z