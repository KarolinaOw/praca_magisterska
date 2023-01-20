package pl.karolinamichalska.logo.spark;

import com.google.cloud.dataproc.v1.GetJobRequest;
import com.google.cloud.dataproc.v1.Job;
import com.google.cloud.dataproc.v1.JobControllerClient;
import com.google.cloud.dataproc.v1.JobControllerSettings;
import com.google.cloud.dataproc.v1.JobPlacement;
import com.google.cloud.dataproc.v1.JobReference;
import com.google.cloud.dataproc.v1.PySparkJob;
import com.google.cloud.dataproc.v1.SubmitJobRequest;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableList.toImmutableList;

public class DataprocSparkJobService implements SparkJobService {

    public static final String REGION = "europe-central2";
    private final JobControllerClient client;

    @Inject
    public DataprocSparkJobService() throws IOException {
        client = JobControllerClient.create(JobControllerSettings.newBuilder()
                .setEndpoint("%s-dataproc.googleapis.com:443".formatted(REGION))
                .build());
    }

    @Override
    public String submitJob(Map<String, String> args) {
        String jobId = UUID.randomUUID().toString();
        client.submitJob(SubmitJobRequest.newBuilder()
                .setJob(Job.newBuilder()
                        .setReference(JobReference.newBuilder()
                                .setJobId(jobId)
                                .build())
                        .setPlacement(JobPlacement.newBuilder()
                                .setClusterName("cluster-spark")
                                .build())
                        .setPysparkJob(PySparkJob.newBuilder()
                                .setMainPythonFileUri("gs://logo-seq-creator/src/job.py")
                                .addAllArgs(args.entrySet().stream()
                                        .flatMap(entry -> Stream.of(
                                                entry.getKey(),
                                                entry.getValue()))
                                        .collect(toImmutableList()))
                                .build())
                        .build())
                .setProjectId("amplified-bee-374812")
                .setRegion(REGION)
                .build());
        return jobId;
    }

    @Override
    public Job getJob(String jobId) {
        return client.getJob(GetJobRequest.newBuilder()
                .setRegion(REGION)
                .setProjectId("amplified-bee-374812")
                .setJobId(jobId)
                .build());
    }
}
