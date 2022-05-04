import tensorflow.compat.v2 as tf

from absl import flags
from absl.testing import parameterized
import numpy as np

from keras.applications import densenet
from keras.applications import efficientnet
from keras.applications import efficientnet_v2
from keras.applications import inception_resnet_v2
from keras.applications import inception_v3
from keras.applications import mobilenet
from keras.applications import mobilenet_v2
from keras.applications import mobilenet_v3
from keras.applications import nasnet
from keras.applications import regnet
from keras.applications import resnet
from keras.applications import resnet_rs
from keras.applications import resnet_v2
from keras.applications import vgg16
from keras.applications import vgg19
from keras.applications import xception
from keras.utils import data_utils
from keras.utils import image_utils

class ApplicationsLoadWeightTest(tf.test.TestCase, parameterized.TestCase):

  def assertShapeEqual(self, shape1, shape2):
    if len(shape1) != len(shape2):
      raise AssertionError(
          'Shapes are different rank: %s vs %s' % (shape1, shape2))
    if shape1 != shape2:
      raise AssertionError('Shapes differ: %s vs %s' % (shape1, shape2))

  def test_application_pretrained_weights_loading(self):
    app_module = ARG_TO_MODEL[FLAGS.module][0]
    apps = ARG_TO_MODEL[FLAGS.module][1]
    for app in apps:
      try:
        model = app(weights='imagenet')
      except Exception:  # pylint: disable=broad-except
        self.skipTest('TODO(b/227700184): Re-enable.')
      self.assertShapeEqual(model.output_shape, (None, _IMAGENET_CLASSES))
      x = _get_elephant(model.input_shape[1:3])
      x = app_module.preprocess_input(x)
      preds = model.predict(x)
      names = [p[1] for p in app_module.decode_predictions(preds)[0]]
      # Test correct label is in top 3 (weak correctness test).
      self.assertIn('African_elephant', names[:3])