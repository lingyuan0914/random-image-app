package com.randomimage.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ArtistDao {
    @Query("SELECT * FROM followed_artists ORDER BY followedTime DESC")
    fun getAllFollowed(): Flow<List<ArtistEntity>>

    @Query("SELECT * FROM followed_artists WHERE uid = :uid")
    suspend fun getArtist(uid: String): ArtistEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun followArtist(artist: ArtistEntity)

    @Query("DELETE FROM followed_artists WHERE uid = :uid")
    suspend fun unfollowArtist(uid: String)

    @Query("SELECT EXISTS(SELECT 1 FROM followed_artists WHERE uid = :uid)")
    suspend fun isFollowing(uid: String): Boolean

    @Query("SELECT uid FROM followed_artists")
    suspend fun getAllFollowedUids(): List<String>
}
