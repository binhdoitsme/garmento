"use client";

import { Button } from "@material-tailwind/react";
import Image from "next/image";

import { useGoogleLogin } from "@react-oauth/google";

export default function Login() {
  const login = useGoogleLogin({
    onSuccess: (tokenResponse) => alert(JSON.stringify(tokenResponse)),
  });
  return (
    <div className="w-full min-h-[28rem] grid grid-cols-2 divide-x divide-gray-700 dark:divide-gray-100">
      <div
        className="flex justify-center items-center"
        style={{ backgroundColor: "rgb(8,5,22)" }}
      >
        <Image src="/logo.png" alt="Logo" width={320} height={320} />
      </div>
      <div className="flex justify-center items-center">
        <Button color="red" variant="gradient" onClick={() => login()}>
          <i className="fab fa-google pr-2" />
          Login with Google
        </Button>
      </div>
    </div>
  );
}
