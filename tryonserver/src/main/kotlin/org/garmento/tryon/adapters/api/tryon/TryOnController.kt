package org.garmento.tryon.adapters.api.tryon

import jakarta.servlet.http.HttpServletRequest
import org.garmento.tryon.services.tryon.TryOnServices
import org.garmento.tryon.services.users.UserServices
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.server.ResponseStatusException
import java.io.BufferedInputStream

@RestController
@RequestMapping("/try-ons")
class TryOnController @Autowired constructor(
    private val services: TryOnServices,
    private val userServices: UserServices,
) {
    private fun getCurrentUser(request: HttpServletRequest) =
        request.userPrincipal?.name?.let { username ->
            userServices.findUserByEmail(username)
        }

    @GetMapping("/{id}")
    fun getTryOnJob(@PathVariable("id") id: String) =
        services.findJobById(id)?.let(TryOnResponse::convert)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

    @PostMapping
    fun createTryOnJob(
        @RequestParam("garmentImage") garmentImage: MultipartFile,
        @RequestParam("preset") preset: String?,
        @RequestParam("referenceImage") referenceImage: MultipartFile?,
    ) = run {
        if (referenceImage == null && preset == null) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST)
        }
        if (referenceImage != null) {
            services.createJob(
                referenceImage = BufferedInputStream(referenceImage.inputStream),
                garmentImage = BufferedInputStream(garmentImage.inputStream),
            )
        } else {
            services.createJobForPreset(
                garmentImage = BufferedInputStream(garmentImage.inputStream),
                preset = preset!!,
            )
        }.let(TryOnResponse::convert)
    }
}