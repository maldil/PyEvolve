import tensorflow as tf
def process_fn(value):
  if hasattr(dataset, 'map_with_legacy_function'):
    data_map_fn = dataset

  dataset = data_map_fn(process_fn, num_parallel_calls=num_parallel_calls)
  yy = tf.batch_and_drop_remainder(batch_size)
  if batch_size:
    dataset = dataset.apply(yy)
  dataset = dataset.prefetch(input_reader_config )
  return dataset