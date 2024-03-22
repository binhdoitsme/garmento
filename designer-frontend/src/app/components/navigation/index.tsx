"use client";
import { GlobalContext } from "@/app/global-state";
import {
  Button,
  IconButton,
  Navbar,
  Typography,
} from "@material-tailwind/react";
import { Inter_Tight } from "next/font/google";
import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import { useContext, useEffect, useState } from "react";
import { ProfileMenu } from "./profile";

const logoFont = Inter_Tight({ weight: ["400", "700"], subsets: ["latin"] });

export default function Navigation() {
  const [openNav, setOpenNav] = useState(false);
  const globalState = useContext(GlobalContext);
  const currentUser = globalState?.currentUser;
  const pathname = usePathname();
  const router = useRouter();

  useEffect(() => {
    window.addEventListener(
      "resize",
      () => window.innerWidth >= 960 && setOpenNav(false)
    );
  }, []);

  useEffect(() => {
    if (currentUser && pathname.endsWith("/login")) {
      router.push("/catalogs");
    }
    if (!pathname.endsWith("/login") && !currentUser) {
      router.push("/home");
    }
  }, [globalState, pathname, router, currentUser]);

  const navList = (
    <ul className="mt-2 mb-4 flex flex-col gap-2 lg:mb-0 lg:mt-0 lg:flex-row lg:items-center lg:gap-6">
      <Typography
        as="li"
        variant="small"
        color="blue-gray"
        className="p-1 font-normal"
      >
        <Link shallow href="/try-on" className="flex items-center">
          Virtual Try-On
        </Link>
      </Typography>
      <Typography
        as="li"
        variant="small"
        color="blue-gray"
        className="p-1 font-normal"
      >
        <Link shallow href="/catalogs" className="flex items-center">
          Catalogs
        </Link>
      </Typography>
      <Typography
        as="li"
        variant="small"
        color="blue-gray"
        className="p-1 font-normal"
      >
        <Link shallow href="#" className="flex items-center">
          Account
        </Link>
      </Typography>
    </ul>
  );

  if (pathname.endsWith("/home")) {
    return <></>;
  }

  return (
    <Navbar className="sticky top-0 z-10 h-max max-w-full rounded-none px-4 py-2 lg:px-8 lg:py-4">
      <div className="flex items-center justify-between text-blue-gray-900">
        <div className="flex items-center">
          <Link
            shallow
            href="/home"
            className={`mr-4 cursor-pointer py-0.5 font-bold text-xl ${logoFont.className}`}
          >
            Garmento
          </Link>
          <Typography className="ml-2 cursor-pointer py-0.5 text-sm">
            {globalState?.breadcrumbs}
          </Typography>
        </div>
        <div className="flex items-center gap-4">
          {currentUser && <div className="mr-4 hidden lg:block">{navList}</div>}
          <div className="flex items-center gap-x-1">
            {currentUser ? (
              <ProfileMenu />
            ) : (
              <Link href={"/login"}>
                <Button
                  variant="outlined"
                  size="sm"
                  className="hidden lg:inline-block"
                >
                  <span>Log In</span>
                </Button>
              </Link>
            )}
          </div>
          <IconButton
            variant="text"
            className="ml-auto h-6 w-6 text-inherit hover:bg-transparent focus:bg-transparent active:bg-transparent lg:hidden"
            ripple={false}
            onClick={() => setOpenNav(!openNav)}
          >
            {openNav ? (
              <svg
                xmlns="http://www.w3.org/2000/svg"
                fill="none"
                className="h-6 w-6"
                viewBox="0 0 24 24"
                stroke="currentColor"
                strokeWidth={2}
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  d="M6 18L18 6M6 6l12 12"
                />
              </svg>
            ) : (
              <svg
                xmlns="http://www.w3.org/2000/svg"
                className="h-6 w-6"
                fill="none"
                stroke="currentColor"
                strokeWidth={2}
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  d="M4 6h16M4 12h16M4 18h16"
                />
              </svg>
            )}
          </IconButton>
        </div>
      </div>
    </Navbar>
  );
}
