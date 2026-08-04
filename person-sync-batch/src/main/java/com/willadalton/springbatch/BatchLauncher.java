package com.willadalton.springbatch;

import com.willadalton.batchrunner.BatchRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class BatchLauncher implements ApplicationRunner {

    private final BatchRunner batchRunner;
    private final String datasetName;

    public BatchLauncher(BatchRunner batchRunner,
                         @Value("${batch.dataset-name}") String datasetName) {
        this.batchRunner = batchRunner;
        this.datasetName = datasetName;
    }

    @Override
    public void run(ApplicationArguments args) {
        batchRunner.run(datasetName);
    }
}
