package org.garmento.tryon.adapters.db.tryon

import org.garmento.tryon.services.tryon.TryOnJob
import org.garmento.tryon.services.tryon.TryOnJobId
import org.garmento.tryon.services.tryon.TryOnJobRepository
import org.garmento.tryon.services.users.UserId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository

@Component
class TryOnRepositoryOnJpa @Autowired constructor(
    private val repository: TryOnRecordRepository,
) : TryOnJobRepository {

    override fun save(job: TryOnJob) {
        repository.save(TryOnRecord.fromDomain(job))
    }

    override fun findById(id: TryOnJobId) =
        repository.findById(id.value).orElse(null)?.toDomain()

    override fun findByIdAndUserId(id: TryOnJobId, userId: UserId) =
        repository.findByIdAndUserId(id.value, userId.value)?.toDomain()

    override fun findAllByUserId(userId: UserId): List<TryOnJob> =
        repository.findAllByUserId(userId.toString()).map { it.toDomain() }
}