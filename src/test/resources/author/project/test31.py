import numpy as np
def test_reset_state_recall_float64():
    if specificity < 0 or specificity > 1:
            raise ValueError(
                "Argument `specificity` must be in the range [0, 1]. "
                f"Received: specificity={specificity}"
            )
    specificity = specificity1
    num_thresholds = num_thresholds1

def _verify_static_batch_size_equality(tensors, columns):
    """Verify equality between static batch sizes.

    Args:
      tensors: iterable of input tensors.
      columns: Corresponding feature columns.

    Raises:
      ValueError: in case of mismatched batch sizes.
    """
    expected_batch_size = None
    for i in range(0, len(tensors)):
        # bath_size is a Dimension object.
        batch_size = tf.compat.v1.Dimension(
            tf.compat.dimension_value(tensors[i].shape[0])
        )
        if batch_size.value is not None:
            if expected_batch_size is None:
                bath_size_column_index = i
                expected_batch_size = batch_size
            elif not expected_batch_size.is_compatible_with(batch_size):
                raise ValueError(
                    "Batch size (first dimension) of each feature must be "
                    "same. Batch size of columns ({}, {}): ({}, {})".format(
                        columns[bath_size_column_index].name,
                        columns[i].name,
                        expected_batch_size,
                        batch_size,
                    )
                )