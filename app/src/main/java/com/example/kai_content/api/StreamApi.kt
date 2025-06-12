package com.example.kai_content.api

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface StreamApi {
//    @GET("api/stream/status/{carriage_id}")
//    suspend fun getStreamStatus(
//        @Path("carriage_id") carriageId: Long
//    ): Response<StreamStatusResponse>

    /**
     * Mengambil status stream dan voting untuk lokasi tertentu.
     * Endpoint: GET api/status?train_id=X&carriage_id=Y
     */
    @GET("api/stream/status")
    suspend fun getStatusForLocation(
        @Query("train_id") trainId: Long,
        @Query("carriage_id") carriageId: Long
    ): Response<StreamStatusResponse>

    @POST("api/voting/vote")
    suspend fun submitVote(@Body voteRequest: VoteRequest): Response<VoteResponse>
}

data class StreamStatusResponse(
    @SerializedName("now_playing")
    val nowPlaying: StreamResponse?,
    @SerializedName("active_voting")
    val activeVoting: Voting?,
    @SerializedName("location_info")
    val locationInfo: LocationInfo?,
    @SerializedName("server_time")
    val serverTime: String
)

data class LocationInfo(
    @SerializedName("train_name")
    val trainName: String,
    @SerializedName("carriage_name")
    val carriageName: String
)

data class StreamResponse(
    @SerializedName("content")
    val content: ContentInfo?,
    @SerializedName("is_live")
    val isLive: Boolean,
    @SerializedName("sync_data")
    val syncData: SyncData?,
    @SerializedName("message")
    val message: String?
)

data class VoteRequest(
    @SerializedName("voting_option_id")
    val votingOptionId: Long
)

data class ContentInfo(
    @SerializedName("id")
    val id: Long,
    @SerializedName("title")
    val title: String,
    @SerializedName("thumbnail")
    val thumbnail: String,
    @SerializedName("description")
    val description: String?,
    @SerializedName("duration_seconds")
    val durationSeconds: Int?,
    @SerializedName("stream_url")
    val streamUrl: String?
)

data class SyncData(
    @SerializedName("playback_position")
    val playbackPosition: Double,
    @SerializedName("server_time")
    val serverTime: String?,
)

data class Voting(
    @SerializedName("id")
    val id: Long,
    @SerializedName("title")
    val title: String,
    @SerializedName("description")
    val description: String?,
    @SerializedName("end_time")
    val endTime: String,
    @SerializedName("total_votes")
    val totalVotes: Int,
    @SerializedName("has_voted")
    val hasVoted: Boolean,
    @SerializedName("options")
    val options: List<VotingOption>
)

data class VotingOption(
    @SerializedName("id")
    val id: Long,
    @SerializedName("content")
    val content: ContentInfo,
    @SerializedName("vote_count")
    val voteCount: Int,
    @SerializedName("vote_percentage")
    val votePercentage: Double
)

data class VoteResponse(
    @SerializedName("message")
    val message: String
)
