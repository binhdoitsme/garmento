import { TokenResponse } from "@react-oauth/google";
import axios, { Axios } from "axios";

export class LoginApi {
  constructor(
    readonly abortController = new AbortController(),
    private readonly _axios: Axios = axios.create({
      withCredentials: false,
      baseURL: process.env.NEXT_PUBLIC_TRY_ON_BACKEND_HOST,
      signal: abortController.signal,
    })
  ) {}

  exchangeToken = (
    tokenResponse: Omit<
      TokenResponse,
      "error" | "error_description" | "error_uri"
    >,
    endpoint = "/tokens"
  ) =>
    this._axios
      .post(endpoint, { token: tokenResponse.access_token })
      .then((response) => {
        console.log(response);
        return response.status === 204;
      })
      .catch((err) => {
        console.log(err);
        return false;
      });

  me = (endpoint = "/me") =>
    this._axios.post(endpoint).then(({ data }) => ({
      email: data.email as string,
      name: data.name as string,
    }));
}
