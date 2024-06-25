"use client";
import { Button, Typography } from "@material-tailwind/react";
import { useState } from "react";
import { TryOnApi, TryOnResponse } from "./api/try-on";

export interface TryOnResultProps {
  isGenerating: boolean;
  setIsGenerating: (value: boolean) => void;
  api: TryOnApi;
  garment?: File;
  preset?: string;
}

export function TryOnResult(props: TryOnResultProps) {
  const { isGenerating, api, setIsGenerating } = props;
  const [result, setResult] = useState<TryOnResponse>();

  const doGenerate = async () => {
    setResult(undefined);
    setIsGenerating(true);

    await api.createJobAndWaitForResult(
      props.garment!,
      undefined,
      props.preset!,
      (result) => {
        setResult(result);
        setIsGenerating(false);
      }
    );
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
        <Button
          color="indigo"
          variant="gradient"
          onClick={doGenerate}
          disabled={!props.garment || !props.preset || isGenerating}
        >
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
      <div className="border border-white sm:w-full md:w-3/5 xl:w-full h-96 flex justify-center items-center object-contain">
        {!result ? (
          "Placeholder"
        ) : (
          <img
            src={result.resultImageURL!}
            alt="image"
            width={600}
            height={900}
            style={{ maxHeight: "24rem", width: "auto" }}
          />
        )}
      </div>
    </div>
  );
}
