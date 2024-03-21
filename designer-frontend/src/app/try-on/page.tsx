"use client";
import { Typography } from "@material-tailwind/react";
import DesignSelect from "./design-selection";
import ReferenceModelSelect from "./model-selection";
import { TryOnResult } from "./try-on-result";

export default function TryOnPage() {
  return (
    <div className="px-4 py-4">
      <Typography variant="h3" className="px-4">
        Designer &gt; Single Try-on
      </Typography>
      <br />
      <div
        className="grid sm:grid-rows-2 xl:grid-rows-1 xl:grid-cols-3
          sm:divide-y sm:divide-x-0 xl:divide-y-0 xl:divide-x
        divide-gray-800 dark:divide-gray-100"
      >
        <div
          className="grid grid-cols-2 xl:col-span-2 divide-x
          divide-gray-800 dark:divide-gray-100"
        >
          <ReferenceModelSelect />
          <DesignSelect />
        </div>
        <TryOnResult />
      </div>
    </div>
  );
}
