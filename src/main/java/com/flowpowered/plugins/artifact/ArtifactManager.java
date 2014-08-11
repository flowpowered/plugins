package com.flowpowered.plugins.artifact;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Future;

import com.flowpowered.plugins.artifact.jobs.LocateJob;
import com.flowpowered.plugins.artifact.jobs.RemoveJob;

public class ArtifactManager {
    private ConcurrentMap<String, Artifact> byName = new ConcurrentHashMap<>();


    /**
     * Makes the Manager try to find the artifact and start tracking it.
     * @return some Future, whose type will be specified once I figure out what I want it to be
     */
    public Future<?> locate(String artifactName) {
        LocateJob job = new LocateJob();

        Artifact newArtifact = new Artifact();
        newArtifact.getJobQueue().add(job);

        Artifact artifact = byName.putIfAbsent(artifactName, newArtifact);
        if (artifact == null) {
            enqueuePulse(artifactName);
        } else {
            // FIXME: Race condition - we may enqueue the job too late.
            artifact.getJobQueue().add(job);
        }
        return job.getFuture();
    }

    /**
     * In any given moment this method can be running at most once per artifact
     */
    public void pulse(String artifactName) {
        Artifact artifact = byName.get(artifactName);
        if (artifact == null) {
            // Should not happen, only we're allowed to set it to null and wouldn't pulse after that
            throw new IllegalStateException("pulsed on nonexistent artifact");
        }
        ArtifactJob job = artifact.getJobQueue().poll();
        if (job != null) {
            // TODO: Job merging
            job.run(artifact);

            if (job instanceof RemoveJob) {
                byName.remove(artifactName);

                for (ArtifactJob j : artifact.getJobQueue()) {
                    if (j instanceof LocateJob) {
                        locate(artifactName);
                        // TODO: add the rest of the queue to the new artifact?
                        break;
                    }
                }

                return; // Don't requeue ourselves;
            }
        }
        enqueuePulse(artifactName);
    }

    /**
     * Makes some thread call {@link #pulse(String)} for the given artifact soon.
     * The definition of "soon" is implementation-specific.
     */
    protected void enqueuePulse(String artifactName) {

    }
}
