package com.example.kai_content.api

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface StreamApi {
    @GET("api/stream/status/{carriage_id}")
    suspend fun getStreamStatus(
        @Path("carriage_id") carriageId: Long
    ): Response<StreamStatusResponse>

    @POST("api/voting/vote")
    suspend fun submitVote(@Body voteRequest: VoteRequest): Response<VoteResponse>
}
data class Carriage(
    @SerializedName("id")
    val id: Long,
    @SerializedName("name")
    val name: String
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
    val durationSeconds: Int? // Added this field
)

data class StreamResponse(
    @SerializedName("content")
    val content: ContentInfo, // Ini adalah konten yang SEDANG tayang
    @SerializedName("stream_url")
    val streamUrl: String?,
    @SerializedName("sync_data")
    val syncData: SyncData?,
    @SerializedName("is_live")
    val isLive: Boolean,
    @SerializedName("next_content") // Ini adalah konten yang AKAN tayang
    val nextContent: NextContentInStreamResponse?
)

data class NextContentInStreamResponse( // PERBAIKI DATA CLASS INI
    @SerializedName("id")
    val id: Long,
    @SerializedName("title")
    val title: String,
    @SerializedName("thumbnail")
    val thumbnail: String,
    @SerializedName("scheduled_time")
    val scheduledTime: String,
    @SerializedName("countdown_seconds")
    val countdownSeconds: Double?,
    @SerializedName("description") // <--- TAMBAH INI
    val description: String?,
    @SerializedName("duration_seconds") // <--- TAMBAH INI
    val durationSeconds: Int?
)

data class SyncData(
    @SerializedName("server_time")
    val serverTime: String,
    @SerializedName("playback_position")
    val playbackPosition: Double,
    @SerializedName("segment_duration")
    val segmentDuration: Int
)

data class VotingResponse(
    @SerializedName("voting")
    val voting: Voting?
)

data class Voting(
    @SerializedName("id")
    val id: Long,
    @SerializedName("title")
    val title: String,
    @SerializedName("description")
    val description: String?,
    @SerializedName("end_time")
    val endTime: String, // non-nullable
    @SerializedName("total_votes")
    val totalVotes: Int,
    @SerializedName("has_voted")
    val hasVoted: Boolean, // non-nullable
    @SerializedName("options")
    val options: List<VotingOption>
)

data class VotingOption(
    @SerializedName("id")
    val id: Long,
    @SerializedName("content")
    val content: ContentInfo, // Use ContentInfo directly
    @SerializedName("vote_count")
    val voteCount: Int,
    @SerializedName("vote_percentage")
    val votePercentage: Int // Should be integer percentage
)

// Vote Submit Models
data class VoteResponse(
    @SerializedName("message")
    val message: String,
    @SerializedName("option_id")
    val optionId: Long,
    @SerializedName("new_vote_count")
    val newVoteCount: Int,
    @SerializedName("updated_voting_options") // Added this to directly update UI
    val updatedVotingOptions: List<VotingOptionUpdate>?
)

data class VotingOptionUpdate( // New data class for updated vote counts
    @SerializedName("id")
    val id: Long,
    @SerializedName("vote_count")
    val voteCount: Int,
    @SerializedName("vote_percentage")
    val votePercentage: Int
)

// Stream Status Response (Main endpoint for client sync)
data class StreamStatusResponse(
    @SerializedName("now_playing")
    val nowPlaying: StreamResponse?, // Can be null if no stream is active or scheduled
    @SerializedName("active_voting")
    val activeVoting: Voting?,
    @SerializedName("carriage")
    val carriage: Carriage?,
    @SerializedName("server_time")
    val serverTime: String // Include server time for client-side sync if needed
)
