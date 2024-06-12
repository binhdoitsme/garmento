package org.garmento.tryon.adapters.db.tryon

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TryOnRecordRepository : JpaRepository<TryOnRecord, String> {
    fun findAllByUserId(userId: String): List<TryOnRecord>
    fun findByIdAndUserId(id: String, userId: String): TryOnRecord?
}
