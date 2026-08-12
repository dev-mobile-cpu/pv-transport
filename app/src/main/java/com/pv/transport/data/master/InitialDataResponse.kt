package com.pv.transport.data.master

import com.google.gson.annotations.SerializedName

data class InitialDataResponse(
    @SerializedName("data") val data: InitialData? = null
)

/**
 * The lists are omitted by the server when nothing changed since the `since` we sent, so a null
 * list means "keep what is already stored" rather than "the list is empty".
 */
data class InitialData(
    @SerializedName("update") val update: Long? = null,
    @SerializedName("type_of_costs") val typeOfCosts: List<MasterIdName>? = null,
    @SerializedName("reasons") val reasons: List<MasterIdValue>? = null,
    @SerializedName("trip_types") val tripTypes: List<MasterIdValue>? = null,
    @SerializedName("fuel_types") val fuelTypes: List<MasterIdName>? = null,
    @SerializedName("fuel_companies") val fuelCompanies: List<MasterFuelCompany>? = null
) {
    val hasLists: Boolean
        get() = typeOfCosts != null || reasons != null || tripTypes != null ||
                fuelTypes != null || fuelCompanies != null
}

data class MasterIdName(
    @SerializedName("id") val id: String? = null,
    @SerializedName("name") val name: String? = null
)

data class MasterIdValue(
    @SerializedName("id") val id: String? = null,
    @SerializedName("value") val value: String? = null
)

data class MasterFuelCompany(
    @SerializedName("id") val id: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("phone") val phone: String? = null,
    @SerializedName("email") val email: String? = null,
    @SerializedName("address") val address: String? = null
)
