import { Button, Spinner } from "@material-tailwind/react";
import react, { useEffect, useMemo, useState } from "react";
import { TryOnApi } from "./api/try-on";
import { DesignsApi } from "./api/designs";
import { AuthApi } from "./api/auth";

/**
 *
 * @param {{
 *  designId: string;
 *  backendHost: string;
 * }} props
 * @returns
 */
function App(props) {
  /** @type {[string, react.Dispatch<react.SetStateAction<string>>]} */
  const [selectedReference, setSelectedReference] = useState();
  /** @type {[string, react.Dispatch<react.SetStateAction<string>>]} */
  const [result, setResult] = useState();
  /** @type {[string, react.Dispatch<react.SetStateAction<boolean>>]} */
  const [isLoading, setIsLoading] = useState(false);
  /** @type {[string, react.Dispatch<react.SetStateAction<string>>]} */
  const [error, setError] = useState("");
  /** @type {[string, react.Dispatch<react.SetStateAction<string>>]} */
  const [selectedFileObjURL, setSelectedFileObjURL] = useState();
  /** @type {[File | undefined, react.Dispatch<react.SetStateAction<File>>]} */
  const [designFile, setDesignFile] = useState();

  /** @type {TryOnApi} */
  const tryOnApi = useMemo(() => new TryOnApi(), []);
  /** @type {DesignsApi} */
  const designApi = useMemo(() => new DesignsApi(), []);
  /** @type {AuthApi} */
  const authApi = useMemo(() => new AuthApi(), []);
  const designId = props.designId;

  useEffect(() => {
    authApi
      .authenticateAsService()
      .then(() => designApi.getDesignAsBlob(props.designId))
      .then(setDesignFile);
  }, []);

  /** @param {React.ChangeEvent<HTMLInputElement>} e */
  const updateSelectedFile = (e) => {
    const file = e.target.files?.[0];
    if (!file) {
      return;
    }
    const selectedFileObjURL = URL.createObjectURL(file);
    setSelectedReference(file);
    setSelectedFileObjURL(selectedFileObjURL);
  };

  const performTryOn = async () => {
    try {
      setIsLoading(true);
      const designAsBlob = await designApi.getDesignAsBlob(designId);
      const designAsFile = new File([designAsBlob], "design.jpg");
      await tryOnApi.createJobAndWaitForResult(
        designAsFile,
        selectedReference,
        (result) => {
          setResult(result);
          setIsLoading(false);
        },
        (err) => {
          setIsLoading(false);
          setError(
            `${err}`.toLowerCase().includes("axios")
              ? "Error connecting to other services"
              : `${err}`
          );
        }
      );
    } catch (err) {
      setTimeout(() => {
        setIsLoading(false);
        setError(
          `${err}`.toLowerCase().includes("axios")
            ? "⚠️ Error connecting to other services"
            : `${err}`
        );
      }, 1000);
    }
  };

  return (
    <div className="App border-collapse border-black border min-w-24 min-h-96 h-96 flex justify-around items-center">
      <div className="border-black border min-w-72 h-full flex justify-center items-center">
        <div className="w-full h-full flex justify-center items-center">
          <label
            htmlFor="dropzone-file"
            className="flex flex-col items-center justify-center w-full h-full border-2 border-gray-300 border-dashed cursor-pointer bg-gray-50 dark:hover:bg-bray-800 dark:bg-gray-700 hover:bg-gray-100 dark:border-gray-600 dark:hover:border-gray-500 dark:hover:bg-gray-600"
          >
            <div className="flex flex-col items-center justify-center">
              {selectedFileObjURL ? (
                <img
                  className="w-full"
                  src={selectedFileObjURL}
                  style={{ maxHeight: "24rem" }}
                />
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
              onChange={updateSelectedFile}
            />
          </label>
        </div>
      </div>
      <Button
        className="h-fit rounded-3xl w-24 flex justify-center items-center"
        color="deep-purple"
        variant="gradient"
        disabled={isLoading || !selectedReference}
        onClick={performTryOn}
      >
        {isLoading ? <Spinner /> : "Go >>"}
      </Button>
      <div className="border-black border min-w-72 h-full flex justify-center items-center">
        {isLoading ? (
          <Spinner />
        ) : !!result ? (
          ""
        ) : (
          error || "Click Go and the result will appear here"
        )}
      </div>
    </div>
  );
}

export default App;
