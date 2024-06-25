import axios, { Axios } from "axios";

export class TokensApi {
  constructor(
    readonly abortController = new AbortController(),
    private readonly _axios: Axios = axios.create({
      withCredentials: false,
      baseURL: process.env.NEXT_PUBLIC_TRY_ON_BACKEND_HOST,
      signal: abortController.signal,
    })
  ) {}

  logOut = (endpoint = "/api/tokens") =>
    this._axios
      .delete(endpoint)
      .then((response) => response.status === 204)
      .catch((err) => {
        console.log(err);
        return false;
      });
}
