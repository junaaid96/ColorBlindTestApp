package io.github.junaaid96.colorvision.feature

import androidx.annotation.StringRes
import io.github.junaaid96.colorvision.R
import io.github.junaaid96.colorvision.color.CvdType
import io.github.junaaid96.colorvision.data.TestKind
import io.github.junaaid96.colorvision.tests.Classification
import io.github.junaaid96.colorvision.tests.Severity

@StringRes fun Classification.label() = when (this) {
    Classification.TYPICAL -> R.string.cls_typical
    Classification.PROTAN -> R.string.cls_protan
    Classification.DEUTAN -> R.string.cls_deutan
    Classification.RED_GREEN -> R.string.cls_red_green
    Classification.TRITAN -> R.string.cls_tritan
    Classification.INCONCLUSIVE -> R.string.cls_inconclusive
}

@StringRes fun Classification.description() = when (this) {
    Classification.TYPICAL -> R.string.cls_typical_desc
    Classification.PROTAN -> R.string.cls_protan_desc
    Classification.DEUTAN -> R.string.cls_deutan_desc
    Classification.RED_GREEN -> R.string.cls_red_green_desc
    Classification.TRITAN -> R.string.cls_tritan_desc
    Classification.INCONCLUSIVE -> R.string.cls_inconclusive_desc
}

@StringRes fun Severity.label() = when (this) {
    Severity.NONE -> R.string.sev_none
    Severity.MILD -> R.string.sev_mild
    Severity.MODERATE -> R.string.sev_moderate
    Severity.STRONG -> R.string.sev_strong
}

@StringRes fun CvdType.label() = when (this) {
    CvdType.PROTAN -> R.string.type_protan
    CvdType.DEUTAN -> R.string.type_deutan
    CvdType.TRITAN -> R.string.type_tritan
}

@StringRes fun TestKind.label() = when (this) {
    TestKind.QUICK -> R.string.home_quick
    TestKind.FULL -> R.string.home_full
    TestKind.ARRANGEMENT -> R.string.home_arrange
    TestKind.CONTRAST -> R.string.home_contrast
    TestKind.PRACTICE -> R.string.home_practice
}
