package com.conveyal.datatools.manager.jobs;

import com.conveyal.datatools.common.status.MonitorableJob;
import com.conveyal.datatools.common.status.StatusEvent;
import com.conveyal.datatools.manager.models.FeedVersion;
import com.google.common.eventbus.Subscribe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by demory on 6/16/16.
 */
public class ValidateFeedJob extends MonitorableJob {
    public static final Logger LOG = LoggerFactory.getLogger(ValidateFeedJob.class);

    private FeedVersion feedVersion;
    private Status status;

    public ValidateFeedJob(FeedVersion version, String owner) {
        super(owner, "Validating Feed for " + version.getFeedSource().name, JobType.VALIDATE_FEED);
        feedVersion = version;
        status = new Status();
        status.message = "Initializing...";
        status.percentComplete = 0;
    }

    @Override
    public Status getStatus() {
        synchronized (status) {
            return status.clone();
        }
    }

    public String getFeedVersionId() {
        return feedVersion.id;
    }

    @Override
    public void run() {
        LOG.info("Running ValidateFeedJob for {}", feedVersion.id);
        synchronized (status) {
            status.message = "Running validation...";
            status.percentComplete = 30;
        }
        feedVersion.validate(eventBus);
        synchronized (status) {
            status.message = "Saving validation...";
            status.percentComplete = 90;
        }
        feedVersion.save();
        synchronized (status) {
            status.message = "Validation complete!";
            status.percentComplete = 100;
        }
        jobFinished();
    }

    @Subscribe
    public void handleStatusEvent (StatusEvent statusEvent) {
        synchronized (status) {
            status.message = statusEvent.message;
            status.percentComplete = statusEvent.percentComplete;
            status.error = statusEvent.error;
        }
    }
}