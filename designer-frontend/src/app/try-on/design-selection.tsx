import { Button, Typography } from "@material-tailwind/react";
import { useState } from "react";

export type DesignSelectProps = {};

export default function DesignSelect(props: DesignSelectProps) {
  const [selectedFile, setSelectedFile] = useState<string>();
  const doShowSelectedFile = (e: React.ChangeEvent<HTMLInputElement>) => {
    const selectedFileObjURL = URL.createObjectURL(e.target.files?.[0]!);
    setSelectedFile(selectedFileObjURL);
  };
  const doUpload = () => {
    alert("Do upload");
  };

  return (
    <div className="flex flex-col gap-2 p-4">
      <Typography variant="h5">Upload your design</Typography>

      <div className="flex items-center justify-center w-full">
        <Button
          className="w-full"
          color="indigo"
          variant="gradient"
          disabled={!selectedFile}
          onClick={doUpload}
        >
          <i className="fas fa-cloud-arrow-up pr-2" />
          Upload
        </Button>
      </div>

      <div className="h-96 flex justify-center items-center">
        <label
          htmlFor="dropzone-file"
          className="flex flex-col items-center justify-center w-full h-full border-2 border-gray-300 border-dashed cursor-pointer bg-gray-50 dark:hover:bg-bray-800 dark:bg-gray-700 hover:bg-gray-100 dark:border-gray-600 dark:hover:border-gray-500 dark:hover:bg-gray-600"
        >
          <div className="flex flex-col items-center justify-center pt-5 pb-6">
            {selectedFile ? (
              <img src={selectedFile} />
            ) : (
              <>
                <i className="fas fa-file-image fa-3x pr-1 pb-4 text-gray-500 dark:text-gray-400" />
                <p className="mb-2 text-sm text-gray-500 dark:text-gray-400 text-center">
                  <span className="font-semibold">
                    Click to open file browser <br />
                  </span>
                  {/* or drag and drop */}
                </p>
                <small className="text-xs text-gray-500 dark:text-gray-400">
                  PNG or JPG (MAX. 768x1024px)
                </small>
              </>
            )}
          </div>
          <input
            id="dropzone-file"
            type="file"
            className="hidden"
            onChange={doShowSelectedFile}
          />
        </label>
      </div>
    </div>
  );
}
