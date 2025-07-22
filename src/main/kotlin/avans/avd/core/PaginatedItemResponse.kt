package avans.avd.core

import kotlinx.serialization.Serializable

@Serializable
data class PaginatedItemResponse<T>(
    val data: List<T>,
    val totalCount: Int
)