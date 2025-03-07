package net.explorviz.adapter.service.converter

/** Contains constants for missing attribute values of spans. */
object DefaultAttributeValues {
    const val DEFAULT_LANDSCAPE_TOKEN = "mytokenvalue"
    const val DEFAULT_LANDSCAPE_SECRET = "mytokensecret"
    const val DEFAULT_APP_NAME = "UNKNOWN-APPLICATION"
    const val DEFAULT_PACKAGE_NAME = "unknownpkg"
    const val DEFAULT_CLASS_NAME = "UnknownClass"

    const val DEFAULT_NAMESPACE = "$DEFAULT_PACKAGE_NAME.$DEFAULT_CLASS_NAME"
    const val DEFAULT_FUNCTION_NAME = "unknownMethod"
}
