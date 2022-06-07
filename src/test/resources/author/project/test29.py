import numpy as np
def test_reset_state_recall_float64():
    sample_weight={"output_1": sample_weight_1,
                    "output_2": sample_weight_2}
    dict_variable = {key:value for (key,value) in sample_weight.items()}

    del sample_weight, dict_variable