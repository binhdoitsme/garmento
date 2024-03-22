"use client";

import {
  GlobalContext,
  GlobalContextActionType,
  GlobalDispatchContext,
} from "@/app/global-state";
import { Button } from "@material-tailwind/react";
import { useGoogleLogin } from "@react-oauth/google";
import Image from "next/image";
import { useCallback, useContext, useEffect, useMemo, useState } from "react";
import { LoginApi } from "./api";
import Spinner from "../components/spinner";
import { useRouter } from "next/navigation";

export default function Login() {
  const globalState = useContext(GlobalContext);
  const globalDispatch = useContext(GlobalDispatchContext);
  const [loading, setLoading] = useState(false);
  const router = useRouter();
  const loginApi = useMemo(() => new LoginApi(), []);
  const onLoginSuccess = useCallback(
    () =>
      loginApi.me().then((me) =>
        globalDispatch?.({
          type: GlobalContextActionType.SET_CURRENT_USER,
          value: { currentUser: me },
        })
      ),
    [loginApi, globalDispatch]
  );
  const login = useGoogleLogin({
    onNonOAuthError: () => setLoading(false),
    onError: () => setLoading(false),
    onSuccess: (tokenResponse) =>
      loginApi
        .exchangeToken(tokenResponse)
        .then((isSuccess) => {
          if (isSuccess) {
            onLoginSuccess().then(() => router.push("/catalogs"));
          } else {
            alert("!ok");
          }
        })
        .finally(() => setLoading(false)),
  });

  useEffect(() => {
    globalDispatch?.({
      type: GlobalContextActionType.SET_BREADCRUMBS,
      value: {},
    });
  }, [globalDispatch]);

  if (globalState?.currentUser) {
    return (
      <div className="w-full min-h-[28rem] flex items-center justify-center">
        <span>
          <Spinner />
        </span>
      </div>
    );
  }

  return (
    <div className="w-full min-h-[28rem] grid grid-cols-2 divide-x divide-gray-700 dark:divide-gray-100">
      <div
        className="flex justify-center items-center"
        style={{ backgroundColor: "rgb(8,5,22)" }}
      >
        <Image src="/logo.png" alt="Logo" width={320} height={320} />
      </div>
      <div className="flex justify-center items-center">
        <Button
          className="flex"
          color="red"
          variant="gradient"
          onClick={() => {
            setLoading(true);
            login();
          }}
          disabled={loading}
        >
          <span className="mr-2">
            {(loading && <Spinner />) || <i className="fab fa-google pr-2" />}
          </span>
          Login with Google
        </Button>
      </div>
    </div>
  );
}
