"use client";
import { Option, Select, Typography } from "@material-tailwind/react";
import { useEffect, useState } from "react";
import { PresetMeta, PresetsApi } from "./api/presets";

export interface ReferenceModelSelectProps {
  selectedPreset?: string;
  setSelectedPreset: (value?: string) => void;
}

const presetsApi = new PresetsApi();

export default function ReferenceModelSelect(props: ReferenceModelSelectProps) {
  // let options: { [key: string]: string } = {
  //   "00001": "/02532_00.jpg",
  //   "00002":
  //     "https://images.unsplash.com/photo-1707845679901-16d668568bed?crop=entropy&cs=tinysrgb&fit=crop&fm=jpg&h=450&ixid=MnwxfDB8MXxyYW5kb218MHx8fHx8fHx8MTcxMDg2NjM2MA&ixlib=rb-4.0.3&q=80&w=300",
  //   "00003":
  //     "https://images.unsplash.com/photo-1709220762690-61f6b533c295?crop=entropy&cs=tinysrgb&fit=crop&fm=jpg&h=450&ixid=MnwxfDB8MXxyYW5kb218MHx8fHx8fHx8MTcxMDg2NjM4MA&ixlib=rb-4.0.3&q=80&w=300",
  //   "00004":
  //     "https://images.unsplash.com/photo-1709978601970-036e92662b46?crop=entropy&cs=tinysrgb&fit=crop&fm=jpg&h=450&ixid=MnwxfDB8MXxyYW5kb218MHx8fHx8fHx8MTcxMDg2NjM5NQ&ixlib=rb-4.0.3&q=80&w=300",
  // };
  const { selectedPreset, setSelectedPreset } = props;
  const [presets, setPresets] = useState<{ [key: string]: PresetMeta }>({});

  useEffect(() => console.log(selectedPreset), [selectedPreset]);

  useEffect(() => {
    presetsApi
      .listPresets()
      .then((presets) =>
        presets.reduce(
          (current, preset) => ({
            ...current,
            [String(preset.name)]: preset,
          }),
          {} as { [key: string]: PresetMeta }
        )
      )
      .then(setPresets);
  }, []);

  return (
    <div className="flex flex-col gap-2 p-4">
      <Typography
        placeholder=""
        onPointerEnterCapture={() => {}}
        onPointerLeaveCapture={() => {}}
        variant="h5"
      >
        Choose Model
      </Typography>
      <Select label="Select Model" onChange={setSelectedPreset}>
        {Object.keys(presets).map((option) => (
          <Option key={option} value={option}>
            {option}
          </Option>
        ))}
      </Select>
      <div className="border border-white h-96 flex justify-center items-center object-contain overflow-clip">
        {selectedPreset ? (
          <img
            src={presets[selectedPreset].refImage}
            alt="image"
            width={600}
            height={900}
            style={{ maxHeight: "24rem", width: "auto" }}
          />
        ) : (
          ""
        )}
      </div>
    </div>
  );
}
