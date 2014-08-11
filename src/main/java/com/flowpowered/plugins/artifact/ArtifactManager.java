package com.flowpowered.plugins.artifact;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Future;

import com.flowpowered.commons.SimpleFuture;

public class ArtifactManager {
    private final ConcurrentMap<String, Artifact> byName = new ConcurrentHashMap<>();

    /**
     * Makes the Manager try to find the artifact and start tracking it.
     * @return some Future, whose type will be specified once I figure out what I want it to be
     */
    public Future<?> locate(String artifactName) {
        Artifact newArtifact = new Artifact();
        Artifact artifact = byName.putIfAbsent(artifactName, newArtifact);
        SimpleFuture<?> future = new SimpleFuture<>();
        if (artifact == null) {
            newArtifact.future = future;
            enqueuePulse(artifactName);
            return future;
        }
        future.setResult(null);
        return future;
    }

    @SuppressWarnings("fallthrough")
    public Future<?> load(String artifactName) {
        Artifact artifact = byName.get(artifactName);
        if (artifact == null) {
            return null;
        }
        switch (artifact.getState()) {
            case UNDEFINED:
            case REMOVED:
                return null;
            case LOCATED:
            case UNLOAD_REQUESTED:
            case REMOVE_REQUESTED:
            case UNLOADING:
            case UNLOADED:
                artifact.state = ArtifactLoadState.LOAD_REQUESTED;
                if (artifact.future != null) artifact.future.cancel(true);
                artifact.future = new SimpleFuture<>();
            case LOAD_REQUESTED:
            case LOADING:
            case LOADED:
                return artifact.future;
            default:
                throw new IllegalStateException("Unhandled state: " + artifact.getState());
        }
    }

    /**
     * In any given moment this method can be running at most once per artifact
     */
    public void pulse(String artifactName) {
        Artifact artifact = byName.get(artifactName);
        if (artifact == null) {
            return;
        }
        switch (artifact.getState()) {
            case UNDEFINED:
                // TODO: locate
                artifact.future.setResult(null);
                artifact.future = null;
                artifact.state = ArtifactLoadState.LOCATED;
                break;
            case LOAD_REQUESTED:
                SimpleFuture<?> loadFuture = artifact.future;
                artifact.state = ArtifactLoadState.LOADING;
                // TODO: load
                loadFuture.setResult(null);
                artifact.state = ArtifactLoadState.LOADED;
                break;
            case UNLOAD_REQUESTED:
                SimpleFuture<?> unloadFuture = artifact.future;
                artifact.state = ArtifactLoadState.UNLOADING;
                // TODO: unloaded
                unloadFuture.setResult(null);
                artifact.state = ArtifactLoadState.UNLOADED;
                break;
            case REMOVE_REQUESTED:
                SimpleFuture<?> removeFuture = artifact.future;
                artifact.state = ArtifactLoadState.REMOVED;
                byName.remove(artifactName);
                removeFuture.setResult(null);
                break;

            case LOCATED:
            case LOADED:
            case UNLOADED:
                // The Artifact is "stable" and shouldn't change
                break;
            case LOADING:
            case UNLOADING:
                throw new IllegalStateException("Pulsing the same artifact multiple times at once!");
            case REMOVED:
                throw new IllegalStateException("Removed artifact being pulsed!");
            default:
                throw new IllegalStateException("Unhandled state: " + artifact.getState());
        }
        enqueuePulse(artifactName);
    }

    /**
     * Makes some thread call {@link #pulse(String)} for the given artifact soon.
     * The definition of "soon" is implementation-specific.
     */
    protected void enqueuePulse(String artifactName) {
        // Allows an artifact to only be queued once
        // aLinkedHashSet.add(artifactName);
    }
}
