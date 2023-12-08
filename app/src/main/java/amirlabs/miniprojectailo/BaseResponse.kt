package amirlabs.miniprojectailo

import com.google.gson.annotations.SerializedName

data class BaseResponse<MODEL>(

    @SerializedName("message")
    val message:String?,

    @SerializedName("data")
    val data:MODEL?
)