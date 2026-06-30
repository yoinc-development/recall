package ch.yoinc.recall.data

import ch.yoinc.recall.Recall
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RecallRepository(private val dao: RecallDao) {
    fun getAllRecalls(): Flow<List<Recall>> = dao.getAll().map { list -> list.map { it.toRecall() } }

    suspend fun insert(recall: Recall) = dao.insert(recall.toEntity())

    suspend fun delete(id: String) = dao.delete(id)
}
