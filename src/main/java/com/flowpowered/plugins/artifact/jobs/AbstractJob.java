package com.flowpowered.plugins.artifact.jobs;

import com.flowpowered.commons.SimpleFuture;

import com.flowpowered.plugins.artifact.ArtifactJob;

public abstract class AbstractJob implements ArtifactJob {
    protected SimpleFuture<?> future = new SimpleFuture<>();

    @Override
    public SimpleFuture<?> getFuture() {
        return future;
    }

}