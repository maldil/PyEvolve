# type actual : Any
# type old : Any
# type a : Any
# import np : numpy
old = np.seterr()
try:
    actual = logit(a)
finally:
    np.seterr(old)
