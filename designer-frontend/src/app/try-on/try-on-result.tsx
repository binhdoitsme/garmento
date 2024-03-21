"use client";
import { Button, Typography } from "@material-tailwind/react";
import { useState } from "react";

export type TryOnResult = {};

export function TryOnResult(props: TryOnResult) {
  const [result, setResult] = useState<{}>();
  const doGenerate = () => {
    alert("Do generate");
    setResult({});
  };

  const doSaveToCatalog = () => {
    alert("Do save to catalog");
  };

  const doDownload = () => {
    alert("Do Download");
  };

  return (
    <div className="md:max-xl:col-span-2 flex flex-col gap-2 items-center p-4">
      <Typography variant="h5">Try-on result</Typography>
      <div
        id="button-group"
        className="flex justify-between sm:w-full md:w-3/5 xl:w-full"
      >
        <Button color="indigo" variant="gradient" onClick={doGenerate}>
          <i className="fas fa-arrows-rotate pr-1" />
          Generate
        </Button>
        <Button disabled={!result} onClick={doSaveToCatalog}>
          <i className="far fa-square-plus pr-1" />
          Save
        </Button>
        <Button disabled={!result} onClick={doDownload}>
          <i className="fas fa-download pr-1" />
          Download
        </Button>
      </div>
      <div className="border border-white sm:w-full md:w-3/5 xl:w-full h-96 flex justify-center items-center">
        Placeholder
      </div>
    </div>
  );
}
