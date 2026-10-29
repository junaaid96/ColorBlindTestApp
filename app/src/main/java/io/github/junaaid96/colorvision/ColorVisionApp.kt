package io.github.junaaid96.colorvision

import android.app.Application
import io.github.junaaid96.colorvision.data.Repository

/** Manual DI: one repository for the whole process. */
class ColorVisionApp : Application() {
    val repository by lazy { Repository(this) }
}
