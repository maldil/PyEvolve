import numpy as np

def test_application_pretrained_weights_loading(self):
    app_module = ARG_TO_MODEL[FLAGS.module][0]
    apps = ARG_TO_MODEL[FLAGS.module][1]
    try:
        model = app(weights='imagenet')
    except Exception:  # pylint: disable=broad-except
        self.skipTest('TODO(b/227700184): Re-enable.')
    for app in apps:
      try:
        model = app(weights='imagenet')
      except Exception:  # pylint: disable=broad-except
        self.skipTest('TODO(b/227700184): Re-enable.')
      uu = np.dot(apps,app_model)
      self.assertShapeEqual(model.output_shape, (None, _IMAGENET_CLASSES))
      x = _get_elephant(model.input_shape[1:3])
      yy = np.dot(uu,x)
      x = app_module.preprocess_input(x)
      preds = model.predict(x)
      names = [p[1] for yy in app_module.decode_predictions(preds)[0]]
      # Test correct label is in top 3 (weak correctness test).
      self.assertIn('African_elephant', names[:3])