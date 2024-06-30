"use client";
import {
  Button,
  Dialog,
  DialogBody,
  DialogFooter,
  DialogHeader,
  Spinner,
  Typography,
} from "@material-tailwind/react";
import React, { useEffect, useMemo, useState } from "react";
import { TryOnApi, TryOnResponse } from "./api/try-on";
import { Catalog, CatalogApi } from "../catalogs/api/catalogs";

export interface TryOnResultProps {
  isGenerating: boolean;
  setIsGenerating: (value: boolean) => void;
  api: TryOnApi;
  garment?: File;
  preset?: string;
}

function SaveToCatalog({
  catalogApi,
  result,
}: {
  catalogApi: CatalogApi;
  result?: TryOnResponse;
}) {
  const [modalOpen, setModalOpen] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [catalogs, setCatalogs] = useState<Catalog[]>([]);

  const openModal = () => setModalOpen(true);
  const closeModal = () => setModalOpen(false);
  const handleAddToCatalog = (id: string, url: string) => async () => {
    setIsLoading(true);
    await catalogApi.addImageToCatalog(id, url);
    closeModal();
    setIsLoading(false);
  };

  useEffect(() => {
    if (!modalOpen) {
      setIsLoading(false);
      return;
    }
    catalogApi.listCatalogs().then(setCatalogs);
  }, [modalOpen]);

  return (
    <>
      <Button disabled={!result} onClick={openModal}>
        <i className="far fa-square-plus pr-1" />
        Save
      </Button>
      <Dialog open={modalOpen} handler={closeModal}>
        <DialogHeader>Confirm delete catalog</DialogHeader>
        <DialogBody>
          Select catalog to add to:
          <div className="flex gap-2 mt-4">
            {catalogs.map((catalog, index) => (
              <React.Fragment key={index}>
                <Button
                  color="blue-gray"
                  variant="gradient"
                  disabled={isLoading}
                  onClick={handleAddToCatalog(
                    catalog.id,
                    result?.resultImageURL ?? ""
                  )}
                >
                  {isLoading && <Spinner />}
                  {catalog.name}
                </Button>
              </React.Fragment>
            ))}
          </div>
        </DialogBody>
      </Dialog>
    </>
  );
}

export function TryOnResult(props: TryOnResultProps) {
  const { isGenerating, api, setIsGenerating } = props;
  const [result, setResult] = useState<TryOnResponse>();
  const catalogApi = useMemo(() => new CatalogApi(), []);

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
    if (!result) {
      return;
    }
    // catalogApi.addImageToCatalog(result.id, result.resultImageURL ?? "")
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
        <SaveToCatalog catalogApi={catalogApi} result={result} />
        {/* <Button disabled onClick={doDownload}>
          <i className="fas fa-download pr-1" />
          Download
        </Button> */}
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
