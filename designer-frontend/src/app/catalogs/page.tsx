"use client";
import {
  Button,
  Card,
  CardBody,
  CardFooter,
  CardHeader,
  Tooltip,
  Typography,
} from "@material-tailwind/react";
import { useContext, useEffect, useMemo, useState } from "react";
import {
  GlobalContextActionType,
  GlobalDispatchContext,
} from "../global-state";
import { Catalog, CatalogApi } from "./api/catalogs";
import { Approve, Delete, Publish, Submit, Unapprove, View } from "./buttons";
import CreateCatalog from "./create";

export default function Catalogs() {
  const globalDispatch = useContext(GlobalDispatchContext);
  const [catalogs, setCatalogs] = useState<Catalog[]>([]);
  const catalogApi = useMemo(() => new CatalogApi(), []);

  const refreshCatalogList = () => catalogApi.listCatalogs().then(setCatalogs);
  useEffect(() => {
    refreshCatalogList();
  }, []);

  // const catalogs: CatalogCardProps[] = [
  //   {
  //     name: "My Catalog",
  //     createdBy: "BinhDH",
  //     approvedBy: "Manager Name",
  //     status: "APPROVED",
  //   },
  //   {
  //     name: "My Catalog",
  //     createdBy: "BinhDH",
  //     status: "SUBMITTED",
  //   },
  //   {
  //     name: "My Catalog",
  //     createdBy: "BinhDH",
  //     status: "DRAFT",
  //   },
  //   {
  //     name: "My Catalog",
  //     createdBy: "BinhDH",
  //     approvedBy: "Manager Name",
  //     status: "PUBLISHED",
  //   },
  // ];

  useEffect(() => {
    globalDispatch?.({
      type: GlobalContextActionType.SET_BREADCRUMBS,
      value: {
        breadcrumbs: "Designer > Catalogs",
      },
    });
    globalDispatch?.({
      type: GlobalContextActionType.SET_TITLE,
      value: {
        title: "Garmento | Manage Catalogs",
      },
    });
  }, [globalDispatch]);

  return (
    <div className="flex flex-col items-center mb-6 py-4">
      <CreateCatalog api={catalogApi} onCreated={refreshCatalogList} />
      <div className="grid gap-2 md:grid-cols-2 xl:grid-cols-4 md:w-5/6 xl:w-5/6">
        {catalogs.map((props, index) => (
          <CatalogCard
            key={index}
            {...props}
            createdBy={props.createdBy.name}
            status={props.status}
            api={catalogApi}
            refreshCatalogs={refreshCatalogList}
          />
        ))}
      </div>
    </div>
  );
}

type CatalogCardProps = {
  api: CatalogApi;
  id: string;
  name: string;
  createdBy: string;
  approvedBy?: string;
  status: "DRAFT" | "SUBMITTED" | "APPROVED" | "PUBLISHED";
  refreshCatalogs: () => void;
};

function CatalogCard(props: CatalogCardProps) {
  const { name, createdBy, approvedBy, status } = props;

  return (
    <Card className="mt-6 w-72 h-full">
      <CardHeader color="blue-gray" className="mx-6 mt-6 rounded-md">
        <img
          src="https://images.unsplash.com/photo-1540553016722-983e48a2cd10?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=800&q=80"
          alt="card-image"
        />
      </CardHeader>
      <CardBody>
        <Typography variant="h5" color="blue-gray" className="mb-2">
          {name}
        </Typography>
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
      </CardBody>
      <CardFooter className="flex gap-2 justify-end py-0">
        <View {...props} />
        {status === "APPROVED" && (
          <Unapprove {...props} onUnapproved={props.refreshCatalogs} />
        )}
        {status === "APPROVED" && (
          <Publish {...props} onPublished={props.refreshCatalogs} />
        )}
        {status === "DRAFT" && (
          <Submit {...props} onSubmitted={props.refreshCatalogs} />
        )}
        {status === "SUBMITTED" && (
          <Approve {...props} onApproved={props.refreshCatalogs} />
        )}
        <Delete {...props} onDeleted={props.refreshCatalogs} />
      </CardFooter>
    </Card>
  );
}
