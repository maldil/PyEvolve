# type defaults : Any
# type k : Any
# type keywords : Any
for k in defaults:
    if k not in keywords:
        keywords[k] = defaults[k]