package at.roteskreuz.stopcorona.model.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import at.roteskreuz.stopcorona.model.entities.session.DbDailyBatchPart
import at.roteskreuz.stopcorona.model.entities.session.DbFullBatchPart
import at.roteskreuz.stopcorona.model.entities.session.DbFullSession
import at.roteskreuz.stopcorona.model.entities.session.DbSession

/**
 * Dao to manage [DbSession].
 */
@Dao
abstract class SessionDao {

    @Insert
    protected abstract suspend fun insertSession(session: DbSession): Long

    @Insert
    protected abstract suspend fun insertFullBatch(batch: DbFullBatchPart): Long

    @Insert
    protected abstract suspend fun insertDailyBatch(batch: DbDailyBatchPart): Long

    @Query("DELETE FROM session WHERE token = :token")
    abstract suspend fun deleteSession(token: String): Int

    @Transaction
    open suspend fun insertOrUpdateFullSession(fullSession: DbFullSession) {
        val token = fullSession.session.token
        deleteSession(token)
        insertSession(fullSession.session)
        fullSession.fullBatchParts.forEach { fullBatchPath ->
            insertFullBatch(fullBatchPath.copy(token = token))
        }
        fullSession.dailyBatchesParts.forEach { dailyBatch ->
            insertDailyBatch(dailyBatch.copy(token = token))
        }
    }

    @Transaction
    @Query("SELECT * FROM session where token = :token")
    abstract suspend fun getFullSession(token: String): DbFullSession
}