import tensorflow as tf
dataset.apply(tf.contrib.data.batch_and_drop_remainder(batch_size))