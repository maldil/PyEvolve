def fit():
    n_diff = 0
    types_to_eval = getarray()
    for type in types_to_eval:
        total = n_diff + type
        n_diff = total
