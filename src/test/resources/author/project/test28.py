import numpy as np
def test_reset_state_recall_float64(self):
    # Test case for GitHub issue 36790.
    try:
            backend.set_floatx("float64")
    finally:
            backend.set_floatx("float32")