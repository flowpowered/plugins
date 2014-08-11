package com.flowpowered.plugins.artifact.jobs;

import com.flowpowered.plugins.artifact.Artifact;
import com.flowpowered.plugins.artifact.ArtifactState;

public class LocateJob extends AbstractJob {
    @Override
    public void run(Artifact artifact) {
        // TODO Auto-generated method stub
        artifact.setState(ArtifactState.LOCATED);
        future.setResult(null);
    }

}
