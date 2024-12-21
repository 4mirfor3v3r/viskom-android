package amirlabs.miniprojectailo

import com.google.gson.annotations.SerializedName

data class BaseResponse<MODEL>(

    @SerializedName("confidence")
    val confidence:String?,

    @SerializedName("prediction")
    val data:MODEL?
)