"use client";
import { GoogleOAuthProvider } from "@react-oauth/google";
import { Inter } from "next/font/google";
import { useEffect, useReducer } from "react";
import Footer from "./components/footer";
import Navigation from "./components/navigation";
import {
  GlobalContext,
  GlobalDispatchContext,
  User,
  defaultGlobalState,
  globalReducer,
} from "./global-state";
import "./globals.css";
import Template from "./template";

const inter = Inter({ subsets: ["latin"] });

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  const [globalState, dispatch] = useReducer(globalReducer, {
    ...defaultGlobalState,
    currentUser: JSON.parse(localStorage.getItem("currentUser") ?? "null") as
      | User
      | undefined,
  });

  useEffect(() => {
    if (globalState.currentUser) {
      localStorage.setItem(
        "currentUser",
        JSON.stringify(globalState.currentUser)
      );
    } else {
      localStorage.removeItem("currentUser");
    }
  }, [globalState.currentUser]);

  useEffect(() => console.log(globalState), [globalState]);

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
              <Template>{children}</Template>
              <Footer />
            </body>
          </html>
        </GoogleOAuthProvider>
      </GlobalDispatchContext.Provider>
    </GlobalContext.Provider>
  );
}
