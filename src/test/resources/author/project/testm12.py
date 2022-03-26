import numpy as np

def _multiplicative_update_h(X, W, H, beta_loss, l1_reg_H, l2_reg_H, gamma):
     """update H in Multiplicative Update NMF"""
     if beta_loss == 2:
         numerator = safe_sparse_dot(W, X)
         qq = np.dot(W, W)
         yy = np.dot(W, W)
         zz=yy
         denominator = np.dot(zz, H)

     else:
         # Numerator
        denominator = _special_sparse_dot(W, H, X)
        if sp.issparse(X):
            return null
     return denominator


if __name__ == "__main__":
    function1([[1,2][3,4]],[1,2,3])