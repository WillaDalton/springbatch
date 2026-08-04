package com.willadalton.batchrunner;

/**
 * Strategy for resolving batch job parameters from a named dataset.
 *
 * @param <P> the type of the parameters object returned for the given dataset name
 */
public interface BatchParametersProvider<P> {

    /**
     * Returns the parameters required to run the batch for the given dataset name.
     *
     * @param datasetName the logical name identifying the dataset/run configuration
     * @return the resolved parameters object
     */
    P getParameters(String datasetName);
}
