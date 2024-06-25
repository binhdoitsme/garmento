import axios, { Axios } from "axios";

export interface PresetMeta {
  name: string;
  refImage: string;
  denseposeImage: string;
  segmented: string;
  poseKeypoints: string;
}

function rewriteUrl(presetUrl: string) {
  return `/api${presetUrl}`;
}

function rewriteAllUrls(responseData: any) {
  return Object.entries(responseData)
    .map(
      ([key, value]) =>
        [key, `${value}`.startsWith("/") ? rewriteUrl(`${value}`) : value] as [
          string,
          string
        ]
    )
    .reduce(
      (current, [key, value]) => ({ ...current, [key]: value }),
      {} as { [key: string]: string }
    ) as any as PresetMeta;
}

export class PresetsApi {
  constructor(
    readonly abortController = new AbortController(),
    private readonly _axios: Axios = axios.create({
      withCredentials: false,
      baseURL: process.env.NEXT_PUBLIC_TRY_ON_BACKEND_HOST,
      signal: abortController.signal,
    })
  ) {}

  async listPresets(endpoint = `/api/presets`) {
    const response = await this._axios.get(endpoint);
    const data = response.data;
    return Array.from(data).map(rewriteAllUrls);
  }

  async getPresetMetadata(preset: string, endpoint = "/api/presets") {
    const response = await this._axios.get(`${endpoint}/${preset}`);
    const data = response.data;
    return rewriteAllUrls(data);
  }
}
