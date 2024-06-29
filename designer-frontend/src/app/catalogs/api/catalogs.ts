import axios, { Axios } from "axios";

export interface Catalog {
  id: string;
  name: string;
  status: "DRAFT" | "SUBMITTED" | "APPROVED" | "PUBLISHED";
  createdBy: { name: string };
  thumbnail: string;
}

export class CatalogApi {
  constructor(
    readonly abortController = new AbortController(),
    private readonly _axios: Axios = axios.create({
      withCredentials: false,
      baseURL: process.env.NEXT_PUBLIC_TRY_ON_BACKEND_HOST,
      signal: abortController.signal,
    })
  ) {}

  async listCatalogs(endpoint = "/api/catalogs") {
    const response = await this._axios.get<Catalog[]>(endpoint);
    return response.data;
  }

  async createCatalog(name: string, endpoint = "/api/catalogs") {
    const response = await this._axios.post<Catalog>(endpoint, { name });
    return response.data;
  }

  async getCatalogDetails(id: string, endpoint = "/api/catalogs") {
    const response = await this._axios.get<
      Catalog & { items: { id: string; url: string }[] }
    >(`${endpoint}/${id}`);
    return response.data;
  }

  async addImageToCatalog(
    id: string,
    imageIds: string[],
    endpoint = "/api/catalogs/{id}/assets"
  ) {
    await this._axios.post(endpoint.replace("{id}", id), { imageIds });
  }

  async removeImageFromCatalog(
    id: string,
    imageIds: string[],
    endpoint = "/api/catalogs/{id}/assets"
  ) {
    await this._axios.request({
      method: "DELETE",
      url: endpoint.replace("{id}", id),
      data: { imageIds },
    });
  }

  async performActionOnCatalog(
    id: string,
    action: "SUBMIT" | "APPROVE" | "UNAPPROVE" | "PUBLISH",
    endpoint = "/api/catalogs"
  ) {
    await this._axios.patch(`${endpoint}/${id}?action=${action}`);
  }
  
  async deleteCatalog(id: string, endpoint = "/api/catalogs") {
    const response = await this._axios.delete(`${endpoint}/${id}`);
    return response.data;
  }
}
