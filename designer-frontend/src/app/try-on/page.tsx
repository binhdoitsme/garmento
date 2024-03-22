"use client";
import { Typography } from "@material-tailwind/react";
import DesignSelect from "./design-selection";
import ReferenceModelSelect from "./model-selection";
import { TryOnResult } from "./try-on-result";
import { useContext, useEffect } from "react";
import { GlobalContextActionType, GlobalDispatchContext } from "../global-state";

export default function TryOnPage() {
  const globalDispatch = useContext(GlobalDispatchContext);

  useEffect(() => {
    globalDispatch?.({
      type: GlobalContextActionType.SET_BREADCRUMBS,
      value: {
        breadcrumbs: "Designer > Virtual Try-on"
      }
    });
    globalDispatch?.({
      type: GlobalContextActionType.SET_TITLE,
      value: {
        title: "Garmento | Virtual Try-on"
      }
    });
  })

  return (
    <div className="px-4 py-4">
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
