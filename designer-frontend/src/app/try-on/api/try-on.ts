import axios, { Axios } from "axios";

export interface TryOnResponse {
  id: string;
  status: "PENDING" | "IN_PROGRESS" | "SUCCESS" | "FAILED" | "ABORTED";
  referenceImageURL?: string;
  garmentImageURL?: string;
  resultImageURL?: string;
}

export class TryOnApi {
  constructor(
    readonly abortController = new AbortController(),
    private readonly _axios: Axios = axios.create({
      withCredentials: false,
      baseURL: process.env.NEXT_PUBLIC_TRY_ON_BACKEND_HOST,
      signal: abortController.signal,
    })
  ) {}

  async createTryOnJob(
    garmentImage: any,
    referenceImage?: any,
    preset?: string,
    endpoint = "/api/try-ons"
  ): Promise<TryOnResponse> {
    if (referenceImage === undefined && preset === undefined) {
      throw Error("Must provide either preset or reference image");
    }

    if (referenceImage !== undefined && preset !== undefined) {
      throw Error(
        "Must only provide either preset or reference image, not both"
      );
    }

    const response = await this._axios.postForm<TryOnResponse>(endpoint, {
      garmentImage,
      referenceImage,
      preset,
    });
    return response.data;
  }

  async getTryOnJobStatus(
    id: string,
    endpoint = "/api/try-ons"
  ): Promise<TryOnResponse> {
    const response = await this._axios.get<TryOnResponse>(`${endpoint}/${id}`);
    return response.data;
  }

  async createJobAndWaitForResult(
    garmentImage: File,
    referenceImage?: File,
    preset?: string,
    onResponse: (response: TryOnResponse) => void = () => undefined,
    timeout = 90_000
  ) {
    const job = await this.createTryOnJob(garmentImage, referenceImage, preset);
    const id = job.id;
    const retryInterval = 5_000;
    const handlePolling: (elapsedTime: number) => Promise<any> = async (
      elapsedTime
    ) => {
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
        return setTimeout(() => handlePolling(elapsedTime), retryInterval);
      }
    };
    await handlePolling(0);
  }
}
