package pl.karolinamichalska.logo.spark;

import com.google.cloud.dataproc.v1.Job;

import java.util.Map;

public interface SparkJobService {

    String submitJob(Map<String, String> args);

    Job getJob(String jobId);
}
