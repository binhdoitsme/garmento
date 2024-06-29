import axios, { Axios } from "axios";

/**
 * @typedef {Object} TryOnResponse
 * @property {string} id
 * @property {"PENDING" | "IN_PROGRESS" | "SUCCESS" | "FAILED" | "ABORTED"} status
 * @property {string} [referenceImageURL]
 * @property {string} [garmentImageURL]
 * @property {string} [resultImageURL]
 */

export class TryOnApi {
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
   * @param {File} garmentImage
   * @param {File} [referenceImage]
   * @param {string} [endpoint="/api/try-ons"]
   * @returns {Promise<TryOnResponse>}
   * @throws {Error}
   */
  async createTryOnJob(
    garmentImage,
    referenceImage,
    endpoint = "/api/try-ons"
  ) {
    if (referenceImage === undefined) {
      throw new Error("Must provide reference image");
    }

    const response = await this._axios.postForm(endpoint, {
      garmentImage,
      referenceImage,
    });
    return response.data;
  }

  /**
   * @param {string} id
   * @param {string} [endpoint="/api/try-ons"]
   * @returns {Promise<TryOnResponse>}
   */
  async getTryOnJobStatus(id, endpoint = "/api/try-ons") {
    const response = await this._axios.get(`${endpoint}/${id}`);
    return response.data;
  }

  /**
   * @param {File} garmentImage
   * @param {File} referenceImage
   * @param {(response: TryOnResponse) => void} [onResponse]
   * @param {(error: any) => void} [onError]
   * @param {number} [timeout=90000]
   */
  async createJobAndWaitForResult(
    garmentImage,
    referenceImage,
    onResponse = () => undefined,
    onError = () => undefined,
    timeout = 90000
  ) {
    const job = await this.createTryOnJob(garmentImage, referenceImage);
    const id = job.id;
    const retryInterval = 5000;

    /** @param {number} elapsedTime */
    const handlePolling = async (elapsedTime) => {
      try {
        const response = await this.getTryOnJobStatus(id);
        const status = response.status;
        if (
          ["SUCCESS", "FAILED", "ABORTED"].includes(status) ||
          elapsedTime >= timeout
        ) {
          onResponse(response);
        } else {
          elapsedTime += retryInterval;
          console.log(elapsedTime);
          setTimeout(() => handlePolling(elapsedTime), retryInterval);
        }
      } catch (err) {
        console.log(err);
        onError(err);
      }
    };

    await handlePolling(0);
  }
}
