package com.pij.horrocks;

import io.reactivex.Observable;

/**
 * <p>Created on 29/12/2017.</p>
 *
 * @author PierreJean
 */

public interface Engine<S, M> {

    Observable<M> runWith(Configuration<S, M> configuration);
}
