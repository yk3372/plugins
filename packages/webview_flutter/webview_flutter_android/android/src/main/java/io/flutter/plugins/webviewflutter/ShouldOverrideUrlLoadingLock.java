package io.flutter.plugins.webviewflutter;

import androidx.annotation.Nullable;
import androidx.core.util.Pair;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Synchronously decide if Android WebView should load a URL or not.
 *
 * This solves a class of issues when the WebView loses "context" that
 * a subsequent page load is the same as what was attempted to be loaded previously.
 * This solves a bug where a HTTP redirect in combination with history manipulations
 * causes a user to be stuck and prevented from going back.
 *
 * Since WebView requests are allowed to happen normally,
 * debugging the WebView and tracking redirects and page load initiators
 * is more accurate and easier.
 * This will also bypass bridge latency and provide a faster navigation.
 *
 * To do this, we must lock in the shouldOverrideUrlLoading callback and send an event to Flutter.
 *
 */
class ShouldOverrideUrlLoadingLock {
    protected enum ShouldOverrideCallbackState {
        UNDECIDED,
        SHOULD_OVERRIDE,
        DO_NOT_OVERRIDE,
    }

    private int nextLockIdentifier = 1;
    private final HashMap<Integer, AtomicReference<ShouldOverrideCallbackState>> shouldOverrideLocks
            = new HashMap<>();

    public synchronized Pair<Integer, AtomicReference<ShouldOverrideCallbackState>> getNewLock() {
        final int lockIdentifier = nextLockIdentifier++;
        final AtomicReference<ShouldOverrideCallbackState> shouldOverride
                = new AtomicReference<>(ShouldOverrideCallbackState.UNDECIDED);
        shouldOverrideLocks.put(lockIdentifier, shouldOverride);
        return new Pair<>(lockIdentifier, shouldOverride);
    }

    @Nullable
    public synchronized AtomicReference<ShouldOverrideCallbackState> getLock(Integer lockIdentifier) {
        return shouldOverrideLocks.get(lockIdentifier);
    }

    public synchronized void removeLock(Integer lockIdentifier) {
        shouldOverrideLocks.remove(lockIdentifier);
    }
}
