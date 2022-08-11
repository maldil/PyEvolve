# type dataset : Any
# import tf : tensorflow
# type batch_size : Any
dataset.apply(tf.batch_and_drop_remainder(batch_size))