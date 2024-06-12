package org.garmento.tryon.services.tryon

interface TryOnScheduler {
    fun schedule(jobId: TryOnJobId)
    fun processJob(jobId: String)
}