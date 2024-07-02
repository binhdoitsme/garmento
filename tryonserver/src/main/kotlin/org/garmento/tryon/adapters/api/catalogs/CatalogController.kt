package org.garmento.tryon.adapters.api.catalogs

import org.garmento.tryon.adapters.api.catalogs.CatalogRequests.CatalogAction
import org.garmento.tryon.adapters.api.catalogs.CatalogRequests.CreateCatalogRequest
import org.garmento.tryon.adapters.api.catalogs.CatalogRequests.CreatedByUser
import org.garmento.tryon.adapters.api.catalogs.CatalogRequests.ImageListRequest
import org.garmento.tryon.adapters.api.catalogs.CatalogResponses.CatalogResponse
import org.garmento.tryon.adapters.api.catalogs.CatalogResponses.CatalogWithImagesResponse
import org.garmento.tryon.services.assets.ImageId
import org.garmento.tryon.services.assets.ImageRepository
import org.garmento.tryon.services.auth.User
import org.garmento.tryon.services.catalogs.Catalog
import org.garmento.tryon.services.catalogs.CatalogId
import org.garmento.tryon.services.catalogs.CatalogServices
import org.garmento.tryon.services.users.UserId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.net.URI

@RestController
@RequestMapping("/catalogs")
class CatalogController @Autowired constructor(
    private val catalogServices: CatalogServices,
    private val imageRepository: ImageRepository,
) {
    private fun getCurrentUser(authentication: Authentication) =
        (authentication.principal as? User)?.apply { println("currentUser" to this) }
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Cannot get catalogs")

    private fun requireManager(user: User, action: () -> Unit) {
        if (user.role.name != "MANAGER") {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST, "Cannot update catalog"
            )
        }
        action()
    }

    private fun requireOwnCatalog(user: User, catalog: Catalog, action: () -> Unit) {
        if (catalog.createdBy.value != user.id) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST, "Cannot update catalog"
            )
        }
        action()
    }

    @PostMapping
    fun createCatalog(
        @RequestBody body: CreateCatalogRequest,
        authentication: Authentication,
    ) = getCurrentUser(authentication).let { user ->
        catalogServices.create(body.name, UserId(user.id)).run {
            CatalogResponse(
                id = id.value,
                name = name,
                status = status.value,
                createdBy = CreatedByUser(user.name),
                thumbnail = imageAssets.firstOrNull()?.url,
            )
        }
    }


    @GetMapping
    fun getCatalogs(
        @RequestParam("page", defaultValue = "1") page: Int,
        @RequestParam("pageSize", defaultValue = "24") pageSize: Int,
        authentication: Authentication,
    ) = getCurrentUser(authentication).let { user ->
        println("User is in role" to user.role)
        when (user.role.name) {
            "DESIGNER" -> catalogServices.findByUser(UserId(user.id), page, pageSize)
            "MANAGER" -> catalogServices.find(page, pageSize)
            else -> throw IllegalAccessException("User role is unsupported")
        }.map { CatalogResponse.fromDomain(it, user) }.let {
            ResponseEntity.ok(it)
        }
    }

    @GetMapping("/{id}")
    fun getCatalogById(
        @PathVariable("id") id: String,
        authentication: Authentication,
    ) = getCurrentUser(authentication).let { user ->
        catalogServices.findBy(CatalogId(id))?.let {
            if (it.createdBy.value == user.id || user.role.name == "MANAGER" || user.role.name == "service") {
                CatalogWithImagesResponse.fromDomain(it, user)
                    .let { body -> ResponseEntity.ok(body) }
            } else null
        } ?: throw ResponseStatusException(
            HttpStatus.NOT_FOUND, "Catalog is not found"
        )
    }

    @PostMapping("/{id}/assets")
    fun addImagesToCatalog(
        @PathVariable("id") id: String,
        @RequestBody imageListRequest: ImageListRequest,
        authentication: Authentication,
    ) = getCurrentUser(authentication).let { user ->
        val catalog =
            catalogServices.findBy(CatalogId(id)) ?: throw ResponseStatusException(
                HttpStatus.NOT_FOUND, "Catalog is not found"
            )

        requireOwnCatalog(user, catalog) {
            imageListRequest.imageURLs.map {
                    imageRepository.save(
                        url = it,
                        id = ImageId(id)
                    )
                }.let { images ->
                    catalogServices.addImages(images, catalog)
                }.let { ResponseEntity.status(HttpStatus.NO_CONTENT).build<Void>() }
        }
    }

    @DeleteMapping("/{id}/assets")
    fun removeImagesFromCatalog(
        @PathVariable("id") id: String,
        @RequestBody imageListRequest: ImageListRequest,
        authentication: Authentication,
    ) = getCurrentUser(authentication).let { user ->
        val catalog =
            catalogServices.findBy(CatalogId(id)) ?: throw ResponseStatusException(
                HttpStatus.NOT_FOUND, "Catalog is not found"
            )

        requireOwnCatalog(user, catalog) {
            imageListRequest.imageURLs.map(::ImageId).let { imageIds ->
                catalogServices.removeImages(imageIds, catalog)
            }
        }
    }.let { ResponseEntity.status(HttpStatus.NO_CONTENT).build<Void>() }

    @PatchMapping("/{id}")
    fun performActionOnCatalog(
        @PathVariable("id") id: String,
        @RequestParam("action") action: CatalogAction,
        authentication: Authentication,
    ) = getCurrentUser(authentication).let { user ->
        val catalog =
            catalogServices.findBy(CatalogId(id)) ?: throw ResponseStatusException(
                HttpStatus.NOT_FOUND, "Catalog is not found"
            )
        when (action) {
            CatalogAction.SUBMIT -> requireOwnCatalog(user, catalog) {
                catalogServices.submit(catalog)
            }

            CatalogAction.APPROVE -> requireManager(user) {
                catalogServices.approve(catalog)
            }

            CatalogAction.UNAPPROVE -> requireManager(user) {
                catalogServices.unapprove(catalog)
            }

            CatalogAction.PUBLISH -> requireOwnCatalog(user, catalog) {
                catalogServices.publish(catalog)
            }
        }
    }.let { ResponseEntity.status(HttpStatus.NO_CONTENT).build<Void>() }

    @DeleteMapping("/{id}")
    fun deleteCatalog(
        @PathVariable("id") id: String,
        authentication: Authentication,
    ) = getCurrentUser(authentication).let { user ->
        val catalog =
            catalogServices.findBy(CatalogId(id)) ?: throw ResponseStatusException(
                HttpStatus.NOT_FOUND, "Catalog is not found"
            )
        requireOwnCatalog(user, catalog) { catalogServices.delete(catalog.id) }
    }.let { ResponseEntity.status(HttpStatus.NO_CONTENT).build<Void>() }
}