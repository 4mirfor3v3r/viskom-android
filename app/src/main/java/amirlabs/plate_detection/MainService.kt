package amirlabs.plate_detection

import io.reactivex.rxjava3.core.Single
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

@JvmSuppressWildcards
interface MainService {
    @POST("detect-image")
    fun submitImage(@Body body: MultipartBody): Single<BaseResponse<List<PredictionModel>>>
}