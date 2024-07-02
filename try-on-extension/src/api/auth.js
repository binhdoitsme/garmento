import axios, { Axios } from "axios";

export class AuthApi {
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
   * @param {string} [endpoint="/api/service-tokens"]
   */
  async authenticateAsService(endpoint = "/api/service-tokens") {
    await this._axios.post(endpoint);
  }
}
