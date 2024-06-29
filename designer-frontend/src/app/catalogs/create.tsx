import {
  Button,
  Dialog,
  DialogBody,
  DialogFooter,
  DialogHeader,
  Input,
} from "@material-tailwind/react";
import { useState } from "react";
import { Catalog, CatalogApi } from "./api/catalogs";

interface CreateCatalogProps {
  api: CatalogApi;
  onCreated: (catalog: Catalog) => void;
}

export default function CreateCatalog({ api, onCreated }: CreateCatalogProps) {
  const [name, setName] = useState<string>("");
  const [modalOpen, setModalOpen] = useState(false);

  const openModal = () => setModalOpen(true);
  const closeModal = () => setModalOpen(false);
  const handleCreateCatalog = async () => {
    const catalog = await api.createCatalog(name);
    setName("");
    closeModal();
    onCreated(catalog);
  };

  return (
    <div className="md:w-5/6 xl:w-5/6 flex justify-start">
      <Button color="indigo" variant="gradient" size="sm" onClick={openModal}>
        <i className="fas fa-file-circle-plus pr-1" /> Create Catalog
      </Button>
      <Dialog open={modalOpen} handler={closeModal}>
        <DialogHeader>Create Catalog</DialogHeader>
        <DialogBody>
          <Input
            label="Name"
            crossOrigin
            onChange={({ target: { value } }) => setName(value)}
          />
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
          <Button
            variant="gradient"
            color="green"
            onClick={handleCreateCatalog}
          >
            <span>Create</span>
          </Button>
        </DialogFooter>
      </Dialog>
    </div>
  );
}
