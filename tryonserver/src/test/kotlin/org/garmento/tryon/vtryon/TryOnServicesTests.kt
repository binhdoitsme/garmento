package org.garmento.tryon.vtryon

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.garmento.tryon.users.User
import org.garmento.tryon.users.UserId
import org.garmento.tryon.users.UserServices
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.InputStream
import java.net.URI
import java.util.*

class TryOnServicesTest {

    private lateinit var tryOnRepository: TryOnRepository
    private lateinit var tryOnStore: TryOnStore
    private lateinit var userServices: UserServices
    private lateinit var tryOnServices: TryOnServices

    private val fakeURL = URI("file:///app/file.jpg").toURL()
    private val stubImageId = ImageId()
    private val userId = UserId("user123")
    private val user = User(userId)

    @BeforeEach
    fun setUp() {
        tryOnRepository = mockk()
        tryOnStore = mockk()
        userServices = mockk()
        tryOnServices = TryOnServices(tryOnRepository, userServices, tryOnStore)
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `createSession returns TryOnSessionDTO`() {
        // Arrange
        val session = TryOnSession(user.id)
        every { userServices.findUser(userId) } returns user
        every { tryOnRepository.save(any()) } returns session
        every { tryOnStore.getPublicURL(any()) } returns fakeURL

        // Act
        val result = tryOnServices.createSession(userId)

        // Assert
        assertEquals(session.id.value, result.id)
    }

    @Test
    fun `getSession returns TryOnSessionDTO`() {
        // Arrange
        val sessionId = TryOnSessionId()
        val user = User(UserId("user123")) // Example user
        val referenceImage = ImageId(UUID.randomUUID().toString())
        val garmentImage = ImageId(UUID.randomUUID().toString())
        val tryOn = TryOn(referenceImage, garmentImage)
        val jobs = mutableMapOf(tryOn.id to tryOn)
        val session = TryOnSession(user.id, id = sessionId, tryOnJobs = jobs).also {
            val result = ImageId(UUID.randomUUID().toString())
            it.completedWithResult(tryOn.id, result)
        }
        every { userServices.findUser(userId) } returns user
        every { tryOnRepository.findByUserAndId(user.id, session.id) } returns session
        every { tryOnStore.getPublicURL(any()) } returns fakeURL

        // Act
        val result = tryOnServices.getSession(userId, sessionId)

        // Assert
        assertEquals(session.id.value, result.id)
        assertNotNull(result.tryOnJobs)
        assertEquals(1, result.tryOnJobs!!.size)
        assertEquals(fakeURL.toString(), result.tryOnJobs!!.first().referenceImageURL)
        assertEquals(fakeURL.toString(), result.tryOnJobs!!.first().garmentImageURL)
        assertEquals(fakeURL.toString(), result.tryOnJobs!!.first().resultURL)
    }

    @Test
    fun `getSession throws SessionNotFound when session not found`() {
        // Arrange
        val sessionId = TryOnSessionId()
        every { userServices.findUser(userId) } returns user
        every { tryOnRepository.findByUserAndId(user.id, sessionId) } returns null

        // Act & Assert
        assertThrows(SessionNotFound::class.java) {
            tryOnServices.getSession(userId, sessionId)
        }
    }

    // ...
    @Test
    fun `createNewTryOnJob creates and returns TryOnSessionDTO`() {
        // Arrange
        val sessionId = TryOnSessionId()
        val referenceImage = mockk<InputStream>()
        val garmentImage = mockk<InputStream>()
        val session = TryOnSession(userId)
        every { userServices.findUser(userId) } returns user
        every { tryOnRepository.save(session) } returns session
        every { tryOnRepository.findByUserAndId(any(), any()) } returns session
        every { tryOnStore.save(any(), any()) } returns stubImageId
        every { tryOnStore.getPublicURL(stubImageId) } returns fakeURL

        // Act
        val result = tryOnServices.createNewTryOnJob(
            userId, sessionId, referenceImage, garmentImage
        )

        // Assert
        assert(session.tryOnJobs.size == 1)
        assert(TryOnId(result.id) in session.tryOnJobs)
        verify { tryOnRepository.save(session) }
    }

    @Test
    fun `processingTryOnJob updates session`() {
        // Arrange
        val sessionId = TryOnSessionId()
        val tryOnId = TryOnId()
        val session = TryOnSession(userId).apply {
            createTryOn(stubImageId, stubImageId, tryOnId)
        }
        every { userServices.findUser(userId) } returns user
        every { tryOnRepository.save(session) } returns session
        every { tryOnRepository.findByUserAndId(any(), any()) } returns session
        every { tryOnStore.save(any(), any()) } returns stubImageId
        every { tryOnStore.getPublicURL(stubImageId) } returns fakeURL

        // Act
        tryOnServices.processingTryOnJob(userId, sessionId, tryOnId)

        // Assert
        assertThrows(NotCompleted::class.java) { session.getResult(tryOnId) }
        assert(session.tryOnJobs[tryOnId]?.status == TryOnStatus.IN_PROGRESS)
        verify { tryOnRepository.save(session) }
    }

    @Test
    fun `markTryOnJobSuccess updates session and returns TryOnSessionDTO`() {
        // Arrange
        val sessionId = TryOnSessionId()
        val resultImage = mockk<InputStream>()
        val tryOnId = TryOnId()
        val session = TryOnSession(userId).apply {
            createTryOn(stubImageId, stubImageId, tryOnId)
        }
        every { userServices.findUser(userId) } returns user
        every { tryOnRepository.save(session) } returns session
        every { tryOnRepository.findById(any()) } returns session
        every { tryOnStore.save(any(), any()) } returns stubImageId
        every { tryOnStore.getPublicURL(stubImageId) } returns fakeURL

        // Act
        val result = tryOnServices.markTryOnJobSuccess(sessionId, tryOnId, resultImage)

        // Assert
        verify { tryOnRepository.save(session) }
        assert(result.id == session.id.value)
    }

    @Test
    fun `markTryOnJobFailed updates session and returns TryOnSessionDTO`() {
        // Arrange
        val sessionId = TryOnSessionId()
        val tryOnId = TryOnId()
        val session = TryOnSession(userId).apply {
            createTryOn(stubImageId, stubImageId, tryOnId)
        }
        every { tryOnRepository.save(session) } returns session
        every { tryOnRepository.findById(any()) } returns session
        every { tryOnStore.save(any(), any()) } returns stubImageId
        every { tryOnStore.getPublicURL(stubImageId) } returns fakeURL

        // Act
        val result = tryOnServices.markTryOnJobFailed(sessionId, tryOnId)

        // Assert
        verify { tryOnRepository.save(session) }
        assert(result.id == session.id.value)
    }
}
