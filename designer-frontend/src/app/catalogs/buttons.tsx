import {
  Button,
  Dialog,
  DialogBody,
  DialogFooter,
  DialogHeader,
  Input,
  Tooltip,
} from "@material-tailwind/react";
import { useRouter } from "next/navigation";
import { useState } from "react";
import { CatalogApi } from "./api/catalogs";

type CatalogCardProps = {
  id: string;
  name: string;
  createdBy: string;
  approvedBy?: string;
  status: "DRAFT" | "SUBMITTED" | "APPROVED" | "PUBLISHED";
  full?: boolean;
};

export function View(props: CatalogCardProps) {
  const router = useRouter();

  return (
    <Tooltip content="View">
      <Button
        className="px-3"
        variant="outlined"
        size="sm"
        onClick={() => router.push(`/catalogs/${props.id}`)}
      >
        <i className="far fa-eye" />
      </Button>
    </Tooltip>
  );
}

export function Submit(
  props: CatalogCardProps & { api: CatalogApi; onSubmitted?: () => void }
) {
  const handleSubmit = async () => {
    await props.api.performActionOnCatalog(props.id, "SUBMIT");
    props.onSubmitted?.();
  };

  return (
    <Tooltip content="Submit for review">
      <Button
        className="px-3"
        variant="outlined"
        color="blue-gray"
        size="sm"
        onClick={handleSubmit}
      >
        <i className="fas fa-clipboard-check" />
        {props.full && " Submit"}
      </Button>
    </Tooltip>
  );
}

export function Approve(
  props: CatalogCardProps & { api: CatalogApi; onApproved?: () => void }
) {
  const handleApprove = async () => {
    await props.api.performActionOnCatalog(props.id, "APPROVE");
    props.onApproved?.();
  };

  return (
    <Tooltip content="Approve">
      <Button
        className="px-3"
        variant="outlined"
        color="green"
        size="sm"
        onClick={handleApprove}
      >
        <i className="fas fa-thumbs-up" />
        {props.full && " Approve"}
      </Button>
    </Tooltip>
  );
}

export function Unapprove(
  props: CatalogCardProps & { api: CatalogApi; onUnapproved?: () => void }
) {
  const handleUnapprove = async () => {
    await props.api.performActionOnCatalog(props.id, "UNAPPROVE");
    props.onUnapproved?.();
  };
  return (
    <Tooltip content="Unapprove">
      <Button
        className="px-3"
        variant="outlined"
        color="red"
        size="sm"
        onClick={handleUnapprove}
      >
        <i className="fas fa-thumbs-down" />
        {props.full && " Unapprove"}
      </Button>
    </Tooltip>
  );
}

export function Publish(
  props: CatalogCardProps & { api: CatalogApi; onPublished?: () => void }
) {
  const handlePublish = async () => {
    await props.api.performActionOnCatalog(props.id, "PUBLISH");
    props.onPublished?.();
  };
  return (
    <Tooltip content="Publish">
      <Button
        className="px-3"
        variant="outlined"
        color="blue-gray"
        size="sm"
        onClick={handlePublish}
      >
        <i className="fas fa-arrow-up-from-bracket" />
        {props.full && " Publish"}
      </Button>
    </Tooltip>
  );
}

export function Delete(
  props: CatalogCardProps & {
    api: CatalogApi;
    onDeleted?: () => void;
  }
) {
  const [modalOpen, setModalOpen] = useState(false);

  const openModal = () => setModalOpen(true);
  const closeModal = () => setModalOpen(false);
  const handleDelete = async () => {
    await props.api.deleteCatalog(props.id);
    closeModal();
    props.onDeleted?.();
  };

  return (
    <>
      <Tooltip content="Delete">
        <Button
          className="px-3"
          variant="outlined"
          color="red"
          size="sm"
          disabled={props.status === "PUBLISHED"}
          onClick={openModal}
        >
          <i className="fas fa-trash" />
          {props.full && " Delete"}
        </Button>
      </Tooltip>
      <Dialog open={modalOpen} handler={closeModal}>
        <DialogHeader>Confirm delete catalog</DialogHeader>
        <DialogBody>
          Are you sure want to delete catalog <strong>{props.name}</strong>?
        </DialogBody>
        <DialogFooter>
          <Button
            variant="text"
            color="gray"
            onClick={closeModal}
            className="mr-1"
          >
            <span>Cancel</span>
          </Button>
          <Button variant="gradient" color="red" onClick={handleDelete}>
            <span>Delete</span>
          </Button>
        </DialogFooter>
      </Dialog>
    </>
  );
}
