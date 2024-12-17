package net.explorviz.adapter.service.converter

/** Contains constants for missing attribute values of spans. */
object DefaultAttributeValues {
    const val DEFAULT_LANDSCAPE_TOKEN = "mytokenvalue"
    const val DEFAULT_LANDSCAPE_SECRET = "mytokensecret"
    const val DEFAULT_GIT_COMMIT_CHECKSUM = "UNKNOWN" // NOPMD
    const val DEFAULT_HOST_IP = "0.0.0.0" // NOPMD
    const val DEFAULT_HOST_NAME = "UNKNOWN-HOST"
    const val DEFAULT_APP_NAME = "UNKNOWN-APPLICATION"
    const val DEFAULT_APP_INSTANCE_ID = "0"
    const val DEFAULT_APP_LANG = "UNKNOWN"
    const val DEFAULT_PACKAGE_NAME = "unknownpkg"
    const val DEFAULT_CLASS_NAME = "UnknownClass"

    // FQN must adhere to the format <pkg.Class.method>, i.e., include at least two '.'
    const val DEFAULT_CLASS_FQN = "$DEFAULT_PACKAGE_NAME.$DEFAULT_CLASS_NAME"
    const val DEFAULT_METHOD = "unknownMethod"
    const val DEFAULT_FQN = "$DEFAULT_CLASS_FQN.$DEFAULT_METHOD"
}
