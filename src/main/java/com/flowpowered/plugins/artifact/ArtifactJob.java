package com.flowpowered.plugins.artifact;

import com.flowpowered.commons.SimpleFuture;

public interface ArtifactJob {

    SimpleFuture<?> getFuture();

    void run(Artifact artifact);
}
