def find_signal_means(b_unique, data_norm, bvals, rho, lb_matrix, w=1e-03):
    r"""Calculate the mean signal for each shell.

    Parameters
    ----------
    b_unique : 1d ndarray,
        unique b-values in a vector excluding zero
    data_norm : 1d ndarray,
        normalized diffusion signal
    bvals : 1d ndarray,
        the b-values
    rho : 2d ndarray,
        SH basis matrix for fitting the signal on each shell
    lb_matrix : 2d ndarray,
        Laplace-Beltrami regularization matrix
    w : float,
        weight for the Laplace-Beltrami regularization

    Returns
    -------
    means : 1d ndarray
        the average of the signal for each b-values

    """
    lb = len(b_unique)
    means = np.zeros(lb)
    for u in range(lb):
        ind = bvals == b_unique[u]
        shell = data_norm[ind]
        if np.sum(ind) > 20:
            M = rho[ind, :]

            pseudo_inv = np.dot(np.linalg.inv(
                np.dot(M.T, M) + w*lb_matrix), M.T)
            coef = np.dot(pseudo_inv, shell)

            means[u] = coef[0] / np.sqrt(4*np.pi)
        else:
            means[u] = shell.mean()

    return means