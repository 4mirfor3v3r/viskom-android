package amirlabs.plate_detection

import com.google.gson.annotations.SerializedName

data class BaseResponse<MODEL>(

    @SerializedName("success")
    val success:Boolean?,

    @SerializedName("data")
    val data:MODEL?,

    @SerializedName("image_link")
    val imageLink:String?,
)


data class PredictionModel(
    @SerializedName("box")
    val box:List<Float>,
    @SerializedName("label")
    val label:Float,
    @SerializedName("conf_score")
    val confidenceScore:Float
)