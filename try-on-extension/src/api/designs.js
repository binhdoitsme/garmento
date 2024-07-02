import axios, { Axios } from "axios";

/**
 * @typedef {Object} DesignResponse
 * @property {string} id
 * @property {string} url
 */

export class DesignsApi {
  /**
   * @param {AbortController} abortController
   * @param {Axios} _axios
   */
  constructor(
    abortController = new AbortController(),
    _axios = axios.create({
      withCredentials: false,
      signal: abortController.signal,
    })
  ) {
    /** @readonly */
    this.abortController = abortController;
    /** @private @readonly */
    this._axios = _axios;
  }

  /**
   * @param {string} id
   * @param {string} [endpoint="/api/catalogs"]
   * @returns {Promise<DesignResponse>}
   */
  async getDesignDetails(id, endpoint = "/api/catalogs") {
    const response = await this._axios.get(`${endpoint}/${id}`);
    return response.data?.items?.[0];
  }

  /**
   * @param {string} id
   * @returns
   */
  async getDesignAsBlob(id) {
    const url = (await this.getDesignDetails(id))?.url;
    if (!url) {
      return;
    }
    const response = await this._axios.get(url, { responseType: "blob" });
    return response.data;
  }
}
