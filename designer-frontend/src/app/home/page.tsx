"use client";
import { Button } from "@material-tailwind/react";
import Image from "next/image";
import Link from "next/link";
import { useContext } from "react";
import { GlobalContext } from "../global-state";

export default function HomePage() {
  const globalState = useContext(GlobalContext);

  return (
    <div
      className="w-full h-screen flex justify-center items-center flex-col"
      style={{ backgroundColor: "rgb(8,4,22)" }}
    >
      <Image src="/logo.png" alt="Logo" width={320} height={320} />
      {globalState?.currentUser ? (
        <>
          <Link href={"/try-on"}>
            <Button variant="gradient" color="indigo">
              Virtual Try-On
            </Button>
          </Link>
          <br />
          <Link href={"/catalogs"}>
            <Button variant="gradient" color="blue-gray">
              View Catalogs
            </Button>
          </Link>
        </>
      ) : (
        <Link href={"/login"}>
          <Button variant="gradient" color="indigo">
            Log in
          </Button>
        </Link>
      )}
    </div>
  );
}
