package com.flowpowered.plugins.artifact;

/**
 *                       UNDEFINED -> LOCATED --> LOAD_REQUESTED -----> LOADING -> LOADED
 *                                              /|     /\       /\           |      /\
 *                                             /      |        |            |       |
 *                                            /       |        |            |       |
 *                              --------------->      |        |            \/      |
 *  REMOVED <- REMOVE_REQUESTED                  UNLOADED <- UNLOADING <- UNLOAD_REQUESTED
 *                              <--------------
 */
public enum ArtifactLoadState {
    UNDEFINED,

    LOCATED,

    LOAD_REQUESTED,
    LOADING,
    LOADED,

    UNLOAD_REQUESTED,
    UNLOADING,
    UNLOADED,

    REMOVE_REQUESTED,
    REMOVED;
}