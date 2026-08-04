package com.willadalton.springbatch.batch;

import org.springframework.core.io.Resource;

/**
 * Parameters resolved for a file-based batch run.
 */
public record FileParameters(Resource inputFile) {
}
