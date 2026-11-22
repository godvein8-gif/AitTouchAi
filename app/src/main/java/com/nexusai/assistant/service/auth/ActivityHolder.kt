package com.nexusai.assistant.service.auth

import android.app.Activity
import java.lang.ref.WeakReference

/** Firebase phone verification needs the current Activity. */
class ActivityHolder {
    private var ref: WeakReference<Activity>? = null

    var current: Activity?
        get() = ref?.get()
        set(value) {
            ref = value?.let { WeakReference(it) }
        }

    fun clear(activity: Activity) {
        if (ref?.get() === activity) ref = null
    }
}
