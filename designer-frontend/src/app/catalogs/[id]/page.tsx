"use client";
import { GlobalContextActionType, GlobalDispatchContext } from "@/app/global-state";
import { Button, Tooltip, Typography } from "@material-tailwind/react";
import { useContext, useEffect } from "react";

export default function CatalogDetailsPage({
  params,
}: {
  params: { id: string };
}) {
  const catalog: CatalogProps = {
    status: "SUBMITTED",
    createdBy: "BinhDH",
    name: "My Catalog Name",
    approvedBy: "",
    items: [
      {
        id: "1",
        url: "https://flowbite.s3.amazonaws.com/docs/gallery/masonry/image.jpg",
      },
      {
        id: "1",
        url: "https://flowbite.s3.amazonaws.com/docs/gallery/masonry/image.jpg",
      },
      {
        id: "1",
        url: "https://flowbite.s3.amazonaws.com/docs/gallery/masonry/image.jpg",
      },
      {
        id: "1",
        url: "https://flowbite.s3.amazonaws.com/docs/gallery/masonry/image.jpg",
      },
      {
        id: "1",
        url: "https://flowbite.s3.amazonaws.com/docs/gallery/masonry/image.jpg",
      },
      {
        id: "1",
        url: "https://flowbite.s3.amazonaws.com/docs/gallery/masonry/image.jpg",
      },
      {
        id: "1",
        url: "https://flowbite.s3.amazonaws.com/docs/gallery/masonry/image.jpg",
      },
      {
        id: "1",
        url: "https://flowbite.s3.amazonaws.com/docs/gallery/masonry/image.jpg",
      },
      {
        id: "1",
        url: "https://flowbite.s3.amazonaws.com/docs/gallery/masonry/image.jpg",
      },
      {
        id: "1",
        url: "https://flowbite.s3.amazonaws.com/docs/gallery/masonry/image.jpg",
      },
      {
        id: "1",
        url: "https://flowbite.s3.amazonaws.com/docs/gallery/masonry/image.jpg",
      },
      {
        id: "1",
        url: "https://flowbite.s3.amazonaws.com/docs/gallery/masonry/image.jpg",
      },
    ],
  };
  const { status, createdBy, name, approvedBy, items } = catalog;

  return (
    <Catalog
      name={name}
      createdBy={createdBy}
      approvedBy={approvedBy}
      status={status}
      items={items}
    />
  );
}

type CatalogProps = {
  name: string;
  createdBy: string;
  approvedBy?: string;
  status: "NOT_SUBMITTED" | "SUBMITTED" | "APPROVED" | "REJECTED" | "PUBLISHED";
  items: CatalogItemProps[];
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

  useEffect(() => {
    globalDispatch?.({
      type: GlobalContextActionType.SET_BREADCRUMBS,
      value: {
        breadcrumbs: `Designer > Catalogs > Catalog "${name}"`,
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
      <div className="md:w-5/6 xl:w-5/6 flex items-start mt-4 flex-col">
        <Typography>
          <i className="fas fa-pen-to-square pr-2 block w-6 text-gray-700" />
          {status === "PUBLISHED" ? "Published" : "Created"} by: {createdBy}
        </Typography>
        {status !== "NOT_SUBMITTED" && (
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
          <Tooltip content="Approve collection">
            <Button variant="outlined" color="green" size="sm">
              <i className="fas fa-thumbs-up pr-2" />
              Approve
            </Button>
          </Tooltip>
        )}
        {status === "APPROVED" && (
          <Tooltip content="Unapprove collection">
            <Button variant="outlined" color="red" size="sm">
              <i className="fas fa-thumbs-down pr-2" />
              Unapprove
            </Button>
          </Tooltip>
        )}
        {status === "NOT_SUBMITTED" && (
          <Tooltip content="Submit collection">
            <Button variant="outlined" color="blue-gray" size="sm">
              <i className="fas fa-clipboard-check pr-2" />
              Submit
            </Button>
          </Tooltip>
        )}
        {status === "APPROVED" && (
          <Tooltip content="Publish collection">
            <Button variant="outlined" color="blue-gray" size="sm">
              <i className="fas fa-arrow-up-from-bracket pr-2" />
              Publish
            </Button>
          </Tooltip>
        )}
        <Tooltip content="Delete collection">
          <Button
            variant="outlined"
            color="red"
            size="sm"
            disabled={status === "PUBLISHED"}
          >
            <i className="fas fa-trash pr-2" />
            Delete
          </Button>
        </Tooltip>
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
