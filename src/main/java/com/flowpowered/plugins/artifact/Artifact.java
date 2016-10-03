package com.flowpowered.plugins.artifact;

import com.flowpowered.commons.SimpleFuture;

public class Artifact {
    protected volatile SimpleFuture<?> future;
    protected volatile ArtifactLoadState state = ArtifactLoadState.UNDEFINED;

    public ArtifactLoadState getState() {
        return state;
    }
}
