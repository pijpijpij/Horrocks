package com.pij.horrocks;

import android.support.annotation.NonNull;

/**
 * <p>Created on 01/01/2018.</p>
 *
 * @author PierreJean
 */

public interface Result<S> {
    @NonNull
    S applyTo(@NonNull S current);
}
