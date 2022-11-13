package com.mnowo.transportationalarmclock.domain.models

data class GooglePredictions(
    val description: String,
    val matched_substrings: List<MatchedSubstring>,
    val place_id: String,
    val structured_formatting: StructuredFormatting,
    val types: List<String>
)

data class MatchedSubstring(
    val length: Int,
    val offset: Int
)

data class StructuredFormatting(
    val main_text: String,
    val secondary_text: String
)
