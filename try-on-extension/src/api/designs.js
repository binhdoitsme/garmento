import axios, { Axios } from "axios";

/**
 * @typedef {Object} DesignResponse
 * @property {string} id
 * @property {string} url
 */

export class DesignsApi {
  /**
   * @param {string} backendHost
   * @param {AbortController} abortController
   * @param {Axios} _axios
   */
  constructor(
    backendHost,
    abortController = new AbortController(),
    _axios = axios.create({
      withCredentials: false,
      baseURL: backendHost,
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
   * @param {string} [endpoint="/api/designs"]
   * @returns {Promise<DesignResponse>}
   */
  async getDesignDetails(id, endpoint = "/api/designs") {
    const response = await this._axios.get(`${endpoint}/${id}`);
    return response.data;
  }

  async getDesignAsBlob(id) {
    const { url } = await this.getDesignDetails(id);
    const response = await this._axios.get(url, { responseType: "blob" });
    return response.data;
  }
}
