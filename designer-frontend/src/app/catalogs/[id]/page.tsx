"use client";
import {
  GlobalContextActionType,
  GlobalDispatchContext,
} from "@/app/global-state";
import { Button, Tooltip, Typography } from "@material-tailwind/react";
import { useContext, useEffect, useMemo, useState } from "react";
import { CatalogApi, Catalog as CatalogType } from "../api/catalogs";
import { Approve, Delete, Publish, Submit, Unapprove } from "../buttons";
import { useRouter } from "next/navigation";

export default function CatalogDetailsPage({
  params: { id },
}: {
  params: { id: string };
}) {
  const [catalog, setCatalog] = useState<CatalogProps>();
  const api = useMemo(() => new CatalogApi(), []);
  const refresh = () =>
    api
      .getCatalogDetails(id)
      .then((catalog) => ({
        ...catalog,
        api,
        createdBy: catalog.createdBy.name,
        approvedBy: catalog.status === "APPROVED" ? "Manager" : undefined,
      }))
      .then(setCatalog);

  useEffect(() => {
    refresh();
  }, []);

  if (!catalog) {
    return <></>;
  }
  return <Catalog {...catalog} refresh={refresh} />;
}

type CatalogProps = {
  api: CatalogApi;
  id: string;
  name: string;
  createdBy: string;
  approvedBy?: string;
  status: "DRAFT" | "SUBMITTED" | "APPROVED" | "PUBLISHED";
  items: CatalogItemProps[];
  refresh?: () => void;
};

type CatalogItemProps = {
  id: string;
  url: string;
};

function CatalogItem(props: CatalogItemProps) {
  const { id, url } = props;
  return (
    <div className="group relative">
      <img className="h-auto max-w-full rounded-lg" src={url} alt="" />
      <div className="absolute w-full h-full top-0 bottom-0 left-0 right-0 flex items-end justify-end p-2">
        <Tooltip content="Remove from collection">
          <Button
            className="px-3 hidden group-hover:inline-block"
            variant="outlined"
            color="red"
            size="sm"
          >
            <i className="fas fa-xmark" />
          </Button>
        </Tooltip>
      </div>
    </div>
  );
}
function Catalog(props: CatalogProps) {
  const { name, createdBy, approvedBy, status, items } = props;
  const globalDispatch = useContext(GlobalDispatchContext);
  const router = useRouter();

  useEffect(() => {
    globalDispatch?.({
      type: GlobalContextActionType.SET_BREADCRUMBS,
      value: {
        breadcrumbs: `Designer > Catalogs > Catalog '${name}'`,
      },
    });
    globalDispatch?.({
      type: GlobalContextActionType.SET_TITLE,
      value: {
        title: "Garmento | Manage Catalogs",
      },
    });
  }, [globalDispatch, name]);

  return (
    <div className="flex flex-col items-center mb-6 py-4">
      <h1 className="text-3xl text-left md:w-5/6 xl:w-5/6">
        Catalog <strong>{name}</strong>
      </h1>
      <div className="md:w-5/6 xl:w-5/6 flex items-start mt-4 flex-col">
        <Typography>
          <i className="fas fa-pen-to-square pr-2 block w-6 text-gray-700" />
          {status === "PUBLISHED" ? "Published" : "Created"} by: {createdBy}
        </Typography>
        {status !== "DRAFT" && status !== "PUBLISHED" && (
          <Typography>
            {approvedBy ? (
              <>
                {" "}
                <i className="fas fa-check pr-2 block w-6 text-green-700" />
                Approved by: {approvedBy}
              </>
            ) : (
              "Not yet approved"
            )}
          </Typography>
        )}
      </div>
      <div className="md:w-5/6 xl:w-5/6 flex justify-end my-4 gap-2">
        {status === "SUBMITTED" && (
          <Approve full {...props} onApproved={props.refresh} />
        )}
        {status === "APPROVED" && (
          <Unapprove full {...props} onUnapproved={props.refresh} />
        )}
        {status === "DRAFT" && (
          <Submit full {...props} onSubmitted={props.refresh} />
        )}
        {status === "APPROVED" && <Publish full {...props} />}
        <Delete full {...props} onDeleted={() => router.push("/catalogs")} />
      </div>
      <div className="md:w-5/6 xl:w-5/6 flex justify-end my-4">
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
          {items.map(({ id, url }, idx) => (
            <CatalogItem key={idx} id={id} url={url} />
          ))}
        </div>
      </div>
    </div>
  );
}
