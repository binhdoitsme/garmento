"use client";
import { GoogleOAuthProvider } from "@react-oauth/google";
import { Inter } from "next/font/google";
import { useEffect, useMemo, useReducer } from "react";
import Footer from "./components/footer";
import Navigation from "./components/navigation";
import {
  GlobalContext,
  GlobalContextActionType,
  GlobalDispatchContext,
  User,
  defaultGlobalState,
  globalReducer,
} from "./global-state";
import "./globals.css";
import Template from "./template";
import { TokensApi } from "./login/api";
import { usePathname, useRouter } from "next/navigation";
import axios from "axios";

const inter = Inter({ subsets: ["latin"] });

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  const [globalState, dispatch] = useReducer(globalReducer, defaultGlobalState);
  const tokensApi = useMemo(() => new TokensApi(), []);
  const pathname = usePathname();
  const router = useRouter();

  const errorComposer = (error: any) => {
    return () => {
      const statusCode = error.response ? error.response.status : null;
      if (statusCode >= 400 && statusCode < 500) {
        router.push("/login");
      }
    };
  };

  axios.interceptors.response.use(undefined, function (error) {
    error.handleGlobally = errorComposer(error);

    return Promise.reject(error);
  });

  useEffect(() => {
    dispatch({ type: GlobalContextActionType.LOADING_START, value: {} });
    // check /api/me first, if unauthorized then redirect to /login. Else redirect to /home
    tokensApi
      .me()
      .catch((_) => undefined)
      .then((currentUser) => {
        dispatch({
          type: GlobalContextActionType.SET_CURRENT_USER,
          value: { currentUser },
        });

        if (!pathname.endsWith("/login") && !currentUser) {
          throw Error();
        }
      })
      .catch(() => router.push("/home"))
      .then(() => {
        dispatch({ type: GlobalContextActionType.LOADING_END, value: {} });
      });
  }, []);

  return (
    <GlobalContext.Provider value={globalState}>
      <GlobalDispatchContext.Provider value={dispatch}>
        <GoogleOAuthProvider
          clientId={process.env.NEXT_PUBLIC_GOOGLE_CLIENT_ID}
        >
          <html lang="en">
            <head>
              <link
                rel="stylesheet"
                href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.2.1/css/all.min.css"
                integrity="sha512-MV7K8+y+gLIBoVD59lQIYicR65iaqukzvf/nwasF0nqhPay5w/9lJmVM2hMDcnK1OnMGCdVK+iQrJ7lzPJQd1w=="
                crossOrigin="anonymous"
                referrerPolicy="no-referrer"
              />
              <title>{globalState.title}</title>
            </head>
            <body
              className={`${inter.className} min-h-screen flex flex-col justify-between`}
            >
              <Navigation />
              <Template>{globalState.isLoading || children}</Template>
              <Footer />
            </body>
          </html>
        </GoogleOAuthProvider>
      </GlobalDispatchContext.Provider>
    </GlobalContext.Provider>
  );
}
