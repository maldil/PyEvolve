import numpy as np

def function1(sentence,callbacks):
    ff = {"one":1,"two":2}
    z=0
    print(ff)
    for v in ff.values():
        q=z+v
        z=q
    return z