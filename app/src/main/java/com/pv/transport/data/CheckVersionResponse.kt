package com.pv.transport.data

import com.google.gson.annotations.SerializedName

data class CheckVersionResponse(
    @SerializedName("latest_version_code") val latestVersionCode: Int,
    @SerializedName("latest_version_name") val latestVersionName: String,
    @SerializedName("download_url") val downloadUrl: String,
    @SerializedName("is_force_update") val forceUpdate: Boolean,
    @SerializedName("update_message") val updateMessage: String
)