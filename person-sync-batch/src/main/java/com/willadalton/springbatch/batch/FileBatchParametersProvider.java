package com.willadalton.springbatch.batch;

import com.willadalton.batchrunner.BatchParametersProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

/**
 * Resolves file-based batch parameters from a dataset name.
 *
 * <p>For the default dataset name the input file is taken from the
 * {@code batch.input-file} configuration property. Other dataset names are
 * resolved relative to the same base location by replacing the filename stem.</p>
 */
@Component
public class FileBatchParametersProvider implements BatchParametersProvider<FileParameters> {

    private final Resource defaultInputFile;
    private final ResourceLoader resourceLoader;
    private final String defaultDatasetName;

    public FileBatchParametersProvider(
            @Value("${batch.input-file}") Resource defaultInputFile,
            ResourceLoader resourceLoader,
            @Value("${batch.dataset-name}") String defaultDatasetName
    ) {
        this.defaultInputFile = defaultInputFile;
        this.resourceLoader = resourceLoader;
        this.defaultDatasetName = defaultDatasetName;
    }

    @Override
    public FileParameters getParameters(String datasetName) {
        if (datasetName == null || datasetName.equals(defaultDatasetName)) {
            return new FileParameters(defaultInputFile);
        }
        Resource resolved = resourceLoader.getResource("classpath:/input/" + datasetName + ".csv");
        return new FileParameters(resolved);
    }
}
