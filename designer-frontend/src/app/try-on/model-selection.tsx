"use client";
import { Option, Select, Typography } from "@material-tailwind/react";
import { useState } from "react";

export type ReferenceModelSelect = {};

export default function ReferenceModelSelect(props: ReferenceModelSelect) {
  let options: {[key: string]: string} = {
    "00001": "https://images.unsplash.com/photo-1707666440778-5cca9c962c3f?crop=entropy&cs=tinysrgb&fit=crop&fm=jpg&h=400&ixid=MnwxfDB8MXxyYW5kb218MHx8fHx8fHx8MTcxMDg2NjMzNA&ixlib=rb-4.0.3&q=80&w=300",
    "00002": "https://images.unsplash.com/photo-1707845679901-16d668568bed?crop=entropy&cs=tinysrgb&fit=crop&fm=jpg&h=450&ixid=MnwxfDB8MXxyYW5kb218MHx8fHx8fHx8MTcxMDg2NjM2MA&ixlib=rb-4.0.3&q=80&w=300",
    "00003": "https://images.unsplash.com/photo-1709220762690-61f6b533c295?crop=entropy&cs=tinysrgb&fit=crop&fm=jpg&h=450&ixid=MnwxfDB8MXxyYW5kb218MHx8fHx8fHx8MTcxMDg2NjM4MA&ixlib=rb-4.0.3&q=80&w=300",
    "00004": "https://images.unsplash.com/photo-1709978601970-036e92662b46?crop=entropy&cs=tinysrgb&fit=crop&fm=jpg&h=450&ixid=MnwxfDB8MXxyYW5kb218MHx8fHx8fHx8MTcxMDg2NjM5NQ&ixlib=rb-4.0.3&q=80&w=300",
  };
  const [selectedModel, setSelectedModel] = useState<string>();

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
      <Select
        label="Select Model"
        value={selectedModel}
        onChange={(value) => setSelectedModel(value)}
      >
        {Object.keys(options).map((option) => (
          <Option key={option} value={option}>
            {option}
          </Option>
        ))}
      </Select>
      <div className="border border-white h-96 flex justify-center items-center object-contain overflow-clip">
        {selectedModel ? <img src={options[selectedModel]} alt="image" width={600} height={900} /> : "Placeholder"}
      </div>
    </div>
  );
}
