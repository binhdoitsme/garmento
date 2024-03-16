package org.garmento.tryon.vtryon

import org.garmento.tryon.users.UserId
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class TryOnSessionTests {
    private val userId = UserId("UserIDTest")
    @Test
    fun `createTryOn adds a TryOn job to the session`() {
        // Arrange
        val session = TryOnSession(userId)
        val referenceImageId = ImageId()
        val garmentImageId = ImageId()

        // Act
        val tryOnId = session.createTryOn(referenceImageId, garmentImageId)

        // Assert
        assertEquals(1, session.tryOnJobs.size)
        assertEquals(referenceImageId, session.tryOnJobs[tryOnId]?.referenceImage)
        assertEquals(garmentImageId, session.tryOnJobs[tryOnId]?.garmentImage)
    }

    @Test
    fun `createTryOn returns the ID of the created TryOn job`() {
        // Arrange
        val session = TryOnSession(userId)
        val referenceImageId = ImageId()
        val garmentImageId = ImageId()

        // Act
        val tryOnId = session.createTryOn(referenceImageId, garmentImageId)

        // Assert
        assertEquals(1, session.tryOnJobs.size)
        assertEquals(tryOnId, session.tryOnJobs.keys.first())
    }

    @Test
    fun `process updates the TryOn job status`() {
        // Arrange
        val session = TryOnSession(userId)
        val referenceImageId = ImageId()
        val garmentImageId = ImageId()
        val tryOnId = session.createTryOn(referenceImageId, garmentImageId)

        // Act
        session.process(tryOnId)

        // Assert
        assert(session.tryOnJobs[tryOnId]?.status == TryOnStatus.IN_PROGRESS)
        // Add more assertions based on your logic
    }

    @Test
    fun `process throws TryOnJobNotFound if TryOn job not found`() {
        // Arrange
        val session = TryOnSession(userId)
        val tryOnId = TryOnId()

        // Act & Assert
        val exception = assertThrows(TryOnJobNotFound::class.java) {
            session.process(tryOnId)
        }
        assert("$tryOnId" in exception.message!!)
    }

    @Test
    fun `completedWithResult updates the TryOn job as succeeded with result`() {
        // Arrange
        val session = TryOnSession(userId)
        val referenceImageId = ImageId()
        val garmentImageId = ImageId()
        val resultImageId = ImageId()
        val tryOnId = session.createTryOn(referenceImageId, garmentImageId)

        // Act
        session.completedWithResult(tryOnId, resultImageId)

        // Assert
        val tryOnJob = session.tryOnJobs[tryOnId]
        assertNotNull(tryOnJob)
        assertEquals(TryOnStatus.SUCCEEDED, tryOnJob!!.status)
        assertEquals(resultImageId, tryOnJob.result)
    }

    @Test
    fun `completedWithResult throws exception if TryOn job not found`() {
        // Arrange
        val session = TryOnSession(userId)
        val tryOnId = TryOnId()

        // Act & Assert
        val exception = assertThrows(TryOnJobNotFound::class.java) {
            session.completedWithResult(tryOnId, ImageId())
        }
        assert("$tryOnId" in exception.message!!)
    }

    @Test
    fun `getResult throws NotCompleted if TryOn job not completed`() {
        // Arrange
        val session = TryOnSession(userId)
        val referenceImageId = ImageId()
        val garmentImageId = ImageId()
        val tryOnId = session.createTryOn(referenceImageId, garmentImageId)

        // Act & Assert
        val exception = assertThrows(NotCompleted::class.java) {
            session.getResult(tryOnId)
        }
        assert("$tryOnId" in exception.message!!)
    }

    @Test
    fun `getResult returns the result of completed TryOn job`() {
        // Arrange
        val session = TryOnSession(userId)
        val referenceImageId = ImageId()
        val garmentImageId = ImageId()
        val resultImageId = ImageId()
        val tryOnId = session.createTryOn(referenceImageId, garmentImageId)
        session.completedWithResult(tryOnId, resultImageId)
        print(session)

        // Act
        val result = session.getResult(tryOnId)

        // Assert
        assertNotNull(result)
        assertEquals(resultImageId, result)
    }
}