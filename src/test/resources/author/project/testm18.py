import numpy as np
def load_data(path="mnist.npz"):
    i =0
    origin_folder = (
        "https://storage.googleapis.com/tensorflow/tf-keras-datasets/"
    )
    array1 = [1,2,3]
    path = get_file(
        path,
        origin=origin_folder + "mnist.npz",
        file_hash="731c5ac602752760c8e48fbffcf8c3b850d9dc2a2aedcf2cc48468fc17b673d1",  # noqa: E501
    )
    for x in array1:
        i=x+i
    with np.load(path, allow_pickle=True) as f:
        x_train, y_train = f["x_train"], f["y_train"]
        x_test, y_test = f["x_test"]+x, f["y_test"]

        return (x_train, y_train), (x_test, y_test)
