package org.garmento.tryon.vtryon

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import org.garmento.tryon.users.User
import org.garmento.tryon.users.UserId
import org.garmento.tryon.users.UserServices
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.net.URI
import java.util.*

class TryOnServicesTest {

    private lateinit var tryOnRepository: TryOnRepository
    private lateinit var tryOnStore: TryOnStore
    private lateinit var userServices: UserServices
    private lateinit var tryOnServices: TryOnServices

    private val fakeURL = URI("file:///app/file.html").toURL()

    @BeforeEach
    fun setUp() {
        tryOnRepository = mockk()
        tryOnStore = mockk()
        userServices = mockk()
        tryOnServices = TryOnServices(tryOnRepository, tryOnStore, userServices)
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `createSession returns TryOnSessionDTO`() {
        // Arrange
        val userId = "user123"
        val user = User(UserId(userId)) // Example user
        val session = TryOnSession(user.id)
        every { userServices.findUser(userId) } returns user
        every { tryOnRepository.save(any()) } returns session
        every { tryOnStore.getPublicURL(any()) } returns fakeURL

        // Act
        val result = tryOnServices.createSession(userId)

        // Assert
        assertEquals(session.id.value.toString(), result.id)
    }

    @Test
    fun `getSession returns TryOnSessionDTO`() {
        // Arrange
        val userId = "user123"
        val sessionId = UUID.randomUUID()
        val user = User(UserId("user123")) // Example user
        val tryOn = TryOn(ImageId(UUID.randomUUID()), ImageId(UUID.randomUUID()))
        val jobs = mutableMapOf(tryOn.id to tryOn)
        val session = TryOnSession(user.id, id = TryOnSessionId(sessionId), tryOnJobs = jobs).also {
            it.completedWithResult(tryOn.id, ImageId(UUID.randomUUID()))
        }
        every { userServices.findUser(userId) } returns user
        every { tryOnRepository.findByUserAndId(user.id, session.id) } returns session
        every { tryOnStore.getPublicURL(any()) } returns fakeURL

        // Act
        val result = tryOnServices.getSession(userId, sessionId.toString())

        // Assert
        assertEquals(session.id.value.toString(), result.id)
        assertNotNull(result.tryOnJobs)
        assertEquals(1, result.tryOnJobs!!.size)
        assertEquals(fakeURL.toString(), result.tryOnJobs!!.first().referenceImageURL)
        assertEquals(fakeURL.toString(), result.tryOnJobs!!.first().garmentImageURL)
        assertEquals(fakeURL.toString(), result.tryOnJobs!!.first().resultURL)
    }

    @Test
    fun `getSession throws SessionNotFound when session not found`() {
        // Arrange
        val userId = "user123"
        val sessionId = UUID.randomUUID()
        val user = User(UserId("user123")) // Example user
        every { userServices.findUser(userId) } returns user
        every { tryOnRepository.findByUserAndId(user.id, TryOnSessionId(sessionId)) } returns null

        // Act & Assert
        assertThrows(SessionNotFound::class.java) {
            tryOnServices.getSession(userId, sessionId.toString())
        }
    }
}
