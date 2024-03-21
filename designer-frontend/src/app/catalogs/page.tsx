"use client";
import {
  Card,
  CardHeader,
  CardBody,
  CardFooter,
  Typography,
  Button,
  Tooltip,
} from "@material-tailwind/react";

export default function Catalogs() {
  return (
    <div className="flex flex-col items-center mb-6 pb-4">
      <div className="md:w-5/6 xl:w-5/6 flex justify-start my-4">
        <Typography variant="h3">Designer &gt; Catalogs</Typography>
      </div>
      <div className="md:w-5/6 xl:w-5/6 flex justify-start">
        <Button color="indigo" variant="gradient" size="sm">
          <i className="fas fa-file-circle-plus pr-1" /> Create Catalog
        </Button>
      </div>
      <div className="grid gap-2 md:grid-cols-2 xl:grid-cols-4 md:w-5/6 xl:w-5/6">
        <CatalogCard
          name="My Catalog"
          createdBy="BinhDH"
          approvedBy="Manager Name"
          status="APPROVED"
        />
        <CatalogCard name="My Catalog" createdBy="BinhDH" status="SUBMITTED" />
        <CatalogCard
          name="My Catalog"
          createdBy="BinhDH"
          status="NOT_SUBMITTED"
        />
        <CatalogCard
          name="My Catalog"
          createdBy="BinhDH"
          approvedBy="Manager Name"
          status="PUBLISHED"
        />
      </div>
    </div>
  );
}

type CatalogCardProps = {
  name: string;
  createdBy: string;
  approvedBy?: string;
  status: "NOT_SUBMITTED" | "SUBMITTED" | "APPROVED" | "REJECTED" | "PUBLISHED";
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
      </CardBody>
      <CardFooter className="flex gap-2 justify-end py-0">
        <Tooltip content="View">
          <Button className="px-3" variant="outlined" size="sm">
            <i className="far fa-eye" />
          </Button>
        </Tooltip>
        {status === "APPROVED" && (
          <Tooltip content="Unapprove">
            <Button
              className="px-3"
              variant="outlined"
              color="red"
              size="sm"
            >
              <i className="fas fa-thumbs-down" />
            </Button>
          </Tooltip>
        )}
        {status === "APPROVED" && (
          <Tooltip content="Publish">
            <Button
              className="px-3"
              variant="outlined"
              color="blue-gray"
              size="sm"
            >
              <i className="fas fa-arrow-up-from-bracket" />
            </Button>
          </Tooltip>
        )}
        {status === "NOT_SUBMITTED" && (
          <Tooltip content="Submit for review">
            <Button
              className="px-3"
              variant="outlined"
              color="blue-gray"
              size="sm"
            >
              <i className="fas fa-clipboard-check" />
            </Button>
          </Tooltip>
        )}
        {status === "SUBMITTED" && (
          <Tooltip content="Approve">
            <Button className="px-3" variant="outlined" color="green" size="sm">
              <i className="fas fa-thumbs-up" />
            </Button>
          </Tooltip>
        )}
        <Tooltip content="Delete">
          <Button
            className="px-3"
            variant="outlined"
            color="red"
            size="sm"
            disabled={status === "PUBLISHED"}
          >
            <i className="fas fa-trash" />
          </Button>
        </Tooltip>
      </CardFooter>
    </Card>
  );
}
