package com.pij.horrocks;

import android.support.annotation.NonNull;

/**
 * <p>Created on 16/11/2017.</p>
 *
 * @author PierreJean
 */
public interface View<M> {

    void display(@NonNull M model);
}