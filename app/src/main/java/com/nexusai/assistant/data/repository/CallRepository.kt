package com.nexusai.assistant.data.repository

import com.nexusai.assistant.data.local.CallDao
import com.nexusai.assistant.data.local.CallRecordEntity
import com.nexusai.assistant.domain.model.CallRecord
import com.nexusai.assistant.domain.model.CallStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CallRepository(private val dao: CallDao) {

    fun observeAll(): Flow<List<CallRecord>> =
        dao.observeAll().map { list -> list.map { it.toModel() } }

    suspend fun save(call: CallRecord): Long = dao.insert(call.toEntity())

    suspend fun delete(id: Long) = dao.delete(id)

    suspend fun deleteAll() = dao.deleteAll()

    private fun CallRecordEntity.toModel() = CallRecord(
        id = id,
        callerLabel = callerLabel,
        startedAt = startedAt,
        durationSec = durationSec,
        status = runCatching { CallStatus.valueOf(status) }.getOrDefault(CallStatus.Completed),
        summary = summary,
        callbackMessage = callbackMessage,
        isDemo = isDemo
    )

    private fun CallRecord.toEntity() = CallRecordEntity(
        id = id,
        callerLabel = callerLabel,
        startedAt = startedAt,
        durationSec = durationSec,
        status = status.name,
        summary = summary,
        callbackMessage = callbackMessage,
        isDemo = isDemo
    )
}
